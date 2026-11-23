package com.students.config;

import com.students.common.constant.RedisKeyConstant;
import com.students.common.context.UserContext;
import com.students.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录解析拦截器（可选登录）。
 *
 * <p><b>设计要点：本拦截器不阻断请求</b>，只负责「尽力解析当前用户」：</p>
 * <ul>
 *     <li>带了有效 token → 把 userId 写入 {@link UserContext}，表示已登录</li>
 *     <li>没带 token、token 无效、token 已被登出覆盖 → 一律按游客处理，userId 为 null</li>
 * </ul>
 *
 * <p>这样同一条链路既能服务已登录用户，也能服务游客；需要强制登录的接口
 * 自行判断 {@code UserContext.getUserId() == null} 并抛出未登录异常。</p>
 *
 * <p>游客浏览限流由独立的拦截器负责，不做在这里。</p>
 *
 * @author students
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    /** 请求头名称 */
    private static final String HEADER_AUTHORIZATION = "Authorization";

    /** Bearer 前缀 */
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader(HEADER_AUTHORIZATION);

        // 1. 没有 token：按游客放行
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return true;
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            return true;
        }

        // 2. 解析 JWT，拿到 userId
        Long userId = jwtUtil.parseUserId(token);
        if (userId == null) {
            log.debug("JWT 无效或已过期，按游客处理");
            return true;
        }

        // 3. 与 Redis 中的 token 比对，处理「已退出登录」「被新设备登录覆盖」两种情况
        String redisToken = stringRedisTemplate.opsForValue()
                .get(RedisKeyConstant.AUTH_TOKEN + userId);
        if (redisToken == null || !redisToken.equals(token)) {
            log.debug("token 已失效（已登出或被覆盖），按游客处理：userId={}", userId);
            return true;
        }

        // 4. 校验通过，写入上下文
        UserContext.setUserId(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 必须清除，防止线程池复用导致串号
        UserContext.clear();
    }
}
