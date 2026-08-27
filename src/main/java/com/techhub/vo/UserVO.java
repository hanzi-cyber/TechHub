package com.techhub.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息返回结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String email;

    private String phone;

    private String avatarUrl;

    /** 个人简介 */
    private String bio;

    /** 状态:1正常 0封禁 */
    private Integer status;

    private LocalDateTime createdAt;
}
