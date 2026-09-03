package com.techhub.controller;

import com.techhub.common.PageResult;
import com.techhub.common.Result;
import com.techhub.dto.SavePostDTO;
import com.techhub.enumsort.SortType;
import com.techhub.service.IPostService;
import com.techhub.vo.PostVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 关注流(拉模式):分页获取当前用户关注的人发布的帖子
     */
    @GetMapping("/feed")
    public Result<PageResult<PostVO>> getFeed(@RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(postService.getFollowFeed(pageNum, pageSize));
    }

    /**
     * 根据ID获取帖子详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<PostVO> getPostById(@PathVariable Long id) {
        PostVO postVO = postService.getPostById(id);
        return Result.success(postVO);
    }
    /**
     * 创建新帖子
     * @param savePostDTO
     * @return
     */
    @PostMapping
    public Result<PostVO> createPost(@RequestBody SavePostDTO savePostDTO) {
        PostVO createdPost = postService.createPost(savePostDTO);
        return Result.success(createdPost);
    }
















}
