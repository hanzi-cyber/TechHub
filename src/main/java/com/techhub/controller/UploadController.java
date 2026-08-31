package com.techhub.controller;

import com.techhub.common.Result;
import com.techhub.service.IOssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Autowired
    private IOssService ossService;

    /**
     * 上传头像,返回可公开访问的图片 URL
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return Result.success(ossService.uploadAvatar(file));
    }
}
