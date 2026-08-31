package com.techhub.controller;

import com.techhub.common.Result;
import com.techhub.dto.CollectDTO;
import com.techhub.service.ICollectRecordService;

import com.techhub.vo.CollectResultVO;
import com.techhub.vo.LikeResultVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
public class CollectRecordController {
    @Autowired
    private ICollectRecordService collectRecordService;

    @PostMapping("/collect")
    public Result<CollectResultVO> collect(@Valid @RequestBody CollectDTO collectDTO){
        return Result.success(collectRecordService.collect(collectDTO));
    }

    @DeleteMapping("/collect")
    public Result<CollectResultVO> cancleCollect(@Valid CollectDTO collectDTO){
        return Result.success(collectRecordService.cancleCollect(collectDTO));
    }
}
