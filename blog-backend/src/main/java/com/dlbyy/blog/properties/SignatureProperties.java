package com.dlbyy.blog.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 请求签名校验配置（签名开关 / 密钥 / 时间窗口）
 * 对应 application.yaml 中的 security.signing 配置块，支持动态调整而无需改代码。
 */
@Data
@Component
@ConfigurationProperties(prefix = "security.signing")
public class SignatureProperties {

    /** 是否开启请求签名校验 */
    private boolean enabled = true;

    /** HMAC 签名密钥 */
    private String secret = "BlogApiSigningSecret2024!";

    /** 时间戳允许的最大误差（秒） */
    private int timestampWindowSeconds = 60;
}
