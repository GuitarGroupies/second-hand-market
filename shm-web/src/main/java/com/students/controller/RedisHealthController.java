package com.students.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 连通性验证接口。
 *
 * <p>访问 {@code GET /api/redis/health} 会依次执行：</p>
 * <ol>
 *     <li>从连接池拿一条真实连接</li>
 *     <li>执行 {@code PING}，Redis 返回 {@code PONG} 才算连通</li>
 *     <li>写入一个带 30 秒过期的 key，再读回来比对</li>
 * </ol>
 *
 * @author students
 */
@Slf4j
@RestController
@RequestMapping("/api/redis")
@RequiredArgsConstructor
public class RedisHealthController {

    /** 验证用的 key，带过期时间避免留下垃圾数据 */
    private static final String PING_KEY = "shm:health:ping";

    private final RedisConnectionFactory connectionFactory;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis 连通性检查。
     *
     * @return 连接与读写结果
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>(8);

        // 1. PING：验证网络 + 认证 + 服务是否存活
        try (RedisConnection connection = connectionFactory.getConnection()) {
            String pong = connection.ping();
            result.put("ping", pong);
            log.info("Redis PING 返回：{}", pong);
        } catch (Exception e) {
            log.error("Redis 连接失败", e);
            result.put("ping", "FAILED: " + e.getMessage());
            result.put("success", false);
            return result;
        }

        // 2. String 模板写入 / 读取（value 为普通字符串）
        try {
            stringRedisTemplate.opsForValue().set(PING_KEY, "hello-redis", Duration.ofSeconds(30));
            String value = stringRedisTemplate.opsForValue().get(PING_KEY);
            Long ttl = stringRedisTemplate.getExpire(PING_KEY);
            result.put("stringValue", value);
            result.put("ttlSeconds", ttl);
            log.info("Redis 读写成功，value={}，剩余 TTL={} 秒", value, ttl);
        } catch (Exception e) {
            log.error("Redis 读写失败", e);
            result.put("stringValue", "FAILED: " + e.getMessage());
            result.put("success", false);
            return result;
        }

        // 3. JSON 模板写入 / 读取（value 为对象，验证序列化配置）
        try {
            Map<String, Object> obj = new HashMap<>(4);
            obj.put("name", "二手交易");
            obj.put("version", 1);
            redisTemplate.opsForValue().set("shm:health:obj", obj, Duration.ofSeconds(30));
            Object readBack = redisTemplate.opsForValue().get("shm:health:obj");
            result.put("objectValue", readBack);
            log.info("Redis JSON 序列化验证成功：{}", readBack);
        } catch (Exception e) {
            log.error("Redis JSON 序列化失败", e);
            result.put("objectValue", "FAILED: " + e.getMessage());
            result.put("success", false);
            return result;
        }

        result.put("success", true);
        return result;
    }
}
