package com.dlbyy.blog.service;

import com.dlbyy.blog.properties.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

/**
 * 登录安全防护服务：
 * 1) 单 IP 维度滑动窗口限流（每分钟最多 N 次登录尝试）
 * 2) 单用户名维度连续失败计数与账户锁定
 * 3) 达到阈值触发告警通知
 * <p>
 * 所有状态基于 Redis，避免高频写库；冷数据通过 TTL 自动清理。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final String IP_KEY_PREFIX = "login_ip:";
    private static final String FAIL_KEY_PREFIX = "login_fail:";
    private static final String LOCK_KEY_PREFIX = "login_lock:";

    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> rateLimitScript;
    private final SecurityProperties securityProperties;
    private final AlertNotifier alertNotifier;

    /**
     * IP 维度登录限流判断。返回 true 表示允许登录，false 表示被限流。
     */
    public boolean tryAcquireIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            ip = "unknown";
        }
        long now = System.currentTimeMillis();
        long windowMillis = (long) securityProperties.getIpWindowSeconds() * 1000L;
        long uniqueId = now + UUID.randomUUID().hashCode();
        Long allowed = stringRedisTemplate.execute(
                rateLimitScript,
                Collections.singletonList(IP_KEY_PREFIX + ip),
                String.valueOf(now),
                String.valueOf(windowMillis),
                String.valueOf(securityProperties.getIpLimitCount()),
                String.valueOf(uniqueId)
        );
        boolean ok = allowed != null && allowed == 1L;
        if (!ok) {
            alertNotifier.notifyIpRateLimited(ip);
        }
        return ok;
    }

    /**
     * 判断用户名是否处于锁定状态。若锁定已过期则自动清除锁定标记。
     */
    public boolean isLocked(String username) {
        String lockKey = LOCK_KEY_PREFIX + username;
        Object expireAt = stringRedisTemplate.opsForValue().get(lockKey);
        if (expireAt == null) {
            return false;
        }
        long expire = Long.parseLong(expireAt.toString());
        if (System.currentTimeMillis() < expire) {
            return true;
        }
        // 已过期，清理
        stringRedisTemplate.delete(lockKey);
        return false;
    }

    /**
     * 获取剩余锁定毫秒数（未锁定返回 0）。
     */
    public long getRemainingLockMillis(String username) {
        Object expireAt = stringRedisTemplate.opsForValue().get(LOCK_KEY_PREFIX + username);
        if (expireAt == null) {
            return 0L;
        }
        long expire = Long.parseLong(expireAt.toString());
        return Math.max(0L, expire - System.currentTimeMillis());
    }

    /**
     * 记录一次登录失败，累加失败计数；达到阈值则锁定账户并触发告警。
     *
     * @return 锁定后的剩余毫秒数（>0 表示本次刚好被锁定），未达阈值返回 0
     */
    public long onLoginFailure(String username) {
        String failKey = FAIL_KEY_PREFIX + username;
        Long count = stringRedisTemplate.opsForValue().increment(failKey);
        if (count == null) {
            count = 1L;
        }
        // 失败计数 Key 设置较长 TTL（避免无限堆积），窗口参考锁定时长
        if (count == 1L) {
            stringRedisTemplate.expire(failKey, (long) securityProperties.getLockMinutes() * 60 + 60, java.util.concurrent.TimeUnit.SECONDS);
        }
        int threshold = securityProperties.getUserFailThreshold();
        if (count >= threshold) {
            long lockMillis = (long) securityProperties.getLockMinutes() * 60 * 1000L;
            long expireAt = System.currentTimeMillis() + lockMillis;
            stringRedisTemplate.opsForValue().set(LOCK_KEY_PREFIX + username, String.valueOf(expireAt), lockMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
            alertNotifier.notifyAccountLocked(username, count.intValue(), securityProperties.getLockMinutes());
            return lockMillis;
        }
        return 0L;
    }

    /**
     * 登录成功后清除失败计数与锁定状态。
     */
    public void onLoginSuccess(String username) {
        stringRedisTemplate.delete(FAIL_KEY_PREFIX + username);
        stringRedisTemplate.delete(LOCK_KEY_PREFIX + username);
    }
}
