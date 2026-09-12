package com.dlbyy.blog.task;

import com.dlbyy.blog.service.BackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 数据库定时备份任务。
 * <p>
 * 通过实现 {@link ScheduledTaskSpi} 接入动态调度管理器，
 * 默认每天凌晨 4 点全库备份并压缩，保留天数由 {@code blog.backup.retention-days} 配置。
 */
@Component
@RequiredArgsConstructor
public class DatabaseBackupTask implements ScheduledTaskSpi {

    private final BackupService backupService;

    @Override
    public String taskKey() {
        return "databaseBackup";
    }

    @Override
    public String taskName() {
        return "数据库备份";
    }

    @Override
    public String description() {
        return "每天凌晨全库备份并压缩，保留最近30天";
    }

    @Override
    public String defaultCron() {
        return "0 0 4 * * ?";
    }

    @Override
    public void run() {
        backupService.backup("auto");
    }
}
