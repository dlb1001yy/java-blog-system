package com.dlbyy.blog.service;

import com.dlbyy.blog.properties.SecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link CaptchaService} 单元测试。
 * 使用 Mockito 模拟 Redis 相关协作者，SecurityProperties 使用真实对象手动设值；
 * Hutool 生成的验证码内容随机，因此仅断言结构性内容（Key 前缀 / 长度 / Redis 调用），不断言具体字符。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CaptchaService 图形验证码服务测试")
class CaptchaServiceTest {

    private static final String CAPTCHA_ID = "abc";
    private static final String CAPTCHA_KEY = "captcha:abc";
    /** 验证码有效期（秒） */
    private static final long EXPIRE_SECONDS = 60;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private SecurityProperties securityProperties;

    private CaptchaService captchaService;

    @BeforeEach
    void setUp() {
        // 真实配置对象，图形验证码开关默认开启（captchaEnabled=true）
        securityProperties = new SecurityProperties();
        // 按构造参数顺序手动注入：(StringRedisTemplate, SecurityProperties)
        captchaService = new CaptchaService(stringRedisTemplate, securityProperties);
    }

    /** 将 opsForValue() 打桩为返回模拟的 ValueOperations */
    private void mockValueOperations() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("generate - 生成图形验证码")
    class Generate {

        @Test
        @DisplayName("返回非空 captchaId 与 data URI 图片，并将答案写入 Redis（captcha: 前缀 + 60 秒 TTL）")
        void shouldReturnCaptchaAndStoreAnswerInRedis() {
            mockValueOperations();

            Map<String, String> result = captchaService.generate();

            String captchaId = result.get("captchaId");
            // captchaId 为随登录请求回传的唯一标识，断言非空即可
            assertThat(captchaId).isNotBlank();
            // image 为完整 data URI，可直接用于 <img src>
            assertThat(result.get("image")).startsWith("data:image/png;base64,");

            // Redis 写入：Key 为 captcha:{captchaId}，值为答案，TTL 60 秒
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            verify(valueOperations).set(keyCaptor.capture(), codeCaptor.capture(),
                    eq(EXPIRE_SECONDS), eq(TimeUnit.SECONDS));
            assertThat(keyCaptor.getValue()).isEqualTo("captcha:" + captchaId);
            // 验证码内容随机，仅断言非空且长度为 4（与 createLineCaptcha 的 codeCount 一致）
            assertThat(codeCaptor.getValue()).isNotBlank().hasSize(4);
        }
    }

    @Nested
    @DisplayName("verify - 校验图形验证码")
    class Verify {

        @Test
        @DisplayName("验证码匹配时返回 true")
        void shouldReturnTrueWhenCodeMatches() {
            mockValueOperations();
            when(valueOperations.getAndDelete(CAPTCHA_KEY)).thenReturn("Xy9Z");

            boolean result = captchaService.verify(CAPTCHA_ID, "Xy9Z");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("验证码大小写不敏感，且对输入做前后空格 trim 处理")
        void shouldBeCaseInsensitiveAndTrimInput() {
            mockValueOperations();
            when(valueOperations.getAndDelete(CAPTCHA_KEY)).thenReturn("Xy9Z");

            assertThat(captchaService.verify(CAPTCHA_ID, "xy9z")).isTrue();
            assertThat(captchaService.verify(CAPTCHA_ID, " Xy9Z ")).isTrue();
        }

        @Test
        @DisplayName("验证码不匹配时返回 false")
        void shouldReturnFalseWhenCodeMismatch() {
            mockValueOperations();
            when(valueOperations.getAndDelete(CAPTCHA_KEY)).thenReturn("abcd");

            boolean result = captchaService.verify(CAPTCHA_ID, "wxyz");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Key 不存在（已过期或已被消费）时返回 false")
        void shouldReturnFalseWhenKeyAbsent() {
            mockValueOperations();
            when(valueOperations.getAndDelete(CAPTCHA_KEY)).thenReturn(null);

            boolean result = captchaService.verify(CAPTCHA_ID, "Xy9Z");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("captchaId 或 code 为 null/空白时返回 false 且不触 Redis")
        void shouldReturnFalseWithoutRedisWhenParamsBlank() {
            assertThat(captchaService.verify(null, "Xy9Z")).isFalse();
            assertThat(captchaService.verify("  ", "Xy9Z")).isFalse();
            assertThat(captchaService.verify(CAPTCHA_ID, null)).isFalse();
            assertThat(captchaService.verify(CAPTCHA_ID, "  ")).isFalse();

            // 参数非法时不应访问 Redis
            verifyNoInteractions(stringRedisTemplate, valueOperations);
        }

        @Test
        @DisplayName("开关关闭时任意入参（含 null）直接放行且不触 Redis")
        void shouldPassWithoutRedisWhenDisabled() {
            securityProperties.setCaptchaEnabled(false);

            assertThat(captchaService.verify(null, null)).isTrue();
            assertThat(captchaService.verify(CAPTCHA_ID, "whatever")).isTrue();

            // 功能关闭时不访问 Redis
            verifyNoInteractions(stringRedisTemplate, valueOperations);
        }

        @Test
        @DisplayName("一次性消费：getAndDelete 读后即删，首次校验成功后再次校验返回 false")
        void shouldConsumeCaptchaOnFirstVerifyOnly() {
            mockValueOperations();
            // 第一次读取返回答案，第二次读取（Key 已被删除）返回 null
            when(valueOperations.getAndDelete(CAPTCHA_KEY))
                    .thenReturn("abcd")
                    .thenReturn(null);

            assertThat(captchaService.verify(CAPTCHA_ID, "abcd")).isTrue();
            assertThat(captchaService.verify(CAPTCHA_ID, "abcd")).isFalse();
        }
    }
}
