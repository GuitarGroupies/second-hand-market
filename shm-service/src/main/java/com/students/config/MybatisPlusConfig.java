package com.students.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 插件配置。
 *
 * @author students
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 注册分页插件与防全表更新插件。
     *
     * <p><b>为什么必须有：</b>没有 PaginationInnerInterceptor 时，selectPage 不会生成
     * LIMIT 子句，而是把整张表查出来——数据量小时不易发现，数据量大时接口直接超时。</p>
     *
     * <p><b>依赖前提：</b>该拦截器依赖 mybatis-plus-jsqlparser-4.9（MyBatis-Plus 3.5.18
     * 中标记为 optional，不自动传递），需在 pom 中显式引入。</p>
     *
     * <p><b>顺序要求：</b>多个内部拦截器时，分页插件必须最后添加。</p>
     *
     * @return MyBatis-Plus 拦截器链
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 防止误写 update/delete 不带 where 条件，导致全表更新
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        // 分页插件必须最后添加
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
