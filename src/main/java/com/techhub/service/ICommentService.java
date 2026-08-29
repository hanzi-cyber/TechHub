package com.techhub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.techhub.common.PageResult;
import com.techhub.entity.Comment;
import com.techhub.vo.CommentVO;

public interface ICommentService extends IService<Comment> {
    PageResult<CommentVO> getCommentsByPostId(Long postId, Integer pageNum, Integer pageSize);
}
