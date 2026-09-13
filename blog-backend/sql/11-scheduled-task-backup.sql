-- ============================================================
-- 定时任务 + 数据库备份 建表脚本 (MySQL 8.0)
-- 执行方式：mysql --default-character-set=utf8mb4 dlbyy_zp_blog < 本文件
-- 说明：脚本幂等，可重复执行；应用启动时 DataInitializer 也会自动
--       补建同名表，此脚本用于 docker initdb 首次建库或手工初始化。
-- ============================================================

USE `dlbyy_zp_blog`;

-- ------------------------------------------------------------
-- 1. 定时任务表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_scheduled_task` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_key` varchar(64) NOT NULL COMMENT '任务唯一标识',
    `task_name` varchar(128) DEFAULT NULL COMMENT '任务名称',
    `description` varchar(255) DEFAULT NULL COMMENT '任务描述',
    `cron_expression` varchar(64) DEFAULT NULL COMMENT 'cron表达式',
    `status` tinyint DEFAULT 1 COMMENT '状态 1:启用 0:暂停',
    `last_execute_time` datetime DEFAULT NULL COMMENT '最近执行时间',
    `last_execute_status` varchar(16) DEFAULT NULL COMMENT '最近执行状态',
    `last_execute_error` text COMMENT '最近执行错误信息',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_key` (`task_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务表';

-- ------------------------------------------------------------
-- 2. 数据库备份记录表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_backup_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `file_name` varchar(255) DEFAULT NULL COMMENT '备份文件名',
    `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)',
    `type` varchar(16) DEFAULT NULL COMMENT '备份类型 auto:定时 manual:手动',
    `status` varchar(16) DEFAULT NULL COMMENT '备份状态 success:成功 failed:失败',
    `error_msg` text COMMENT '失败原因',
    `backup_time` datetime DEFAULT NULL COMMENT '备份时间',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_backup_time` (`backup_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据库备份记录表';

-- ------------------------------------------------------------
-- 3. 内置任务种子（databaseBackup：每天凌晨 4 点全库备份）
--    应用启动时 DynamicTaskManager 也会自动补插，此处幂等预置
-- ------------------------------------------------------------
INSERT INTO `sys_scheduled_task`
    (`task_key`, `task_name`, `description`, `cron_expression`, `status`)
SELECT 'databaseBackup', '数据库备份', '每天凌晨全库备份并压缩，保留最近30天', '0 0 4 * * ?', 1
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_scheduled_task` WHERE `task_key` = 'databaseBackup'
);

INSERT INTO `sys_scheduled_task`
    (`task_key`, `task_name`, `description`, `cron_expression`, `status`)
SELECT 'operationLogClean', '操作日志清理', '每天凌晨清理超过保留天数的后台操作日志', '0 30 3 * * ?', 1
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_scheduled_task` WHERE `task_key` = 'operationLogClean'
);
