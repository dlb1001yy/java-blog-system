# 统一分类/标签管理 Spec

## Why
文章、面试题、题库三个模块的分类/标签管理方式不一致：文章用 id 关联 `blog_category`/`blog_tag` 表，面试题用硬编码技术方向 + 自由文本标签，题库分类按名称字符串复用 category 表。且各导入流程（文章 Markdown、面试题 Markdown、试题 Excel）导入的分类/标签不会自动落到分类/标签管理表中，无法统一维护。三个模块的分类/标签不冲突，共用同一套 `blog_category`/`blog_tag` 表，不做模块区分。

## What Changes
- 题库管理（ExamQuestionList.vue）：分类下拉改为按名称匹配 `blog_category`（现有已复用，保持不变，值仍存名称）。
- 面试题管理（InterviewQuestionList.vue）：技术方向下拉改为从分类接口动态加载（移除硬编码 5 项）；标签改为基于 `blog_tag` 的多选下拉（allow-create，仍存逗号分隔名称字符串，字段存储不变）。
- 后端 getOrCreate 逻辑（按名称，全模块共用）：
  - 面试题保存/导入：技术方向、标签名称若在对应表中不存在，自动新增到 `blog_category` / `blog_tag`。
  - 试题 Excel 导入：分类不存在时由"报错拒绝"改为自动新增到 `blog_category`；保存/编辑同样 getOrCreate。
  - 文章保存时 `tagIds` 中混入的新建标签名称字符串（allow-create）由后端 getOrCreate 落库 `blog_tag` 并正确关联。
  - 文章 Markdown 导入（MarkdownImportService）：支持解析 front-matter/首行元信息中的分类与标签，不存在则自动新增并关联。
- 提供初始化 SQL（09 号）：将面试题硬编码的 5 个技术方向（后端/前端/数据库/DevOps/算法）初始化进 `blog_category`（若不存在）。

### 不做的（保持简单）
- 不给 `blog_category` 增加 type 字段，不做模块区分，三模块共用同一套分类/标签。
- 不改动 `interview_question.category/tags`、`exam_question.category` 的存储结构（仍为名称字符串，不做外键/中间表改造）。
- 不改动 blog-frontend 门户端展示逻辑（按名称读取不受影响）。

## Impact
- Affected code:
  - 后端：`CategoryService(Impl)`、`TagService(Impl)`、`InterviewQuestionService(Impl)`、`ExamQuestionServiceImpl`、`ArticleService(Impl)`、`MarkdownImportService`
  - SQL：新增 `blog-backend/sql/09-init-interview-categories.sql`
  - admin 前端：`InterviewQuestionList.vue`、`ExamQuestionList.vue`
- 兼容性：存量文章分类、试题分类不受影响；现有面试题/试题数据仍按名称匹配展示。

## ADDED Requirements

### Requirement: 导入时自动新增分类/标签
系统 SHALL 在以下场景对不存在的分类/标签自动新增到 `blog_category` / `blog_tag`，而不是报错或丢弃：
- 试题 Excel 导入/保存
- 面试题保存/Markdown 导入：技术方向 → `blog_category`；标签 → `blog_tag`
- 文章保存（allow-create 标签）与文章 Markdown 导入（front-matter 分类/标签）

#### Scenario: 试题导入新分类
- **WHEN** Excel 中某题分类"JVM 调优"在 `blog_category` 中不存在
- **THEN** 系统自动新建该分类并成功导入，分类管理中可见

#### Scenario: 面试题导入新标签
- **WHEN** Markdown 导入的面试题标签含"Spring"而 `blog_tag` 无此标签
- **THEN** 系统自动新建标签"Spring"，标签管理中可见

### Requirement: 面试题表单选项动态化
面试题管理的技术方向下拉 SHALL 从分类接口加载；标签 SHALL 从标签接口加载并可创建新标签。

## MODIFIED Requirements

### Requirement: 面试题技术方向
技术方向选项 SHALL 不再前端硬编码，改为统一从分类管理维护，新方向可在导入或分类管理中产生。
