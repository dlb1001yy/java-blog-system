# Checklist

## 后端接口
- [x] `PortalArticleController` 新增 `search` 方法，参数 keyword/current/size，返回已发布文章分页
- [x] `PortalArticleController` 新增 `related` 方法，返回同分类相关文章最多 3 篇
- [x] 新建 `PortalStatsController`，`GET /portal/stats` 返回 articleCount/viewCount/tagCount/categoryCount
- [x] `PortalMessageController` 新增 `list` 方法，返回 status=1 的留言
- [x] 新建 `UserController`，`GET /user/info` 鉴权后返回当前用户信息
- [x] `SecurityConfig` 中 `/user/**` 改为 authenticated，其余配置不变

## 前端基础设施
- [x] 新建 `common/theme.js` 集中定义 colors/spacing/radii/shadows/fontSize
- [x] `uni.scss` 同步暴露 scss 变量
- [x] `common/api.js` 新增 searchArticles/getRelatedArticles/getStats/getMessages/getUserInfo 方法

## 前端组件
- [x] `ArticleItem.vue` 使用现代卡片样式（柔和阴影、12px 圆角、胶囊徽章、SVG 图标）
- [x] 新建 `Skeleton.vue` 支持 article 与 detail 两种预设
- [x] 新建 `Icon.vue` 内置至少 10 个常用 SVG 图标
- [x] `TabBar.vue` 高度 56px、激活态顶部圆点指示器
- [x] 新建 `SearchBar.vue` 圆角输入框 + 搜索图标
- [x] 新建 `CategoryChips.vue` 横向滚动分类 chips

## 前端页面
- [x] `pages/index/index.vue`：hero 区 + 搜索栏 + 分类 chips + 类型 chips + 文章列表 + 骨架屏
- [x] `pages/article/detail.vue`：现代排版 + 浮动点赞 + 相关文章 + 现代评论卡片
- [x] `pages/resume/index.vue`：hero 卡片 + 技能标签云 + 卡片化时间线 + 项目卡片
- [x] `pages/mine/index.vue`：渐变 hero + 统计网格 + SVG 图标菜单
- [x] `pages/mine/login.vue`：mesh gradient 背景 + 玻璃拟态登录卡片

## 设计一致性
- [x] 所有页面统一使用 theme.js 中的颜色变量，不再硬编码 #667eea/#764ba2
- [x] 所有页面统一使用 theme.js 中的圆角与阴影
- [x] 所有 emoji 图标（📄ℹ️📱📧📍👁📁❤👤💻💼🎓）替换为 SVG 图标
- [x] 所有页面底部留出 TabBar 高度（56px + safe-area）

## 功能验证
- [x] 首页可搜索文章、可按分类与类型筛选、骨架屏正常显示
- [x] 文章详情页点赞按钮可点击、相关文章区块显示、评论正常加载
- [x] 简历页头像、技能、时间线、项目卡片正常渲染
- [x] 我的页统计数据显示、菜单可点击、退出登录可用
- [x] 登录页可正常登录并返回上一页
- [x] TabBar 三个 tab 切换正常，激活态指示器显示正确
