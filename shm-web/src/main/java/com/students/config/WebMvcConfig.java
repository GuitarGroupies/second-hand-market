package com.students.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置，注册自定义拦截器。
 *
 * @author students
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    private final GuestLimitInterceptor guestLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 顺序很重要：先解析登录态写入 UserContext，再由限流器判断是否游客
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**");

        // 仅对「消耗浏览额度」的接口限流；
        // /api/product/mine 等需要登录的接口天然不会被游客访问到
        registry.addInterceptor(guestLimitInterceptor)
                .addPathPatterns(
                        "/api/product/list",
                        "/api/product/*",
                        "/api/comment/product/*"
                )
                .excludePathPatterns("/api/product/mine");
    }
}
