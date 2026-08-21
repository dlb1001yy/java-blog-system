-- ============================================================
-- 简历模块多用户化增量脚本
-- 执行方式：mysql --default-character-set=utf8mb4 dlbyy_zp_blog < 本文件
-- ============================================================

USE `dlbyy_zp_blog`;

-- 简历归属用户；站长现有简历回填为 NULL，保持"站长简历"语义（getOne(null)）
ALTER TABLE `resume_info`
    ADD COLUMN `user_id` bigint DEFAULT NULL COMMENT '所属用户ID(NULL为站长简历)' AFTER `id`,
    ADD UNIQUE KEY `uk_resume_user` (`user_id`);
