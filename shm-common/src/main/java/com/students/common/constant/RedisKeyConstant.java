package com.students.common.constant;
/**
 * Redis key 统一管理常量。
 *
 * <p>所有 Redis key 的前缀都集中在这里定义，避免在业务代码中散落字符串常量
 * （拼写错误无法被编译器发现，且后期修改前缀要全局搜索）。</p>
 *
 * <p>命名规范：{@code 业务:对象:标识}，例如 {@code shm:auth:token:xxx}。</p>
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
    /** 全局前缀 */
    private  static final String PREFIX="shm:";
    /** 用户登录 Token，完整 key 为 shm:auth:token:{userId} */
    public static final String AUTH_TOKEN = PREFIX + "auth:token:";

    /** 登录 Token 有效期（秒），需与 JWT 有效期保持一致 */
    public static final long AUTH_TOKEN_TTL_SECONDS = 7 * 24 * 60 * 60L;
    /** 用户信息缓存，完整 key 为 shm:user:info:{userId} */
    public static final String USER_INFO = PREFIX + "user:info:";



}
