# 分类/标签管理分页展示 Spec

## Why
admin 端分类管理（CategoryList.vue）与标签管理（TagList.vue）当前调用 `getAll()` 一次性加载全部数据，数据量增大后页面渲染慢、操作不便，需要改为与文章列表一致的分页展示。

## What Changes
- 后端：`AdminCategoryController` 新增 `GET /admin/categories/page` 分页接口（参数 current/size/name，按 sort 升序、createTime 降序排序），与已有的 `AdminTagController#page` 保持一致。
- admin 前端：
  - `CategoryList.vue`：`fetchData` 改用 `categoryApi.getPage`，新增名称搜索框与 `el-pagination` 分页条（样式与交互对齐 `ArticleList.vue`）。
  - `TagList.vue`：`fetchData` 改用 `tagApi.getPage`，新增名称搜索框与 `el-pagination` 分页条。
- 保留 `getAll()` 接口与前端 API 方法不变（文章编辑等处仍在使用）。

### 不做的（保持简单）
- 不改后端 `GET /admin/categories`、`GET /admin/tags` 全量接口。
- 不做分类/标签列表的排序字段筛选、时间范围筛选。
- 不改门户端（blog-frontend）逻辑。

## Impact
- Affected code:
  - 后端：`blog-backend/src/main/java/com/dlbyy/blog/controller/admin/AdminCategoryController.java`
  - admin 前端：`src/views/CategoryList.vue`、`src/views/TagList.vue`（API 层 `category.js`/`tag.js` 已有 `getPage`，无需改动）
- 兼容性：无破坏性变更；批量删除/编辑/新增后的刷新逻辑改为按当前页码刷新。

## ADDED Requirements

### Requirement: 分类分页查询接口
系统 SHALL 提供 `GET /admin/categories/page` 接口，支持 `current`（默认 1）、`size`（默认 10）、`name`（可选，模糊匹配）参数，返回 MyBatis-Plus `Page<Category>` 结构（records/total），排序为 sort 升序、createTime 降序。

#### Scenario: 分页查询分类
- **WHEN** 请求 `/admin/categories/page?current=1&size=10`
- **THEN** 返回第一页最多 10 条分类及总数 total

#### Scenario: 按名称模糊搜索
- **WHEN** 请求 `/admin/categories/page?name=Java`
- **THEN** 仅返回名称包含"Java"的分类

## MODIFIED Requirements

### Requirement: 分类管理列表展示
分类管理页 SHALL 以分页表格形式展示数据：支持按名称搜索、切换每页条数（10/20/50/100）、跳转页码；新增/编辑/删除/批量删除成功后刷新当前页。

#### Scenario: 分页浏览
- **WHEN** 分类总数超过每页条数
- **THEN** 表格下方显示分页条，可翻页查看

#### Scenario: 删除后刷新
- **WHEN** 删除当前页最后一条数据
- **THEN** 刷新后页码不越界（由后端分页兜底返回空页或前端回退上一页）

### Requirement: 标签管理列表展示
标签管理页 SHALL 以分页表格形式展示数据：支持按名称搜索、切换每页条数、跳转页码；复用已有 `GET /admin/tags/page` 接口（current/size/name）。

#### Scenario: 分页浏览标签
- **WHEN** 标签总数超过每页条数
- **THEN** 表格下方显示分页条，可翻页查看
