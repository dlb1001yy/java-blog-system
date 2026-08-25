# 刷题页技术方向与管理端取值统一 Spec

## Why
前台刷题页（Interview.vue）技术方向是前端硬编码 `['后端','前端','数据库','DevOps','算法']`，而管理端面试题管理的方向选项来自 `blog_category` 表（动态），两边取值脱节：管理员可录入"Java基础/Spring框架"等方向，但前台筛选器永远选不到。

## What Changes
- 后端 PortalInterviewController 新增 `GET /portal/interview/categories`：返回面试题实际存在的方向列表（`SELECT DISTINCT category FROM interview_question WHERE status=1 AND category IS NOT NULL`，按名称排序），无需登录。
- 前台 Interview.vue：删除硬编码 `categories` 常量，改为页面加载时调用新接口获取方向列表（接口失败时回退到原硬编码五项，保证可用性）。
- blog-frontend/src/api/interview.js 新增 `getCategories()` 方法。
- 管理端不改（其选项仍来自 blog_category，用于录入）。

## Impact
- Affected specs: 无
- Affected code:
  - blog-backend：`controller/portal/PortalInterviewController.java`、`service/InterviewQuestionService(+Impl)`
  - blog-frontend：`src/api/interview.js`、`src/views/Interview.vue`

## ADDED Requirements
### Requirement: 刷题页技术方向动态获取
系统 SHALL 在前台刷题页展示的"技术方向"取值与题库实际数据一致。

#### Scenario: 正常加载
- **WHEN** 用户打开刷题页
- **THEN** 方向筛选列表来自 `/portal/interview/categories`（题库中已启用题目的去重方向）

#### Scenario: 接口异常降级
- **WHEN** 方向接口请求失败
- **THEN** 使用内置默认方向 ['后端','前端','数据库','DevOps','算法']，页面不报错
