package com.techhub.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录返回结果
 */
@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Bearer Token,后续请求放入 Authorization 头 */
    private String token;

    /** 用户信息 */
    private UserVO user;
}
