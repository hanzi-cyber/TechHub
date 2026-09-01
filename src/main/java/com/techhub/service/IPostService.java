package com.techhub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.techhub.common.PageResult;
import com.techhub.dto.SavePostDTO;
import com.techhub.entity.Post;
import com.techhub.enumsort.SortType;
import com.techhub.vo.PostVO;

public interface IPostService extends IService<Post> {
    PageResult<PostVO> getPosts(Integer pageNum, Integer pageSize, SortType sort, String keyword, Integer tagId);

    PostVO getPostById(Long id);

    PostVO createPost(SavePostDTO savePostDTO);

    /**
     * 失效帖子详情缓存(帖子数据变更时调用,如点赞/收藏/评论数变化)
     *
     * @param postId 帖子ID
     */
    void evictPostCache(Long postId);
}
