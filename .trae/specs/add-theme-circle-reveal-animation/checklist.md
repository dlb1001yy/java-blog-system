# Checklist

- [x] `global.css` 包含 `::view-transition-old(root), ::view-transition-new(root)` 的 `animation: none; mix-blend-mode: normal;` 规则及 z-index 层级（old: 1 / new: 9999）
- [x] `app.js` 的 `toggleTheme(event)` 存在能力检测：`document.startViewTransition` 不存在时直接无动画切换
- [x] 存在 `prefers-reduced-motion: reduce` 检测，开启时跳过动画直接切换
- [x] 扩散半径通过 `Math.hypot(Math.max(x, innerWidth-x), Math.max(y, innerHeight-y))` 计算，保证覆盖全屏
- [x] 动画通过 `document.documentElement.animate` + `pseudoElement: '::view-transition-new(root)'` 的 clip-path circle 关键帧实现，时长 500ms
- [x] 无点击事件（event 缺失）时有兜底圆心（视口中心），不报错
- [x] `applyTheme`/`initTheme`/localStorage 持久化/系统偏好初始化逻辑未被改动
- [x] Header 主题按钮点击可触发动画（事件对象正常透传到 store，`@click="appStore.toggleTheme"` 无需修改）
- [x] `npm run build` 构建通过，无编译错误（built in 27.02s）
