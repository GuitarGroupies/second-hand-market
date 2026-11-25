package com.students.service;

/**
 * 商品浏览量统计服务。
 *
 * @author students
 */
public interface ViewCountService {

    /**
     * 浏览量 +1（只写 Redis，不碰数据库）。
     *
     * @param productId 商品 ID
     */
    void increment(Long productId);

    /**
     * 把 Redis 中累积的浏览量批量同步到数据库。
     *
     * <p>由定时任务调用。</p>
     */
    void flushToDatabase();
}
