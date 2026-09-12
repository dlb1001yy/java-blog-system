package com.dlbyy.blog.task;

/**
 * 定时任务 SPI 接口。
 * <p>
 * 实现类标注 {@code @Component} 后由 Spring 容器自动收集，
 * {@link DynamicTaskManager} 启动时会遍历所有实现，
 * 按 {@link #taskKey()} 幂等写入 sys_scheduled_task 并注册 cron 调度。
 * 新增定时任务只需实现本接口，无需改动管理器与管理端代码。
 */
public interface ScheduledTaskSpi {

    /**
     * 任务唯一标识（写入 sys_scheduled_task.task_key，全局唯一）
     */
    String taskKey();

    /**
     * 任务名称（展示用）
     */
    String taskName();

    /**
     * 任务描述（展示用）
     */
    String description();

    /**
     * 默认 cron 表达式（首次注册时使用，Spring CronExpression 格式）
     */
    String defaultCron();

    /**
     * 任务执行逻辑。异常由调度管理器统一捕获并记录，不会中断调度。
     */
    void run() throws Exception;
}
