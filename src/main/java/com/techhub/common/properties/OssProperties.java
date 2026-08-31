package com.techhub.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 配置(application-dev.yml 中的 aliyun.oss.*)
 */
@Data
@ConfigurationProperties(prefix = "aliyun.oss")
@Component
public class OssProperties {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
}
