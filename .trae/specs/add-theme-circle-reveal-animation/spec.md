# 主题切换圆形扩散动画 Spec（View Transitions API）

## Why
当前 blog-admin 的亮/暗主题切换只有 CSS 过渡（颜色渐变），缺乏视觉冲击力。View Transitions API 由浏览器对页面新旧状态拍照生成伪元素快照（`::view-transition-old(root)` / `::view-transition-new(root)`），配合 `clip-path` 的 `circle()` Keyframe 动画可实现「点击处向四周圆形扩散」的切换动画，且零依赖、纯原生。

## 技术方案

### 核心机制
1. **CSS 层**：在全局样式中为 `::view-transition-old(root)` / `::view-transition-new(root)` 设置 `animation: none; mix-blend-mode: normal;`，关闭默认淡入淡出，让自定义 clip-path 动画接管；并设置层级（新快照在上层扩散）。
2. **JS 层（集成到现有 Pinia store）**：`app` store 的 `toggleTheme(event)` 改造：
   - 兼容性降级：`document.startViewTransition` 不存在，或 `prefers-reduced-motion: reduce` 时，直接调用 `applyTheme()` 无动画切换（渐进增强 + 无障碍）。
   - 支持时：取点击坐标 `(event.clientX, event.clientY)`，用 `Math.hypot(Math.max(x, innerWidth-x), Math.max(y, innerHeight-y))` 计算覆盖全屏的最终半径；`document.startViewTransition(() => applyTheme(反向主题))` 包裹现有 `applyTheme`；`transition.ready.then()` 中对 `html` 元素执行 `document.documentElement.animate({ clipPath: [circle(0px at x y), circle(endRadius at x y)] }, { duration, easing, pseudoElement: '::view-transition-new(root)' })`。
   - 现有持久化（localStorage）、`prefers-color-scheme` 初始化逻辑完全不变——动画只是包裹 `applyTheme` 的调用方式。
3. **UI 入口适配**：[Header.vue] 现有 Moon/Sunny 按钮点击时传入原生 `MouseEvent`（`@click="appStore.toggleTheme"` 默认即收到事件，无需改模板；仅需确保事件对象透传到 store）。
4. **图表联动**：Dashboard 的 `watch(theme)` 在 `startViewTransition` 回调内触发，属同一帧 DOM 更新，快照会包含新主题图表颜色，无需额外处理。
5. **参数细节**：动画时长 500ms、`ease-in-out`；转向动画在任意方向均为「新画面从点击处扩散」（对 dark→light 同样对 new 快照做扩散，简化实现，视觉一致）。

### 复用与落地位置
- 不新建 `theme.css`（项目已有 `tokens.css`/`global.css`），View Transition 伪元素规则追加到 `global.css`。
- 示例代码中的 `isDark` ref、App.vue 演示组件、按钮样式均不采用——集成进现有 store/Header。
- 项目为 JS（非 TS），无需 `@ts-expect-error`。

## What Changes
- 修改 `blog-admin/src/stores/app.js`：`toggleTheme(event?)` 重构为 View Transitions 包裹版（含能力检测、`prefers-reduced-motion` 降级、坐标/半径计算、clip-path Keyframe 动画）
- 修改 `blog-admin/src/assets/styles/global.css`：追加 `::view-transition-old(root)` / `::view-transition-new(root)` 的 `animation: none; mix-blend-mode: normal;` 与 z-index 层级规则
- 修改 `blog-admin/src/layout/Header.vue`：如需，确保 click 事件对象传递给 `toggleTheme`（`@click="appStore.toggleTheme"` 原生事件已透传，预计无改动或极小改动）
- 不改 `applyTheme`/`initTheme`、tokens.css、Dashboard、后端
- **BREAKING**：无

## Impact
- Affected specs: `add-element-plus-theme-switching`（其"主题状态管理与持久化"需求行为不变，仅切换路径增加动画包裹）
- Affected code:
  - `blog-admin/src/stores/app.js`
  - `blog-admin/src/assets/styles/global.css`
  - `blog-admin/src/layout/Header.vue`（视情况）
- 不影响：持久化逻辑、系统偏好初始化、ECharts 适配、后端、其他前端

## ADDED Requirements

### Requirement: 圆形扩散切换动画
系统 SHALL 在支持 View Transitions API 的浏览器中，从主题按钮点击坐标处产生圆形扩散动画覆盖全屏，动画作用于 `::view-transition-new(root)` 伪元素，时长约 500ms。

#### Scenario: 扩散动画生效
- **WHEN** 用户在支持 View Transitions 的 Chrome/Edge 中点击 Header 主题按钮
- **THEN** 新主题画面以点击点为圆心、以到屏幕最远角的距离为半径做 clip-path 圆形扩散，主题持久化行为不变

### Requirement: 渐进增强降级
系统 SHALL 在 `document.startViewTransition` 不存在或系统开启 `prefers-reduced-motion: reduce` 时，直接无动画切换主题。

#### Scenario: 旧浏览器降级
- **WHEN** 浏览器不支持 View Transitions API
- **THEN** 点击按钮立即切换主题，无报错、无动画

#### Scenario: 减弱动画偏好
- **WHEN** 用户系统开启"减少动态效果"
- **THEN** 跳过扩散动画，直接切换

## MODIFIED Requirements

### Requirement: 主题切换 UI 入口
Header 主题按钮点击行为升级为携带点击坐标的动画切换入口；图标/tooltip 联动逻辑不变。
