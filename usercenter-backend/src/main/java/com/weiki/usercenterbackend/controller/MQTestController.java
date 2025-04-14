package com.weiki.usercenterbackend.controller;

import com.weiki.usercenterbackend.common.ResultUtils;
import com.weiki.usercenterbackend.model.response.BaseResponse;
import com.weiki.usercenterbackend.mq.producer.UserMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息队列测试控制器
 * 仅在spring.rabbitmq.enabled=true时生效
 */
@RestController
@RequestMapping("/api/mq")
@Slf4j
@ConditionalOnProperty(prefix = "spring.rabbitmq", name = "enabled", havingValue = "true")
public class MQTestController {

    @Autowired
    private UserMessageProducer userMessageProducer;

    /**
     * 发送用户注册消息测试
     */
    @PostMapping("/sendRegisterMessage")
    public BaseResponse<Map<String, Object>> sendRegisterMessage(@RequestParam Long userId,
                                                 @RequestParam String content) {
        log.info("发送用户注册消息测试: userId={}, content={}", userId, content);
        
        String messageId = userMessageProducer.sendUserRegisterMessage(userId, content);
        
        Map<String, Object> result = new HashMap<>();
        result.put("messageId", messageId);
        result.put("success", true);
        
        return ResultUtils.success(result);
    }

    /**
     * 发送用户活动消息测试
     */
    @PostMapping("/sendActivityMessage")
    public BaseResponse<Map<String, Object>> sendActivityMessage(@RequestParam Long userId,
                                                @RequestParam String content,
                                                @RequestParam(required = false) String extraData) {
        log.info("发送用户活动消息测试: userId={}, content={}, extraData={}", userId, content, extraData);
        
        String messageId = userMessageProducer.sendUserActivityMessage(userId, content, extraData);
        
        Map<String, Object> result = new HashMap<>();
        result.put("messageId", messageId);
        result.put("success", true);
        
        return ResultUtils.success(result);
    }
} 