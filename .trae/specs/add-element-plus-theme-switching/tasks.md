# Tasks

- [x] Task 1: 引入 Element Plus 官方暗色变量并构建暗色令牌层
  - [x] SubTask 1.1: 修改 `blog-admin/src/main.js`，在 `element-plus/dist/index.css` 之后引入 `element-plus/theme-chalk/dark/css-vars.css`
  - [x] SubTask 1.2: 修改 `blog-admin/src/assets/styles/tokens.css`，按 spec 映射表新增 `html.dark` 块：覆盖 `--bg-*`/`--text-*`/`--border-color`/阴影令牌，并对齐覆盖 `--el-bg-color`/`--el-text-color-*`/`--el-border-color*`/`--el-fill-color-blank`/`--el-color-primary-light-3~9`/`--el-color-primary-dark-2`/`--el-mask-color`
- [x] Task 2: 实现主题状态管理与持久化
  - [x] SubTask 2.1: 修改 `blog-admin/src/stores/app.js`，新增 `theme` ref（'light' | 'dark'）、`applyTheme(t, persist)`（统一应用入口：更新状态 + 切换 `document.documentElement` 的 `dark` 类，`persist=true` 时写 `localStorage('blog-admin-theme')`）、`toggleTheme()`（调用 `applyTheme` 反向主题，持久化）、`initTheme()`（优先 localStorage 并复用 `applyTheme`；无记录时读取 `window.matchMedia('(prefers-color-scheme: dark)')` 且不持久化，保持跟随系统）
  - [x] SubTask 2.2: 修改 `blog-admin/src/App.vue`，在 `onMounted` 中调用 `appStore.initTheme()`
- [x] Task 3: Header 增加主题切换按钮
  - [x] SubTask 3.1: 修改 `blog-admin/src/layout/Header.vue`，在"全屏"图标旁新增主题按钮：亮色显示 `Moon`（tooltip"切换暗色模式"），暗色显示 `Sunny`（tooltip"切换亮色模式"），点击调用 `appStore.toggleTheme()`，复用现有 `header-icon` 样式
- [x] Task 4: Dashboard ECharts 主题适配
  - [x] SubTask 4.1: 修改 `blog-admin/src/views/Dashboard.vue`，将图表 option 中硬编码的主题相关颜色（坐标轴线 `#E7E5E4`、轴标签/图例文字色等）提取为主题感知取值（从 `getComputedStyle(document.documentElement)` 读取 `--border-color`/`--text-secondary` 等令牌）
  - [x] SubTask 4.2: `watch` app store 的 `theme`，主题变化时对三个图表重新 `setOption` 更新颜色并调用 `resize()`
- [x] Task 5: 全局过渡与硬编码亮色排查
  - [x] SubTask 5.1: 修改 `blog-admin/src/assets/styles/global.css`，为 `body` 添加 `background-color`/`color`/`border-color` 的 0.3s ease 过渡
  - [x] SubTask 5.2: 在 `blog-admin/src/` 内搜索硬编码亮色背景（`#fff`、`#FFFFFF`、`background: white` 等），将影响暗色观感的改引用 `var(--bg-card)`/`var(--bg-subtle)` 令牌（文字色/装饰性渐变不动）
- [x] Task 6: 构建与运行验证
  - [x] SubTask 6.1: 执行 `npm run build` 确认无编译错误（exit 0）；如可行启动 `npm run dev` 人工验证亮/暗切换、刷新持久化、EP 组件与图表暗色表现

# Task Dependencies
- Task 2 依赖 Task 1（html.dark 类与暗色变量就绪后切换才有视觉效果）
- Task 3、Task 4 依赖 Task 2（需要 theme 状态与 toggleTheme）
- Task 5 可与 Task 3、Task 4 并行
- Task 6 依赖 Task 1 ~ Task 5 全部完成
