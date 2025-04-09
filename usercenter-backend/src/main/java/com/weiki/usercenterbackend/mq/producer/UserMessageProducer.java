package com.weiki.usercenterbackend.mq.producer;

import com.weiki.usercenterbackend.mq.config.RabbitMQConfig;
import com.weiki.usercenterbackend.mq.model.UserMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

/**
 * 用户消息生产者
 * 仅在spring.rabbitmq.enabled=true时生效
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "spring.rabbitmq", name = "enabled", havingValue = "true")
public class UserMessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送用户注册消息
     * @param userId 用户ID
     * @param content 消息内容
     * @return 消息ID
     */
    public String sendUserRegisterMessage(Long userId, String content) {
        return sendUserMessage(userId, "register", content, null);
    }

    /**
     * 发送用户活动消息
     * @param userId 用户ID
     * @param content 消息内容
     * @param extraData 附加数据
     * @return 消息ID
     */
    public String sendUserActivityMessage(Long userId, String content, String extraData) {
        return sendUserMessage(userId, "activity", content, extraData);
    }

    /**
     * 发送用户相关消息
     * @param userId 用户ID
     * @param type 消息类型
     * @param content 消息内容
     * @param extraData 附加数据
     * @return 消息ID
     */
    private String sendUserMessage(Long userId, String type, String content, String extraData) {
        String messageId = UUID.randomUUID().toString();
        
        // 创建消息对象
        UserMessage message = UserMessage.builder()
                .messageId(messageId)
                .userId(userId)
                .type(type)
                .content(content)
                .extraData(extraData)
                .createTime(new Date())
                .build();
        
        // 创建关联数据（用于消息确认）
        CorrelationData correlationData = new CorrelationData(messageId);
        
        // 根据消息类型选择不同的路由键发送
        String routingKey;
        if ("register".equals(type)) {
            routingKey = RabbitMQConfig.USER_REGISTER_KEY;
        } else if ("activity".equals(type)) {
            routingKey = RabbitMQConfig.USER_ACTIVITY_KEY;
        } else {
            // 默认使用注册路由
            routingKey = RabbitMQConfig.USER_REGISTER_KEY;
        }
        
        log.info("发送消息到MQ，消息ID: {}, 路由键: {}", messageId, routingKey);
        
        // 发送消息
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.USER_EXCHANGE,
                routingKey,
                message,
                correlationData
        );
        
        return messageId;
    }
} 