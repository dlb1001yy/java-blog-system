package com.dlbyy.blog.service;

import com.dlbyy.blog.properties.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 告警通知接口。默认实现为日志告警，预留扩展点（邮件 / 短信 / Webhook）。
 * 当账户因连续登录失败达到阈值被锁定时触发。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertNotifier {

    private final SecurityProperties securityProperties;

    /**
     * 账户锁定告警
     *
     * @param username  被锁定的用户名
     * @param failCount 失败次数
     * @param lockMinutes 锁定分钟数
     */
    public void notifyAccountLocked(String username, int failCount, int lockMinutes) {
        if (!securityProperties.isAlertEnabled()) {
            return;
        }
        // 默认：记录告警日志。生产环境可在此接入邮件 / 企业微信 / 钉钉 / Webhook。
        log.warn("[安全告警] 账户 '{}' 因连续登录失败 {} 次已被锁定 {} 分钟，请关注是否存在暴力破解风险。",
                username, failCount, lockMinutes);
    }

    /**
     * IP 级登录限流触发告警
     *
     * @param ip 客户端 IP
     */
    public void notifyIpRateLimited(String ip) {
        if (!securityProperties.isAlertEnabled()) {
            return;
        }
        log.warn("[安全告警] IP '{}' 触发登录限流阈值，可能存在暴力破解风险。", ip);
    }
}
