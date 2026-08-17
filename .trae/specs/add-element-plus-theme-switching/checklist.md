# Checklist

- [x] `main.js` 已引入 `element-plus/theme-chalk/dark/css-vars.css`（在 `dist/index.css` 与 `tokens.css` 之间）
- [x] `tokens.css` 存在 `html.dark` 块，暗色令牌值与 spec 映射表一致（`--bg-page: #0B0F0E`、`--bg-card: #131A18` 等）
- [x] `html.dark` 下 EP 变量已对齐覆盖（`--el-bg-color`、`--el-text-color-*`、`--el-border-color*`、`--el-fill-color-blank`、`--el-color-primary-light-3~9`、`--el-color-primary-dark-2`）
- [x] `app` store 提供 `theme` 状态、`applyTheme(t, persist)` 统一应用入口、`toggleTheme()`、`initTheme()`；手动切换/恢复已有记录时持久化到 `localStorage('blog-admin-theme')`，首次跟随系统不持久化
- [x] 首次访问（无 localStorage）时跟随系统 `prefers-color-scheme`
- [x] `App.vue` 挂载时调用 `initTheme()`，刷新后主题恢复且无闪白
- [x] Header.vue 存在主题切换按钮：light 显示 Moon / dark 显示 Sunny，带 tooltip，点击即切换
- [x] 切换主题后表格、表单、下拉、对话框、消息提示等 EP 组件无白色背景残留（`--el-bg-color`/`--el-fill-color-blank` 等已映射暗色令牌）
- [x] Dashboard 三个 ECharts 图表在主题切换后坐标轴线/文字颜色更新为暗色适配值并正常重绘（`getThemeColors` + `watch(theme)` + `setOption` + `resize`）
- [x] `global.css` 中 `body` 具备背景/文字/边框色过渡，主题切换平滑
- [x] `blog-admin/src/` 中无影响暗色模式的硬编码亮色背景残留（`Login.vue` 登录框、`layout/index.vue` 头部已改令牌；剩余 `rgba(255,255,255,低透明)` 均为深色渐变基底上的装饰叠加与 12px logo 白点，不影响暗色观感）
- [x] `npm run build` 构建通过，无编译错误（exit 0，17.23s）
