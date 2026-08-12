package com.dlbyy.blog.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 安全相关配置（限流 / 账户锁定 / 密码加密 / Cookie）
 * 对应 application.yaml 中的 security.login 配置块，支持动态调整而无需改代码。
 */
@Data
@Component
@ConfigurationProperties(prefix = "security.login")
public class SecurityProperties {

    /** 单个 IP 在窗口期内的登录最大尝试次数 */
    private int ipLimitCount = 5;

    /** IP 限流窗口（秒） */
    private int ipWindowSeconds = 60;

    /** 同一用户名连续失败达到该次数后锁定账户 */
    private int userFailThreshold = 5;

    /** 账户锁定时长（分钟） */
    private int lockMinutes = 15;

    /** 是否开启锁定告警通知 */
    private boolean alertEnabled = true;

    /** BCrypt 加密强度（盐值轮数） */
    private int bcryptStrength = 12;

    /** 访问令牌有效期（分钟） */
    private int accessTokenMinutes = 15;

    /** 刷新令牌有效期（天） */
    private int refreshTokenDays = 7;

    /** refresh Cookie 是否带 Secure 属性（生产 true；本地 HTTP 联调置 false） */
    private boolean cookieSecure = true;

    /** refresh Cookie 是否带 SameSite=None（跨站携带） */
    private String cookieSameSite = "None";
}
