package com.techhub.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新当前用户资料请求参数
 */
@Data
public class UpdateUserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 个人简介 */
    private String bio;

    /** 头像地址 */
    private String avatarUrl;
}
