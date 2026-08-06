package com.dlbyy.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * 限流相关 Bean 配置
 * <p>
 * 将 classpath:lua/rate_limit.lua 加载为 {@link DefaultRedisScript}，
 * 供 {@code RateLimitAspect} 通过 RedisTemplate#execute 原子执行。
 */
@Configuration
public class RateLimitConfig {

    @Bean
    public DefaultRedisScript<Long> rateLimitScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/rate_limit.lua")));
        script.setResultType(Long.class);
        return script;
    }
}
