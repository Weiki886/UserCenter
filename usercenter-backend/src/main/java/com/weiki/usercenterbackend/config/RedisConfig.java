package com.weiki.usercenterbackend.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis配置
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 配置RedisTemplate
     *
     * @param connectionFactory Redis连接工厂
     * @return RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 使用StringRedisSerializer来序列化和反序列化redis的key
        template.setKeySerializer(new StringRedisSerializer());
        
        // 使用GenericJackson2JsonRedisSerializer来序列化和反序列化redis的value
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Hash的value也采用GenericJackson2JsonRedisSerializer的序列化方式
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置StringRedisTemplate
     *
     * @param connectionFactory Redis连接工厂
     * @return StringRedisTemplate
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    /**
     * 配置Redis缓存管理器
     *
     * @param connectionFactory Redis连接工厂
     * @return CacheManager
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 默认配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // 特定缓存配置
        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
        // 用户缓存配置 - 缓存1小时
        configMap.put("userCache", defaultConfig.entryTtl(Duration.ofHours(1)));
        // 用户列表缓存配置 - 缓存10分钟
        configMap.put("userListCache", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        // 短期缓存配置 - 缓存5分钟
        configMap.put("shortTermCache", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configMap)
                .transactionAware()
                .build();
    }

    /**
     * 配置Redis过期键监听器容器
     *
     * @param connectionFactory Redis连接工厂
     * @return RedisMessageListenerContainer
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // 监听过期键事件
        container.addMessageListener((message, pattern) -> {
            // 处理过期键事件
            String expiredKey = new String(message.getBody());
            System.out.println("Key expired: " + expiredKey);
        }, new PatternTopic("__keyevent@*__:expired"));
        return container;
    }
} 