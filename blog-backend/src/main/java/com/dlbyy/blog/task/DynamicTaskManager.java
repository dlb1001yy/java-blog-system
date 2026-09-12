package com.dlbyy.blog.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dlbyy.blog.common.exception.BusinessException;
import com.dlbyy.blog.entity.ScheduledTask;
import com.dlbyy.blog.mapper.SchemaMapper;
import com.dlbyy.blog.mapper.ScheduledTaskMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 动态定时任务管理器。
 * <p>
 * 通过自建 {@link ThreadPoolTaskScheduler} + Spring {@link CronExpression}
 * 实现基于数据库配置（sys_scheduled_task）的动态调度：
 * <ul>
 *     <li>启动时收集容器内全部 {@link ScheduledTaskSpi} 实现，幂等同步种子记录；</li>
 *     <li>支持运行时修改 cron（reschedule）、暂停（pause）、恢复（resume）、手动触发（triggerOnce）；</li>
 *     <li>执行结果（时间/状态/错误）回写数据库供后台展示；</li>
 *     <li>按 taskKey 做防重入：上一轮未跑完时本轮直接跳过。</li>
 * </ul>
 * 本类实现 {@link CommandLineRunner}，run() 开头自行幂等建表后再同步种子，
 * 避免与 {@code DataInitializer} 的执行顺序耦合。
 */
@Slf4j
@Component
public class DynamicTaskManager implements CommandLineRunner {

    /** 任务状态：启用 */
    private static final int STATUS_ENABLED = 1;
    /** 任务状态：暂停 */
    private static final int STATUS_DISABLED = 0;
    /** 错误信息回写数据库时的最大长度（超出截断） */
    private static final int ERROR_MAX_LENGTH = 1000;

    private final List<ScheduledTaskSpi> taskSpis;
    private final ScheduledTaskMapper scheduledTaskMapper;
    private final SchemaMapper schemaMapper;

    /** 自建调度线程池（不占用 Spring 默认 @Scheduled 线程） */
    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

    /** taskKey -> 调度句柄 */
    private final Map<String, ScheduledFuture<?>> taskFutures = new ConcurrentHashMap<>();
    /** taskKey -> 执行中标记（防重入） */
    private final Map<String, AtomicBoolean> runningFlags = new ConcurrentHashMap<>();
    /** taskKey -> SPI 实现 */
    private final Map<String, ScheduledTaskSpi> spiMap = new ConcurrentHashMap<>();

    public DynamicTaskManager(List<ScheduledTaskSpi> taskSpis,
                              ScheduledTaskMapper scheduledTaskMapper,
                              SchemaMapper schemaMapper) {
        this.taskSpis = taskSpis;
        this.scheduledTaskMapper = scheduledTaskMapper;
        this.schemaMapper = schemaMapper;
    }

    @PostConstruct
    public void init() {
        // poolSize 2：任务量少且单任务可能耗时（如全库备份），避免线程过多
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("blog-task-");
        scheduler.initialize();
    }

    @Override
    public void run(String... args) {
        ensureTables();
        syncAndRegister();
    }

    /**
     * 幂等确保 sys_scheduled_task 表存在（不依赖 DataInitializer 的执行顺序）
     */
    private void ensureTables() {
        try {
            if (schemaMapper.countScheduledTaskTable() == 0) {
                schemaMapper.createScheduledTaskTable();
                log.info("[DynamicTaskManager] 已创建定时任务表 sys_scheduled_task");
            }
        } catch (Exception e) {
            log.warn("[DynamicTaskManager] 创建 sys_scheduled_task 表时出错（可忽略，下次启动重试）：{}", e.getMessage());
        }
    }

    /**
     * 同步种子记录并注册调度：
     * 1) SPI Bean 无对应记录则插入默认记录（status=1, cron=defaultCron()）；
     * 2) 表中 status=1 且存在对应 Bean 的注册调度；
     * 3) 表中有记录但无对应 Bean 的告警跳过。
     */
    private void syncAndRegister() {
        for (ScheduledTaskSpi spi : taskSpis) {
            spiMap.put(spi.taskKey(), spi);
            ScheduledTask record = getByTaskKey(spi.taskKey());
            if (record == null) {
                ScheduledTask seed = new ScheduledTask();
                seed.setTaskKey(spi.taskKey());
                seed.setTaskName(spi.taskName());
                seed.setDescription(spi.description());
                seed.setCronExpression(spi.defaultCron());
                seed.setStatus(STATUS_ENABLED);
                scheduledTaskMapper.insert(seed);
                log.info("[DynamicTaskManager] 已注册定时任务: {} [{}] cron={}", spi.taskKey(), spi.taskName(), spi.defaultCron());
                schedule(spi, spi.defaultCron());
            } else if (record.getStatus() != null && record.getStatus() == STATUS_ENABLED) {
                schedule(spi, record.getCronExpression());
            } else {
                log.info("[DynamicTaskManager] 定时任务 {} 处于暂停状态，跳过调度", spi.taskKey());
            }
        }
        // 表中存在但无对应 SPI Bean 的记录（如任务代码已下线），仅告警
        List<ScheduledTask> records = scheduledTaskMapper.selectList(null);
        for (ScheduledTask record : records) {
            if (!spiMap.containsKey(record.getTaskKey())) {
                log.warn("[DynamicTaskManager] 定时任务 '{}' 在数据库中存在但未找到对应实现 Bean，跳过调度", record.getTaskKey());
            }
        }
    }

    /**
     * 注册（或重新注册）一个任务的 cron 调度
     */
    private void schedule(ScheduledTaskSpi spi, String cron) {
        cancel(spi.taskKey());
        if (cron == null || cron.isEmpty()) {
            log.warn("[DynamicTaskManager] 定时任务 {} 的 cron 为空，跳过调度", spi.taskKey());
            return;
        }
        ScheduledFuture<?> future = scheduler.schedule(() -> runWithRecord(spi),
                triggerContext -> {
                    // 由 CronTrigger 语义计算下一次触发时间（Trigger 需返回 Instant）
                    ZonedDateTime next = CronExpression.parse(cron).next(ZonedDateTime.now());
                    return next == null ? null : next.toInstant();
                });
        taskFutures.put(spi.taskKey(), future);
    }

    /**
     * 取消一个任务的调度（不中断正在执行的那一轮）
     */
    private void cancel(String taskKey) {
        ScheduledFuture<?> future = taskFutures.remove(taskKey);
        if (future != null) {
            future.cancel(false);
        }
    }

    /**
     * 执行任务并回写执行结果。
     * <ul>
     *     <li>成功：回写 last_execute_time / status=success / 清空 error；</li>
     *     <li>失败：回写 failed 与错误摘要（截断），记录 error 日志，不向外抛出；</li>
     *     <li>同一 taskKey 防重入：正在执行时本轮直接跳过。</li>
     * </ul>
     */
    public void runWithRecord(ScheduledTaskSpi spi) {
        AtomicBoolean running = runningFlags.computeIfAbsent(spi.taskKey(), k -> new AtomicBoolean(false));
        if (!running.compareAndSet(false, true)) {
            log.warn("[DynamicTaskManager] 定时任务 {} 上一次执行尚未结束，跳过本次触发", spi.taskKey());
            return;
        }
        try {
            log.info("[DynamicTaskManager] 定时任务 {} [{}] 开始执行", spi.taskKey(), spi.taskName());
            spi.run();
            scheduledTaskMapper.update(null, new LambdaUpdateWrapper<ScheduledTask>()
                    .eq(ScheduledTask::getTaskKey, spi.taskKey())
                    .set(ScheduledTask::getLastExecuteTime, LocalDateTime.now())
                    .set(ScheduledTask::getLastExecuteStatus, "success")
                    .set(ScheduledTask::getLastExecuteError, ""));
            log.info("[DynamicTaskManager] 定时任务 {} 执行成功", spi.taskKey());
        } catch (Exception e) {
            log.error("[DynamicTaskManager] 定时任务 {} 执行失败", spi.taskKey(), e);
            scheduledTaskMapper.update(null, new LambdaUpdateWrapper<ScheduledTask>()
                    .eq(ScheduledTask::getTaskKey, spi.taskKey())
                    .set(ScheduledTask::getLastExecuteTime, LocalDateTime.now())
                    .set(ScheduledTask::getLastExecuteStatus, "failed")
                    .set(ScheduledTask::getLastExecuteError, briefError(e)));
        } finally {
            running.set(false);
        }
    }

    /**
     * 修改任务 cron 表达式并重新调度
     *
     * @param taskKey 任务标识
     * @param newCron 新 cron 表达式（Spring CronExpression 格式）
     */
    public void reschedule(String taskKey, String newCron) {
        ScheduledTaskSpi spi = requireSpi(taskKey);
        if (!CronExpression.isValidExpression(newCron)) {
            throw new BusinessException("cron 表达式非法: " + newCron);
        }
        scheduledTaskMapper.update(null, new LambdaUpdateWrapper<ScheduledTask>()
                .eq(ScheduledTask::getTaskKey, taskKey)
                .set(ScheduledTask::getCronExpression, newCron)
                .set(ScheduledTask::getStatus, STATUS_ENABLED));
        // 修改 cron 视为恢复启用，统一走注册逻辑
        schedule(spi, newCron);
        log.info("[DynamicTaskManager] 定时任务 {} 已调整为 cron={}", taskKey, newCron);
    }

    /**
     * 暂停任务（取消调度，数据库状态置 0）
     */
    public void pause(String taskKey) {
        requireSpi(taskKey);
        scheduledTaskMapper.update(null, new LambdaUpdateWrapper<ScheduledTask>()
                .eq(ScheduledTask::getTaskKey, taskKey)
                .set(ScheduledTask::getStatus, STATUS_DISABLED));
        cancel(taskKey);
        log.info("[DynamicTaskManager] 定时任务 {} 已暂停", taskKey);
    }

    /**
     * 恢复任务（按数据库中的 cron 重新注册调度）
     */
    public void resume(String taskKey) {
        ScheduledTaskSpi spi = requireSpi(taskKey);
        ScheduledTask record = getByTaskKey(taskKey);
        if (record == null || record.getCronExpression() == null) {
            throw new BusinessException("任务记录缺失或 cron 为空，无法恢复");
        }
        scheduledTaskMapper.update(null, new LambdaUpdateWrapper<ScheduledTask>()
                .eq(ScheduledTask::getTaskKey, taskKey)
                .set(ScheduledTask::getStatus, STATUS_ENABLED));
        schedule(spi, record.getCronExpression());
        log.info("[DynamicTaskManager] 定时任务 {} 已恢复，cron={}", taskKey, record.getCronExpression());
    }

    /**
     * 手动触发一次任务执行（异步，立即返回）
     */
    public void triggerOnce(String taskKey) {
        ScheduledTaskSpi spi = requireSpi(taskKey);
        scheduler.execute(() -> runWithRecord(spi));
        log.info("[DynamicTaskManager] 定时任务 {} 已手动触发", taskKey);
    }

    /**
     * 计算任务下一次触发时间（列表展示用）
     *
     * @return 下一次触发时间；任务未启用或 cron 非法时返回 null
     */
    public LocalDateTime nextTriggerTime(String taskKey) {
        ScheduledTask record = getByTaskKey(taskKey);
        if (record == null || record.getStatus() == null || record.getStatus() != STATUS_ENABLED
                || record.getCronExpression() == null || !CronExpression.isValidExpression(record.getCronExpression())) {
            return null;
        }
        ZonedDateTime next = CronExpression.parse(record.getCronExpression()).next(ZonedDateTime.now());
        return next == null ? null : next.toLocalDateTime();
    }

    /**
     * 按 taskKey 查询任务记录
     */
    public ScheduledTask getByTaskKey(String taskKey) {
        return scheduledTaskMapper.selectOne(new LambdaQueryWrapper<ScheduledTask>()
                .eq(ScheduledTask::getTaskKey, taskKey));
    }

    /**
     * 获取 SPI 实现，不存在则抛业务异常
     */
    private ScheduledTaskSpi requireSpi(String taskKey) {
        ScheduledTaskSpi spi = spiMap.get(taskKey);
        if (spi == null) {
            throw new BusinessException("未找到任务实现: " + taskKey);
        }
        return spi;
    }

    /**
     * 生成错误摘要（类名 + 消息 + 有限堆栈，截断到 {@link #ERROR_MAX_LENGTH} 字符）
     */
    private String briefError(Exception e) {
        StringBuilder sb = new StringBuilder(e.getClass().getName());
        if (e.getMessage() != null) {
            sb.append(": ").append(e.getMessage());
        }
        StackTraceElement[] stack = e.getStackTrace();
        for (int i = 0; i < Math.min(stack.length, 5); i++) {
            sb.append("\n    at ").append(stack[i]);
        }
        String text = sb.toString();
        return text.length() > ERROR_MAX_LENGTH ? text.substring(0, ERROR_MAX_LENGTH) : text;
    }

    @PreDestroy
    public void destroy() {
        // 停机时取消全部调度并关闭线程池，避免任务悬挂
        taskFutures.values().forEach(f -> f.cancel(false));
        taskFutures.clear();
        scheduler.shutdown();
    }
}
