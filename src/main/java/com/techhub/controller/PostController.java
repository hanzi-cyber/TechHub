package com.techhub.controller;

import com.techhub.common.PageResult;
import com.techhub.common.Result;
import com.techhub.enumsort.SortType;
import com.techhub.service.IPostService;
import com.techhub.vo.PostVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private IPostService postService;

    /**
     * 分页获取帖子列表
     * @param pageNum  页码,默认1
     * @param pageSize 每页条数,默认10
     * @param sort     排序方式,latest最新 / hot热门,默认latest
     * @param keyword  关键词(可选)
     * @param tagId    标签ID(可选)
     */
    @GetMapping
    public Result<PageResult<PostVO>> getPosts(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                               @RequestParam(defaultValue = "latest") SortType sort,
                                               @RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) Integer tagId) {
        PageResult<PostVO> pageResult = postService.getPosts(pageNum, pageSize, sort, keyword, tagId);
        return Result.success(pageResult);
    }
}
