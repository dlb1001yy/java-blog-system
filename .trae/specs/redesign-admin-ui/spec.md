# Blog-Admin 后台管理 UI 现代化重构 Spec

## Why
当前 blog-admin 的整体样式停留在 2018 年 vue-element-admin 模板风格：暗蓝灰侧边栏 (#304156)、纯白扁平头部、灰色背景 (#f0f2f5)、默认 Element Plus 蓝色 (#409eff)、列表页只是 `el-card` 直接套 `el-table`，缺少视觉层次、品牌感和现代感。客户打开后台第一眼就是"模板感"。需要按 2024-2026 年流行的后台设计趋势（玻璃拟态、渐变强调色、柔和彩色阴影、大圆角、Bento 网格、微交互、现代字体栈）对整体视觉与布局进行重构，让客户一进入就感到"高级、舒服、有冲击力"。

## What Changes
- 建立统一的设计令牌（CSS 变量）：主色 / 强调渐变 / 中性色 / 阴影 / 圆角 / 间距 / 字号，集中定义在 `src/assets/styles/tokens.css`，全局引入
- 主色方案重做：用现代靛蓝/紫色渐变系（`#6366F1` → `#8B5CF6` 主色渐变，`#06B6D4` 辅色）替换默认 Element Plus 蓝；中性背景换为 `#F8FAFC`
- 字体栈升级：优先 `Inter` / `Plus Jakarta Sans`（通过 CDN），降级到系统字体
- **登录页**：左右分屏布局，左侧品牌区（动态渐变 + 网格 + 产品卖点），右侧玻璃拟态登录卡
- **Layout 布局**：侧边栏改为渐变深色（`#1E1B4B` → `#312E81`），头部改为带 `backdrop-filter` 模糊的玻璃条，主内容区背景加柔和渐变光斑
- **Sidebar**：渐变背景 + 激活态发光指示条 + 悬浮微动效 + 折叠态圆角胶囊 logo
- **Header**：玻璃模糊背景 + 现代面包屑 + 全屏/刷新图标加圆角 hover 背景胶囊 + 用户区头像加渐变描边
- **TagsView**：现代化胶囊标签（激活态渐变填充 + 关闭按钮缩放动效），整体更圆润
- **Dashboard**：Bento 网格布局；统计卡用渐变背景 + 大号数字 + 微光斑；图表卡片大圆角 + 柔和阴影；待办列表用现代卡片
- **列表页统一**：抽取 `PageContainer` 组件（页面标题 + 描述 + 操作区），搜索栏改为圆角卡片，表格去掉硬边框改用行分割线 + 悬浮行高亮，分页右下角带卡片包裹
- **全局滚动条**：更细更柔（4px，圆角，半透明），hover 时主色
- **微交互**：路由切换动画升级为 slide-fade；卡片 hover 提升 + 阴影渐变；按钮 hover 微缩放
- 不改后端接口、不改路由结构、不改业务逻辑，仅重构视觉与布局
- **BREAKING**：无（仅样式与少量模板结构调整，功能行为保持不变）

## Impact
- Affected specs: 无（blog-admin 此前无独立 spec）
- Affected code:
  - 新增：`src/assets/styles/tokens.css`、`src/components/PageContainer.vue`
  - 修改：`src/main.js`（引入 tokens.css 与字体）、`src/App.vue`、`src/assets/styles/global.css`
  - 修改：`src/layout/index.vue`、`src/layout/Sidebar.vue`、`src/layout/Header.vue`、`src/layout/TagsView.vue`
  - 修改：`src/views/Login.vue`、`src/views/Dashboard.vue`
  - 修改：`src/views/ArticleList.vue`、`src/views/CategoryList.vue`、`src/views/TagList.vue`、`src/views/CommentList.vue`、`src/views/MessageList.vue`、`src/views/LinkList.vue`、`src/views/Settings.vue`、`src/views/ResumeEdit.vue`、`src/views/ArticleEdit.vue`
- 不影响：`src/api/*`、`src/stores/*`、`src/router/*`、后端代码

## ADDED Requirements

### Requirement: 设计令牌系统
系统 SHALL 在 `src/assets/styles/tokens.css` 中以 CSS 自定义属性集中定义颜色、渐变、阴影、圆角、间距、字号、过渡等设计令牌，并在 `main.js` 中于 `global.css` 之前引入。

#### Scenario: 令牌可用
- **WHEN** 任意组件使用 `var(--color-primary)` 等变量
- **THEN** 能正确解析为令牌定义的值

#### Scenario: Element Plus 主题覆盖
- **WHEN** 页面渲染 Element Plus 组件
- **THEN** 主色按钮、链接、激活态使用新的 `#6366F1` 主色（通过 `--el-color-primary` 覆盖）

### Requirement: PageContainer 通用页头组件
系统 SHALL 提供一个 `src/components/PageContainer.vue` 组件，统一列表/表单页的页头（标题、描述、操作区插槽）与内容区包裹，避免每个页面重复写卡片标题。

#### Scenario: 列表页使用
- **WHEN** 列表页用 `<PageContainer title="文章管理" description="...">` 包裹
- **THEN** 页面顶部显示标题、描述，下方为内容插槽，视觉与全站统一

### Requirement: 现代登录页
登录页 SHALL 采用左右分屏布局：左侧品牌展示区（动态渐变背景 + 网格 + 标语），右侧玻璃拟态登录卡片。

#### Scenario: 桌面端展示
- **WHEN** 在桌面端访问 `/login`
- **THEN** 左侧显示品牌区，右侧显示登录表单卡片

#### Scenario: 移动端自适应
- **WHEN** 视口宽度 < 768px
- **THEN** 左侧品牌区隐藏，仅显示居中的登录卡片

### Requirement: Dashboard Bento 网格
Dashboard SHALL 使用 Bento 风格网格布局展示统计卡与图表，统计卡使用渐变背景与大号数字。

#### Scenario: 统计卡视觉
- **WHEN** Dashboard 渲染统计卡
- **THEN** 每张卡有独立渐变背景、白色文字、大号数字、悬浮上浮效果

### Requirement: 列表页统一视觉规范
所有列表页（文章/分类/标签/评论/留言/友链）SHALL 使用 PageContainer 包裹，搜索区为圆角卡片，表格无硬边框、行悬浮高亮，分页带卡片包裹。

#### Scenario: 表格视觉
- **WHEN** 渲染数据表格
- **THEN** 无外边框，仅行间分割线，行 hover 时背景为主色浅色

## MODIFIED Requirements

### Requirement: 后台整体布局
布局保持"侧边栏 + 头部 + 主内容"三段式，但视觉重做：侧边栏渐变深色背景，头部玻璃模糊，主内容区柔和渐变背景。侧边栏宽度、折叠行为、路由结构不变。

### Requirement: Header 工具区
头部保留折叠按钮、面包屑、刷新、全屏、用户下拉，但视觉升级：图标加圆角胶囊 hover 背景，用户头像加渐变描边，整体玻璃模糊。

### Requirement: TagsView 标签
保留标签新增/关闭/右键菜单功能，视觉改为圆润胶囊：激活态渐变填充 + 白字，非激活态浅色背景 + 主色文字，关闭按钮 hover 缩放。
