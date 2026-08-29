# Tasks

- [x] Task 1: 后端新增分类分页接口
  - [x] SubTask 1.1: 在 `AdminCategoryController` 中新增 `GET /admin/categories/page`（参数 current=1、size=10、name 可选模糊匹配），按 sort 升序、createTime 降序排序，返回 `Result<Page<Category>>`，写法对齐 `AdminTagController#page`
  - [x] SubTask 1.2: 编译验证 `mvn compile`（或等价构建）通过
- [x] Task 2: 前端分类管理页分页改造（CategoryList.vue）
  - [x] SubTask 2.1: `fetchData` 改用 `categoryApi.getPage`，取 `res.data.records` / `res.data.total`
  - [x] SubTask 2.2: 增加名称搜索框（搜索/重置）与 `el-pagination` 分页条（page-sizes 10/20/50/100，layout 含 total/sizes/prev/pager/next/jumper），交互与样式对齐 `ArticleList.vue`
  - [x] SubTask 2.3: 新增/编辑/删除/批量删除成功后刷新当前页并清理选中行
- [x] Task 3: 前端标签管理页分页改造（TagList.vue）
  - [x] SubTask 3.1: `fetchData` 改用 `tagApi.getPage`（接口已存在）
  - [x] SubTask 3.2: 增加名称搜索框与 `el-pagination` 分页条，交互与样式对齐 `ArticleList.vue`
  - [x] SubTask 3.3: 新增/编辑/删除/批量删除成功后刷新当前页并清理选中行
- [x] Task 4: 验证
  - [x] SubTask 4.1: `npm run build`（blog-admin）构建通过
  - [x] SubTask 4.2: 按 checklist.md 逐项核对

# Task Dependencies
- Task 2、Task 3 依赖 Task 1（分类分页接口）；Task 2 与 Task 3 可并行
- Task 4 依赖 Task 1-3
