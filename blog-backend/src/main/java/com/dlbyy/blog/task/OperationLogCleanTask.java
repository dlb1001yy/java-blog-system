package com.dlbyy.blog.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dlbyy.blog.entity.OperationLog;
import com.dlbyy.blog.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 操作日志定时清理任务。
 * <p>
 * 通过实现 {@link ScheduledTaskSpi} 接入动态调度管理器，
 * 每天凌晨清理超过保留天数的后台操作日志（sys_operation_log），
 * 保留天数由 {@code blog.cleanup.retention-days} 配置。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogCleanTask implements ScheduledTaskSpi {

    private final OperationLogMapper operationLogMapper;

    /** 操作日志保留天数（超期自动清理） */
    @Value("${blog.cleanup.retention-days:90}")
    private int retentionDays;

    @Override
    public String taskKey() {
        return "operationLogClean";
    }

    @Override
    public String taskName() {
        return "操作日志清理";
    }

    @Override
    public String description() {
        return "每天凌晨清理超过保留天数的后台操作日志";
    }

    @Override
    public String defaultCron() {
        return "0 30 3 * * ?"; // 每天 3:30（错开 4:00 的数据库备份）
    }

    @Override
    public void run() {
        // 防误配：保留天数 <= 0 会误删全部日志，跳过执行并告警
        if (retentionDays <= 0) {
            log.warn("[OperationLogCleanTask] 配置 blog.cleanup.retention-days={} 非法（须为正数），跳过本次清理", retentionDays);
            return;
        }
        LocalDateTime expireBefore = LocalDateTime.now().minusDays(retentionDays);
        int deleted = operationLogMapper.delete(new LambdaQueryWrapper<OperationLog>()
                .lt(OperationLog::getCreateTime, expireBefore));
        log.info("[OperationLogCleanTask] 已清理 {} 天前的操作日志 {} 条", retentionDays, deleted);
    }
}
