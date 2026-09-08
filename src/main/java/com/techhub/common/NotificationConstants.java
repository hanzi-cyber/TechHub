package com.techhub.common;

/**
 * 通知相关常量
 */
public class NotificationConstants {

    private NotificationConstants() {
    }

    // ===== 通知类型 type(落库到 t_notification.type) =====
    public static final int TYPE_LIKE = 1;
    public static final int TYPE_COMMENT = 2;
    public static final int TYPE_FOLLOW = 3;
    public static final int TYPE_SYSTEM = 4;
    public static final int TYPE_COLLECT = 5;

    // ===== 通知目标类型 targetType(落库到 t_notification.target_type) =====
    /** 帖子(点击跳转到帖子详情) */
    public static final int TARGET_POST = 1;
    /** 用户(点击跳转到对方主页,关注通知用) */
    public static final int TARGET_USER = 3;

    // ===== 点赞目标类型(与 LikeDTO.targetType 语义一致,仅用于定位接收者,不落库) =====
    public static final int LIKE_TARGET_POST = 1;
    public static final int LIKE_TARGET_COMMENT = 2;

    /** 评论/回复内容在通知里的最大长度,超出截断 */
    public static final int CONTENT_PREVIEW_LENGTH = 50;
}
