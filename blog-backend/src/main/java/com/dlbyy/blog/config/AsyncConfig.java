package com.dlbyy.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 * <p>
 * 启用 {@link EnableAsync}，并为 ES 同步监听器提供专用线程池 {@code esSyncExecutor}，
 * 避免使用默认 SimpleAsyncTaskExecutor 导致无限创建线程。
 * <p>
 * 拒绝策略采用 CallerRunsPolicy：队列满时由调用线程执行，起到天然限流作用。
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("esSyncExecutor")
    public Executor esSyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(500);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("es-sync-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 操作日志异步落库专用线程池：队列容量放大（1000）以承受写操作高峰，
     * 队列满时由调用线程兜底执行，避免日志丢失影响主流程。
     */
    @Bean("opLogExecutor")
    public Executor opLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(1000);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("op-log-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
