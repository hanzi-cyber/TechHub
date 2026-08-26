package com.techhub.common;

import lombok.Getter;

/**
 * 统一返回状态码
 */
@Getter
public enum ResultCode {

    // 通用
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统内部错误"),

    // 业务错误码(1xxx 用户, 2xxx 帖子, 3xxx 评论, 4xxx 互动)
    USERNAME_EXISTS(1001, "用户名已存在"),
    EMAIL_EXISTS(1002, "邮箱已被注册"),
    USER_NOT_FOUND(1003, "用户不存在"),
    PASSWORD_ERROR(1004, "用户名或密码错误"),
    ACCOUNT_DISABLED(1005, "账号已被封禁"),

    POST_NOT_FOUND(2001, "帖子不存在"),
    COMMENT_NOT_FOUND(3001, "评论不存在"),

    REPEAT_LIKE(4001, "请勿重复点赞"),
    REPEAT_COLLECT(4002, "请勿重复收藏");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
