-- ============================================================
-- 文章表演进 DDL (MySQL 8.0)
-- 基于 PRD: 新增 is_deleted / tags / status 字段，遵循逻辑删除与自动填充规范
-- ============================================================

USE `dlbyy_zp_blog`;

-- ------------------------------------------------------------
-- 1. 新增列
-- ------------------------------------------------------------

-- 逻辑删除字段：0 正常 / 1 已删
ALTER TABLE `blog_article`
    ADD COLUMN `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除 0:正常 1:已删' AFTER `is_publish`;

-- 标签字段（冗余存储，逗号分隔的标签名，便于列表快速展示）
ALTER TABLE `blog_article`
    ADD COLUMN `tags` varchar(500) DEFAULT NULL COMMENT '标签名(逗号分隔)' AFTER `category_id`;

-- 文章状态：0 草稿 / 1 已发布 / 2 下线
ALTER TABLE `blog_article`
    ADD COLUMN `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态 0:草稿 1:已发布 2:下线' AFTER `tags`;

-- ------------------------------------------------------------
-- 2. 索引定义
-- ------------------------------------------------------------

-- 逻辑删除过滤索引（几乎所有查询都带 is_deleted = 0）
ALTER TABLE `blog_article` ADD INDEX `idx_is_deleted` (`is_deleted`);

-- 状态索引（按发布状态筛选）
ALTER TABLE `blog_article` ADD INDEX `idx_status` (`status`);

-- 复合索引：列表分页常用 (is_deleted, status, create_time DESC)
ALTER TABLE `blog_article` ADD INDEX `idx_deleted_status_create` (`is_deleted`, `status`, `create_time`);

-- ------------------------------------------------------------
-- 3. 数据迁移（可选）：将 is_publish 值同步到 status
-- ------------------------------------------------------------
-- 已发布(is_publish=1) → status=1，草稿(is_publish=0) → status=0
UPDATE `blog_article` SET `status` = `is_publish` WHERE `is_publish` IS NOT NULL;

-- ============================================================
-- 完整建表 DDL（参考，用于全新环境）
-- ============================================================
--
-- CREATE TABLE `blog_article` (
--     `id`           bigint       NOT NULL AUTO_INCREMENT,
--     `title`        varchar(200) NOT NULL COMMENT '标题',
--     `content`      longtext     NOT NULL COMMENT '内容',
--     `category_id`  bigint       DEFAULT NULL COMMENT '分类ID',
--     `tags`         varchar(500) DEFAULT NULL COMMENT '标签名(逗号分隔)',
--     `status`       tinyint      NOT NULL DEFAULT 0 COMMENT '状态 0:草稿 1:已发布 2:下线',
--     `is_deleted`   tinyint      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0:正常 1:已删',
--     `create_time`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
--     `update_time`  datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
--     PRIMARY KEY (`id`),
--     INDEX `idx_category_id` (`category_id`),
--     INDEX `idx_is_deleted` (`is_deleted`),
--     INDEX `idx_status` (`status`),
--     INDEX `idx_deleted_status_create` (`is_deleted`, `status`, `create_time`)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';
