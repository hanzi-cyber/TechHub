package com.techhub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.techhub.common.PageResult;
import com.techhub.dto.SaveCommentDTO;
import com.techhub.entity.Comment;
import com.techhub.vo.CommentVO;

public interface ICommentService extends IService<Comment> {
    PageResult<CommentVO> getCommentsByPostId(Long postId, Integer pageNum, Integer pageSize);

    /**
     * 发布评论或楼中楼回复
     *
     * @param postId 帖子ID
     * @param dto    评论内容(parentId、replyToUserId、content)
     * @return 新评论VO(含评论者与被回复者信息)
     */
    CommentVO createComment(Long postId, SaveCommentDTO dto);
}
