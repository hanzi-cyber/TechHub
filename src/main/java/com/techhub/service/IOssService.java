package com.techhub.service;

import org.springframework.web.multipart.MultipartFile;

public interface IOssService {

    /**
     * 上传头像到 OSS,返回可公开访问的图片 URL
     *
     * @param file 图片文件
     * @return 图片 URL
     */
    String uploadAvatar(MultipartFile file);
}
