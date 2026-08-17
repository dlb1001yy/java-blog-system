# Element Plus 动态主题切换 Spec（亮 / 暗模式）

## Why
blog-admin 目前只有亮色主题。Element Plus 内部已全面基于 CSS 变量构建（`--el-*` 体系），且项目已有 `tokens.css` 设计令牌层，因此通过「`html.dark` 类 + CSS 变量覆盖」实现运行时主题切换是最原生、零依赖的方案（无需 SCSS 编译、无需第三方库）。增加暗色模式可降低夜间使用的视觉疲劳，并提升后台的现代感。

## 技术方案

### 核心机制
1. **Element Plus 官方暗色变量**：引入 `element-plus/theme-chalk/dark/css-vars.css`（EP 2.4 支持），该文件在 `html.dark` 选择器下重定义全部 `--el-*` 变量，组件自动变暗。
2. **设计令牌暗色层**：在 `tokens.css` 中新增 `html.dark { ... }` 块，覆盖项目自有令牌（`--bg-*`、`--text-*`、`--border-color`、阴影等）。因组件样式全部引用令牌，暗色自动生效。
3. **状态与切换**：Pinia `app` store 增加 `theme` 状态（`'light' | 'dark'`），`toggleTheme()` 切换 `document.documentElement` 的 `dark` 类并写入 `localStorage`；初始化时优先读取 localStorage，无记录则跟随系统 `prefers-color-scheme`。
4. **UI 入口**：Header.vue 增加 日/月 图标切换按钮（Sunny / Moon）。
5. **图表适配**：Dashboard.vue 的 ECharts 颜色为 JS 硬编码（坐标轴 `#E7E5E4`、文字色等），需 watch 主题变化重设 option 中的主题相关颜色并 `resize()`。

### 暗色令牌映射表（深绿夜色系，延续森林绿品牌）

| 令牌 | 亮色值 | 暗色值 |
|------|--------|--------|
| `--bg-page` | `#FAFAF9` | `#0B0F0E` |
| `--bg-card` | `#FFFFFF` | `#131A18` |
| `--bg-subtle` | `#F5F5F4` | `#1A2321` |
| `--text-primary` | `#1C1917` | `#F5F7F6` |
| `--text-regular` | `#57534E` | `#C3CDC9` |
| `--text-secondary` | `#A8A29E` | `#7E8C87` |
| `--border-color` | `#E7E5E4` | `#24302C` |
| `--shadow-sm` | 现亮色值 | `0 1px 2px rgba(0,0,0,0.4), 0 1px 3px rgba(0,0,0,0.5)` |
| `--shadow-md` | 现亮色值 | `0 4px 6px -1px rgba(0,0,0,0.45), 0 2px 4px -2px rgba(0,0,0,0.4)` |
| `--shadow-lg` | 现亮色值 | `0 10px 15px -3px rgba(0,0,0,0.5), 0 4px 6px -4px rgba(0,0,0,0.45)` |
| `--shadow-xl` | 现亮色值 | `0 20px 25px -5px rgba(0,0,0,0.55), 0 8px 10px -6px rgba(0,0,0,0.5)` |
| `--gradient-sidebar` | `#0F2A23 → #143D31` | 保持不变（本就是深色） |

### Element Plus 变量暗色覆盖（对齐品牌与令牌）

暗色下 `--el-color-primary-light-N` 的语义反转（向暗色混合而非白色），需按 EP 官方混合公式（与 `#141414` 混合）重算覆盖：

| 令牌 | 暗色值 |
|------|--------|
| `--el-bg-color` | `var(--bg-card)` |
| `--el-bg-color-page` | `var(--bg-page)` |
| `--el-bg-color-overlay` | `var(--bg-card)` |
| `--el-text-color-primary` | `var(--text-primary)` |
| `--el-text-color-regular` | `var(--text-regular)` |
| `--el-text-color-secondary` | `var(--text-secondary)` |
| `--el-border-color` 及 `*-light*`/`*-dark*` 系列 | `var(--border-color)` / 同亮度微调 |
| `--el-fill-color-blank` | `var(--bg-card)` |
| `--el-color-primary` | `#059669`（不变） |
| `--el-color-primary-light-3` | `#103B2E` |
| `--el-color-primary-light-5` | `#0D553F` |
| `--el-color-primary-light-7` | `#0A6F50` |
| `--el-color-primary-light-8` | `#087C58` |
| `--el-color-primary-light-9` | `#078961` |
| `--el-color-primary-dark-2` | `#37AB87`（暗色下变亮，用于 hover） |
| `--el-mask-color` | `rgba(0, 0, 0, 0.7)` |

### 主题切换过渡
在 `global.css` 为 `body` 及主要容器添加 `transition: background-color 0.3s ease, color 0.3s ease, border-color 0.3s ease`，使切换平滑。

## What Changes
- 修改 `blog-admin/src/main.js`：引入 `element-plus/theme-chalk/dark/css-vars.css`
- 修改 `blog-admin/src/assets/styles/tokens.css`：新增 `html.dark` 令牌块（含 `--el-*` 对齐覆盖）
- 修改 `blog-admin/src/stores/app.js`：新增 `theme` 状态、`toggleTheme()`、`initTheme()`（localStorage + prefers-color-scheme 兜底）
- 修改 `blog-admin/src/App.vue`：挂载时调用 `initTheme()` 应用初始主题
- 修改 `blog-admin/src/layout/Header.vue`：新增主题切换按钮（Sunny/Moon 图标，带 tooltip）
- 修改 `blog-admin/src/views/Dashboard.vue`：ECharts 主题适配（watch theme，动态读取令牌色并更新图表 option）
- 修改 `blog-admin/src/assets/styles/global.css`：主题色过渡动画
- 排查 `src/` 内暗色下会失效的硬编码亮色（如 `#fff`、`white` 背景类），改引用令牌
- **BREAKING**：无（纯前端样式增强，接口与业务逻辑不变）

## Impact
- Affected specs: `refresh-admin-color-palette`（其定义的亮色令牌保持为 light 模式基准值，本 spec 在其上叠加暗色层）、`redesign-admin-ui`（令牌架构不变）
- Affected code:
  - `blog-admin/src/main.js`
  - `blog-admin/src/assets/styles/tokens.css`、`global.css`
  - `blog-admin/src/stores/app.js`
  - `blog-admin/src/App.vue`
  - `blog-admin/src/layout/Header.vue`
  - `blog-admin/src/views/Dashboard.vue`
- 不影响：后端、`blog-frontend`、`blog-app`、API 层、路由

## ADDED Requirements

### Requirement: 暗色主题令牌层
系统 SHALL 在 `tokens.css` 中提供 `html.dark` 选择器下的完整暗色令牌集（背景、文字、边框、阴影、EP 变量对齐），使所有引用令牌的组件在暗色模式下自动适配。

#### Scenario: 暗色令牌生效
- **WHEN** `<html>` 元素带有 `dark` 类
- **THEN** `var(--bg-page)` 解析为 `#0B0F0E`，`var(--bg-card)` 解析为 `#131A18`

#### Scenario: Element Plus 组件暗色渲染
- **WHEN** 切换到暗色模式后渲染表格、表单、下拉、对话框等 EP 组件
- **THEN** 组件背景/文字/边框使用暗色令牌（无白色残留）

### Requirement: 主题状态管理与持久化
系统 SHALL 在 Pinia `app` store 中维护 `theme` 状态，切换时同步 `html.dark` 类与 `localStorage('blog-admin-theme')`。

#### Scenario: 切换并持久化
- **WHEN** 用户点击 Header 的主题切换按钮
- **THEN** `html` 类立即切换、页面平滑过渡、localStorage 更新

#### Scenario: 刷新恢复
- **WHEN** 页面刷新且 localStorage 存在主题记录
- **THEN** 恢复上次主题，无闪白

#### Scenario: 首次访问跟随系统
- **WHEN** 无 localStorage 记录且系统为 `prefers-color-scheme: dark`
- **THEN** 默认进入暗色模式

### Requirement: 主题切换 UI 入口
Header 右侧工具区 SHALL 提供日/月图标按钮，亮色时显示 Moon（点击转暗），暗色时显示 Sunny（点击转亮），带 tooltip。

#### Scenario: 图标联动
- **WHEN** 主题为 light
- **THEN** 按钮显示 Moon 图标，tooltip 为"切换暗色模式"（反之亦然）

### Requirement: ECharts 图表主题适配
Dashboard 的趋势图/分类图/标签图 SHALL 在主题切换时更新坐标轴线、标签文字等主题相关颜色并重绘。

#### Scenario: 图表颜色随主题更新
- **WHEN** 在 Dashboard 页面切换主题
- **THEN** 图表坐标轴与文字颜色变为暗色适配值（如轴线 `#24302C`），图表无亮色残留

## MODIFIED Requirements

### Requirement: 设计令牌系统
令牌定义方式从单一 `:root` 变为「`:root` 亮色基准 + `html.dark` 暗色覆盖」，亮色值不变（维持 `refresh-admin-color-palette` 的森林绿方案）。
