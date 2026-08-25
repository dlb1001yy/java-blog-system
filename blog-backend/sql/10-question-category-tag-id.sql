-- ============================================================
-- 面试题/考试题 分类外键化 + 标签关联表迁移脚本 (MySQL 8.0)
-- 执行方式：mysql --default-character-set=utf8mb4 dlbyy_zp_blog < 本文件
-- 说明：
--   1) interview_question.category(varchar) -> category_id(bigint, 关联 blog_category)
--   2) interview_question.tags(逗号串) -> interview_question_tag(question_id, tag_id) 关联表
--   3) exam_question.category(varchar) -> category_id(bigint, 关联 blog_category)
--   脚本幂等：可重复执行，已迁移/已删除的部分自动跳过
-- ============================================================

USE `dlbyy_zp_blog`;

-- ------------------------------------------------------------
-- 0. 通用辅助存储过程：为表添加列（存在则跳过）
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS `add_column_if_not_exists`;
DELIMITER $$
CREATE PROCEDURE `add_column_if_not_exists`(
    IN p_table VARCHAR(64),
    IN p_column VARCHAR(64),
    IN p_definition VARCHAR(500)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table
          AND COLUMN_NAME = p_column
    ) THEN
        SET @sql = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_definition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- 通用辅助存储过程：删除表列（不存在则跳过）
DROP PROCEDURE IF EXISTS `drop_column_if_exists`;
DELIMITER $$
CREATE PROCEDURE `drop_column_if_exists`(
    IN p_table VARCHAR(64),
    IN p_column VARCHAR(64)
)
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table
          AND COLUMN_NAME = p_column
    ) THEN
        SET @sql = CONCAT('ALTER TABLE `', p_table, '` DROP COLUMN `', p_column, '`');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- 通用辅助存储过程：删除索引（不存在则跳过）
DROP PROCEDURE IF EXISTS `drop_index_if_exists`;
DELIMITER $$
CREATE PROCEDURE `drop_index_if_exists`(
    IN p_table VARCHAR(64),
    IN p_index VARCHAR(64)
)
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table
          AND INDEX_NAME = p_index
    ) THEN
        SET @sql = CONCAT('ALTER TABLE `', p_table, '` DROP INDEX `', p_index, '`');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- ------------------------------------------------------------
-- 1. interview_question 新增 category_id 列 + 索引
-- ------------------------------------------------------------
CALL `add_column_if_not_exists`('interview_question', 'category_id',
    'bigint DEFAULT NULL COMMENT ''分类ID(关联 blog_category)'' AFTER `id`');

-- 索引存在性由 add_column_if_not_exists 幂等保证（仅新列时创建；老索引重复创建忽略）
DROP PROCEDURE IF EXISTS `add_idx_iq_category`;
DELIMITER $$
CREATE PROCEDURE `add_idx_iq_category`()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'interview_question'
          AND INDEX_NAME = 'idx_category_id'
    ) THEN
        ALTER TABLE `interview_question` ADD INDEX `idx_category_id` (`category_id`);
    END IF;
END$$
DELIMITER ;
CALL `add_idx_iq_category`();

-- ------------------------------------------------------------
-- 2. exam_question 新增 category_id 列 + 索引
-- ------------------------------------------------------------
CALL `add_column_if_not_exists`('exam_question', 'category_id',
    'bigint DEFAULT NULL COMMENT ''分类ID(关联 blog_category)'' AFTER `id`');

DROP PROCEDURE IF EXISTS `add_idx_eq_category`;
DELIMITER $$
CREATE PROCEDURE `add_idx_eq_category`()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'exam_question'
          AND INDEX_NAME = 'idx_category_id'
    ) THEN
        ALTER TABLE `exam_question` ADD INDEX `idx_category_id` (`category_id`);
    END IF;
END$$
DELIMITER ;
CALL `add_idx_eq_category`();

-- ------------------------------------------------------------
-- 3. interview_question 分类迁移
--    先按 blog_category.name 匹配回填，未匹配的名称插入 blog_category 后再回填
-- ------------------------------------------------------------
-- 3.1 已匹配的回填
UPDATE `interview_question` q
JOIN `blog_category` c ON c.`name` = q.`category`
SET q.`category_id` = c.`id`
WHERE q.`category_id` IS NULL AND q.`category` IS NOT NULL;

-- 3.2 未匹配的分类插入 blog_category（按名称去重，幂等）
INSERT INTO `blog_category` (`name`, `sort`)
SELECT DISTINCT q.`category`, 99
FROM `interview_question` q
WHERE q.`category` IS NOT NULL
  AND q.`category_id` IS NULL
  AND NOT EXISTS (SELECT 1 FROM `blog_category` c WHERE c.`name` = q.`category`);

-- 3.3 回填剩余（包含 exam_question 迁移前需要的新分类见第 5 节）
UPDATE `interview_question` q
JOIN `blog_category` c ON c.`name` = q.`category`
SET q.`category_id` = c.`id`
WHERE q.`category_id` IS NULL AND q.`category` IS NOT NULL;

-- ------------------------------------------------------------
-- 4. interview_question_tag 关联表 + 标签迁移（从 tags 逗号串拆分）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `interview_question_tag` (
    `id`          bigint   NOT NULL AUTO_INCREMENT,
    `question_id` bigint   NOT NULL COMMENT '面试题ID',
    `tag_id`      bigint   NOT NULL COMMENT '标签ID(关联 blog_tag)',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_question_tag` (`question_id`, `tag_id`),
    INDEX `idx_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试题-标签关联表';

-- 4.1 未匹配的标签插入 blog_tag（利用递归 CTE 拆分逗号串，幂等）
INSERT INTO `blog_tag` (`name`)
SELECT DISTINCT t.`tag_name`
FROM (
    WITH RECURSIVE seq AS (
        SELECT 1 AS n
        UNION ALL
        SELECT n + 1 FROM seq WHERE n < 50
    )
    SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(q.`tags`, ',', s.n), ',', -1)) AS `tag_name`
    FROM `interview_question` q
    CROSS JOIN seq s
    WHERE q.`tags` IS NOT NULL
      AND q.`tags` <> ''
      AND s.n <= 1 + LENGTH(q.`tags`) - LENGTH(REPLACE(q.`tags`, ',', ''))
) t
WHERE t.`tag_name` <> ''
  AND NOT EXISTS (SELECT 1 FROM `blog_tag` g WHERE g.`name` = t.`tag_name`);

-- 4.2 写入关联表（唯一索引 + NOT EXISTS 双重幂等）
INSERT INTO `interview_question_tag` (`question_id`, `tag_id`)
SELECT DISTINCT q.`id`, g.`id`
FROM (
    WITH RECURSIVE seq AS (
        SELECT 1 AS n
        UNION ALL
        SELECT n + 1 FROM seq WHERE n < 50
    )
    SELECT q.`id` AS question_id,
           TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(q.`tags`, ',', s.n), ',', -1)) AS `tag_name`
    FROM `interview_question` q
    CROSS JOIN seq s
    WHERE q.`tags` IS NOT NULL
      AND q.`tags` <> ''
      AND s.n <= 1 + LENGTH(q.`tags`) - LENGTH(REPLACE(q.`tags`, ',', ''))
) q
JOIN `blog_tag` g ON g.`name` = q.`tag_name`
WHERE q.`tag_name` <> ''
  AND NOT EXISTS (
      SELECT 1 FROM `interview_question_tag` qt
      WHERE qt.`question_id` = q.`question_id` AND qt.`tag_id` = g.`id`
  );

-- ------------------------------------------------------------
-- 5. exam_question 分类迁移
-- ------------------------------------------------------------
-- 5.1 未匹配的分类插入 blog_category（幂等）
INSERT INTO `blog_category` (`name`, `sort`)
SELECT DISTINCT eq.`category`, 99
FROM `exam_question` eq
WHERE eq.`category` IS NOT NULL
  AND eq.`category_id` IS NULL
  AND NOT EXISTS (SELECT 1 FROM `blog_category` c WHERE c.`name` = eq.`category`);

-- 5.2 回填
UPDATE `exam_question` eq
JOIN `blog_category` c ON c.`name` = eq.`category`
SET eq.`category_id` = c.`id`
WHERE eq.`category_id` IS NULL AND eq.`category` IS NOT NULL;

-- ------------------------------------------------------------
-- 6. 迁移完成后删除旧列与旧索引
-- ------------------------------------------------------------
-- 仅当 category_id 已全部回填（无 NULL 且旧列存在）时才删除旧列，保证安全
DROP PROCEDURE IF EXISTS `drop_legacy_columns`;
DELIMITER $$
CREATE PROCEDURE `drop_legacy_columns`()
BEGIN
    -- interview_question
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'interview_question' AND COLUMN_NAME = 'category')
       AND NOT EXISTS (SELECT 1 FROM `interview_question`
                       WHERE `category` IS NOT NULL AND `category_id` IS NULL) THEN
        ALTER TABLE `interview_question` DROP COLUMN `category`;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'interview_question' AND COLUMN_NAME = 'tags')
       AND NOT EXISTS (SELECT 1 FROM `interview_question`
                       WHERE `tags` IS NOT NULL AND `tags` <> '') THEN
        ALTER TABLE `interview_question` DROP COLUMN `tags`;
    END IF;

    -- exam_question
    IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'exam_question' AND COLUMN_NAME = 'category')
       AND NOT EXISTS (SELECT 1 FROM `exam_question`
                       WHERE `category` IS NOT NULL AND `category_id` IS NULL) THEN
        ALTER TABLE `exam_question` DROP COLUMN `category`;
    END IF;
END$$
DELIMITER ;
CALL `drop_legacy_columns`();

-- 删除旧索引（列已删除时索引自动消失，此处兜底）
CALL `drop_index_if_exists`('interview_question', 'idx_category');
CALL `drop_index_if_exists`('exam_question', 'idx_category');

-- ------------------------------------------------------------
-- 7. 清理临时存储过程
-- ------------------------------------------------------------
DROP PROCEDURE IF EXISTS `add_column_if_not_exists`;
DROP PROCEDURE IF EXISTS `drop_column_if_exists`;
DROP PROCEDURE IF EXISTS `drop_index_if_exists`;
DROP PROCEDURE IF EXISTS `add_idx_iq_category`;
DROP PROCEDURE IF EXISTS `add_idx_eq_category`;
DROP PROCEDURE IF EXISTS `drop_legacy_columns`;
