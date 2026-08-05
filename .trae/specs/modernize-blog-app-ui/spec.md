# Blog-App 现代化 UI 重构 Spec

## Why
当前 blog-app 项目的整体样式与布局停留在几年前：使用过时的紫蓝渐变 (#667eea/#764ba2)、扁平卡片配 emoji 图标、缺少设计系统、加载体验简陋、缺少搜索/分类筛选等基础功能。需要根据 2024-2026 年流行的移动端博客设计趋势（卡片化、留白、轻玻璃拟态、柔和阴影、骨架屏、主题色系统、微交互）对 app 进行整体重构，并补齐所需的后端接口。

## What Changes
- 建立统一的设计令牌系统（颜色、间距、圆角、阴影、字号），集中管理
- 配色方案重做：用现代靛蓝/青色系（#4F46E5 主色 + #06B6D4 辅色 + #F1F5F9 背景）替换旧紫蓝渐变
- 首页重构：顶部搜索栏、分类横向滑动 chips、文章卡片用现代卡片样式（柔和阴影 + 大圆角 + 标签徽章），骨架屏替代"加载中..."
- 文章详情重构：阅读体验优化（行高、字号、留白）、点赞浮动按钮、相关文章推荐区块、现代评论卡片
- 简历页重构：现代个人主页布局（hero 区 + 技能标签云 + 卡片化时间线 + 项目卡片网格）
- 我的页重构：现代个人中心（渐变 hero + 统计数据 + 菜单列表带 SVG 图标）
- 登录页重构：现代认证界面（mesh gradient 背景 + 玻璃拟态登录卡片）
- TabBar 优化：增大触摸区域、激活态指示器动画、SVG 图标统一描边
- 后端新增接口：
  - `GET /portal/articles/search` 关键字搜索文章
  - `GET /portal/articles/{id}/related` 相关文章推荐
  - `GET /portal/stats` 站点统计数据（文章数、浏览数、标签数）
  - `GET /portal/messages` 公开留言列表（审核通过的）
  - `GET /user/info` 当前登录用户信息（需鉴权）

## Impact
- Affected specs: 无
- Affected code:
  - 前端 `blog-app/`：所有页面、组件、common 目录
  - 后端 `blog-backend/src/main/java/com/dlbyy/blog/`：portal 控制器、service、entity

## ADDED Requirements

### Requirement: 设计令牌系统
The system SHALL provide a centralized design token file (`common/theme.js`) that defines colors, spacing, radii, shadows, typography used across all pages and components.

#### Scenario: 统一主题应用
- **WHEN** 开发者修改主题色
- **THEN** 所有页面与组件自动应用新主题色，无需逐个文件修改

### Requirement: 现代首页布局
The system SHALL provide a modern home page with search bar at top, horizontally scrollable category chips, and modern article cards.

#### Scenario: 首页浏览
- **WHEN** 用户打开 app 首页
- **THEN** 顶部展示搜索栏 + banner 区
- **AND** 下方展示横向滚动的分类 chips（全部/原创/转载/翻译 + 真实分类）
- **AND** 列表展示带柔和阴影、大圆角的现代文章卡片
- **AND** 加载时展示骨架屏而非"加载中..."文字

#### Scenario: 搜索文章
- **WHEN** 用户在搜索栏输入关键字并确认
- **THEN** 调用 `/portal/articles/search` 接口返回匹配文章
- **AND** 高亮显示搜索结果中匹配的关键字

### Requirement: 现代文章详情页
The system SHALL provide a modern article reading experience with optimized typography, floating like button, related articles, and modern comment cards.

#### Scenario: 阅读文章
- **WHEN** 用户进入文章详情
- **THEN** 标题、meta 信息、内容、配图排版舒适，行高 1.8
- **AND** 右下角展示浮动点赞按钮
- **AND** 文末展示相关文章推荐（最多 3 篇）

#### Scenario: 评论展示
- **WHEN** 文章有评论
- **THEN** 评论以带头像/首字母圆形 avatar 的卡片展示
- **AND** 显示评论时间相对格式（如"3 小时前"）

### Requirement: 现代简历页
The system SHALL provide a modern resume page with hero section, skill tag cloud, card-style timeline, and project grid.

#### Scenario: 浏览简历
- **WHEN** 用户访问简历页
- **THEN** 顶部展示 hero 卡片（头像、姓名、职位、社交链接）
- **AND** 技能以标签云形式展示，不同颜色区分熟练度
- **AND** 工作经历与教育背景用卡片化时间线展示
- **AND** 项目经验用卡片网格展示

### Requirement: 现代我的页
The system SHALL provide a modern profile page with gradient hero, stats grid, and icon menu list.

#### Scenario: 浏览我的页
- **WHEN** 用户访问"我的"
- **THEN** 顶部展示渐变 hero（头像、昵称、简介）
- **AND** 下方展示统计数据网格（文章数、浏览数等）
- **AND** 菜单项以 SVG 图标 + 文字 + 箭头形式展示

### Requirement: 现代登录页
The system SHALL provide a modern login screen with mesh gradient background and glassmorphism card.

#### Scenario: 登录
- **WHEN** 用户进入登录页
- **THEN** 背景展示柔和的 mesh gradient
- **AND** 登录表单以玻璃拟态卡片形式居中展示
- **AND** 输入框获得焦点时有微动画反馈

### Requirement: 后端文章搜索接口
The system SHALL provide `GET /portal/articles/search` endpoint for keyword-based article search.

#### Scenario: 关键字搜索
- **WHEN** 客户端请求 `/portal/articles/search?keyword=java&page=1&size=10`
- **THEN** 返回标题或摘要包含关键字的文章分页结果
- **AND** 仅返回已发布文章

### Requirement: 相关文章推荐接口
The system SHALL provide `GET /portal/articles/{id}/related` endpoint returning related articles.

#### Scenario: 获取相关文章
- **WHEN** 客户端请求 `/portal/articles/123/related`
- **THEN** 返回与该文章同分类或同标签的最多 3 篇文章（不含自身）

### Requirement: 站点统计接口
The system SHALL provide `GET /portal/stats` endpoint returning site-wide statistics.

#### Scenario: 获取站点统计
- **WHEN** 客户端请求 `/portal/stats`
- **THEN** 返回 `{articleCount, viewCount, tagCount, categoryCount}` 字段

### Requirement: 公开留言列表接口
The system SHALL provide `GET /portal/messages` endpoint returning approved messages.

#### Scenario: 获取公开留言
- **WHEN** 客户端请求 `/portal/messages`
- **THEN** 返回 `status=1`（已审核）的留言列表，按时间倒序

### Requirement: 当前用户信息接口
The system SHALL provide `GET /user/info` endpoint returning the current authenticated user's info.

#### Scenario: 获取当前用户
- **WHEN** 客户端携带有效 token 请求 `/user/info`
- **THEN** 返回当前登录用户的 id、username、nickname、avatar、email
- **WHEN** 未携带 token 或 token 无效
- **THEN** 返回 401

## MODIFIED Requirements

### Requirement: TabBar 组件
修改 TabBar 组件以使用更大触摸区域（高度 56px）、激活态指示器动画（顶部圆点或下划线）、保持原有 SVG 图标但统一为 24x24 viewBox 与 1.8px 描边。

### Requirement: ArticleItem 组件
修改 ArticleItem 组件以使用现代卡片样式：柔和阴影（0 2px 8px rgba(0,0,0,0.06)）、12px 圆角、徽章用胶囊形（border-radius: 999px）、底部统计用 SVG 图标替代 emoji。

### Requirement: 首页分类筛选
修改首页 tabs，将原"全部/原创/转载/翻译"分类筛选改为同时支持分类（来自 `/portal/categories`）和类型筛选的横向 chips 组件。

## REMOVED Requirements
无
