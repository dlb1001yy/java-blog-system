# 修复 blog-app 底部导航栏图标展示

## Summary
`blog-app/pages.json` 的 `tabBar` 引用了 6 个 PNG 图标文件（`static/tab/home.png` 等），但 `static/tab/` 目录完全不存在，导致底部三个 tab 图标无法展示。采用用户选定的「自定义 tabBar + SVG 图标」方案：移除原生 tabBar 配置，新建一个 `TabBar.vue` 组件（内联 SVG 图标，蓝/灰双色态），在 3 个 tab 页面引入并固定在底部，切换使用 `uni.reLaunch` 模拟 tab 行为。**不涉猎其他功能**。

## Current State Analysis
- [blog-app/pages.json](file:///d:\my-project\java-blog-system\blog-app\pages.json) 第 46-71 行定义了 `tabBar`，list 引用 `static/tab/home.png`、`home-active.png`、`resume.png`、`resume-active.png`、`mine.png`、`mine-active.png` 共 6 个文件。
- [blog-app/static/](file:///d:\my-project\java-blog-system\blog-app\static\) 实际只有 `logo.png`，`static/tab/` 目录及 6 个图标全部缺失 → 底部三个图标显示为空白/破图。
- 3 个 tab 页面：
  - [pages/index/index.vue](file:///d:\my-project\java-blog-system\blog-app\pages\index\index.vue) — Options API，根节点 `.container`（`min-height: 100vh`）。
  - [pages/resume/index.vue](file:///d:\my-project\java-blog-system\blog-app\pages\resume\index.vue) — `<script setup>`，根节点 `.resume-page`（已有 `padding-bottom: 20px`）。
  - [pages/mine/index.vue](file:///d:\my-project\java-blog-system\blog-app\pages\mine\index.vue) — `<script setup>`，根节点 `.mine-page`（`min-height: 100vh`）。
- 项目无 iconfont 依赖；引入 iconfont 会增加外部资源，不符合最小改动。
- 内联 SVG 无需额外资源、跨端一致、可着色，是「好看」与「简单」的最优平衡。

## Proposed Changes

### 1. 新建 `blog-app/components/TabBar.vue`
自定义底部导航组件，固定 3 项：首页 / 简历 / 我的。

**Props**：
- `current: String` — 当前页路径，如 `/pages/index/index`，用于高亮当前项。

**Template**：
- 外层 `.tab-bar`（`position: fixed; bottom: 0; left: 0; right: 0;` 白底 + 顶部 1px 边框 + `padding-bottom: env(safe-area-inset-bottom)` 适配 iPhone X+）。
- 每项 `.tab-item` 包含一个内联 SVG（24x24，`stroke: currentColor`，`fill: none`，线性图标风格）+ 文本。
- 项的 `:class` 根据 `current === item.path` 切换 `active` 类，控制 `color`：默认 `#909399`，激活 `#409eff`。
- 点击触发 `uni.reLaunch({ url: item.path })`（移除原生 tabBar 后页面不再是 tab 页，用 reLaunch 模拟 tab 切换、避免页面栈堆积）。

**SVG 图标**（简洁线性风格，统一 24x24 viewBox，stroke-width 2）：
- 首页：房子轮廓（屋顶 + 主体 + 门）。
- 简历：文档轮廓（带折角 + 内部两条横线）。
- 我的：人形轮廓（圆头 + 肩膀弧线）。

**数据**：
```js
const list = [
  { path: '/pages/index/index',  text: '首页', icon: 'home' },
  { path: '/pages/resume/index', text: '简历', icon: 'resume' },
  { path: '/pages/mine/index',   text: '我的', icon: 'mine' }
]
```
icon 用 `v-if`/`v-else-if` 切换对应 SVG 内联块。

**样式要点**：
- `.tab-bar` 高度 50px + safe-area，`display: flex`，`box-shadow: 0 -1px 6px rgba(0,0,0,0.04)`。
- `.tab-item` `flex: 1`，`display: flex; flex-direction: column; align-items: center; gap: 2px`，`font-size: 11px`。
- SVG `width: 22px; height: 22px`。

### 2. 修改 `blog-app/pages.json`
- **删除整个 `tabBar` 块**（第 46-71 行）。原因：原生 tabBar 依赖缺失的 PNG 图标，且若保留会导致与自定义组件出现双底部栏。移除后 3 个页面变为普通页面，改由自定义组件 + `uni.reLaunch` 承担导航。
- `pages`、`globalStyle`、`easycom` 块保持不变。

### 3. 修改 `blog-app/pages/index/index.vue`（Options API）
- `import TabBar from '@/components/TabBar.vue'`，在 `components: { ArticleItem, TabBar }` 注册。
- 模板：在 `.container` 内末尾（`status` 之后、闭合 `</view>` 之前）追加 `<TabBar current="/pages/index/index" />`。
- 样式：`.container` 增加 `padding-bottom: calc(50px + env(safe-area-inset-bottom));` 防止列表底部被 tabBar 遮挡。

### 4. 修改 `blog-app/pages/resume/index.vue`（`<script setup>`）
- `import TabBar from '@/components/TabBar.vue'`。
- 模板：在 `.resume-page` 内末尾（最后一个 `</view>` 之前）追加 `<TabBar current="/pages/resume/index" />`。
- 样式：`.resume-page` 的 `padding-bottom: 20px` 改为 `padding-bottom: calc(50px + env(safe-area-inset-bottom) + 12px);`。

### 5. 修改 `blog-app/pages/mine/index.vue`（`<script setup>`）
- `import TabBar from '@/components/TabBar.vue'`。
- 模板：在 `.mine-page` 内末尾（`logout-wrapper` 之后、闭合 `</view>` 之前）追加 `<TabBar current="/pages/mine/index" />`。
- 样式：`.mine-page` 增加 `padding-bottom: calc(50px + env(safe-area-inset-bottom) + 12px);`。

## Assumptions & Decisions
- **导航方式**：移除原生 tabBar 后，页面不再是 tab 页，`uni.switchTab` 不可用。选用 `uni.reLaunch` 而非 `uni.redirectTo`：reLaunch 关闭所有页面栈、打开目标页，行为最接近原生 tab（无返回栈堆积）。代价是切换时页面重新挂载、重新请求数据——对本项目（首页分页、简历单次加载、我的页轻量）可接受。
- **不引入 iconfont/SVG sprite**：避免增加外部资源与构建配置，内联 SVG 零依赖、跨端一致。
- **不修改 mine.vue 既有的 `goPage` 逻辑**（如 `/pages/about/index` 跳转），属于其他功能，不在本次范围。
- **不修改 article/detail.vue、mine/login.vue**：非 tab 页，不显示 TabBar。
- **图标风格**：线性 stroke 图标（非填充），统一 2px 描边，与 Element Plus / 现代移动端 UI 风格一致，保证「好看」。
- **easycom 不自动引入**：手动 `import` 保证 Options API 与 setup 两种写法一致、可控。

## Verification Steps
1. 启动 blog-app（H5 端 `npm run dev:h5` 或对应命令），进入首页：底部应出现白色导航栏，3 个 SVG 图标 + 文字正常显示，「首页」高亮蓝色。
2. 点击「简历」：跳转到简历页，底部「简历」高亮，其余灰色，图标清晰无破图。
3. 点击「我的」：跳转到我的页，「我的」高亮。
4. 在 3 个页面间反复切换：无页面栈堆积（返回键不会在 tab 间回退），图标与文字始终居中、对齐、颜色正确。
5. 检查列表类页面（首页）滚动到底：最后一条文章不被 tabBar 遮挡（有底部留白）。
6. 浏览器控制台无 404 图片报错、无 Vue 组件注册警告。
