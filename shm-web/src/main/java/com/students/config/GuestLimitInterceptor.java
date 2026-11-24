package com.students.config;

import com.students.common.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 游客浏览限流拦截器。
 *
 * @author students
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GuestLimitInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 已登录用户不限流
        if (UserContext.getUserId() != null) {
            return true;
        }
        // TODO 游客限流逻辑待实现
        return true;
    }
}
