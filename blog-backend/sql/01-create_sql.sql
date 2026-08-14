-- 设置客户端连接字符集，防止中文乱码
SET NAMES utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS dlbyy_zp_blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE dlbyy_zp_blog;

-- 1. 用户表
CREATE TABLE `sys_user` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `username` varchar(50) NOT NULL COMMENT '用户名',
                            `password` varchar(100) NOT NULL COMMENT '密码',
                            `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
                            `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
                            `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
                            `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
                            `gender` tinyint DEFAULT 0 COMMENT '性别 0:未知 1:男 2:女',
                            `signature` varchar(255) DEFAULT NULL COMMENT '个性签名',
                            `role` varchar(20) DEFAULT 'user' COMMENT '角色 admin/user',
                            `status` tinyint DEFAULT 1 COMMENT '状态 0:禁用 1:正常',
                            `fail_count` int DEFAULT 0 COMMENT '连续登录失败次数（锁定辅助）',
                            `lock_until` datetime DEFAULT NULL COMMENT '账户锁定到期时间（NULL 表示未锁定）',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                            `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 文章分类表
CREATE TABLE `blog_category` (
                                 `id` bigint NOT NULL AUTO_INCREMENT,
                                 `name` varchar(50) NOT NULL COMMENT '分类名称',
                                 `sort` int DEFAULT 0 COMMENT '排序',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章分类表';

-- 3. 文章标签表
CREATE TABLE `blog_tag` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `name` varchar(50) NOT NULL COMMENT '标签名称',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签表';

-- 4. 文章表
CREATE TABLE `blog_article` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `user_id` bigint NOT NULL COMMENT '作者ID',
                                `category_id` bigint DEFAULT NULL COMMENT '分类ID',
                                `title` varchar(200) NOT NULL COMMENT '标题',
                                `summary` varchar(500) DEFAULT NULL COMMENT '摘要',
                                `content` longtext NOT NULL COMMENT '内容',
                                `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图',
                                `type` tinyint DEFAULT 0 COMMENT '类型 0:原创 1:转载 2:翻译',
                                `source_url` varchar(255) DEFAULT NULL COMMENT '原文链接(转载时)',
                                `source_name` varchar(100) DEFAULT NULL COMMENT '来源名称',
                                `view_count` int DEFAULT 0 COMMENT '浏览量',
                                `like_count` int DEFAULT 0 COMMENT '点赞数',
                                `comment_count` int DEFAULT 0 COMMENT '评论数',
                                `is_top` tinyint DEFAULT 0 COMMENT '是否置顶',
                                `is_publish` tinyint DEFAULT 0 COMMENT '是否发布 0:草稿 1:已发布',
                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`),
                                KEY `idx_user_id` (`user_id`),
                                KEY `idx_category_id` (`category_id`),
                                KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- 5. 文章标签关联表
CREATE TABLE `blog_article_tag` (
                                    `article_id` bigint NOT NULL,
                                    `tag_id` bigint NOT NULL,
                                    PRIMARY KEY (`article_id`, `tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签关联表';

-- 6. 评论表
CREATE TABLE `blog_comment` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `article_id` bigint NOT NULL COMMENT '文章ID',
                                `user_id` bigint DEFAULT NULL COMMENT '评论用户ID',
                                `nickname` varchar(50) NOT NULL COMMENT '昵称(游客)',
                                `email` varchar(100) DEFAULT NULL COMMENT '邮箱(游客)',
                                `content` text NOT NULL COMMENT '评论内容',
                                `parent_id` bigint DEFAULT 0 COMMENT '父评论ID',
                                `reply_to` varchar(50) DEFAULT NULL COMMENT '回复对象',
                                `status` tinyint DEFAULT 0 COMMENT '状态 0:待审核 1:通过 2:拒绝',
                                `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`),
                                KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 7. 留言表
CREATE TABLE `blog_message` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `nickname` varchar(50) NOT NULL COMMENT '昵称',
                                `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
                                `content` text NOT NULL COMMENT '留言内容',
                                `status` tinyint DEFAULT 0 COMMENT '状态 0:待审核 1:通过 2:拒绝',
                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='留言表';

-- 8. 简历信息表
CREATE TABLE `resume_info` (
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `name` varchar(50) NOT NULL COMMENT '姓名',
                               `job_title` varchar(100) DEFAULT NULL COMMENT '求职岗位',
                               `gender` tinyint DEFAULT 0 COMMENT '性别',
                               `birth_date` date DEFAULT NULL COMMENT '出生日期',
                               `phone` varchar(20) DEFAULT NULL COMMENT '电话',
                               `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
                               `address` varchar(255) DEFAULT NULL COMMENT '地址',
                               `avatar` varchar(255) DEFAULT NULL COMMENT '照片',
                               `summary` text COMMENT '个人简介',
                               `skills` text COMMENT '技能特长(JSON)',
                               `work_experience` text COMMENT '工作经历(JSON)',
                               `education` text COMMENT '教育背景(JSON)',
                               `projects` text COMMENT '项目经验(JSON)',
                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                               `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历信息表';

-- 9. 友情链接表
CREATE TABLE `blog_link` (
                             `id` bigint NOT NULL AUTO_INCREMENT,
                             `name` varchar(100) NOT NULL COMMENT '网站名称',
                             `url` varchar(255) NOT NULL COMMENT '网站地址',
                             `description` varchar(255) DEFAULT NULL COMMENT '描述',
                             `logo` varchar(255) DEFAULT NULL COMMENT 'Logo',
                             `sort` int DEFAULT 0,
                             `status` tinyint DEFAULT 1,
                             `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='友情链接表';

-- 10. 网站配置表
CREATE TABLE `sys_config` (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `config_key` varchar(100) NOT NULL COMMENT '配置键',
                              `config_value` text COMMENT '配置值',
                              `description` varchar(255) DEFAULT NULL,
                              `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 初始化管理员账号 (密码: admin123)
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `role`, `status`)
VALUES ('admin', '$2a$12$hXcsSy38q6OZE3WLA71IVOIMyIM0Xp/gLS.D8gZAB12v1rqtYuKD.', '管理员', 'admin', 1);

-- 初始化分类
INSERT INTO `blog_category` (`name`, `sort`) VALUES
                                                 ('Java基础', 1), ('Spring框架', 2), ('数据库', 3),
                                                 ('前端技术', 4), ('DevOps', 5), ('面试总结', 6);

-- 简历信息表扩展字段（增量更新，旧数据不受影响）
ALTER TABLE `resume_info`
    ADD COLUMN `marital_status` tinyint DEFAULT NULL COMMENT '婚姻状况(0未婚 1已婚 2离异)' AFTER `gender`,
    ADD COLUMN `work_years` int DEFAULT NULL COMMENT '工作年限' AFTER `marital_status`,
    ADD COLUMN `expected_salary` varchar(50) DEFAULT NULL COMMENT '期望薪资' AFTER `work_years`,
    ADD COLUMN `highest_education` varchar(20) DEFAULT NULL COMMENT '最高学历' AFTER `expected_salary`,
    ADD COLUMN `job_search_status` tinyint DEFAULT NULL COMMENT '求职状态(0离职-随时到岗 1在职-暂不流动 2在职-考虑机会)' AFTER `highest_education`,
    ADD COLUMN `hukou` varchar(100) DEFAULT NULL COMMENT '户籍所在地' AFTER `job_search_status`,
    ADD COLUMN `self_evaluation` text COMMENT '自我评价' AFTER `summary`,
    ADD COLUMN `certificates` text COMMENT '证书荣誉(JSON数组)' AFTER `projects`,
    ADD COLUMN `interests` varchar(500) DEFAULT NULL COMMENT '兴趣爱好' AFTER `certificates`;