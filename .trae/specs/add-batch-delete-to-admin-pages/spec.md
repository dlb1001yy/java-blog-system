# 管理后台批量删除功能 Spec

## Why
管理后台多数列表页（分类、标签、友链、面试题、考试题目、试卷、歌曲、歌单等）只有单条删除，操作效率低；文章/评论/留言页面已有批量删除，但部分后端批量删除接口实际缺失，需要统一补齐前后端能力。

## What Changes
- 后端为以下实体新增批量删除接口 `DELETE /admin/{resource}/batch`（请求体 `{ ids: number[] }`）：
  - 分类 `/admin/categories/batch`
  - 标签 `/admin/tags/batch`
  - 友链 `/admin/links/batch`
  - 面试题 `/admin/interview-questions/batch`
  - 考试题目 `/admin/exam-questions/batch`
  - 试卷 `/admin/exam-papers/batch`
  - 留言 `/admin/messages/batch`
  - 文章 `/admin/articles/batch`（前端已调用但后端缺失，补齐）
  - 歌曲 `/admin/music/songs/batch`
  - 歌单 `/admin/music/playlists/batch`
- 前端对应 api 模块新增 `batchDelete(ids)` 方法（article/comment/message 已有则校对路径）。
- 前端以下列表页添加表格多选列（`type="selection"`）+「批量删除」按钮 + 二次确认 + 成功后刷新列表：
  - CategoryList、TagList、LinkList、MessageList、InterviewQuestionList、ExamQuestionList、ExamPaperList、MusicManage（歌曲、歌单两个表格）
  - ArticleList、CommentList 已具备，仅校验无需改动（除非接口路径不一致）
- 不涉及删除的页面（UserList、OperationLogList、ResumeManage、Dashboard 等）不在本次范围内。

## Impact
- Affected specs: 无冲突
- Affected code:
  - 后端：`blog-backend/src/main/java/com/dlbyy/blog/controller/admin/*Controller.java`（新增批量删除端点，复用现有 service `removeByIds`/`removeById` 循环）
  - 前端：`blog-admin/src/api/*.js`、`blog-admin/src/views/*List.vue`、`MusicManage.vue`

## ADDED Requirements
### Requirement: 批量删除
系统 SHALL 为支持删除的实体提供批量删除接口与前端批量删除操作。

#### Scenario: 批量删除成功
- **WHEN** 管理员在列表页勾选多条记录并点击「批量删除」，确认后
- **THEN** 后端删除对应记录，前端提示「批量删除成功」并刷新列表

#### Scenario: 未选择记录
- **WHEN** 未勾选任何记录
- **THEN** 「批量删除」按钮处于禁用状态

#### Scenario: 权限控制
- **WHEN** 批量删除请求到达后端
- **THEN** 与单条删除保持相同的 `@Admin` 权限校验
