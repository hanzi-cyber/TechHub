package com.techhub.controller;

import com.techhub.common.Result;
import com.techhub.dto.LikeDTO;
import com.techhub.service.ILikeRecordService;
import com.techhub.vo.LikeResultVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class LikeRecordController {

    @Autowired
    private ILikeRecordService likeRecordService;

    /**
     * 点赞(幂等)
     */
    @PostMapping("/like")
    public Result<LikeResultVO> like(@Valid @RequestBody LikeDTO likeDTO) {
        return Result.success(likeRecordService.like(likeDTO));
    }

    /**
     * 取消点赞(幂等)
     */
    @DeleteMapping("/like")
    public Result<LikeResultVO> unlike(@Valid LikeDTO likeDTO) {
        return Result.success(likeRecordService.unlike(likeDTO));
    }
}
