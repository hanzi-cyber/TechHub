package com.techhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.techhub.common.PageResult;
import com.techhub.common.exception.BusinessException;
import com.techhub.context.BaseContext;
import com.techhub.dto.SaveCommentDTO;
import com.techhub.entity.Comment;
import com.techhub.entity.Post;
import com.techhub.entity.User;
import com.techhub.mapper.CommentMapper;
import com.techhub.mapper.PostMapper;
import com.techhub.service.ICommentService;
import com.techhub.service.IHotRankService;
import com.techhub.service.IPostService;
import com.techhub.service.IUserService;
import com.techhub.vo.CommentVO;
import com.techhub.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    /** 评论状态:1正常 */
    private static final int COMMENT_STATUS_NORMAL = 1;
    /** 评论状态:0已删除 */
    private static final int COMMENT_STATUS_DELETE = 0;

    /** 帖子状态:1已发布 */
    private static final int POST_STATUS_PUBLISHED = 1;

    /** 父评论ID:0 表示一级评论 */
    private static final long ROOT_PARENT_ID = 0L;

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private IUserService userService;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private IPostService postService;
    @Autowired
    private IHotRankService hotRankService;

    @Override
    public PageResult<CommentVO> getCommentsByPostId(Long postId, Integer pageNum, Integer pageSize) {
        // ============ 第一段:分页查一级评论(楼层) ============
        Page<Comment> page = commentMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getPostId, postId)
                        .eq(Comment::getParentId, ROOT_PARENT_ID)
                        .eq(Comment::getStatus, COMMENT_STATUS_NORMAL)
                        .orderByDesc(Comment::getCreatedAt));

        PageResult<CommentVO> result = new PageResult<>();
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotal(page.getTotal());

        List<Comment> roots = page.getRecords();
        if (roots.isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        // ============ 第二段:批量查这些楼层的楼中楼回复 ============
        List<Long> rootIds = roots.stream().map(Comment::getId).collect(Collectors.toList());
        List<Comment> children = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .in(Comment::getParentId, rootIds)
                        .eq(Comment::getStatus, COMMENT_STATUS_NORMAL)
                        .orderByAsc(Comment::getCreatedAt));
        // 按楼层分组:Map<楼层id, 该楼的回复列表>
        Map<Long, List<Comment>> childrenMap = children.stream()
                .collect(Collectors.groupingBy(Comment::getParentId));

        // ============ 批量查所有涉及的用户(楼层+回复+被回复对象,一次 IN) ============
        List<Long> userIds = new ArrayList<>();
        roots.forEach(c -> userIds.add(c.getUserId()));
        children.forEach(c -> {
            userIds.add(c.getUserId());
            if (c.getReplyToUserId() != null) {
                userIds.add(c.getReplyToUserId());
            }
        });
        Map<Long, UserVO> userMap = buildUserMap(userIds);

        // ============ 组装:楼层 + 各自的 replies ============
        List<CommentVO> commentVOS = roots.stream().map(root -> {
            CommentVO rootVO = toVO(root, userMap);
            List<CommentVO> replyVOs = childrenMap.getOrDefault(root.getId(), Collections.emptyList())
                    .stream()
                    .map(child -> toVO(child, userMap))
                    .collect(Collectors.toList());
            rootVO.setReplies(replyVOs);
            return rootVO;
        }).collect(Collectors.toList());

        result.setRecords(commentVOS);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO createComment(Long postId, SaveCommentDTO dto) {
        // 1、参数校验
        if (postId == null) {
            throw new BusinessException("帖子ID不能为空");
        }
        if (dto == null || dto.getContent() == null || dto.getContent().isBlank()) {
            throw new BusinessException("评论内容不能为空");
        }

        // 2、帖子必须存在且已发布
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() != POST_STATUS_PUBLISHED) {
            throw new BusinessException("帖子不存在或不可评论");
        }

        Long userId = BaseContext.getCurrentId();

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(dto.getContent().trim());
        comment.setLikeCount(0);
        comment.setStatus(COMMENT_STATUS_NORMAL);

        // 3、楼中楼:parentId 统一「拍平」到楼层
        Long parentId = dto.getParentId() == null ? ROOT_PARENT_ID : dto.getParentId();
        Long replyToUserId = dto.getReplyToUserId();

        if (parentId != ROOT_PARENT_ID) {
            Comment parent = getById(parentId);
            if (parent == null || !postId.equals(parent.getPostId()) || parent.getStatus() != COMMENT_STATUS_NORMAL) {
                throw new BusinessException("回复的评论不存在");
            }
            // 回复的目标本身是楼中楼回复时,归属到它所在的楼层
            if (parent.getParentId() != ROOT_PARENT_ID) {
                parentId = parent.getParentId();
            }
            // 没显式传回复对象时,默认回复父评论的作者
            if (replyToUserId == null) {
                replyToUserId = parent.getUserId();
            }
            comment.setReplyToUserId(replyToUserId);
        }
        comment.setParentId(parentId);

        save(comment);

        // 4、帖子评论数 +1(原子自增,避免并发下读-改-写丢失更新)
        postMapper.update(null, new UpdateWrapper<Post>()
                .eq("id", postId)
                .setSql("comment_count = comment_count + 1"));
        // 失效帖子详情缓存,保证详情页评论数实时一致
        postService.evictPostCache(postId);
        hotRankService.incrComment(postId);

        // 5、组装返回(评论者 + 被回复者)
        return buildVO(comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCommentById(Long id) {
        // 1、评论必须存在且未删除
        Comment comment = getById(id);
        if (comment == null || comment.getStatus() != COMMENT_STATUS_NORMAL) {
            throw new BusinessException("评论不存在或已删除");
        }

        // 2、只能删自己的评论
        if (!comment.getUserId().equals(BaseContext.getCurrentId())) {
            throw new BusinessException("无权删除他人评论");
        }

        // 3、软删当前评论
        comment.setStatus(COMMENT_STATUS_DELETE);
        updateById(comment);

        // 4、删的是楼层时,连带软删其楼中楼回复
        int deletedCount = 1;
        boolean isFloor = comment.getParentId() != null && comment.getParentId() == ROOT_PARENT_ID;
        if (isFloor) {
            long replyCount = count(new LambdaQueryWrapper<Comment>()
                    .eq(Comment::getParentId, id)
                    .eq(Comment::getStatus, COMMENT_STATUS_NORMAL));
            update(new UpdateWrapper<Comment>()
                    .eq("parent_id", id)
                    .eq("status", COMMENT_STATUS_NORMAL)
                    .set("status", COMMENT_STATUS_DELETE));
            deletedCount += (int) replyCount;
        }

        // 5、帖子评论数减去实际删除条数(原子自减,避免并发丢失更新)
        postMapper.update(null, new UpdateWrapper<Post>()
                .eq("id", comment.getPostId())
                .setSql("comment_count = comment_count - " + deletedCount));
        // 失效帖子详情缓存
        postService.evictPostCache(comment.getPostId());
        hotRankService.decrComment(comment.getPostId(), deletedCount);
    }

    /** 批量查用户,建立 id -> UserVO 映射(去重) */
    private Map<Long, UserVO> buildUserMap(List<Long> userIds) {
        return userService.listByIds(userIds.stream().distinct().collect(Collectors.toList())).stream()
                .collect(Collectors.toMap(User::getId, user -> {
                    UserVO vo = new UserVO();
                    BeanUtils.copyProperties(user, vo);
                    return vo;
                }));
    }

    /** 组装单条评论 VO(评论者 + 被回复者) */
    private CommentVO buildVO(Comment comment) {
        List<Long> userIds = new ArrayList<>();
        userIds.add(comment.getUserId());
        if (comment.getReplyToUserId() != null) {
            userIds.add(comment.getReplyToUserId());
        }
        return toVO(comment, buildUserMap(userIds));
    }

    /** Comment 实体转 CommentVO,并配好评论者与被回复对象 */
    private CommentVO toVO(Comment comment, Map<Long, UserVO> userMap) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);
        vo.setUser(userMap.get(comment.getUserId()));
        if (comment.getReplyToUserId() != null) {
            vo.setReplyToUser(userMap.get(comment.getReplyToUserId()));
        }
        return vo;
    }
}
