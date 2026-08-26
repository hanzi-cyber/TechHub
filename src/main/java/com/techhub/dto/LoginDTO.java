package com.techhub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求参数
 */
@Data
public class LoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
