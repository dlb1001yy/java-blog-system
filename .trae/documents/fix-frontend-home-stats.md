# 修复 blog-frontend 首页统计数据不显示

## Summary
首页 `Home.vue` 的 Banner 区有「文章数 / 浏览数 / 分类数」三个统计，但页面从未调用后端统计接口获取数据，导致一直显示 0。后端 `PortalStatsController` 已提供 `GET /api/portal/stats` 接口且字段齐全，仅需前端补上调用。

## Current State Analysis
- **后端**：`PortalStatsController.stats()` 返回 `{ articleCount, viewCount, tagCount, categoryCount }`，接口已就绪。
- **前端 `Home.vue`**：
  - 定义了 `stats = reactive({ articleCount: 0, totalViews: 0, categoryCount: 0 })`
  - 模板引用 `stats.articleCount` / `stats.totalViews` / `stats.categoryCount`
  - `onMounted` 只调用了 `fetchArticles()`，**没有任何获取统计数据的逻辑**
- **前端 `api/article.js`**：没有调用 `/portal/stats` 的方法。
- **字段名不一致**：前端用 `totalViews`，后端返回 `viewCount`。

## Proposed Changes

### 1. `blog-frontend/src/api/article.js`
在 `export default` 对象内（建议放在 `getArticles` 之后）新增方法：
```js
// 获取站点统计信息
getStats() {
  return request.get('/portal/stats')
}
```

### 2. `blog-frontend/src/views/Home.vue`
**模板**：把 `stats.totalViews` 改为 `stats.viewCount`，与后端字段对齐。
```html
<span class="stat-number">{{ stats.viewCount || 0 }}</span>
```

**script setup**：
- 把 `stats` reactive 的 `totalViews` 字段改为 `viewCount`
```js
const stats = reactive({
  articleCount: 0,
  viewCount: 0,
  categoryCount: 0
})
```
- 新增 `fetchStats` 函数并放在 `onMounted` 中调用
```js
const fetchStats = async () => {
  const res = await articleApi.getStats()
  Object.assign(stats, res.data)
}

onMounted(() => {
  fetchArticles()
  fetchStats()
})
```

## Assumptions & Decisions
- 后端 `GET /portal/stats` 在 `SecurityConfig` 中已 `permitAll`（`/portal/**` 放行），无需鉴权。
- 字段对齐选择「改前端跟随后端」而非「改后端跟随前端」，因为后端接口可能被其他端（如 blog-app）复用，且 `viewCount` 命名更准确。
- 不修改 `tagCount`（后端额外返回但首页未使用），保留供后续扩展。
- 接口失败时 `request.js` 拦截器已统一 `ElMessage.error`，`stats` 保持初始 0 值，不影响文章列表加载。

## Verification
1. 启动 `blog-frontend`（`npm run dev`）与后端
2. 打开首页，Banner 区三个数字应显示真实统计值（与数据库已发布文章数、浏览量总和、分类数一致）
3. 浏览器 Network 面板确认 `GET /api/portal/stats` 返回 200 且响应含 `articleCount/viewCount/categoryCount`
4. 控制台无报错
