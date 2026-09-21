# 三端深度视觉统一 Spec

## Why
上一变更（unify-brand-style-across-clients）已完成三端令牌级主色统一，但调研发现 14 项视觉割裂问题：门户首页 Hero 仍是紫色渐变与品牌正面冲突、三端 Logo 四种来源互不相同、站名分裂（"Java 博客" vs "Java码农笔记"）、圆角/阴影/过渡刻度不对齐、卡片 hover 与按压反馈各自为政、骨架屏与空状态三套表现。需要在令牌统一基础上做视觉语言层面的深度统一。

## What Changes
- **品牌资产统一**：站名统一为"Java码农笔记"；以 frontend 页头咖啡杯+代码符号为品牌图形，三端 favicon 统一为翡翠绿渐变版本；app 首页 Hero 补品牌图形露出
- **frontend 品牌色冲突清理**：首页 Hero 紫色渐变（#667eea→#764ba2）改品牌绿渐变；LatestBlogCard 占位封面混入的紫色 #9b59b6 改品牌渐变；Home 模块图标配色从 Element 旧默认色改为与 app 宫格一致的品牌色板
- **设计刻度对齐**：app 圆角阶梯 4/8/12/16 升级为 8/12/16/20 并清理页面内 10/12/14px 硬编码；frontend 阴影改 slate 基（rgba(15,23,42)）对齐 admin；三端过渡时长令牌化对齐 admin 的 0.15/0.25s 刻度
- **交互反馈统一**：frontend 可交互卡片 hover 统一为 translateY(-4px)+shadow-hover（LatestBlogCard/Home 的硬编码阴影改令牌）；app 按压反馈统一为 scale(0.98)+opacity .9（module-item/latest-item 对齐）；admin 表格行 hover 全局收敛一次定义并删除各视图重复 :deep；删除 admin 死代码（零使用的 .hover-lift、与 scoped 冲突的 global.css .slide-fade 纵向定义）
- **加载与空状态统一**：frontend SkeletonCard 底色改令牌色；admin el-empty image-size 统一 80；app 首页"最新文章"加载失败由静默隐藏改为显示空态/重试反馈
- **不做**：admin 加骨架屏（后台 v-loading 合理）、app 补 reduced-motion（本次范围外）、改三端布局结构

## Impact
- Affected specs: unify-brand-style-across-clients（令牌基准延续）
- Affected code:
  - 品牌：`blog-admin/public/favicon.svg`、`blog-admin/public/icons.svg`、`blog-admin/src/layout/index.vue`（站名）、`blog-frontend/public/favicon.svg`、`blog-app/pages/index/index.vue`（Hero）、`blog-app/static/logo.png`
  - frontend：`src/views/Home.vue`、`src/components/LatestBlogCard.vue`、`src/assets/styles/global.css`、`src/components/SkeletonCard.vue`
  - app：`uni.scss`、`common/theme.js`、`pages/index/index.vue`、`components/ArticleItem.vue`（按压规范参照）
  - admin：`src/assets/styles/global.css`、各列表视图的重复 :deep 表格 hover（ArticleList/UserList/CommentList/BackupManage 等）

## ADDED Requirements

### Requirement: 统一品牌资产
三端 SHALL 使用统一站名"Java码农笔记"与统一品牌图形（咖啡杯+代码符号，翡翠绿渐变 #059669→#14B8A6）。

#### Scenario: 站名一致
- **WHEN** 查看 admin 侧栏、frontend 页头、app 首页 Hero
- **THEN** 站名均为"Java码农笔记"

#### Scenario: favicon 品牌化
- **WHEN** 查看 blog-admin 与 blog-frontend 的浏览器标签图标
- **THEN** 均为翡翠绿渐变咖啡杯图形，无紫色闪电、无蓝色残留

#### Scenario: app 品牌露出
- **WHEN** 打开 app 首页
- **THEN** Hero 区含品牌图形（H5 内联 SVG；如目标平台含小程序则用 image 引 PNG）+ 统一站名

### Requirement: 品牌色零冲突
三端源码中 SHALL 不存在与品牌冲突的紫色系（#667eea、#764ba2、#9b59b6、#863bff、#aa3bff）与 Element 旧默认色（#409eff、#67c23a 作为模块图标配色）残留。

#### Scenario: 门户 Hero 品牌化
- **WHEN** 访问 frontend 首页
- **THEN** Hero 背景为翡翠绿系渐变，装饰元素不出现紫色

### Requirement: 统一设计刻度
三端圆角 SHALL 对齐 8/12/16/20 阶梯；阴影统一 slate 基（rgba(15,23,42,*)）；过渡时长统一 0.15s（fast）/0.25s（base）刻度。

#### Scenario: app 圆角对齐
- **WHEN** 检查 blog-app 圆角令牌与页面硬编码
- **THEN** $radius 为 8/12/16/20，无 10/14px 游离值

### Requirement: 统一交互反馈语言
可交互卡片 hover/按压 SHALL 统一：web 端 translateY(-4px)+阴影升格，app 端 scale(0.98)+opacity .9；admin 表格行 hover 主色浅底全局定义一次。

#### Scenario: 卡片反馈一致
- **WHEN** hover frontend 的 ArticleCard/LatestBlogCard/模块卡
- **THEN** 反馈幅度与阴影令牌一致，无硬编码阴影值

#### Scenario: app 按压一致
- **WHEN** 按压 app 的宫格项/文章项
- **THEN** 均为 scale(0.98)+opacity .9 反馈

#### Scenario: admin 表格 hover 收敛
- **WHEN** 检查 admin 列表视图
- **THEN** 表格行 hover 样式仅在 global.css 定义一次，视图内无重复 :deep

### Requirement: 统一加载与空状态
frontend 骨架屏 SHALL 使用令牌色；admin 空态 image-size 统一 80；app 首页最新文章加载失败 SHALL 显示空态反馈而非静默隐藏。

#### Scenario: app 失败反馈
- **WHEN** app 首页最新文章接口失败
- **THEN** 显示空态提示与重试入口，区块不再消失

#### Scenario: 构建可用
- **WHEN** 执行三端 web 项目生产构建
- **THEN** blog-admin 与 blog-frontend 构建成功
