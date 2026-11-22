package com.students.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 序列化配置。
 *
 * <p><b>为什么要自定义：</b>Spring Boot 默认的 {@code RedisTemplate<Object, Object>} 使用
 * JDK 序列化（{@code JdkSerializationRedisSerializer}），写进 Redis 的 key 和 value 都是
 * 一串二进制乱码，用 {@code redis-cli GET} 看全是 {@code ¬í...}，既不可读也无法跨语言
 * 解析，调试时完全看不到数据。</p>
 *
 * <p><b>这里改成：</b>key 用纯字符串（{@code StringRedisSerializer}），value 用 JSON
 * （{@code GenericJackson2JsonRedisSerializer}）。这样 {@code redis-cli} 里能直接看懂存了什么。</p>
 *
 * @author students
 */
@Configuration
public class RedisConfig {

    /**
     * 字符串 key + JSON value 的 RedisTemplate。
     *
     * @param factory Spring Boot 自动配置好的连接工厂（读取 yml 里 spring.data.redis 配置）
     * @return 配置好序列化器的模板
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        ObjectMapper objectMapper = new ObjectMapper();
        // 允许序列化任意可见性字段（包括 private）
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 写入类型信息，反序列化时才能还原成原对象而不是 LinkedHashMap
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL);

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();
        return template;
    }
}
