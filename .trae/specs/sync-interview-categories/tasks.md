# Tasks
- [x] Task 1: 后端新增方向列表接口
  - [x] InterviewQuestionService(+Impl) 新增 `listEnabledCategories()`：查询启用状态题目去重 category
  - [x] PortalInterviewController 新增 `GET /portal/interview/categories`（公开访问，与该控制器其他端点权限一致）
- [x] Task 2: 前台接入
  - [x] blog-frontend/src/api/interview.js 新增 `getCategories()`
  - [x] Interview.vue：`categories` 改为 ref，onMounted 拉取，失败回退默认五项
- [ ] Task 3: 验证（不执行 mvn/npm，用户手动）
  - [x] 按 checklist.md 核对
  - [ ] 用户手动执行 mvn compile / npm run build

# Task Dependencies
- Task 2 依赖 Task 1 的接口路径
