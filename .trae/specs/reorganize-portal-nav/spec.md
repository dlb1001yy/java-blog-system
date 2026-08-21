# 门户端导航结构重组 Spec

## Why
当前头部导航项过多（首页/文章/分类/标签/归档/刷题/考试/成绩/音乐/简历/留言/关于），需要精简头部：分类/标签/归档移出导航，留言与关于合并为新菜单，并合理布局被迁移的内容。

## What Changes
- 头部导航移除「分类」「标签」「归档」三个菜单项
- 文章列表页（ArticleList.vue）右侧栏：标签模块下方新增「归档」模块（按年月分组的文章统计+跳转）
- 头部「留言」「关于」合并为一个新菜单「关于站点」（/about-site），新页面承载：留言板 + 关于我 两个区块，合理布局与样式
- **BREAKING**：`/category`、`/tags`、`/archives` 独立页面路由保留还是删除？——保留路由（用户可能书签/文章内链跳转），仅头部不显示入口，避免死链
- 路由 `/message-board` 与 `/about` 保留（老链接可访问），新页面为聚合页

## Impact
- Affected specs: 门户端导航、文章列表布局
- Affected code: `blog-frontend/src/components/AppHeader.vue`、`src/views/ArticleList.vue`（右侧栏）、新增 `src/views/AboutSite.vue`、`src/router/index.js`

## ADDED Requirements
### Requirement: 文章页右侧归档模块
文章列表页右侧栏标签模块下方 SHALL 展示归档（按年/月分组，显示每月文章数，点击跳转归档筛选）。

#### Scenario: Success case
- **WHEN** 用户进入文章列表页
- **THEN** 右侧栏显示标签，其下方显示归档模块，样式与现有卡片一致

### Requirement: 关于站点聚合页
新菜单「关于站点」（/about-site）SHALL 在单页内合理布局「留言板」与「关于我」两个区块。

#### Scenario: Success case
- **WHEN** 用户点击头部「关于站点」
- **THEN** 进入聚合页，可查看关于内容并留言，布局美观合理

## MODIFIED Requirements
### Requirement: 头部导航
头部导航 SHALL 仅包含：首页、文章、刷题、考试、音乐、简历、关于站点（+登录态相关入口），不再显示分类/标签/归档/留言/关于独立项。
