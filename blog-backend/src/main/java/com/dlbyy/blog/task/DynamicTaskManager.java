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
import org.springframework.dao.DuplicateKeyException;
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
import java.util.regex.Pattern;

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
    /** 任务标识格式：字母开头的字母/数字/中划线（与前端校验一致） */
    private static final Pattern TASK_KEY_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9-]*$");

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
     * 2) 表中 status=1 且能解析到 Bean 的记录注册调度（支持 taskKey 形如 "{spiKey}-后缀" 的多实例）；
     * 3) 表中有记录但无法解析到 Bean 的告警跳过。
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
            }
        }
        // 全表扫描注册调度（含多实例记录，如 databaseBackup-daily）
        List<ScheduledTask> records = scheduledTaskMapper.selectList(null);
        for (ScheduledTask record : records) {
            if (resolveSpi(record.getTaskKey()) == null) {
                log.warn("[DynamicTaskManager] 定时任务 '{}' 在数据库中存在但未找到对应实现 Bean，跳过调度", record.getTaskKey());
                continue;
            }
            if (record.getStatus() != null && record.getStatus() == STATUS_ENABLED && record.getCronExpression() != null) {
                schedule(record.getTaskKey(), record.getCronExpression());
            } else {
                log.info("[DynamicTaskManager] 定时任务 {} 处于暂停状态，跳过调度", record.getTaskKey());
            }
        }
    }

    /**
     * 注册（或重新注册）一个任务的 cron 调度。
     * <p>
     * 记录驱动：不依赖 SPI 对象，仅按 taskKey 注册，执行时再解析
     * {@link #runWithRecord(String)}，故表记录无对应 Bean 也能注册（执行时回写失败）。
     */
    private void schedule(String taskKey, String cron) {
        cancel(taskKey);
        if (cron == null || cron.isEmpty()) {
            log.warn("[DynamicTaskManager] 定时任务 {} 的 cron 为空，跳过调度", taskKey);
            return;
        }
        ScheduledFuture<?> future = scheduler.schedule(() -> runWithRecord(taskKey),
                triggerContext -> {
                    // 由 CronTrigger 语义计算下一次触发时间（Trigger 需返回 Instant）
                    ZonedDateTime next = CronExpression.parse(cron).next(ZonedDateTime.now());
                    return next == null ? null : next.toInstant();
                });
        taskFutures.put(taskKey, future);
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
     * 按任务标识执行并回写执行结果（记录驱动入口）。
     * <p>
     * 执行时才从容器解析 SPI Bean：取不到则不执行业务逻辑，
     * 直接回写 failed 与"未找到任务实现"提示并告警返回。
     */
    public void runWithRecord(String taskKey) {
        ScheduledTaskSpi spi = resolveSpi(taskKey);
        if (spi == null) {
            log.warn("[DynamicTaskManager] 定时任务 {} 未找到任务实现，跳过执行", taskKey);
            scheduledTaskMapper.update(null, new LambdaUpdateWrapper<ScheduledTask>()
                    .eq(ScheduledTask::getTaskKey, taskKey)
                    .set(ScheduledTask::getLastExecuteTime, LocalDateTime.now())
                    .set(ScheduledTask::getLastExecuteStatus, "failed")
                    .set(ScheduledTask::getLastExecuteError, "未找到任务实现: " + taskKey));
            return;
        }
        runWithRecord(spi);
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
     * 暂停任务（取消调度，数据库状态置 0）
     */
    public void pause(String taskKey) {
        requireRecord(taskKey);
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
        requireRecord(taskKey);
        ScheduledTask record = getByTaskKey(taskKey);
        if (record == null || record.getCronExpression() == null) {
            throw new BusinessException("任务记录缺失或 cron 为空，无法恢复");
        }
        scheduledTaskMapper.update(null, new LambdaUpdateWrapper<ScheduledTask>()
                .eq(ScheduledTask::getTaskKey, taskKey)
                .set(ScheduledTask::getStatus, STATUS_ENABLED));
        schedule(taskKey, record.getCronExpression());
        log.info("[DynamicTaskManager] 定时任务 {} 已恢复，cron={}", taskKey, record.getCronExpression());
    }

    /**
     * 手动触发一次任务执行（异步，立即返回）
     */
    public void triggerOnce(String taskKey) {
        requireRecord(taskKey);
        scheduler.execute(() -> runWithRecord(taskKey));
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
     * 校验任务记录存在（按表记录驱动，不要求 SPI Bean 一定存在），不存在抛业务异常
     */
    private void requireRecord(String taskKey) {
        if (getByTaskKey(taskKey) == null) {
            throw new BusinessException("任务不存在: " + taskKey);
        }
    }

    /**
     * 解析任务标识对应的 SPI 实现：
     * 先精确匹配（taskKey == spiKey），再按 "-" 分界做最长前缀匹配
     * （支持同类型多实例，如 "databaseBackup-daily" -> databaseBackup）。
     */
    private ScheduledTaskSpi resolveSpi(String taskKey) {
        if (taskKey == null) {
            return null;
        }
        ScheduledTaskSpi exact = spiMap.get(taskKey);
        if (exact != null) {
            return exact;
        }
        ScheduledTaskSpi best = null;
        int bestLen = -1;
        for (Map.Entry<String, ScheduledTaskSpi> entry : spiMap.entrySet()) {
            String spiKey = entry.getKey();
            // 前缀须形如 "{spiKey}-"，且取最长匹配避免 SPI key 本身互为前缀时歧义
            if (taskKey.length() > spiKey.length() + 1
                    && taskKey.startsWith(spiKey + "-")
                    && spiKey.length() > bestLen) {
                best = entry.getValue();
                bestLen = spiKey.length();
            }
        }
        return best;
    }

    /**
     * 新增定时任务记录（任务类型必须可解析到容器内实现）。
     * <p>
     * taskKey 可为 SPI 的 taskKey（如 databaseBackup）或 "{spiKey}-后缀"
     * （如 databaseBackup-daily，同类型多实例，重启后调度不丢失）。
     *
     * @param taskKey     任务唯一标识（须能解析到 {@link ScheduledTaskSpi} 实现）
     * @param taskName    任务名称
     * @param description 任务描述
     * @param cron        cron 表达式（Spring CronExpression 格式）
     * @param status      状态 1:启用 0:暂停（null 默认启用）
     * @return 插入后的任务记录
     */
    public ScheduledTask create(String taskKey, String taskName, String description, String cron, Integer status) {
        if (taskKey == null || taskKey.isBlank()) {
            throw new BusinessException("任务标识不能为空");
        }
        // 格式与前端校验一致：字母开头的字母/数字/中划线（保证 UI 编辑时 disabled 字段校验可过）
        if (!TASK_KEY_PATTERN.matcher(taskKey).matches()) {
            throw new BusinessException("任务标识仅支持字母开头的字母/数字/中划线: " + taskKey);
        }
        if (!CronExpression.isValidExpression(cron)) {
            throw new BusinessException("cron 表达式非法: " + cron);
        }
        if (getByTaskKey(taskKey) != null) {
            throw new BusinessException("任务标识已存在: " + taskKey);
        }
        // 新增任务必须能解析到 SPI 类型，否则只是无法执行的死记录
        if (resolveSpi(taskKey) == null) {
            throw new BusinessException("未找到任务类型实现: " + taskKey);
        }
        ScheduledTask task = new ScheduledTask();
        task.setTaskKey(taskKey);
        task.setTaskName(taskName);
        task.setDescription(description);
        task.setCronExpression(cron);
        task.setStatus(status == null ? STATUS_ENABLED : status);
        try {
            scheduledTaskMapper.insert(task);
        } catch (DuplicateKeyException e) {
            // 并发创建同 taskKey 撞唯一索引，转为友好业务提示
            throw new BusinessException("任务标识已存在: " + taskKey);
        }
        if (task.getStatus() == STATUS_ENABLED) {
            schedule(taskKey, cron);
        }
        log.info("[DynamicTaskManager] 已新增定时任务: {} [{}] cron={}", taskKey, taskName, cron);
        return task;
    }

    /**
     * 编辑定时任务（name/描述/cron/状态，null 字段不更新）。
     * <p>
     * 调度同步按「最终状态 finalStatus（status==null ? 旧状态 : status）」
     * 与「最终 cron finalCron（cron==null ? 旧 cron : cron）」计算，保证组合场景正确：
     * <ul>
     *     <li>finalStatus=0（暂停）：无论 cron 是否变更一律 cancel，暂停态不保留调度；</li>
     *     <li>finalStatus=1（启用）且（cron 变更或旧状态非启用，即原本未注册）：
     *         按最终 cron 重新注册（schedule 内部先 cancel 再注册，幂等）；</li>
     *     <li>其余情况（保持启用且 cron 未变）：调度不动。</li>
     * </ul>
     *
     * @param id          任务记录主键
     * @param taskName    任务名称（null 不更新）
     * @param description 任务描述（null 不更新）
     * @param cron        新 cron 表达式（null 不更新）
     * @param status      状态 1:启用 0:暂停（null 不更新）
     * @return 更新后的任务记录
     */
    public ScheduledTask update(Long id, String taskName, String description, String cron, Integer status) {
        ScheduledTask record = scheduledTaskMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("任务不存在: id=" + id);
        }
        if (cron != null && !CronExpression.isValidExpression(cron)) {
            throw new BusinessException("cron 表达式非法: " + cron);
        }
        if (status != null && status != STATUS_ENABLED && status != STATUS_DISABLED) {
            throw new BusinessException("status 非法: " + status);
        }
        boolean cronChanged = cron != null && !cron.equals(record.getCronExpression());
        Integer oldStatus = record.getStatus();
        Integer finalStatus = status == null ? oldStatus : status;
        String finalCron = cron == null ? record.getCronExpression() : cron;
        LambdaUpdateWrapper<ScheduledTask> wrapper = new LambdaUpdateWrapper<ScheduledTask>()
                .eq(ScheduledTask::getId, id);
        if (taskName != null) {
            wrapper.set(ScheduledTask::getTaskName, taskName);
        }
        if (description != null) {
            wrapper.set(ScheduledTask::getDescription, description);
        }
        if (cron != null) {
            wrapper.set(ScheduledTask::getCronExpression, cron);
        }
        if (status != null) {
            wrapper.set(ScheduledTask::getStatus, status);
        }
        scheduledTaskMapper.update(null, wrapper);
        // 调度同步：暂停态一律无调度；启用态且 cron 变更或原本未启用时按最终 cron 注册
        if (finalStatus != null && finalStatus == STATUS_DISABLED) {
            cancel(record.getTaskKey());
        } else if (finalStatus != null && finalStatus == STATUS_ENABLED
                && (cronChanged || oldStatus == null || oldStatus != STATUS_ENABLED)) {
            schedule(record.getTaskKey(), finalCron);
        }
        log.info("[DynamicTaskManager] 已编辑定时任务: id={} taskKey={}", id, record.getTaskKey());
        return scheduledTaskMapper.selectById(id);
    }

    /**
     * 列出容器内全部任务类型元信息（后台新增任务时下拉选择用）
     */
    public List<SpiOption> listSpiOptions() {
        return taskSpis.stream()
                .map(spi -> new SpiOption(spi.taskKey(), spi.taskName(), spi.description(), spi.defaultCron()))
                .toList();
    }

    /**
     * 任务类型元信息（对应容器内一个 {@link ScheduledTaskSpi} 实现）
     */
    public record SpiOption(String taskKey, String taskName, String description, String defaultCron) {
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
