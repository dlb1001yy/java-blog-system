-- ============================================================
-- 多功能模块 DDL (MySQL 8.0)
-- 面试刷题 / 在线考试 / 音乐播放 / 用户管理增强
-- 遵循现有规范：BaseEntity 字段（id/create_time/update_time/is_deleted）
-- ============================================================

USE `dlbyy_z`;

-- ------------------------------------------------------------
-- 1. 面试刷题模块
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `interview_question` (
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `category`    varchar(50)  NOT NULL COMMENT '技术方向：后端/前端/数据库/DevOps/算法',
    `difficulty`  varchar(20)  NOT NULL DEFAULT '中等' COMMENT '难度：简单/中等/困难',
    `title`       varchar(500) NOT NULL COMMENT '题目标题/题干',
    `tags`        varchar(200) DEFAULT NULL COMMENT '标签（逗号分隔）',
    `answer`      longtext     COMMENT '参考答案（支持 Markdown/代码块）',
    `status`      tinyint      NOT NULL DEFAULT 1 COMMENT '状态 0:停用 1:启用',
    `is_deleted`  tinyint      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0:正常 1:已删',
    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_category` (`category`),
    INDEX `idx_difficulty` (`difficulty`),
    INDEX `idx_deleted_status` (`is_deleted`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试题表';

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
    `category`         varchar(50)  DEFAULT NULL COMMENT '分类（知识领域）',
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
    INDEX `idx_category` (`category`),
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
-- 4. 示例数据（可选，用于本地演示）
-- ------------------------------------------------------------

INSERT INTO `interview_question` (`category`, `difficulty`, `title`, `tags`, `answer`, `status`) VALUES
('后端', '简单', '什么是 JVM 的自动装箱与拆箱？', 'Java,基础', '自动装箱是编译器将基本类型自动包装为对应包装类（int → Integer），拆箱则相反。频繁装箱可能引发性能问题，建议使用缓存池（-128~127）。', 1),
('后端', '中等', 'HashMap 在 JDK 1.8 中的底层实现？', 'Java,集合', '数组 + 链表 + 红黑树。链表长度超过 8 且数组长度 ≥ 64 时树化；扩容阈值 0.75，容量为 2 的幂。', 1),
('数据库', '中等', 'InnoDB 的 MVCC 机制是如何实现的？', 'MySQL,事务', '通过隐藏列 trx_id、roll_pointer 与 undo log 版本链实现，Read View 决定可见性。RC 每次查询生成 Read View，RR 只在第一次生成。', 1),
('前端', '简单', 'Vue 的响应式原理是什么？', 'Vue,响应式', 'Vue 3 使用 Proxy 拦截对象读写，收集依赖并在 set 时触发更新。', 1),
('算法', '困难', '手写 LRU 缓存（O(1) 复杂度）', '算法,设计', 'HashMap + 双向链表：HashMap 定位节点，链表维护访问顺序，头插最新、尾删最旧。', 1);

INSERT INTO `exam_question` (`stem`, `type`, `category`, `difficulty`, `options`, `correct`, `reference_answer`, `score`, `status`) VALUES
('Spring Boot 的自动装配主要依赖哪个注解？', 1, 'Spring', '简单', '["A. @ComponentScan","B. @EnableAutoConfiguration","C. @SpringBootApplication 扫描","D. @Conditional"]', '[1]', '自动装配核心是 @EnableAutoConfiguration 导入的 AutoConfigurationImportSelector。', 2, 1),
('下列属于 MySQL 索引失效场景的有？', 2, '数据库', '中等', '["A. 对索引列使用函数","B. LIKE 以 % 开头","C. 联合索引最左前缀匹配","D. 隐式类型转换"]', '[0,1,3]', 'A/B/D 均会导致索引失效；C 是正确使用方式。', 4, 1),
('Redis 是单线程处理命令的。', 3, 'Redis', '简单', '["A. 对","B. 错"]', '[0]', '命令执行单线程（6.0 后 IO 多线程），避免锁竞争。', 2, 1),
('HTTP 状态码 401 表示______。', 4, '计算机网络', '简单', NULL, '["未授权(Unauthorized)"]', '401 表示请求未经授权，需要身份认证。', 2, 1),
('简述 TCP 三次握手过程及原因。', 5, '计算机网络', '中等', NULL, NULL, 'SYN → SYN+ACK → ACK；双方确认收发能力，防止失效连接请求。', 10, 1),
('实现一个函数，判断字符串是否为回文（忽略大小写与非字母数字字符）。', 6, '算法', '中等', NULL, NULL, '双指针首尾向中间移动，跳过非字母数字字符并统一小写比较，O(n)。', 10, 1);

INSERT INTO `exam_paper` (`title`, `description`, `total_score`, `duration`, `status`) VALUES
('Java 全栈能力测试卷', '涵盖 Spring、MySQL、Redis、网络与算法的综合测试', 30, 45, 1);

INSERT INTO `exam_paper_question` (`paper_id`, `question_id`, `sort_order`, `score`)
SELECT p.id, q.id, q.id, q.score
FROM `exam_paper` p, `exam_question` q
WHERE p.title = 'Java 全栈能力测试卷' AND q.is_deleted = 0;

INSERT INTO `music_playlist` (`name`, `description`, `status`) VALUES
('专注编程', '适合敲代码的纯音乐歌单', 1),
('轻松午后', '午后放松轻音乐', 1);
