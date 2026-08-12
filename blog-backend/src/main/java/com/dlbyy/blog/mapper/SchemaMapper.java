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
}
