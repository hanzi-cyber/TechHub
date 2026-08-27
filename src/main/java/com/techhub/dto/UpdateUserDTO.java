package com.techhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 更新当前用户资料请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户名(登录名,唯一) */
    private String username;

    /** 邮箱(唯一) */
    private String email;

    /** 手机号(唯一) */
    private String phone;

    /** 个人简介 */
    private String bio;

    /** 头像地址 */
    private String avatarUrl;
}
