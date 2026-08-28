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
}
