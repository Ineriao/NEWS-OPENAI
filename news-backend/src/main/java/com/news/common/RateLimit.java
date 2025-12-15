package com.news.common;

import java.lang.annotation.*;

/**
 * 速率限制注解
 * 用于限制接口的访问频率，防止恶意请求
 *
 * 使用示例：
 * @RateLimit(limit = 5, period = 60)  // 60秒内最多5次请求
 * @GetMapping("/api/example")
 * public Result example() { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 时间窗口内允许的最大请求次数
     * 默认：10次
     */
    int limit() default 10;

    /**
     * 时间窗口（秒）
     * 默认：60秒
     */
    int period() default 60;

    /**
     * 限制类型
     * IP: 按IP地址限制
     * USER: 按用户ID限制（需登录）
     * GLOBAL: 全局限制（所有请求共享配额）
     */
    LimitType type() default LimitType.IP;

    /**
     * 限制类型枚举
     */
    enum LimitType {
        IP,     // 按IP限制
        USER,   // 按用户限制
        GLOBAL  // 全局限制
    }
}
