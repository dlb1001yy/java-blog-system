package com.dlbyy.blog.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解
 * <p>
 * 配合 {@code RateLimitAspect} 切面，基于 Redis + Lua 滑动窗口算法实现。
 * 标注于 Controller 方法上，超限将抛出 {@code RateLimitException}。
 *
 * <pre>
 * 示例：60 秒内同一 IP 仅允许调用 5 次
 * {@code @RateLimit(key = "login", time = 60, count = 5)}
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流业务 key 标识。
     * <p>
     * 最终 Redis Key = rate_limit:{key}:{method}:{clientIp}；
     * 若留空，则使用 {@code 类名#方法名} 作为 key。
     */
    String key() default "";

    /**
     * 时间窗口（秒），默认 60 秒
     */
    int time() default 60;

    /**
     * 时间窗口内允许的最大请求次数，默认 100 次
     */
    int count() default 100;
}
