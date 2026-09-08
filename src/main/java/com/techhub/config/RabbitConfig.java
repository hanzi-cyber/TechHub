package com.techhub.config;

import com.techhub.common.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置:声明交换机、队列、绑定关系,消息统一用 JSON 序列化
 */
@Configuration
public class RabbitConfig {

    /** 通知交换机(topic,便于后续按不同路由键扩展) */
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(MqConstants.NOTIFICATION_EXCHANGE, true, false);
    }

    /** 通知队列(持久化) */
    @Bean
    public Queue notificationQueue() {
        return new Queue(MqConstants.NOTIFICATION_QUEUE, true);
    }

    /** 队列绑定到交换机,按路由键匹配 */
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with(MqConstants.NOTIFICATION_ROUTING_KEY);
    }

    /** 消息统一用 JSON 序列化(替代默认 Java 序列化,可读性好、跨语言) */
    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
