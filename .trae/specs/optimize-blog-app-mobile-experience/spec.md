# Blog-App 移动端体验深度优化 Spec

## Why
blog-app 已完成现代化 UI 重构（modernize-blog-app-ui），但作为移动端应用仍缺少移动端核心竞争力：无暗黑模式、样式硬编码亮色 Token 无法运行时切换；断网即不可用（无离线缓存）；分享只能复制链接（无海报）；列表图片无懒加载/渐显过渡；所有页面在主包内（无分包）。需围绕"手势流畅度、弱网/断网体验、跨端复用与轻量化"四个维度进行深度优化。

## What Changes
- **CSS 变量令牌层**：在 `App.vue` 全局样式中将 Design Tokens 暴露为 `--app-*` CSS 自定义属性（亮色），页面根节点 `.theme-dark` 类覆盖暗色值，含 `--app-safe-bottom` 安全区变量
- **暗黑模式**：`common/theme.js` 增加暗色色板、响应式主题状态、`initTheme/toggleTheme`（storage 持久化）、监听 `uni.onThemeChange` 跟随系统；`manifest.json` 开启 `darkmode` + 新建 `theme.json` 适配原生导航栏；我的页提供主题切换入口
- **微交互与自定义刷新**：卡片 `:active` 缩放触控反馈；首页改用 `scroll-view` refresher 实现品牌脉冲动画下拉刷新 + 三点跳动加载动画（替代纯文字）
- **自定义 NavBar**：新建 `components/NavBar.vue`（statusBarHeight 适配刘海屏），登录页接入
- **图片体验**：新建 `common/imageUrl.js`（CDN host 配置化追加 resize/WebP 参数，默认原样返回）；封面图 `lazy-load` + `@load` 渐显（软 LQIP）；`utils/markdown.js` 支持内容图片分段渲染，详情页交替渲染 rich-text/image，点击 `uni.previewImage` 预览
- **离线缓存**：新建 `common/offline.js`（网络监听、offlineMode 响应式标记、文章列表/详情缓存、稍后阅读存储）；首页断网降级读缓存并提示"已进入离线阅读模式"；详情页写缓存/降级读缓存 + "加入稍后阅读"
- **稍后阅读页 + 侧滑手势**：自研轻量 `components/SwipeCell.vue` 侧滑组件（项目无 uni_modules/npm 依赖，不引入 uni-ui）；新建分包页"稍后阅读"（列表 + 侧滑删除 + 离线可读）；我的页加入口
- **Canvas 分享海报**：内联轻量 QR 编码器 `utils/qrcode.js`（零依赖）；`components/SharePoster.vue` 绘制品牌渐变头部 + 标题/摘要 + 二维码海报；详情页分享入口，App/小程序保存相册、H5 长按保存、复制链接
- **分包加载**：`pages.json` 配置 `subPackages`，将"我的"（含登录）与"简历"页移入 `subpkg` 分包，同步更新 `TabBar.vue`、`request.js`、mine 页内跳转路径

## Impact
- Affected specs: modernize-blog-app-ui（在其成果之上增量演进，不推翻既有设计）
- Affected code:
  - `blog-app/App.vue`、`uni.scss`、`pages.json`、`manifest.json`、`theme.json`（新增）
  - `blog-app/common/theme.js`、`config.js`、`request.js`（仅路径）、`imageUrl.js`（新增）、`offline.js`（新增）
  - `blog-app/utils/markdown.js`、`qrcode.js`（新增）
  - `blog-app/components/`：ArticleItem、TabBar、NavBar（新增）、SwipeCell（新增）、SharePoster（新增）
  - `blog-app/pages/`：index、article/detail、mine/index、mine/login、resume/index（移至分包）、subpkg/readlater（新增）

## ADDED Requirements

### Requirement: CSS 变量令牌层
The system SHALL expose Design Tokens as `--app-*` CSS custom properties on `page` selector in `App.vue`, with `.theme-dark` class overrides, consistent with `common/theme.js` values.

#### Scenario: 令牌一处修改全端生效
- **WHEN** 开发者修改 App.vue 中 `--app-primary` 或 theme.js 中对应 Token
- **THEN** 所有引用 `var(--app-primary)` 的页面与组件自动应用新值

### Requirement: 暗黑模式
The system SHALL support light/dark theme with runtime switching, system-follow, and storage persistence; native navigation bars adapt via theme.json on App side.

#### Scenario: 跟随系统切换
- **WHEN** 系统切换深色模式且用户未手动覆盖主题
- **THEN** uni.onThemeChange 触发后全站内容区切换为暗色 Token，无需刷新页面

#### Scenario: 手动切换
- **WHEN** 用户在"我的"页切换主题（跟随系统/亮色/暗色）
- **THEN** 选择立即生效并持久化，重启后保持

### Requirement: 卡片触控反馈
The system SHALL provide press-scale feedback (transform: scale(0.98) + opacity 0.9, transition 0.15s) on article cards, related-article items and menu items via `:active` state.

#### Scenario: 按压文章卡片
- **WHEN** 用户按下文章列表卡片
- **THEN** 卡片平滑缩放至 0.98 并降低不透明度，松开恢复

### Requirement: 自定义下拉刷新与加载动画
The system SHALL replace native pull-down refresh on home page with scroll-view refresher rendering a brand pulse animation, and render a three-dot bouncing animation for load-more.

#### Scenario: 下拉刷新
- **WHEN** 用户在首页下拉
- **THEN** 展示品牌色脉冲圆点动画，释放后刷新列表并收起

#### Scenario: 触底加载
- **WHEN** 列表滚动到底部加载更多
- **THEN** 展示三点跳动动画，加载完成后追加数据

### Requirement: 自定义导航栏组件
The system SHALL provide `components/NavBar.vue` with statusBarHeight padding, back button, and title slot for pages using custom navigation style.

#### Scenario: 刘海屏适配
- **WHEN** 在带刘海/灵动岛的设备上打开使用 NavBar 的页面
- **THEN** 内容从状态栏安全区之下开始，标题与返回键不被遮挡

### Requirement: 图片懒加载与渐显
The system SHALL apply `lazy-load` to list images and fade-in transition on image load (soft LQIP); CDN optimization params appended only when host matches configured CDN host.

#### Scenario: 列表滚动加载封面
- **WHEN** 用户滚动文章列表
- **THEN** 封面图进入视口才加载，加载完成后从占位底色渐显

### Requirement: 文章图片预览
The system SHALL split markdown HTML into segments (rich-text + standalone image), render them alternately, and open `uni.previewImage` with all content images on tap, supporting swipe zoom.

#### Scenario: 点击内容图片
- **WHEN** 用户点击文章正文中的图片
- **THEN** 全屏预览该图片，可左右滑动切换本文其他图片

### Requirement: 离线阅读
The system SHALL cache first-page article list and visited article details via uni storage, detect network loss, and degrade to local cache with an "已进入离线阅读模式" notice.

#### Scenario: 断网打开首页
- **WHEN** 网络断开时用户进入首页且本地有缓存
- **THEN** 列表从缓存渲染并提示"已进入离线阅读模式"

#### Scenario: 断网读详情
- **WHEN** 网络断开时用户打开已缓存的文章详情
- **THEN** 从缓存渲染正文，评论区可缺省

### Requirement: 稍后阅读与侧滑删除
The system SHALL provide read-later storage (add from article detail), a subpackage list page where entries render offline and support swipe-to-delete via self-built SwipeCell component.

#### Scenario: 加入稍后阅读
- **WHEN** 用户在详情页点击"稍后阅读"
- **THEN** 文章（含正文）写入本地存储，按钮状态更新，去重添加

#### Scenario: 侧滑删除
- **WHEN** 用户在稍后阅读列表左滑条目并点击删除
- **THEN** 条目从列表与存储中移除，其余条目自动收起侧滑

### Requirement: Canvas 分享海报
The system SHALL generate a share poster via canvas (brand gradient header, wrapped title/summary, site info, QR code of article URL) and support saving to album (App/MP), long-press save (H5), and copying the link.

#### Scenario: 生成并保存海报
- **WHEN** 用户在详情页点击分享并选择"保存海报"
- **THEN** Canvas 绘制完成弹层展示海报，App/小程序端保存至相册并提示成功，H5 端提示长按保存

### Requirement: 分包加载
The system SHALL move mine (index + login) and resume pages into a `subpkg` subpackage, keeping index and article detail in the main package, with all internal path references updated.

#### Scenario: 访问分包页面
- **WHEN** 用户通过 TabBar 进入"简历"/"我的"或跳转登录
- **THEN** 分包页面正常加载，导航与返回行为不变，主包体积减小

## MODIFIED Requirements

### Requirement: 设计令牌系统（原 modernize-blog-app-ui）
在 SCSS 变量（编译期）基础上新增 CSS 自定义属性层（运行期），主题相关 surface 颜色（背景/卡片/文字/边框/分割线）优先使用 `var(--app-*)`（带 Token 兜底值），非主题色（品牌色等）保持 SCSS 变量。

### Requirement: 首页（原 modernize-blog-app-ui）
列表区域改为固定高度 scroll-view（refresher-enabled + scrolltolower），移除 onPullDownRefresh/onReachBottom 依赖；断网时降级渲染离线缓存列表。

### Requirement: 文章详情页（原 modernize-blog-app-ui）
新增操作条：稍后阅读、分享海报；正文图片分段渲染可预览；成功加载后写入离线缓存；新增自定义 Navbar 右侧分享入口或浮动分享按钮（与现有点赞 FAB 并列）。

### Requirement: TabBar 组件（原 modernize-blog-app-ui）
简历/我的路径改为分包路径 `/subpkg/...`；背景与边框使用 `var(--app-*)` 适配暗黑模式。

## REMOVED Requirements
无
