package com.students.common.constant;

/**
 * Redis key 统一管理常量。
 *
 * <p>所有 Redis key 的前缀都集中在这里定义，避免在业务代码中散落字符串常量
 * （拼写错误无法被编译器发现，且后期修改前缀要全局搜索）。</p>
 *
 * <p>命名规范：{@code 业务:对象:标识}，例如 {@code shm:auth:token:1001}。</p>
 *
 * @author students
 */
public final class RedisKeyConstant {

    /**
     * 私有构造，禁止实例化。
     * <p>工具类 / 常量类不需要创建对象，私有化构造器可以防止被 new。</p>
     */
    private RedisKeyConstant() {
    }

    /** 全局前缀，私有：外部只能使用下面封装好的常量，避免绕过统一管理 */
    private static final String PREFIX = "shm:";

    // ==================== 认证相关 ====================

    /** 用户登录 Token，完整 key 为 shm:auth:token:{userId} */
    public static final String AUTH_TOKEN = PREFIX + "auth:token:";

    /** 登录 Token 有效期（秒），需与 JWT 有效期保持一致 */
    public static final long AUTH_TOKEN_TTL_SECONDS = 7 * 24 * 60 * 60L;

    // ==================== 用户相关 ====================

    /** 用户信息缓存，完整 key 为 shm:user:info:{userId} */
    public static final String USER_INFO = PREFIX + "user:info:";

    // ==================== 商品相关 ====================

    /** 商品详情缓存，完整 key 为 shm:product:detail:{productId} */
    public static final String PRODUCT_DETAIL = PREFIX + "product:detail:";

    /** 商品浏览量计数，完整 key 为 shm:product:view:{productId} */
    public static final String PRODUCT_VIEW = PREFIX + "product:view:";

    /** 商品详情缓存有效期（分钟） */
    public static final long PRODUCT_DETAIL_TTL_MINUTES = 30L;

    /** 缓存空值标记，用于防止缓存穿透 */
    public static final String CACHE_NULL = "NULL";

    // ==================== 游客限流 ====================

    /** 游客浏览次数，完整 key 为 shm:guest:browse:{ip} */
    public static final String GUEST_BROWSE = PREFIX + "guest:browse:";

    /** 游客浏览次数限制（次/天） */
    public static final int GUEST_BROWSE_LIMIT = 10;

    /** 游客浏览次数计数有效期（秒），一天 */
    public static final long GUEST_BROWSE_TTL_SECONDS = 24 * 60 * 60L;
}
