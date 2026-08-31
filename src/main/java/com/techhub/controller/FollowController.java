package com.techhub.controller;

import com.techhub.common.Result;
import com.techhub.service.IFollowService;
import com.techhub.vo.FollowResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    @Autowired
    private IFollowService followService;

    /**
     * 关注(幂等),路径上的 userId 为被关注用户ID
     */
    @PostMapping("/{userId}")
    public Result<FollowResultVO> follow(@PathVariable Long userId) {
        return Result.success(followService.follow(userId));
    }

    /**
     * 取消关注(幂等),路径上的 userId 为被关注用户ID
     */
    @DeleteMapping("/{userId}")
    public Result<FollowResultVO> unfollow(@PathVariable Long userId) {
        return Result.success(followService.unfollow(userId));
    }
}
