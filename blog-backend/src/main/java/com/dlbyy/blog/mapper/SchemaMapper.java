package com.dlbyy.blog.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 数据库 schema 幂等迁移。
 * 用于在 docker compose 重新部署（已有旧库）时，自动补全新增的字段，
 * 避免依赖 MySQL initdb 目录（该目录仅在首次建库时执行一次）。
 */
@Mapper
public interface SchemaMapper {

    @Select("SELECT 1")
    int ping();

    @Select("SELECT COUNT(*) FROM information_schema.columns " +
            "WHERE table_schema = DATABASE() AND table_name = 'sys_user' AND column_name = 'fail_count'")
    int countFailCountColumn();

    @Select("SELECT COUNT(*) FROM information_schema.columns " +
            "WHERE table_schema = DATABASE() AND table_name = 'sys_user' AND column_name = 'lock_until'")
    int countLockUntilColumn();

    @Update("ALTER TABLE sys_user ADD COLUMN fail_count int DEFAULT 0 COMMENT '连续登录失败次数（锁定辅助）'")
    void addFailCountColumn();

    @Update("ALTER TABLE sys_user ADD COLUMN lock_until datetime DEFAULT NULL COMMENT '账户锁定到期时间（NULL 表示未锁定）'")
    void addLockUntilColumn();

    @Select("SELECT COUNT(*) FROM information_schema.tables " +
            "WHERE table_schema = DATABASE() AND table_name = 'sys_operation_log'")
    int countOperationLogTable();

    @Update("CREATE TABLE IF NOT EXISTS sys_operation_log (" +
            "id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID', " +
            "username varchar(50) DEFAULT NULL COMMENT '操作人', " +
            "operation varchar(100) DEFAULT NULL COMMENT '操作描述', " +
            "method varchar(10) DEFAULT NULL COMMENT 'HTTP方法', " +
            "uri varchar(255) DEFAULT NULL COMMENT '请求路径', " +
            "params text COMMENT '请求参数', " +
            "ip varchar(50) DEFAULT NULL COMMENT '客户端IP', " +
            "status tinyint DEFAULT 1 COMMENT '操作状态 1:成功 0:失败', " +
            "error_msg text COMMENT '异常信息', " +
            "cost_ms bigint DEFAULT NULL COMMENT '耗时毫秒', " +
            "create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
            "PRIMARY KEY (id), " +
            "KEY idx_username (username), " +
            "KEY idx_create_time (create_time) " +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台操作日志表'")
    void createOperationLogTable();

    @Select("SELECT COUNT(*) FROM information_schema.tables " +
            "WHERE table_schema = DATABASE() AND table_name = 'sys_scheduled_task'")
    int countScheduledTaskTable();

    @Update("CREATE TABLE IF NOT EXISTS sys_scheduled_task (" +
            "id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID', " +
            "task_key varchar(64) NOT NULL COMMENT '任务唯一标识', " +
            "task_name varchar(128) DEFAULT NULL COMMENT '任务名称', " +
            "description varchar(255) DEFAULT NULL COMMENT '任务描述', " +
            "cron_expression varchar(64) DEFAULT NULL COMMENT 'cron表达式', " +
            "status tinyint DEFAULT 1 COMMENT '状态 1:启用 0:暂停', " +
            "last_execute_time datetime DEFAULT NULL COMMENT '最近执行时间', " +
            "last_execute_status varchar(16) DEFAULT NULL COMMENT '最近执行状态', " +
            "last_execute_error text COMMENT '最近执行错误信息', " +
            "create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
            "update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', " +
            "PRIMARY KEY (id), " +
            "UNIQUE KEY uk_task_key (task_key) " +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务表'")
    void createScheduledTaskTable();

    @Select("SELECT COUNT(*) FROM information_schema.tables " +
            "WHERE table_schema = DATABASE() AND table_name = 'sys_backup_record'")
    int countBackupRecordTable();

    @Update("CREATE TABLE IF NOT EXISTS sys_backup_record (" +
            "id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID', " +
            "file_name varchar(255) DEFAULT NULL COMMENT '备份文件名', " +
            "file_size bigint DEFAULT NULL COMMENT '文件大小(字节)', " +
            "type varchar(16) DEFAULT NULL COMMENT '备份类型 auto:定时 manual:手动', " +
            "status varchar(16) DEFAULT NULL COMMENT '备份状态 success:成功 failed:失败', " +
            "error_msg text COMMENT '失败原因', " +
            "backup_time datetime DEFAULT NULL COMMENT '备份时间', " +
            "create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
            "PRIMARY KEY (id), " +
            "KEY idx_backup_time (backup_time) " +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据库备份记录表'")
    void createBackupRecordTable();
}
