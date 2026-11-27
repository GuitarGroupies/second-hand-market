package com.students.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.students.common.constant.RedisKeyConstant;
import com.students.common.context.UserContext;
import com.students.common.result.Result;
import com.students.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 游客浏览限流拦截器。
 *
 * <p>已登录用户放行且不计数；游客按 IP 计入每日额度，超限后返回
 * ResultCode.GUEST_LIMIT，引导用户注册登录。</p>
 *
 * @author students
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GuestLimitInterceptor implements HandlerInterceptor {

    /**
     * 原子计数脚本：INCR 后若为首次访问则设置过期时间。
     *
     * <p><b>为什么必须用 Lua：</b>INCR 与 EXPIRE 分开执行时，
     * 若中间发生故障导致 EXPIRE 未执行，该 key 将永不过期，
     * 对应 IP 会被永久封禁。</p>
     */
    private static final DefaultRedisScript<Long> INCR_WITH_TTL = new DefaultRedisScript<>(
            "local c = redis.call('INCR', KEYS[1]) "
                    + "if c == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end "
                    + "return c",
            Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    /** 是否启用限流 */
    @Value("${shm.guest.enabled:true}")
    private boolean enabled;

    /** 每日额度 */
    @Value("${shm.guest.limit:10}")
    private int limit;

    /** 本地开发白名单，逗号分隔 */
    @Value("${shm.guest.whitelist:127.0.0.1,0:0:0:0:0:0:0:1}")
    private String whitelistStr;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 1. 已登录用户不限流
        if (UserContext.getUserId() != null) {
            return true;
        }
        // 2. 功能关闭时放行
        if (!enabled) {
            return true;
        }
        // 3. 白名单 IP 放行（本地开发）
        String ip = getClientIp(request);
        if (inWhitelist(ip)) {
            return true;
        }

        // 4. 原子计数并判断
        String key = RedisKeyConstant.GUEST_BROWSE + ip;
        Long count = stringRedisTemplate.execute(
                INCR_WITH_TTL,
                Collections.singletonList(key),
                String.valueOf(RedisKeyConstant.GUEST_BROWSE_TTL_SECONDS));

        if (count != null && count > limit) {
            log.info("游客浏览超限：ip={}, count={}, limit={}", ip, count, limit);
            return writeLimitResponse(response);
        }
        return true;
    }

    /**
     * 取真实客户端 IP。
     *
     * <p>经过反向代理时 getRemoteAddr() 拿到的是代理 IP，
     * 需要优先读取 X-Forwarded-For 的第一个地址。</p>
     *
     * @param request 请求
     * @return 客户端 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 判断 IP 是否命中白名单。
     *
     * @param ip 客户端 IP
     * @return 命中返回 true
     */
    private boolean inWhitelist(String ip) {
        if (ip == null || whitelistStr == null || whitelistStr.isBlank()) {
            return false;
        }
        List<String> whitelist = Arrays.stream(whitelistStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        return whitelist.contains(ip);
    }

    /**
     * 写出限流提示。
     *
     * @param response 响应
     * @return 恒为 false（中断请求）
     */
    private boolean writeLimitResponse(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(
                Result.fail(ResultCode.GUEST_LIMIT)));
        return false;
    }
}
