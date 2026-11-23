package com.students.common.context;

/**
 * 当前登录用户上下文。
 *
 * <p>基于 ThreadLocal 实现：同一线程内可随处取到当前请求的用户 ID，
 * 无需在每个方法签名里层层传递 userId 参数。</p>
 *
 * <p><b>必须在请求结束时清除</b>：Tomcat 使用线程池，线程会被复用，
 * 不清除会导致下一个请求读到上一个用户的 ID（串号事故）。</p>
 *
 * <p><b>游客场景</b>：未登录时 {@link #getUserId()} 返回 null，
 * 业务代码需要判空，或在需要登录的接口里主动抛出未登录异常。</p>
 *
 * @author students
 */
public final class UserContext {

    /** 存放当前线程的用户 ID */
    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    private UserContext() {
    }

    /**
     * 设置当前用户 ID。
     *
     * @param userId 用户 ID
     */
    public static void setUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    /**
     * 获取当前用户 ID。
     *
     * @return 用户 ID；游客或未登录时返回 null
     */
    public static Long getUserId() {
        return CURRENT_USER_ID.get();
    }

    /**
     * 清除当前线程的用户 ID。
     * <p>必须在拦截器的 afterCompletion 中调用，防止线程池复用导致串号。</p>
     */
    public static void clear() {
        CURRENT_USER_ID.remove();
    }
}
