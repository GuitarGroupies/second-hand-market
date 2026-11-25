package com.students.service.impl;

import com.students.common.constant.RedisKeyConstant;
import com.students.mapper.ProductMapper;
import com.students.service.ViewCountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 商品浏览量统计实现。
 *
 * <p><b>TODO（上线前必改）：</b>当前用 KEYS 扫描 key，数据量大时会阻塞 Redis；
 * 生产环境应改用 SCAN 游标遍历。</p>
 *
 * <p>设计思路：热门商品可能一秒被访问上千次，如果每次都 UPDATE 数据库，
 * 行锁会成为瓶颈。因此走「Redis 计数 + 定时批量落库」：</p>
 *
 * <ol>
 *     <li>每次访问只执行一次 {@code INCR}（内存操作，微秒级）</li>
 *     <li>定时任务每 5 分钟把计数批量写回 MySQL，并清空 Redis 计数</li>
 * </ol>
 *
 * @author students
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ViewCountServiceImpl implements ViewCountService {

    private final StringRedisTemplate stringRedisTemplate;

    private final ProductMapper productMapper;

    @Override
    public void increment(Long productId) {
        // 只做一次 INCR，不设过期时间——定时任务刷库后会主动删除
        stringRedisTemplate.opsForValue().increment(RedisKeyConstant.PRODUCT_VIEW + productId);
    }

    @Override
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void flushToDatabase() {
        Set<String> keys;
        try {
            keys = stringRedisTemplate.keys(RedisKeyConstant.PRODUCT_VIEW + "*");
        } catch (Exception e) {
            log.error("扫描浏览量 key 失败", e);
            return;
        }
        if (keys == null || keys.isEmpty()) {
            return;
        }

        int success = 0;
        for (String key : keys) {
            try {
                String value = stringRedisTemplate.opsForValue().get(key);
                if (value == null) {
                    continue;
                }
                long delta = Long.parseLong(value);
                if (delta <= 0) {
                    stringRedisTemplate.delete(key);
                    continue;
                }
                Long productId = Long.valueOf(key.substring(RedisKeyConstant.PRODUCT_VIEW.length()));
                productMapper.incrViewCount(productId, (int) delta);
                // 落库成功后再删除，避免同步失败导致计数丢失
                stringRedisTemplate.delete(key);
                success++;
            } catch (Exception e) {
                log.error("同步浏览量失败：key={}", key, e);
            }
        }
        log.info("浏览量同步完成：待处理 {} 个商品，成功 {} 个", keys.size(), success);
    }
}
