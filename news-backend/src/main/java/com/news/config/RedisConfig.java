package com.news.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.news.common.CacheConstants;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 缓存配置类
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 配置 RedisTemplate
     * 使用 JSON 序列化，便于调试和跨语言
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // JSON 序列化配置
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        mapper.registerModule(new JavaTimeModule());
        jsonSerializer.setObjectMapper(mapper);

        // Key 使用 String 序列化
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value 使用 JSON 序列化
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置缓存管理器
     * 为不同缓存设置不同的 TTL
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        // JSON 序列化器
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        mapper.registerModule(new JavaTimeModule());
        jsonSerializer.setObjectMapper(mapper);

        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        // 各缓存的自定义配置
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // 分类缓存 - 1小时
        cacheConfigs.put(CacheConstants.CACHE_CATEGORY,
                defaultConfig.entryTtl(Duration.ofSeconds(CacheConstants.TTL_CATEGORY)));

        // 热搜缓存 - 30分钟
        cacheConfigs.put(CacheConstants.CACHE_HOT_SEARCH,
                defaultConfig.entryTtl(Duration.ofSeconds(CacheConstants.TTL_HOT_SEARCH)));

        // 新闻缓存 - 10分钟
        cacheConfigs.put(CacheConstants.CACHE_NEWS,
                defaultConfig.entryTtl(Duration.ofSeconds(CacheConstants.TTL_NEWS_LIST)));

        // 评论数缓存 - 2分钟
        cacheConfigs.put(CacheConstants.CACHE_COMMENT_COUNT,
                defaultConfig.entryTtl(Duration.ofSeconds(CacheConstants.TTL_COMMENT_COUNT)));

        // 新闻列表缓存 - 5分钟
        cacheConfigs.put(CacheConstants.CACHE_NEWS_LIST,
                defaultConfig.entryTtl(Duration.ofSeconds(CacheConstants.TTL_NEWS_DETAIL)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
