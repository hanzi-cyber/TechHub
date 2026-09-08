package com.techhub.common;

/**
 * RabbitMQ 交换机 / 队列 / 路由键常量
 */
public class MqConstants {

    private MqConstants() {
    }

    /** 通知交换机(topic 类型,便于后续按不同路由键扩展) */
    public static final String NOTIFICATION_EXCHANGE = "techhub.notification.exchange";

    /** 通知队列 */
    public static final String NOTIFICATION_QUEUE = "techhub.notification.queue";

    /** 通知路由键 */
    public static final String NOTIFICATION_ROUTING_KEY = "notification";
}
