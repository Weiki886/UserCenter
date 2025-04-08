package com.weiki.usercenterbackend.service;

import com.weiki.usercenterbackend.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MessageConsumer {
    
    @RabbitListener(queues = RabbitMQConfig.USER_OPERATION_QUEUE)
    public void handleUserOperation(String message) {
        log.info("收到用户操作消息: {}", message);
        // 这里可以添加更多的处理逻辑，比如记录到数据库等
    }
} 