# 面试题/题库分类标签改存 ID Spec

## Why
面试题（interview_question）与题库（exam_question）目前分类 category 存的是名称字符串、tags 存名称逗号串。分类/标签改名后数据失联，且无法利用 id 关联与校验。

## What Changes
- **BREAKING** `interview_question` 表：`category`(varchar) 改为 `category_id`(bigint)，`tags` 改为关联表 `interview_question_tag`(question_id, tag_id)（参考 `blog_article_tag` 模式）；保留 name 数据迁移 SQL。
- **BREAKING** `exam_question` 表：`category` 改为 `category_id`；如 tags 字段存在则同样处理（按实际调研 exam_question 无 tags，仅 category）。
- 后端实体 InterviewQuestion/ExamQuestion 字段改 id；Service 保存（含新增/修改/导入）时按 id 校验存在性（前端仍传 id；Markdown 导入中解析到的 name 由后端 `getOrCreateByName` 转为 id）。
- 查询接口（管理端分页筛选、前台 Portal 列表筛选）参数由名称改为分类 id；返回时补充/转换为名称以便展示（列表返回 category 名称字段，如 categoryName/tags 名称数组）。
- 前端 blog-admin：InterviewQuestionList.vue、ExamQuestionList.vue 表单/筛选下拉 value 改为 id，回显与展示改用返回的名称字段。
- 前台门户（blog-app / blog-frontend）若调用面试题方向筛选接口，同步改为传 id（由子代理调研确认）。
- 标签多选改为从已有标签列表选择（value=id），导入解析出的新标签名由后端自动创建。

## Impact
- Affected code:
  - SQL：`03-multi_modules.sql` 及新增迁移脚本（interview_question、exam_question 结构变更 + 数据迁移 name→id）
  - 后端：InterviewQuestion/ExamQuestion 实体、AdminInterviewQuestionController、AdminExamQuestionController、InterviewQuestionServiceImpl、ExamQuestionService、PortalInterviewController、PortalExamController、新增 InterviewQuestionTag 实体/Mapper
  - 前端 blog-admin：InterviewQuestionList.vue、ExamQuestionList.vue
  - 前台门户调用处（待调研确认）

## ADDED Requirements

### Requirement: 面试题分类/标签按 ID 存储
interview_question SHALL 以 category_id 引用 blog_category，以关联表 interview_question_tag 引用 blog_tag。

#### Scenario: 新增/修改面试题（传 id）
- **WHEN** 管理员保存面试题，提交 categoryId 与 tagIds
- **THEN** 后端校验 id 存在（分类不存在则报错；标签 id 必须存在），按 id 落库

#### Scenario: Markdown 导入（文本中是名称）
- **WHEN** 导入内容解析出技术方向/标签为名称
- **THEN** 后端按名称查分类/标签，不存在则自动创建，转换为 id 存储

### Requirement: 题库（exam_question）分类按 ID 存储
exam_question SHALL 以 category_id 引用 blog_category，保存与筛选均按 id；展示返回名称。

### Requirement: 查询与展示兼容
- 管理/前台分页筛选参数改为 categoryId（或同时兼容 name 一段时间——不保留，直接切换）
- 列表接口返回中包含分类名称（categoryName）与标签名称数组（tagNames），供前端直接展示

### Requirement: 前端表单/筛选使用 ID
- InterviewQuestionList.vue、ExamQuestionList.vue 的方向下拉与标签多选 value 为 id；编辑回显用 id；列表展示用返回名称
