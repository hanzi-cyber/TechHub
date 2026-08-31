package com.techhub.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.techhub.common.exception.BusinessException;
import com.techhub.common.properties.OssProperties;
import com.techhub.context.BaseContext;
import com.techhub.service.IOssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class OssServiceImpl implements IOssService {

    /** 头像大小上限:5MB */
    private static final long MAX_SIZE = 5 * 1024 * 1024;

    @Autowired
    private OssProperties ossProperties;

    @Override
    public String uploadAvatar(MultipartFile file) {
        // 1、基础校验
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("只能上传图片文件");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("图片大小不能超过 5MB");
        }

        // 2、生成唯一对象名,避免同名覆盖
        Long userId = BaseContext.getCurrentId();
        String ext = getExtension(file.getOriginalFilename());
        String objectKey = "avatar/" + userId + "/" + UUID.randomUUID().toString().replace("-", "") + ext;

        // 3、用 AccessKey 直接上传
        OSS ossClient = new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret());
        try (InputStream in = file.getInputStream()) {
            ossClient.putObject(ossProperties.getBucketName(), objectKey, in);
        } catch (IOException e) {
            throw new BusinessException("图片上传失败");
        } finally {
            ossClient.shutdown();
        }

        // 4、拼接访问 URL(前提:bucket 已设为公共读)
        return "https://" + ossProperties.getBucketName() + "." + ossProperties.getEndpoint() + "/" + objectKey;
    }

    /** 取小写扩展名(含点),没有则默认 .jpg */
    private String getExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf(".")).toLowerCase();
        }
        return ".jpg";
    }
}
