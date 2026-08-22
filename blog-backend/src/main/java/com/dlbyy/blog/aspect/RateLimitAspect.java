package com.dlbyy.blog.aspect;

import com.dlbyy.blog.annotation.RateLimit;
import com.dlbyy.blog.common.exception.RateLimitException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.UUID;

/**
 * 接口限流切面
 * <p>
 * 拦截所有标注 {@link RateLimit} 的方法，基于 Redis + Lua 滑动窗口算法
 * 进行原子性限流判定。Redis Key 维度：业务 key + 方法签名 + 客户端 IP。
 * <p>
 * 使用 {@link StringRedisTemplate} 执行 Lua 脚本，避免 JSON 序列化器
 * 对 ARGV 参数加引号导致 tonumber 解析失败。
 * <p>
 * 超限时抛出 {@link RateLimitException}，由全局异常处理器返回 429。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> rateLimitScript;

    @Around("@annotation(com.dlbyy.blog.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        String key = buildKey(rateLimit.key(), method);

        long now = System.currentTimeMillis();
        long windowMillis = rateLimit.time() * 1000L;
        String uniqueId = now + "-" + UUID.randomUUID();

        Long allowed = stringRedisTemplate.execute(
                rateLimitScript,
                Collections.singletonList(key),
                String.valueOf(now),
                String.valueOf(windowMillis),
                String.valueOf(rateLimit.count()),
                uniqueId
        );

        if (allowed == null || allowed == 0L) {
            log.warn("接口限流触发 | key={} | method={} | limit={}/{}s", key, method.getName(), rateLimit.count(), rateLimit.time());
            throw new RateLimitException("请求过于频繁，请稍后再试");
        }

        return joinPoint.proceed();
    }

    /**
     * 构造限流 Redis Key：rate_limit:{bizKey}:{methodSignature}:{clientIp}
     */
    private String buildKey(String annotationKey, Method method) {
        String bizKey = annotationKey.isEmpty()
                ? method.getDeclaringClass().getSimpleName() + ":" + method.getName()
                : annotationKey;
        String ip = getClientIp();
        return "rate_limit:" + bizKey + ":" + (ip == null ? "unknown" : ip);
    }

    /**
     * 获取客户端真实 IP（兼容多级反向代理场景）
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return null;
            }
            HttpServletRequest request = attrs.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        } catch (Exception e) {
            log.warn("获取客户端 IP 失败", e);
            return null;
        }
    }
}
