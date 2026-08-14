package com.dlbyy.blog.service;

import com.dlbyy.blog.properties.SecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link LoginAttemptService} 单元测试。
 * 使用 Mockito 模拟 Redis 相关协作者，SecurityProperties 使用真实对象手动设值。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginAttemptService 登录安全防护服务测试")
class LoginAttemptServiceTest {

    private static final String USERNAME = "user";
    private static final String FAIL_KEY = "login_fail:user";
    private static final String LOCK_KEY = "login_lock:user";
    /** 锁定时长：15 分钟 = 900000 毫秒 */
    private static final long LOCK_MILLIS = 15 * 60 * 1000L;
    /** 失败计数 Key 的 TTL：锁定分钟数 * 60 + 60 = 960 秒 */
    private static final long FAIL_TTL_SECONDS = 15 * 60 + 60L;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private DefaultRedisScript<Long> rateLimitScript;

    @Mock
    private AlertNotifier alertNotifier;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Captor
    private ArgumentCaptor<List<String>> keysCaptor;

    private SecurityProperties securityProperties;

    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        // 真实配置对象，手动设置测试参数
        securityProperties = new SecurityProperties();
        securityProperties.setUserFailThreshold(3);
        securityProperties.setLockMinutes(15);
        securityProperties.setIpLimitCount(5);
        securityProperties.setIpWindowSeconds(60);
        // 按构造参数顺序手动注入：(StringRedisTemplate, DefaultRedisScript, SecurityProperties, AlertNotifier)
        loginAttemptService = new LoginAttemptService(
                stringRedisTemplate, rateLimitScript, securityProperties, alertNotifier);
    }

    /** 将 opsForValue() 打桩为返回模拟的 ValueOperations */
    private void mockValueOperations() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("tryAcquireIp - IP 维度限流")
    class TryAcquireIp {

        @Test
        @DisplayName("Lua 脚本返回 1L 时允许登录且不触发告警")
        void shouldAllowWhenScriptReturnsOne() {
            when(stringRedisTemplate.execute(eq(rateLimitScript), anyList(), any(), any(), any(), any()))
                    .thenReturn(1L);

            boolean result = loginAttemptService.tryAcquireIp("1.2.3.4");

            assertThat(result).isTrue();
            verify(alertNotifier, never()).notifyIpRateLimited(anyString());
        }

        @Test
        @DisplayName("Lua 脚本返回 0L 时拒绝登录并触发 IP 限流告警")
        void shouldDenyAndNotifyWhenScriptReturnsZero() {
            when(stringRedisTemplate.execute(eq(rateLimitScript), anyList(), any(), any(), any(), any()))
                    .thenReturn(0L);

            boolean result = loginAttemptService.tryAcquireIp("1.2.3.4");

            assertThat(result).isFalse();
            verify(alertNotifier).notifyIpRateLimited("1.2.3.4");
        }

        @Test
        @DisplayName("Lua 脚本返回 null 时拒绝登录并触发 IP 限流告警")
        void shouldDenyWhenScriptReturnsNull() {
            when(stringRedisTemplate.execute(eq(rateLimitScript), anyList(), any(), any(), any(), any()))
                    .thenReturn(null);

            boolean result = loginAttemptService.tryAcquireIp("1.2.3.4");

            assertThat(result).isFalse();
            verify(alertNotifier).notifyIpRateLimited("1.2.3.4");
        }

        @Test
        @DisplayName("ip 为 null 时使用 unknown 作为限流 Key")
        void shouldUseUnknownKeyWhenIpIsNull() {
            when(stringRedisTemplate.execute(eq(rateLimitScript), anyList(), any(), any(), any(), any()))
                    .thenReturn(1L);

            boolean result = loginAttemptService.tryAcquireIp(null);

            assertThat(result).isTrue();
            verify(stringRedisTemplate).execute(eq(rateLimitScript), keysCaptor.capture(),
                    any(), any(), any(), any());
            assertThat(keysCaptor.getValue()).containsExactly("login_ip:unknown");
        }

        @Test
        @DisplayName("ip 为空字符串时使用 unknown 作为限流 Key")
        void shouldUseUnknownKeyWhenIpIsEmpty() {
            when(stringRedisTemplate.execute(eq(rateLimitScript), anyList(), any(), any(), any(), any()))
                    .thenReturn(1L);

            boolean result = loginAttemptService.tryAcquireIp("");

            assertThat(result).isTrue();
            verify(stringRedisTemplate).execute(eq(rateLimitScript), keysCaptor.capture(),
                    any(), any(), any(), any());
            assertThat(keysCaptor.getValue()).containsExactly("login_ip:unknown");
        }
    }

    @Nested
    @DisplayName("isLocked - 账户锁定状态判断")
    class IsLocked {

        @Test
        @DisplayName("锁定 Key 不存在时返回 false 且不删除 Key")
        void shouldReturnFalseWhenKeyAbsent() {
            mockValueOperations();

            boolean result = loginAttemptService.isLocked(USERNAME);

            assertThat(result).isFalse();
            verify(stringRedisTemplate, never()).delete(anyString());
        }

        @Test
        @DisplayName("锁定过期时间为未来时间戳时返回 true 且不删除 Key")
        void shouldReturnTrueWhenNotExpired() {
            mockValueOperations();
            when(valueOperations.get(LOCK_KEY))
                    .thenReturn(String.valueOf(System.currentTimeMillis() + 300_000L));

            boolean result = loginAttemptService.isLocked(USERNAME);

            assertThat(result).isTrue();
            verify(stringRedisTemplate, never()).delete(anyString());
        }

        @Test
        @DisplayName("锁定过期时间为过去时间戳时返回 false 并自动清理 Key")
        void shouldReturnFalseAndDeleteWhenExpired() {
            mockValueOperations();
            when(valueOperations.get(LOCK_KEY))
                    .thenReturn(String.valueOf(System.currentTimeMillis() - 1_000L));

            boolean result = loginAttemptService.isLocked(USERNAME);

            assertThat(result).isFalse();
            verify(stringRedisTemplate).delete(LOCK_KEY);
        }
    }

    @Nested
    @DisplayName("getRemainingLockMillis - 剩余锁定时长")
    class GetRemainingLockMillis {

        @Test
        @DisplayName("锁定 Key 不存在时返回 0")
        void shouldReturnZeroWhenKeyAbsent() {
            mockValueOperations();

            long result = loginAttemptService.getRemainingLockMillis(USERNAME);

            assertThat(result).isZero();
        }

        @Test
        @DisplayName("未过期的未来时间戳返回大于 0 的剩余毫秒数")
        void shouldReturnPositiveRemainingWhenLocked() {
            mockValueOperations();
            when(valueOperations.get(LOCK_KEY))
                    .thenReturn(String.valueOf(System.currentTimeMillis() + 300_000L));

            long result = loginAttemptService.getRemainingLockMillis(USERNAME);

            assertThat(result).isBetween(5_000L, 600_000L);
        }

        @Test
        @DisplayName("已过期的时间戳返回 0")
        void shouldReturnZeroWhenExpired() {
            mockValueOperations();
            when(valueOperations.get(LOCK_KEY))
                    .thenReturn(String.valueOf(System.currentTimeMillis() - 1_000L));

            long result = loginAttemptService.getRemainingLockMillis(USERNAME);

            assertThat(result).isZero();
        }
    }

    @Nested
    @DisplayName("onLoginFailure - 登录失败计数与锁定")
    class OnLoginFailure {

        @Test
        @DisplayName("失败次数为 2（未达阈值 3）时不锁定、不告警、不设置 TTL")
        void shouldNotLockWhenBelowThreshold() {
            mockValueOperations();
            when(valueOperations.increment(FAIL_KEY)).thenReturn(2L);

            long result = loginAttemptService.onLoginFailure(USERNAME);

            assertThat(result).isZero();
            verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
            verify(alertNotifier, never()).notifyAccountLocked(anyString(), anyInt(), anyInt());
            verify(stringRedisTemplate, never()).expire(anyString(), anyLong(), any(TimeUnit.class));
        }

        @Test
        @DisplayName("失败次数为 1（首次失败）时为失败计数 Key 设置 TTL")
        void shouldExpireFailKeyOnFirstFailure() {
            mockValueOperations();
            when(valueOperations.increment(FAIL_KEY)).thenReturn(1L);

            long result = loginAttemptService.onLoginFailure(USERNAME);

            assertThat(result).isZero();
            verify(stringRedisTemplate).expire(FAIL_KEY, FAIL_TTL_SECONDS, TimeUnit.SECONDS);
        }

        @Test
        @DisplayName("失败次数达到阈值 3 时写入锁定 Key 并触发告警")
        void shouldLockAndNotifyWhenReachThreshold() {
            mockValueOperations();
            when(valueOperations.increment(FAIL_KEY)).thenReturn(3L);

            long result = loginAttemptService.onLoginFailure(USERNAME);

            assertThat(result).isEqualTo(LOCK_MILLIS);
            verify(valueOperations).set(eq(LOCK_KEY), anyString(), eq(LOCK_MILLIS), eq(TimeUnit.MILLISECONDS));
            verify(alertNotifier).notifyAccountLocked(USERNAME, 3, 15);
            // 非首次失败不应再刷新失败计数 TTL
            verify(stringRedisTemplate, never()).expire(anyString(), anyLong(), any(TimeUnit.class));
        }

        @Test
        @DisplayName("increment 返回 null 时按 1 次失败处理（设置 TTL，未达阈值返回 0）")
        void shouldTreatNullIncrementAsOne() {
            mockValueOperations();
            when(valueOperations.increment(FAIL_KEY)).thenReturn(null);

            long result = loginAttemptService.onLoginFailure(USERNAME);

            assertThat(result).isZero();
            verify(stringRedisTemplate).expire(FAIL_KEY, FAIL_TTL_SECONDS, TimeUnit.SECONDS);
        }
    }

    @Nested
    @DisplayName("onLoginSuccess - 登录成功清理")
    class OnLoginSuccess {

        @Test
        @DisplayName("登录成功后删除失败计数 Key 与锁定 Key")
        void shouldDeleteFailAndLockKeys() {
            loginAttemptService.onLoginSuccess(USERNAME);

            verify(stringRedisTemplate).delete(FAIL_KEY);
            verify(stringRedisTemplate).delete(LOCK_KEY);
        }
    }
}
