package com.techhub.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.techhub.common.MqConstants;
import com.techhub.entity.Notification;
import com.techhub.mapper.NotificationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * 通知消费者:从 RabbitMQ 拉取通知消息并落库
 */
@Slf4j
@Component
public class NotificationListener {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = MqConstants.NOTIFICATION_QUEUE)
    public void onMessage(Message message, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            NotificationMessage msg = objectMapper.readValue(message.getBody(), NotificationMessage.class);
            Notification notification = new Notification();
            notification.setUserId(msg.getUserId());
            notification.setSenderId(msg.getSenderId());
            notification.setType(msg.getType());
            notification.setTargetType(msg.getTargetType());
            notification.setTargetId(msg.getTargetId());
            notification.setContent(msg.getContent());
            notification.setIsRead(0);
            notificationMapper.insert(notification);
            // 手动确认:消费成功
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("通知消息消费失败,deliveryTag={}", deliveryTag, e);
            try {
                // 不回队列(避免坏消息被无限重投),生产环境应投递死信队列做兜底
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ex) {
                log.error("通知消息 nack 失败", ex);
            }
        }
    }
}
