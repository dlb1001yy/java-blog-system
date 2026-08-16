package com.dlbyy.blog.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CORS 跨域来源白名单配置
 * 对应 application.yaml 中的 security.cors 配置块，支持动态调整而无需改代码。
 */
@Data
@Component
@ConfigurationProperties(prefix = "security.cors")
public class CorsProperties {

    /**
     * 允许携带凭证的跨域来源列表（逗号分隔）。
     * 默认 * 仅用于本地开发（按 Origin 模式匹配）；
     * 生产环境必须通过 CORS_ALLOWED_ORIGINS 配置为精确域名，如 https://yourdomain.com。
     */
    private List<String> allowedOrigins = List.of("*");
}
