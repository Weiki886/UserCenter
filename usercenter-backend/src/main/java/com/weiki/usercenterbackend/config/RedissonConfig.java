package com.weiki.usercenterbackend.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson配置类
 */
@Configuration
public class RedissonConfig {

    @Value("${redisson.address}")
    private String address;

    @Value("${redisson.database}")
    private int database;

    @Value("${redisson.timeout}")
    private int timeout;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        // 单节点模式配置
        config.useSingleServer()
                .setAddress(address)
                .setDatabase(database)
                .setTimeout(timeout);
        return Redisson.create(config);
    }
} 