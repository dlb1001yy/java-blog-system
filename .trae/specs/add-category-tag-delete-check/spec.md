# 分类/标签删除关联校验 Spec

## Why
当前删除分类或标签时直接物理删除，不检查是否被文章引用。被引用的分类删除后文章 `categoryId` 悬空，标签删除后 `blog_article_tag` 中间表残留脏数据。

## What Changes
- 后端在删除分类前校验：若存在文章引用该分类（`blog_article.category_id`），拒绝删除并返回引用计数错误信息。
- 后端在删除标签前校验：若存在文章标签关联（`blog_article_tag.tag_id`），拒绝删除并返回引用计数错误信息。
- 单个删除与批量删除均执行上述校验。
- 前端 CategoryList.vue / TagList.vue 删除时展示后端返回的具体错误提示（如"该分类下有 N 篇文章，无法删除"）。

## Impact
- Affected code:
  - 后端：`AdminCategoryController.java`、`AdminTagController.java`、`CategoryServiceImpl.java`、`TagServiceImpl.java`（或新增校验逻辑）、`ArticleMapper`/`ArticleTagMapper` 查询
  - 前端：`blog-admin/src/views/CategoryList.vue`、`TagList.vue` 错误提示处理

## ADDED Requirements

### Requirement: 分类删除关联校验
系统在删除分类前 SHALL 检查该分类是否被文章引用。

#### Scenario: 分类被文章引用
- **WHEN** 管理员删除一个被 N（N>0）篇文章引用的分类（单个或批量）
- **THEN** 删除被拒绝，返回错误信息"该分类下存在 N 篇文章，无法删除"（批量时列出所有被引用分类或提示哪些不可删除，且整批拒绝或仅删除未被引用的——采用整批拒绝并提示）

#### Scenario: 分类未被引用
- **WHEN** 管理员删除未被任何文章引用的分类
- **THEN** 删除成功

### Requirement: 标签删除关联校验
系统在删除标签前 SHALL 检查 `blog_article_tag` 中是否存在该标签的关联记录。

#### Scenario: 标签被文章关联
- **WHEN** 管理员删除被 N 条文章标签关联引用的标签
- **THEN** 删除被拒绝，返回错误信息"该标签已被 N 篇文章使用，无法删除"

#### Scenario: 标签未被关联
- **WHEN** 管理员删除未被任何文章使用的标签
- **THEN** 删除成功

### Requirement: 前端错误提示
- **WHEN** 删除被后端拒绝
- **THEN** 前端弹出具体错误信息（沿用现有 request.js 的错误处理或 ElMessage 展示后端 message）
