-- ============================================================
-- 多功能模块 DDL (MySQL 8.0)
-- 面试刷题 / 在线考试 / 音乐播放 / 用户管理增强
-- 遵循现有规范：BaseEntity 字段（id/create_time/update_time/is_deleted）
-- ============================================================

USE `dlbyy_zp_blog`;

-- ------------------------------------------------------------
-- 1. 面试刷题模块
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `interview_question` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `category_id` bigint       DEFAULT NULL COMMENT '分类ID(关联 blog_category)',
    `difficulty`  varchar(20)  NOT NULL DEFAULT '中等' COMMENT '难度：简单/中等/困难',
    `title`       varchar(500) NOT NULL COMMENT '题目标题/题干',
    `answer`      longtext     COMMENT '参考答案（支持 Markdown/代码块）',
    `status`      tinyint      NOT NULL DEFAULT 1 COMMENT '状态 0:停用 1:启用',
    `is_deleted`  tinyint      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0:正常 1:已删',
    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_difficulty` (`difficulty`),
    INDEX `idx_deleted_status` (`is_deleted`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试题表';

CREATE TABLE IF NOT EXISTS `interview_question_tag` (
    `id`          bigint   NOT NULL AUTO_INCREMENT,
    `question_id` bigint   NOT NULL COMMENT '面试题ID',
    `tag_id`      bigint   NOT NULL COMMENT '标签ID(关联 blog_tag)',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_question_tag` (`question_id`, `tag_id`),
    INDEX `idx_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试题-标签关联表';

CREATE TABLE IF NOT EXISTS `interview_favorite` (
    `id`          bigint   NOT NULL AUTO_INCREMENT,
    `user_id`     bigint   NOT NULL COMMENT '用户ID',
    `question_id` bigint   NOT NULL COMMENT '面试题ID',
    `type`        tinyint  NOT NULL DEFAULT 0 COMMENT '类型 0:收藏 1:错题',
    `is_deleted`  tinyint  NOT NULL DEFAULT 0 COMMENT '逻辑删除 0:正常 1:已删',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_question_type` (`user_id`, `question_id`, `type`),
    INDEX `idx_user_type` (`user_id`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试题收藏/错题本表';

-- ------------------------------------------------------------
-- 2. 在线考试模块
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `exam_question` (
    `id`               bigint       NOT NULL AUTO_INCREMENT,
    `stem`             longtext     NOT NULL COMMENT '题干（支持 Markdown/代码块）',
    `type`             tinyint      NOT NULL COMMENT '题型 1:单选 2:多选 3:判断 4:填空 5:简答 6:编程',
    `category_id`      bigint       DEFAULT NULL COMMENT '分类ID(关联 blog_category)',
    `difficulty`       varchar(20)  NOT NULL DEFAULT '中等' COMMENT '难度：简单/中等/困难',
    `options`          text         DEFAULT NULL COMMENT '选项 JSON 数组（客观题）',
    `correct`          text         DEFAULT NULL COMMENT '正确答案 JSON（索引/布尔/字符串数组）',
    `reference_answer` longtext     COMMENT '参考答案/解析（主观题批改对照 & 成绩解析）',
    `score`            decimal(5,1) NOT NULL DEFAULT 2 COMMENT '题目分值',
    `usage_count`      int          NOT NULL DEFAULT 0 COMMENT '使用次数（被试卷引用次数）',
    `status`           tinyint      NOT NULL DEFAULT 1 COMMENT '状态 0:停用 1:启用 2:待审核',
    `is_deleted`       tinyint      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0:正常 1:已删',
    `create_time`      datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_deleted_status` (`is_deleted`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试题目表';

CREATE TABLE IF NOT EXISTS `exam_paper` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `title`       varchar(200) NOT NULL COMMENT '试卷名称',
    `description` varchar(500) DEFAULT NULL COMMENT '试卷说明',
    `total_score` decimal(6,1) NOT NULL DEFAULT 100 COMMENT '总分',
    `duration`    int          NOT NULL DEFAULT 90 COMMENT '考试时长（分钟）',
    `status`      tinyint      NOT NULL DEFAULT 0 COMMENT '状态 0:草稿 1:已发布 2:已停用',
    `is_deleted`  tinyint      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0:正常 1:已删',
    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='试卷表';

CREATE TABLE IF NOT EXISTS `exam_paper_question` (
    `id`          bigint   NOT NULL AUTO_INCREMENT,
    `paper_id`    bigint   NOT NULL COMMENT '试卷ID',
    `question_id` bigint   NOT NULL COMMENT '题目ID',
    `sort_order`  int      NOT NULL DEFAULT 0 COMMENT '题目顺序',
    `score`       decimal(5,1) NOT NULL DEFAULT 2 COMMENT '本卷该题分值',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_paper_question` (`paper_id`, `question_id`),
    INDEX `idx_paper_sort` (`paper_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='试卷-题目关联表';

CREATE TABLE IF NOT EXISTS `exam_record` (
    `id`               bigint       NOT NULL AUTO_INCREMENT,
    `paper_id`         bigint       NOT NULL COMMENT '试卷ID',
    `user_id`          bigint       NOT NULL COMMENT '考生用户ID',
    `answers`          longtext     COMMENT '考生答案 JSON: [{questionId, answer, marked}]',
    `objective_score`  decimal(6,1) DEFAULT NULL COMMENT '客观题得分（自动判分）',
    `subjective_score` decimal(6,1) DEFAULT NULL COMMENT '主观题得分（人工批改）',
    `final_score`      decimal(6,1) DEFAULT NULL COMMENT '最终得分（阅卷提交后汇总）',
    `switch_count`     int          NOT NULL DEFAULT 0 COMMENT '切屏次数',
    `duration_seconds` int          DEFAULT NULL COMMENT '实际用时（秒）',
    `status`           tinyint      NOT NULL DEFAULT 0 COMMENT '状态 0:待批改 1:已发布',
    `submit_time`      datetime     DEFAULT NULL COMMENT '交卷时间',
    `is_deleted`       tinyint      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0:正常 1:已删',
    `create_time`      datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_paper_status` (`paper_id`, `status`),
    INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试答卷记录表';

CREATE TABLE IF NOT EXISTS `exam_marking` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `record_id`   bigint       NOT NULL COMMENT '答卷ID',
    `question_id` bigint       NOT NULL COMMENT '题目ID',
    `score`       decimal(5,1) DEFAULT NULL COMMENT '评分',
    `comment`     varchar(1000) DEFAULT NULL COMMENT '评语',
    `status`      tinyint      NOT NULL DEFAULT 0 COMMENT '状态 0:草稿 1:已确认',
    `is_deleted`  tinyint      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0:正常 1:已删',
    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_record_question` (`record_id`, `question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='主观题批改表';

-- ------------------------------------------------------------
-- 3. 音乐模块
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `music_song` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `title`       varchar(200) NOT NULL COMMENT '歌曲名',
    `artist`      varchar(100) DEFAULT NULL COMMENT '歌手',
    `album`       varchar(100) DEFAULT NULL COMMENT '专辑',
    `duration`    int          DEFAULT NULL COMMENT '时长（秒）',
    `cover`       varchar(500) DEFAULT NULL COMMENT '封面URL',
    `file_url`    varchar(500) NOT NULL COMMENT '音频文件URL（FileStorageService）',
    `format`      varchar(10)  DEFAULT 'mp3' COMMENT '格式',
    `file_size`   bigint       DEFAULT NULL COMMENT '文件大小（字节）',
    `play_count`  int          NOT NULL DEFAULT 0 COMMENT '播放次数',
    `is_deleted`  tinyint      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0:正常 1:已删',
    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='歌曲表';

CREATE TABLE IF NOT EXISTS `music_playlist` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `name`        varchar(100) NOT NULL COMMENT '歌单名',
    `cover`       varchar(500) DEFAULT NULL COMMENT '封面URL',
    `description` varchar(500) DEFAULT NULL COMMENT '描述',
    `status`      tinyint      NOT NULL DEFAULT 1 COMMENT '状态 0:草稿 1:已发布',
    `is_deleted`  tinyint      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0:正常 1:已删',
    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='歌单表';

CREATE TABLE IF NOT EXISTS `music_playlist_song` (
    `id`          bigint   NOT NULL AUTO_INCREMENT,
    `playlist_id` bigint   NOT NULL COMMENT '歌单ID',
    `song_id`     bigint   NOT NULL COMMENT '歌曲ID',
    `sort_order`  int      NOT NULL DEFAULT 0 COMMENT '顺序',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_playlist_song` (`playlist_id`, `song_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='歌单-歌曲关联表';

-- ------------------------------------------------------------
-- 3.5 存量表升级（幂等，写法与 10-question-category-tag-id.sql 一致）
--     背景：老库的 interview_question / exam_question 使用
--     category(varchar) + tags(逗号串) 字段，本段将其迁移为
--     category_id(bigint) + interview_question_tag 关联表。
--     新表环境（无旧列）或已执行过 10 号脚本的库：本段全部跳过，无任何变更。
-- ------------------------------------------------------------

-- 通用辅助存储过程：为表添加列（表不存在或列已存在则跳过）
DROP PROCEDURE IF EXISTS `add_column_if_not_exists`;
DELIMITER $$
CREATE PROCEDURE `add_column_if_not_exists`(
    IN p_table VARCHAR(64),
    IN p_column VARCHAR(64),
    IN p_definition VARCHAR(500)
)
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.TABLES
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table
    ) AND NOT EXISTS (
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

-- 存量表升级主过程：仅在检测到旧列（category/tags）时执行迁移
DROP PROCEDURE IF EXISTS `upgrade_legacy_question_tables`;
DELIMITER $$
CREATE PROCEDURE `upgrade_legacy_question_tables`()
BEGIN
    DECLARE v_iq_category BOOLEAN DEFAULT FALSE;
    DECLARE v_iq_tags BOOLEAN DEFAULT FALSE;
    DECLARE v_eq_category BOOLEAN DEFAULT FALSE;

    SELECT EXISTS(SELECT 1 FROM information_schema.COLUMNS
                  WHERE TABLE_SCHEMA = DATABASE()
                    AND TABLE_NAME = 'interview_question' AND COLUMN_NAME = 'category')
        INTO v_iq_category;

    SELECT EXISTS(SELECT 1 FROM information_schema.COLUMNS
                  WHERE TABLE_SCHEMA = DATABASE()
                    AND TABLE_NAME = 'interview_question' AND COLUMN_NAME = 'tags')
        INTO v_iq_tags;

    SELECT EXISTS(SELECT 1 FROM information_schema.COLUMNS
                  WHERE TABLE_SCHEMA = DATABASE()
                    AND TABLE_NAME = 'exam_question' AND COLUMN_NAME = 'category')
        INTO v_eq_category;

    IF v_iq_category OR v_iq_tags OR v_eq_category THEN
        -- 公共：新增 category_id 列 + 索引（仅老表需要）
        IF v_iq_category OR v_iq_tags THEN
            CALL `add_column_if_not_exists`('interview_question', 'category_id',
                'bigint DEFAULT NULL COMMENT ''分类ID(关联 blog_category)'' AFTER `id`');
            IF NOT EXISTS (
                SELECT 1 FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'interview_question' AND INDEX_NAME = 'idx_category_id'
            ) THEN
                ALTER TABLE `interview_question` ADD INDEX `idx_category_id` (`category_id`);
            END IF;
        END IF;

        IF v_eq_category THEN
            CALL `add_column_if_not_exists`('exam_question', 'category_id',
                'bigint DEFAULT NULL COMMENT ''分类ID(关联 blog_category)'' AFTER `id`');
            IF NOT EXISTS (
                SELECT 1 FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'exam_question' AND INDEX_NAME = 'idx_category_id'
            ) THEN
                ALTER TABLE `exam_question` ADD INDEX `idx_category_id` (`category_id`);
            END IF;
        END IF;
    END IF;

    -- ============ 3.5.1 interview_question：category / tags 迁移 ============
    IF v_iq_category THEN
        -- 分类按名称回填（显式 COLLATE utf8mb4_unicode_ci 避免两表排序规则不一致报 1267）
        UPDATE `interview_question` q
        JOIN `blog_category` c ON c.`name` COLLATE utf8mb4_unicode_ci = q.`category`
        SET q.`category_id` = c.`id`
        WHERE q.`category_id` IS NULL AND q.`category` IS NOT NULL;

        -- 未匹配的分类插入 blog_category（sort=99，按名称去重，幂等）
        INSERT INTO `blog_category` (`name`, `sort`)
        SELECT DISTINCT q.`category`, 99
        FROM `interview_question` q
        WHERE q.`category` IS NOT NULL
          AND q.`category_id` IS NULL
          AND NOT EXISTS (SELECT 1 FROM `blog_category` c
                          WHERE c.`name` COLLATE utf8mb4_unicode_ci = q.`category`);

        -- 回填剩余
        UPDATE `interview_question` q
        JOIN `blog_category` c ON c.`name` COLLATE utf8mb4_unicode_ci = q.`category`
        SET q.`category_id` = c.`id`
        WHERE q.`category_id` IS NULL AND q.`category` IS NOT NULL;

        -- 无残留才删除旧列（保证不丢数据）
        IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'interview_question' AND COLUMN_NAME = 'category')
           AND NOT EXISTS (SELECT 1 FROM `interview_question`
                           WHERE `category` IS NOT NULL AND `category_id` IS NULL) THEN
            ALTER TABLE `interview_question` DROP COLUMN `category`;
        END IF;
    END IF;

    IF v_iq_tags THEN

        -- tags 逗号串拆分（递归 CTE）：未匹配标签插入 blog_tag（幂等）
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
          AND NOT EXISTS (SELECT 1 FROM `blog_tag` g
                          WHERE g.`name` COLLATE utf8mb4_unicode_ci = t.`tag_name`);

        -- 写入 interview_question_tag（NOT EXISTS 幂等，派生表列引用用 question_id）
        INSERT INTO `interview_question_tag` (`question_id`, `tag_id`)
        SELECT DISTINCT q.`question_id`, g.`id`
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
        JOIN `blog_tag` g ON g.`name` COLLATE utf8mb4_unicode_ci = q.`tag_name`
        WHERE q.`tag_name` <> ''
          AND NOT EXISTS (
              SELECT 1 FROM `interview_question_tag` qt
              WHERE qt.`question_id` = q.`question_id` AND qt.`tag_id` = g.`id`
          );

    END IF;  -- v_iq_tags

    -- tags 无残留才删除旧列（保证不丢数据）
    IF v_iq_tags THEN
        IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'interview_question' AND COLUMN_NAME = 'tags')
           AND NOT EXISTS (SELECT 1 FROM `interview_question`
                           WHERE `tags` IS NOT NULL AND `tags` <> '') THEN
            ALTER TABLE `interview_question` DROP COLUMN `tags`;
        END IF;
    END IF;

    IF v_iq_category OR v_iq_tags THEN
        CALL `drop_index_if_exists`('interview_question', 'idx_category');
    END IF;

    -- ============ 3.5.2 exam_question：仅处理 category ============
    IF v_eq_category THEN
        -- 未匹配的分类插入 blog_category（幂等）
        INSERT INTO `blog_category` (`name`, `sort`)
        SELECT DISTINCT eq.`category`, 99
        FROM `exam_question` eq
        WHERE eq.`category` IS NOT NULL
          AND eq.`category_id` IS NULL
          AND NOT EXISTS (SELECT 1 FROM `blog_category` c
                          WHERE c.`name` COLLATE utf8mb4_unicode_ci = eq.`category`);

        -- 回填
        UPDATE `exam_question` eq
        JOIN `blog_category` c ON c.`name` COLLATE utf8mb4_unicode_ci = eq.`category`
        SET eq.`category_id` = c.`id`
        WHERE eq.`category_id` IS NULL AND eq.`category` IS NOT NULL;

        -- 无残留才删除旧列
        IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE()
                     AND TABLE_NAME = 'exam_question' AND COLUMN_NAME = 'category')
           AND NOT EXISTS (SELECT 1 FROM `exam_question`
                           WHERE `category` IS NOT NULL AND `category_id` IS NULL) THEN
            ALTER TABLE `exam_question` DROP COLUMN `category`;
        END IF;

        CALL `drop_index_if_exists`('exam_question', 'idx_category');
    END IF;
END$$
DELIMITER ;

CALL `upgrade_legacy_question_tables`();

-- 清理临时存储过程
DROP PROCEDURE IF EXISTS `add_column_if_not_exists`;
DROP PROCEDURE IF EXISTS `drop_index_if_exists`;
DROP PROCEDURE IF EXISTS `upgrade_legacy_question_tables`;

-- ------------------------------------------------------------
-- 4. 示例数据（可选，用于本地演示）
-- ------------------------------------------------------------

-- 分类/标签：先确保 blog_category / blog_tag 中存在对应记录（幂等）
INSERT INTO `blog_category` (`name`, `sort`)
SELECT c.`name`, c.`sort` FROM (
    SELECT '后端' AS `name`, 1 AS `sort` UNION ALL
    SELECT '数据库', 3 UNION ALL
    SELECT '前端', 2 UNION ALL
    SELECT '算法', 5 UNION ALL
    SELECT 'Spring', 10 UNION ALL
    SELECT 'Redis', 11 UNION ALL
    SELECT '计算机网络', 12
) c
WHERE NOT EXISTS (SELECT 1 FROM `blog_category` bc WHERE bc.`name` COLLATE utf8mb4_unicode_ci = c.`name`);

INSERT INTO `blog_tag` (`name`)
SELECT t.`name` FROM (
    SELECT 'Java' AS `name` UNION ALL
    SELECT '基础' UNION ALL
    SELECT '集合' UNION ALL
    SELECT 'MySQL' UNION ALL
    SELECT '事务' UNION ALL
    SELECT 'Vue' UNION ALL
    SELECT '响应式' UNION ALL
    SELECT '算法' UNION ALL
    SELECT '设计'
) t
WHERE NOT EXISTS (SELECT 1 FROM `blog_tag` g WHERE g.`name` COLLATE utf8mb4_unicode_ci = t.`name`);

INSERT INTO `interview_question` (`category_id`, `difficulty`, `title`, `answer`, `status`) VALUES
((SELECT id FROM `blog_category` WHERE `name` = '后端' LIMIT 1), '简单', '什么是 JVM 的自动装箱与拆箱？', '自动装箱是编译器将基本类型自动包装为对应包装类（int → Integer），拆箱则相反。频繁装箱可能引发性能问题，建议使用缓存池（-128~127）。', 1),
((SELECT id FROM `blog_category` WHERE `name` = '后端' LIMIT 1), '中等', 'HashMap 在 JDK 1.8 中的底层实现？', '数组 + 链表 + 红黑树。链表长度超过 8 且数组长度 ≥ 64 时树化；扩容阈值 0.75，容量为 2 的幂。', 1),
((SELECT id FROM `blog_category` WHERE `name` = '数据库' LIMIT 1), '中等', 'InnoDB 的 MVCC 机制是如何实现的？', '通过隐藏列 trx_id、roll_pointer 与 undo log 版本链实现，Read View 决定可见性。RC 每次查询生成 Read View，RR 只在第一次生成。', 1),
((SELECT id FROM `blog_category` WHERE `name` = '前端' LIMIT 1), '简单', 'Vue 的响应式原理是什么？', 'Vue 3 使用 Proxy 拦截对象读写，收集依赖并在 set 时触发更新。', 1),
((SELECT id FROM `blog_category` WHERE `name` = '算法' LIMIT 1), '困难', '手写 LRU 缓存（O(1) 复杂度）', 'HashMap + 双向链表：HashMap 定位节点，链表维护访问顺序，头插最新、尾删最旧。', 1);

-- 面试题-标签关联（幂等，按标题匹配）
INSERT INTO `interview_question_tag` (`question_id`, `tag_id`)
SELECT q.`id`, g.`id`
FROM `interview_question` q
JOIN `blog_tag` g ON FIND_IN_SET(g.`name`, 'Java,基础,集合,MySQL,事务,Vue,响应式,算法,设计')
WHERE (
    (q.`title` LIKE '什么是 JVM 的自动装箱与拆箱%' AND g.`name` IN ('Java', '基础')) OR
    (q.`title` LIKE 'HashMap 在 JDK 1.8%' AND g.`name` IN ('Java', '集合')) OR
    (q.`title` LIKE 'InnoDB 的 MVCC%' AND g.`name` IN ('MySQL', '事务')) OR
    (q.`title` LIKE 'Vue 的响应式原理%' AND g.`name` IN ('Vue', '响应式')) OR
    (q.`title` LIKE '手写 LRU 缓存%' AND g.`name` IN ('算法', '设计'))
)
AND NOT EXISTS (
    SELECT 1 FROM `interview_question_tag` qt
    WHERE qt.`question_id` = q.`id` AND qt.`tag_id` = g.`id`
);

INSERT INTO `exam_question` (`stem`, `type`, `category_id`, `difficulty`, `options`, `correct`, `reference_answer`, `score`, `status`) VALUES
('Spring Boot 的自动装配主要依赖哪个注解？', 1, (SELECT id FROM `blog_category` WHERE `name` = 'Spring' LIMIT 1), '简单', '["A. @ComponentScan","B. @EnableAutoConfiguration","C. @SpringBootApplication 扫描","D. @Conditional"]', '[1]', '自动装配核心是 @EnableAutoConfiguration 导入的 AutoConfigurationImportSelector。', 2, 1),
('下列属于 MySQL 索引失效场景的有？', 2, (SELECT id FROM `blog_category` WHERE `name` = '数据库' LIMIT 1), '中等', '["A. 对索引列使用函数","B. LIKE 以 % 开头","C. 联合索引最左前缀匹配","D. 隐式类型转换"]', '[0,1,3]', 'A/B/D 均会导致索引失效；C 是正确使用方式。', 4, 1),
('Redis 是单线程处理命令的。', 3, (SELECT id FROM `blog_category` WHERE `name` = 'Redis' LIMIT 1), '简单', '["A. 对","B. 错"]', '[0]', '命令执行单线程（6.0 后 IO 多线程），避免锁竞争。', 2, 1),
('HTTP 状态码 401 表示______。', 4, (SELECT id FROM `blog_category` WHERE `name` = '计算机网络' LIMIT 1), '简单', NULL, '["未授权(Unauthorized)"]', '401 表示请求未经授权，需要身份认证。', 2, 1),
('简述 TCP 三次握手过程及原因。', 5, (SELECT id FROM `blog_category` WHERE `name` = '计算机网络' LIMIT 1), '中等', NULL, NULL, 'SYN → SYN+ACK → ACK；双方确认收发能力，防止失效连接请求。', 10, 1),
('实现一个函数，判断字符串是否为回文（忽略大小写与非字母数字字符）。', 6, (SELECT id FROM `blog_category` WHERE `name` = '算法' LIMIT 1), '中等', NULL, NULL, '双指针首尾向中间移动，跳过非字母数字字符并统一小写比较，O(n)。', 10, 1);

INSERT INTO `exam_paper` (`title`, `description`, `total_score`, `duration`, `status`)
SELECT 'Java 全栈能力测试卷', '涵盖 Spring、MySQL、Redis、网络与算法的综合测试', 30, 45, 1
WHERE NOT EXISTS (SELECT 1 FROM `exam_paper` WHERE `title` COLLATE utf8mb4_unicode_ci = 'Java 全栈能力测试卷');

INSERT INTO `exam_paper_question` (`paper_id`, `question_id`, `sort_order`, `score`)
SELECT p.id, q.id, q.id, q.score
FROM `exam_paper` p, `exam_question` q
WHERE p.title COLLATE utf8mb4_unicode_ci = 'Java 全栈能力测试卷'
  AND q.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM `exam_paper_question` pq
      WHERE pq.paper_id = p.id AND pq.question_id = q.id
  );

INSERT INTO `music_playlist` (`name`, `description`, `status`)
SELECT m.name, m.description, m.status FROM (
    SELECT '专注编程' AS name, '适合敲代码的纯音乐歌单' AS description, 1 AS status
    UNION ALL
    SELECT '轻松午后', '午后放松轻音乐', 1
) m
WHERE NOT EXISTS (SELECT 1 FROM `music_playlist` p WHERE p.name COLLATE utf8mb4_unicode_ci = m.name);
