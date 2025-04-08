package com.weiki.usercenterbackend.service;

import com.weiki.usercenterbackend.config.RabbitMQConfig;
import com.weiki.usercenterbackend.model.domain.User;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MessageProducer {
    
    @Resource
    private RabbitTemplate rabbitTemplate;
    
    public void sendUserOperationMessage(User user, String operation) {
        String message = String.format("用户[%s]执行了[%s]操作", user.getUserAccount(), operation);
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.USER_OPERATION_EXCHANGE,
            RabbitMQConfig.USER_OPERATION_ROUTING_KEY,
            message
        );
    }
} 