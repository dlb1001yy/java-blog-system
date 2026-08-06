package com.dlbyy.blog.common.exception;

/**
 * 限流异常
 * <p>
 * 当接口请求超过 {@code @RateLimit} 配置的阈值时抛出。
 * 由 {@code GlobalExceptionHandler} 统一捕获并返回 429 状态码。
 */
public class RateLimitException extends RuntimeException {

    public RateLimitException(String message) {
        super(message);
    }
}
