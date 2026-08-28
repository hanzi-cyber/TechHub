package com.techhub.controller;

import com.techhub.common.Result;
import com.techhub.service.ITagService;
import com.techhub.vo.TagVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired
    private ITagService tagService;

    @GetMapping
    public Result<List<TagVO>> getTags(){
        List<TagVO> tagVOList = tagService.getTags();
        return Result.success(tagVOList);
    }
}
