package com.techhub.controller;

import com.techhub.common.PageResult;
import com.techhub.common.Result;
import com.techhub.service.ICommentService;
import com.techhub.vo.CommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CommentController {


    @Autowired
    private ICommentService commentService;
    @GetMapping("/posts/{postId}/comments")
    public Result<PageResult<CommentVO>> getCommentsByPostId(@PathVariable Long postId ,
                                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<CommentVO> result = commentService.getCommentsByPostId(postId,pageNum,pageSize);
        return Result.success(result);
    }
}
