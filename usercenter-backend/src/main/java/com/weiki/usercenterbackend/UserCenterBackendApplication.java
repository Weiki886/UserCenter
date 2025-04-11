package com.weiki.usercenterbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * 启动类
 */
@SpringBootApplication(exclude = {
        // 当spring.redis.enabled为false时排除RedissonAutoConfiguration
        RedissonAutoConfiguration.class
})
@MapperScan("com.weiki.usercenterbackend.mapper")
public class UserCenterBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserCenterBackendApplication.class, args);
    }
} 