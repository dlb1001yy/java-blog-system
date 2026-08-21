-- ============================================================
-- 简历分享与审核增量脚本
-- 执行方式：mysql --default-character-set=utf8mb4 dlbyy_zp_blog < 本文件
-- ============================================================

USE `dlbyy_zp_blog`;

-- 简历审核状态
ALTER TABLE `resume_info`
    ADD COLUMN `status` tinyint DEFAULT 0 COMMENT '审核状态 0待审核 1通过 2拒绝',
    ADD COLUMN `audit_remark` varchar(200) DEFAULT NULL COMMENT '审核备注';

-- 简历分享链接
CREATE TABLE `resume_share` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `resume_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `share_token` VARCHAR(64) NOT NULL,
    `expire_time` DATETIME NULL COMMENT 'NULL=永久',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_share_token` (`share_token`),
    KEY `idx_share_resume` (`resume_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历分享链接';
