package com.weiki.usercenterbackend.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String USER_OPERATION_QUEUE = "user.operation.queue";
    public static final String USER_OPERATION_EXCHANGE = "user.operation.exchange";
    public static final String USER_OPERATION_ROUTING_KEY = "user.operation";
    
    @Bean
    public Queue userOperationQueue() {
        return new Queue(USER_OPERATION_QUEUE, true);
    }
    
    @Bean
    public DirectExchange userOperationExchange() {
        return new DirectExchange(USER_OPERATION_EXCHANGE);
    }
    
    @Bean
    public Binding userOperationBinding() {
        return BindingBuilder.bind(userOperationQueue())
                .to(userOperationExchange())
                .with(USER_OPERATION_ROUTING_KEY);
    }
} 