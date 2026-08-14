package com.dlbyy.blog.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.dlbyy.blog.properties.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 图形验证码服务：
 * 1) 生成图形验证码，答案存入 Redis（60 秒有效期），返回 captchaId + Base64 图片
 * 2) 登录前校验验证码（一次性消费，读后即删，防止重放）
 * <p>
 * 可通过 security.login.captcha-enabled 配置开关，关闭时校验直接放行。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaService {

    /** Redis Key 前缀 */
    private static final String KEY_PREFIX = "captcha:";

    /** 验证码有效期（秒） */
    private static final long EXPIRE_SECONDS = 60;

    private final StringRedisTemplate stringRedisTemplate;
    private final SecurityProperties securityProperties;

    /**
     * 生成图形验证码。
     *
     * @return captchaId → 验证码唯一标识（随登录请求回传）；image → 完整 data URI（可直接用于 &lt;img src&gt;）
     */
    public Map<String, String> generate() {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(130, 40, 4, 8);
        String captchaId = UUID.randomUUID().toString();
        stringRedisTemplate.opsForValue().set(KEY_PREFIX + captchaId, captcha.getCode(), EXPIRE_SECONDS, TimeUnit.SECONDS);

        Map<String, String> result = new HashMap<>();
        result.put("captchaId", captchaId);
        result.put("image", captcha.getImageBase64Data());
        return result;
    }

    /**
     * 校验图形验证码（忽略大小写）。一次性消费：读取后立即删除，防止同一验证码重放。
     *
     * @return true 表示校验通过（或验证码功能已关闭）
     */
    public boolean verify(String captchaId, String code) {
        // 功能关闭时不触 Redis，直接放行
        if (!securityProperties.isCaptchaEnabled()) {
            return true;
        }
        if (captchaId == null || captchaId.isBlank() || code == null || code.isBlank()) {
            return false;
        }
        String stored = stringRedisTemplate.opsForValue().getAndDelete(KEY_PREFIX + captchaId);
        return stored != null && stored.equalsIgnoreCase(code.trim());
    }
}
