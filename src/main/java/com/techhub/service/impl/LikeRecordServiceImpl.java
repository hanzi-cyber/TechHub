package com.techhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.techhub.common.exception.BusinessException;
import com.techhub.context.BaseContext;
import com.techhub.dto.LikeDTO;
import com.techhub.entity.Comment;
import com.techhub.entity.LikeRecord;
import com.techhub.entity.Post;
import com.techhub.mapper.CommentMapper;
import com.techhub.mapper.LikeRecordMapper;
import com.techhub.mapper.PostMapper;
import com.techhub.service.IHotRankService;
import com.techhub.service.ILikeRecordService;
import com.techhub.service.IPostService;
import com.techhub.vo.LikeResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeRecordServiceImpl extends ServiceImpl<LikeRecordMapper, LikeRecord> implements ILikeRecordService {

    // 点赞目标类型
    private static final Integer POST_TYPE = 1;
    private static final Integer COMMENT_TYPE = 2;

    @Autowired
    private LikeRecordMapper likeRecordMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private IPostService postService;
    @Autowired
    private IHotRankService hotRankService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LikeResultVO like(LikeDTO likeDTO) {
        validate(likeDTO);
        Long userId = BaseContext.getCurrentId();

        // 原子 upsert:唯一键保证不重复插入,ON DUPLICATE KEY 保证并发下也不出错
        int affected = likeRecordMapper.insertOrLike(userId, likeDTO.getTargetType(), likeDTO.getTargetId());
        // 只有真正发生状态变化(新点赞 / 由取消恢复)才 +1
        if (affected > 0) {
            updateTargetCount(likeDTO.getTargetType(), likeDTO.getTargetId(), 1);
            afterPostLikeChanged(likeDTO.getTargetType(), likeDTO.getTargetId(), true);
        }
        return new LikeResultVO(true, readTargetCount(likeDTO.getTargetType(), likeDTO.getTargetId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LikeResultVO unlike(LikeDTO likeDTO) {
        validate(likeDTO);
        Long userId = BaseContext.getCurrentId();

        // 仅当已点赞时才置 0,幂等
        int affected = likeRecordMapper.unlike(userId, likeDTO.getTargetType(), likeDTO.getTargetId());
        if (affected > 0) {
            updateTargetCount(likeDTO.getTargetType(), likeDTO.getTargetId(), -1);
            afterPostLikeChanged(likeDTO.getTargetType(), likeDTO.getTargetId(), false);
        }
        return new LikeResultVO(false, readTargetCount(likeDTO.getTargetType(), likeDTO.getTargetId()));
    }

    /** 参数与目标类型校验 */
    private void validate(LikeDTO likeDTO) {
        if (likeDTO == null || likeDTO.getTargetType() == null || likeDTO.getTargetId() == null) {
            throw new BusinessException("点赞参数不完整");
        }
        if (!POST_TYPE.equals(likeDTO.getTargetType()) && !COMMENT_TYPE.equals(likeDTO.getTargetType())) {
            throw new BusinessException("不支持的点赞类型");
        }
    }

    /** 按目标类型更新计数(+1 / -1),原子自增/自减,避免并发丢失更新 */
    private void updateTargetCount(Integer targetType, Long targetId, int delta) {
        if (POST_TYPE.equals(targetType)) {
            postMapper.update(null, new LambdaUpdateWrapper<Post>()
                    .eq(Post::getId, targetId)
                    .setSql("like_count = like_count + " + delta));
        } else {
            commentMapper.update(null, new LambdaUpdateWrapper<Comment>()
                    .eq(Comment::getId, targetId)
                    .setSql("like_count = like_count + " + delta));
        }
    }

    /** 读取目标最新点赞数(同时校验目标存在) */
    private Integer readTargetCount(Integer targetType, Long targetId) {
        if (POST_TYPE.equals(targetType)) {
            Post post = postMapper.selectById(targetId);
            if (post == null) {
                throw new BusinessException("帖子不存在");
            }
            return post.getLikeCount();
        }
        Comment comment = commentMapper.selectById(targetId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        return comment.getLikeCount();
    }

    /** 帖子点赞数变化时:失效详情缓存 + 更新热度榜 */
    private void afterPostLikeChanged(Integer targetType, Long targetId, boolean isLike) {
        if (POST_TYPE.equals(targetType)) {
            postService.evictPostCache(targetId);
            if (isLike) {
                hotRankService.incrLike(targetId);
            } else {
                hotRankService.decrLike(targetId);
            }
        }
    }
}
