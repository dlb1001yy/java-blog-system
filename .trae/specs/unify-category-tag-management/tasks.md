# Tasks

- [x] Task 1: 初始化 SQL `sql/09-init-interview-categories.sql`：将面试题 5 个硬编码技术方向（后端/前端/数据库/DevOps/算法）插入 `blog_category`（不存在时）。
- [x] Task 2: 后端 getOrCreate 工具逻辑：
  - [x] 2.1 `TagService` 增加 getOrCreateByName（返回标签）
  - [x] 2.2 `CategoryService` 增加 getOrCreateByName
- [x] Task 3: 面试题保存/导入自动落库：`InterviewQuestionServiceImpl` 保存与批量导入时，技术方向 getOrCreate 到 blog_category，标签逐个 getOrCreate 到 blog_tag。
- [x] Task 4: 试题保存/导入自动落库：`ExamQuestionServiceImpl` 保存与 Excel 导入中，分类不存在时由报错改为 getOrCreate 到 blog_category。
- [x] Task 5: 文章链路自动落库：
  - [x] 5.1 `ArticleServiceImpl.saveArticleWithTags`：tagIds 中为字符串名称的项 getOrCreate 后替换为 id
  - [x] 5.2 `MarkdownImportService`：支持解析元信息中分类/标签，自动新增并关联
- [x] Task 6: admin 前端面试题页（InterviewQuestionList.vue）：技术方向下拉改为加载分类接口；标签改为标签接口多选 allow-create（仍提交逗号分隔名称）。
- [x] Task 7: 验证：后端编译通过（GraalVM JDK 21，mvn compile 成功）；核对导入新分类/标签自动出现在分类/标签管理列表。

# Task Dependencies
- Task 3、4、5 依赖 Task 2
- Task 6 可与 Task 3-5 并行
- Task 7 依赖全部
