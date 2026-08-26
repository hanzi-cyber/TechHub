package com.techhub.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知返回结果
 */
@Data
public class NotificationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 接收者ID */
    private Long userId;

    /** 触发者ID */
    private Long senderId;

    /** 类型:1点赞 2评论 3关注 4系统 */
    private Integer type;

    /** 目标类型:1帖子 2评论 */
    private Integer targetType;

    /** 目标ID */
    private Long targetId;

    private String content;

    /** 是否已读:1是 0否 */
    private Integer isRead;

    private LocalDateTime createdAt;
}
