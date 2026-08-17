# Tasks

- [x] Task 1: 追加 View Transitions 伪元素样式
  - [x] SubTask 1.1: 修改 `blog-admin/src/assets/styles/global.css`，追加规则：`::view-transition-old(root), ::view-transition-new(root) { animation: none; mix-blend-mode: normal; }` 与层级规则（`::view-transition-old(root) { z-index: 1; }`、`::view-transition-new(root) { z-index: 9999; }`），带中文注释说明用途
- [x] Task 2: 改造 toggleTheme 为扩散动画切换
  - [x] SubTask 2.1: 修改 `blog-admin/src/stores/app.js`：`toggleTheme(event)` 重构——能力检测（`document.startViewTransition` 存在且 `!matchMedia('(prefers-reduced-motion: reduce)').matches`），不支持时直接 `applyTheme(反向)`；支持时取 `event.clientX/clientY`（无事件对象时以视口中心为圆心兜底），`Math.hypot` 计算覆盖全屏半径，`document.startViewTransition(() => applyTheme(反向))`，`transition.ready.then()` 中 `document.documentElement.animate({ clipPath: [circle(0px at x y), circle(endRadius px at x y)] }, { duration: 500, easing: 'ease-in-out', pseudoElement: '::view-transition-new(root)' })`；`applyTheme`/`initTheme`/持久化逻辑不动
- [x] Task 3: 确认 Header 事件透传
  - [x] SubTask 3.1: 检查 `blog-admin/src/layout/Header.vue` 主题按钮 `@click="appStore.toggleTheme"`：Vue 内联方法引用默认透传原生 MouseEvent，确认无需修改（el-icon 无自定义 click emit，事件落到根原生元素；Pinia action 即普通函数引用，透传正常）
- [x] Task 4: 构建验证
  - [x] SubTask 4.1: 执行 `npm run build` 确认无编译错误（built in 27.02s，exit 0）

# Task Dependencies
- Task 2 依赖 Task 1（伪元素样式就绪后动画才正确显示）
- Task 3 依赖 Task 2（toggleTheme 签名确定后验证透传）
- Task 4 依赖 Task 1 ~ Task 3 全部完成
