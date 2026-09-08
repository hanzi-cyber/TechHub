package com.techhub.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通知消息体:生产者 -> RabbitMQ -> 消费者
 * 只携带最小必要字段,消费者据此落库
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 接收者ID */
    private Long userId;

    /** 触发者ID */
    private Long senderId;

    /** 通知类型:1点赞 2评论 3关注 5收藏 */
    private Integer type;

    /** 目标类型:1帖子 3用户 */
    private Integer targetType;

    /** 目标ID(帖子ID / 用户ID) */
    private Long targetId;

    /** 通知内容(冗余快照,发送前已拼好) */
    private String content;
}
