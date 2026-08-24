-- ============================================================
-- 面试题模块初始化分类脚本
-- 执行方式：mysql --default-character-set=utf8mb4 dlbyy_zp_blog < 本文件
-- ============================================================

USE `dlbyy_zp_blog`;

-- 面试题五大技术方向分类（不存在才插入）
INSERT INTO `blog_category` (`name`, `sort`)
SELECT '后端', 1 WHERE NOT EXISTS (SELECT 1 FROM `blog_category` WHERE `name` = '后端');
INSERT INTO `blog_category` (`name`, `sort`)
SELECT '前端', 2 WHERE NOT EXISTS (SELECT 1 FROM `blog_category` WHERE `name` = '前端');
INSERT INTO `blog_category` (`name`, `sort`)
SELECT '数据库', 3 WHERE NOT EXISTS (SELECT 1 FROM `blog_category` WHERE `name` = '数据库');
INSERT INTO `blog_category` (`name`, `sort`)
SELECT 'DevOps', 4 WHERE NOT EXISTS (SELECT 1 FROM `blog_category` WHERE `name` = 'DevOps');
INSERT INTO `blog_category` (`name`, `sort`)
SELECT '算法', 5 WHERE NOT EXISTS (SELECT 1 FROM `blog_category` WHERE `name` = '算法');
