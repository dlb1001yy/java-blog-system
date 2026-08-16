package com.dlbyy.blog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * API 文档配置（Knife4j / SpringDoc OpenAPI）
 * 通过 swagger.enabled 开关控制：本地开发默认开启，生产环境通过 SWAGGER_ENABLED=false 关闭。
 */
@Configuration
@ConditionalOnProperty(name = "swagger.enabled", havingValue = "true", matchIfMissing = true)
public class SwaggerConfig {
    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Java博客系统 API")
                        .description("Java程序员博客系统前后端接口文档")
                        .version("v1.0"));
    }
}