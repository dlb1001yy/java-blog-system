-- ============================================================
-- 需求补齐增量脚本（EduHub-Plus 需求文档差距项）
-- 面试题 tips / 试卷及格线 / 答卷作弊标记 / 歌曲歌词
-- 执行方式：mysql --default-character-set=utf8mb4 dlbyy_zp_blog < 本文件
-- ============================================================

USE `dlbyy_zp_blog`;

ALTER TABLE `interview_question`
    ADD COLUMN `tips` longtext COMMENT '解题思路/拓展（可选，Markdown）' AFTER `answer`;

ALTER TABLE `exam_paper`
    ADD COLUMN `pass_score` decimal(6,1) NOT NULL DEFAULT 60 COMMENT '及格线' AFTER `total_score`;

ALTER TABLE `exam_record`
    ADD COLUMN `cheat_flag` tinyint NOT NULL DEFAULT 0 COMMENT '作弊标记 0:正常 1:切屏超限' AFTER `switch_count`;

ALTER TABLE `music_song`
    ADD COLUMN `lyric` longtext COMMENT '歌词 LRC 文本（可选）' AFTER `file_url`;

-- 示例数据补充：为已有面试题附解题思路、为试卷设置及格线
UPDATE `interview_question` SET `tips` = '面试时建议先说定义，再结合场景（缓存池、树化阈值、Read View 时机）展开，最后提实践注意点。' WHERE `id` = 1;
UPDATE `exam_paper` SET `pass_score` = 18 WHERE `title` = 'Java 全栈能力测试卷';
