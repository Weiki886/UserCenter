package com.weiki.usercenterbackend.mq.consumer;

import com.rabbitmq.client.Channel;
import com.weiki.usercenterbackend.mq.config.RabbitMQConfig;
import com.weiki.usercenterbackend.mq.model.UserMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 用户消息消费者
 * 仅在spring.rabbitmq.enabled=true时生效
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "spring.rabbitmq", name = "enabled", havingValue = "true")
public class UserMessageConsumer {

    /**
     * 处理用户注册消息
     */
    @RabbitListener(queues = RabbitMQConfig.USER_REGISTER_QUEUE)
    public void handleUserRegisterMessage(UserMessage userMessage, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("收到用户注册消息: {}", userMessage);
            
            // TODO: 实现业务逻辑，例如发送欢迎邮件、初始化用户数据等
            
            // 确认消息已处理
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理用户注册消息异常", e);
            // 拒绝消息，不重新入队
            channel.basicReject(deliveryTag, false);
        }
    }

    /**
     * 处理用户活动消息
     */
    @RabbitListener(queues = RabbitMQConfig.USER_ACTIVITY_QUEUE)
    public void handleUserActivityMessage(UserMessage userMessage, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("收到用户活动消息: {}", userMessage);
            
            // TODO: 实现业务逻辑，例如记录用户活动、更新统计数据等
            
            // 确认消息已处理
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理用户活动消息异常", e);
            // 拒绝消息，不重新入队
            channel.basicReject(deliveryTag, false);
        }
    }
} 