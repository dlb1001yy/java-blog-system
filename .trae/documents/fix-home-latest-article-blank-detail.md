# 修复：首页最新文章点击后详情页空白

## 概述

首页"最新文章"卡片点击后跳转到详情页面显示空白。根因是跳转路径与路由定义不匹配：卡片跳转 `/articles/{id}`（复数），而路由表只注册了 `/article/:id`（单数）。vue-router 匹配不到路由，`<router-view>` 渲染为空，页面呈现空白。

## 现状分析

- 路由定义（正确）：[blog-frontend/src/router/index.js#L15-L18](d:\my-project\java-blog-system\blog-frontend\src\router\index.js)
  - `/article/:id` → `ArticleDetail.vue`
- 首页最新文章卡片（错误）：[blog-frontend/src/components/LatestBlogCard.vue#L2](d:\my-project\java-blog-system\blog-frontend\src\components\LatestBlogCard.vue)
  ```vue
  <div class="latest-card card" @click="$router.push(`/articles/${article.id}`)">
  ```
  使用了复数 `articles`，无匹配路由 → 空白。
- 对比：文章列表页卡片 [blog-frontend/src/components/ArticleCard.vue#L88](d:\my-project\java-blog-system\blog-frontend\src\components\ArticleCard.vue) 使用 `/article/${id}`（单数），跳转正常，验证了根因。
- 后端接口 `/portal/articles/{id}` 存在且正常（`PortalArticleController.detail`），无需改动。

## 修改方案

**文件：`blog-frontend/src/components/LatestBlogCard.vue`（第 2 行）**

将：
```vue
<div class="latest-card card" @click="$router.push(`/articles/${article.id}`)">
```
改为：
```vue
<div class="latest-card card" @click="$router.push(`/article/${article.id}`)">
```

仅此一处改动，与 ArticleCard 的跳转路径保持一致。

## 假设与决策

- 不新增 `/articles/:id` 路由做兼容——项目内正确约定是 `/article/:id`（ArticleCard、ArticleDetail 的上一篇/下一篇均使用单数），改卡片跳转路径即可。
- 无需改动后端。

## 验证步骤

1. 启动前端 `npm run dev`（及后端服务）。
2. 打开首页，点击"最新文章"任一卡片。
3. 确认成功跳转到 `/blog/article/{id}` 且文章详情（标题、正文、目录、评论）正常渲染。
4. 顺带回归：文章列表页点击卡片跳详情仍正常；详情页内"上一篇/下一篇"跳转正常。
