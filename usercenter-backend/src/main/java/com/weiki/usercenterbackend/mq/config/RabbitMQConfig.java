package com.weiki.usercenterbackend.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 * 仅在spring.rabbitmq.enabled=true时生效
 */
@Configuration
@EnableRabbit
@ConditionalOnProperty(prefix = "spring.rabbitmq", name = "enabled", havingValue = "true")
public class RabbitMQConfig {

    // 定义交换机常量
    public static final String USER_EXCHANGE = "user.exchange";
    
    // 定义队列常量
    public static final String USER_REGISTER_QUEUE = "user.register.queue";
    public static final String USER_ACTIVITY_QUEUE = "user.activity.queue";
    
    // 定义路由键常量
    public static final String USER_REGISTER_KEY = "user.register";
    public static final String USER_ACTIVITY_KEY = "user.activity";
    
    /**
     * 用户相关的交换机
     */
    @Bean
    public DirectExchange userExchange() {
        return new DirectExchange(USER_EXCHANGE, true, false);
    }
    
    /**
     * 用户注册队列
     */
    @Bean
    public Queue userRegisterQueue() {
        return new Queue(USER_REGISTER_QUEUE, true);
    }
    
    /**
     * 用户活动队列
     */
    @Bean
    public Queue userActivityQueue() {
        return new Queue(USER_ACTIVITY_QUEUE, true);
    }
    
    /**
     * 用户注册队列绑定交换机
     */
    @Bean
    public Binding bindingRegister() {
        return BindingBuilder.bind(userRegisterQueue()).to(userExchange()).with(USER_REGISTER_KEY);
    }
    
    /**
     * 用户活动队列绑定交换机
     */
    @Bean
    public Binding bindingActivity() {
        return BindingBuilder.bind(userActivityQueue()).to(userExchange()).with(USER_ACTIVITY_KEY);
    }
    
    /**
     * 使用JSON序列化消息
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    /**
     * 定制RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        
        // 开启发送确认
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                // 记录日志
                System.err.println("消息发送失败：" + cause);
            }
        });
        
        // 开启发送失败退回
        rabbitTemplate.setReturnsCallback(returned -> {
            System.err.println("消息被退回：" + returned.getMessage());
        });
        
        return rabbitTemplate;
    }
} 