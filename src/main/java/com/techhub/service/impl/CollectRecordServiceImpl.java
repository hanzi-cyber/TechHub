package com.techhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.techhub.common.Result;
import com.techhub.common.exception.BusinessException;
import com.techhub.context.BaseContext;
import com.techhub.dto.CollectDTO;
import com.techhub.entity.CollectRecord;
import com.techhub.entity.Post;
import com.techhub.mapper.CollectRecordMapper;
import com.techhub.mapper.PostMapper;
import com.techhub.service.ICollectRecordService;
import com.techhub.service.IPostService;
import com.techhub.vo.CollectResultVO;
import org.apache.ibatis.annotations.Insert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CollectRecordServiceImpl extends ServiceImpl<CollectRecordMapper, CollectRecord> implements ICollectRecordService {
    @Autowired
    private CollectRecordMapper collectRecordMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private IPostService postService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CollectResultVO collect(CollectDTO collectDTO) {
        Long postId = collectDTO.getPostId();
        if(postId == null){
            throw new BusinessException("该帖子已不存在");
        }
        Long userId = BaseContext.getCurrentId();
        //insert 先插入数据如果存在的话就执行更新
        int affected = collectRecordMapper.creatCollectRecord(userId,postId);
        if(affected>0){
            postMapper.update(new LambdaUpdateWrapper<Post>()
                    .eq(Post::getId,postId)
                    .setSql("collect_count = collect_count + 1"));
            postService.evictPostCache(postId);
        }
        return new CollectResultVO(true,readCollectCount(postId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CollectResultVO cancleCollect(CollectDTO collectDTO) {
        Long postId = collectDTO.getPostId();
        if(postId == null){
            throw new BusinessException("该帖子已不存在");
        }
        Long userId = BaseContext.getCurrentId();
        int affected = collectRecordMapper.cancleCollect(userId,postId);
        if(affected > 0){
            postMapper.update(null,new LambdaUpdateWrapper<Post>()
                    .eq(Post::getId,postId)
                    .setSql("collect_count = collect_count - 1"));
            postService.evictPostCache(postId);
        }
        return new CollectResultVO(false,readCollectCount(postId));
    }

    private Integer readCollectCount(Long postId) {

        Post post = postMapper.selectById(postId);
        if (post == null){
            throw new BusinessException("该帖子已不存在");
        }
        return post.getCollectCount();
    }
}
