# 门户端登录/注册入口 + 刷题/考试/简历登录限制

## Summary
部署后看不到登录/注册，根因是 AppHeader.vue 从未加入口。本计划：1) Header 加登录/注册入口与用户名/退出；2) 刷题、考试（列表+答题+成绩）、简历相关页面要求登录，未登录重定向 /login（带 redirect 回跳）。

## Current State Analysis
- 路由 `src/router/index.js`（见 104-111 行 router 定义）：无守卫。涉及路由：
  - 刷题：`/interview`（L65）
  - 考试：`/exam`（L76）、`/exam/:paperId`（L97）、`/scores`（L81）
  - 简历：`/profile/resume`（L55，编辑）、`/resume`（L45）、`/resume/:userId`（L50，公开简历页）
- `src/stores/user.js`：token/userInfo + setUser；无 logout
- `src/components/AppHeader.vue:9-34`：header-right 仅搜索框，无登录区；「我的简历」已 v-if="userStore.token"
- Login.vue 已支持 `route.query.redirect` 回跳
- 博客/音乐/留言/关于保持公开浏览

## Proposed Changes

### 1. AppHeader.vue（入口）
- 未登录（`!userStore.token`）：搜索框右侧「登录」（/login）「注册」（/register）链接，沿用现有导航样式
- 已登录：昵称/用户名 + 「退出」按钮（调 userStore.logout()）

### 2. stores/user.js
新增 `logout()`：清空 token/userInfo 并移除 localStorage（token、userInfo）

### 3. router/index.js（登录限制）
- 加 `meta: { requiresAuth: true }`：`/interview`、`/exam`、`/exam/:paperId`（与已有 hideLayout 合并）、`/scores`、`/profile/resume`、`/resume`、`/resume/:userId`
- `router.beforeEach`：`to.meta.requiresAuth && !localStorage.getItem('token')` 时 `next('/login?redirect=' + encodeURIComponent(to.fullPath))`，否则放行
- 登录成功后 Login.vue 按 redirect 回跳（已实现）

## Assumptions & Decisions
- 简历页（含公开页 /resume/:userId）按用户要求全部登录后可见
- 首页/博客/音乐/留言/关于保持公开
- 退出为纯前端清 token；不新增用户菜单/头像

## Verification
1. `npm run build` 通过
2. 未登录访问 /interview、/exam、/exam/1、/scores、/resume、/profile/resume → 跳 /login，登录后回跳原页；公开页（/、/articles、/music）不受影响
3. 登录后 Header 显示用户名/退出，退出后恢复登录/注册入口
4. 服务器 `git pull && docker compose up -d --build blog-frontend` 后生效
