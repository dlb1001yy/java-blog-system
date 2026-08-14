# Blog-Admin 配色焕新 Spec（深森林绿系）

## Why
当前 blog-admin 使用靛蓝/紫色配色（#6366F1 → #8B5CF6），这是 2021-2024 年 SaaS 产品最泛滥的配色方案。据 2026 年设计趋势研究，该色系已被"每个 AI 产品趋同使用"，导致强烈的模板感和廉价感（用户反馈"太土"）。需要替换为 2026 年第一大色彩趋势——**深森林绿（Deep Forest Green）**，搭配暖中性背景，让后台沉稳、成熟、有辨识度，同时降低长时间使用的视觉疲劳。

## 新配色方案

### 主色映射

| 令牌 | 旧值（靛蓝） | 新值（翡翠/森林绿） |
|------|-------------|-------------------|
| `--color-primary` | `#6366F1` | `#059669` |
| `--color-primary-light` | `#818CF8` | `#10B981` |
| `--color-primary-dark` | `#4F46E5` | `#047857` |
| `--color-secondary` | `#06B6D4` | `#0D9488` |
| `--color-accent` | `#8B5CF6` | `#14B8A6` |

### 渐变

| 令牌 | 旧值 | 新值 |
|------|------|------|
| `--gradient-primary` | `linear-gradient(135deg, #6366F1 0%, #8B5CF6 100%)` | `linear-gradient(135deg, #059669 0%, #14B8A6 100%)` |
| `--gradient-sidebar` | `linear-gradient(180deg, #1E1B4B 0%, #312E81 100%)` | `linear-gradient(180deg, #0F2A23 0%, #143D31 100%)` |

### 暖中性背景（从冷蓝灰切换到暖石色）

| 令牌 | 旧值 | 新值 |
|------|------|------|
| `--bg-page` | `#F8FAFC` | `#FAFAF9` |
| `--bg-subtle` | `#F1F5F9` | `#F5F5F4` |
| `--text-primary` | `#0F172A` | `#1C1917` |
| `--text-regular` | `#475569` | `#57534E` |
| `--text-secondary` | `#94A3B8` | `#A8A29E` |
| `--border-color` | `#E2E8F0` | `#E7E5E4` |

### 主色阴影

| 令牌 | 旧值 | 新值 |
|------|------|------|
| `--shadow-primary` | `rgba(99, 102, 241, 0.35)` | `rgba(5, 150, 105, 0.35)` |

### Element Plus 主题覆盖

| 令牌 | 旧值 | 新值 |
|------|------|------|
| `--el-color-primary` | `#6366F1` | `#059669` |
| `--el-color-primary-light-3` | `#818CF8` | `#34D399` |
| `--el-color-primary-light-5` | `#A5B4FC` | `#6EE7B7` |
| `--el-color-primary-light-7` | `#C7D2FE` | `#A7F3D0` |
| `--el-color-primary-light-8` | `#E0E7FF` | `#D1FAE5` |
| `--el-color-primary-light-9` | `#EEF2FF` | `#ECFDF5` |
| `--el-color-primary-dark-2` | `#4F46E5` | `#047857` |

### 硬编码颜色替换规则

项目中散落的硬编码靛蓝色值（`rgba(99, 102, 241, ...)` / `#6366F1` / `#818CF8` 等）全部替换为对应的森林绿色值：
- `rgba(99, 102, 241, X)` → `rgba(5, 150, 105, X)`
- `rgba(139, 92, 246, X)` → `rgba(20, 184, 166, X)`
- `#6366F1` → `#059669`
- `#818CF8` → `#10B981`
- `#4F46E5` → `#047857`
- `#8B5CF6` → `#14B8A6`
- `#06B6D4` → `#0D9488`

## What Changes
- 修改 `src/assets/styles/tokens.css`：将全部颜色令牌从靛蓝/紫色切换到森林绿/翡翠色系，中性色从冷蓝灰切换到暖石色
- 修改 `src/assets/styles/global.css`：滚动条半透明色值从靛蓝 rgba 替换为森林绿 rgba
- 修改 `src/layout/index.vue`：主内容区渐变光斑的 rgba 色值替换
- 修改 `src/layout/Header.vue`：头像描边阴影 rgba 色值替换
- 修改 `src/views/Login.vue`：品牌区渐变背景、输入框聚焦阴影、登录按钮阴影的硬编码色值替换
- 修改 `src/views/Dashboard.vue`：ECharts 图表色值（柱状图/面积图/饼图）、统计卡渐变背景替换
- 修改 `src/views/ArticleEdit.vue`、`src/views/ResumeEdit.vue`、`src/views/Settings.vue`：输入框聚焦阴影的硬编码 rgba 替换
- 不改后端接口、不改路由结构、不改业务逻辑，仅替换色值
- **BREAKING**：无（仅样式色值替换，功能行为不变）

## Impact
- Affected specs: `redesign-admin-ui`（配色方案被修改，但设计令牌架构不变）
- Affected code:
  - `src/assets/styles/tokens.css`（核心令牌文件）
  - `src/assets/styles/global.css`
  - `src/layout/index.vue`、`src/layout/Header.vue`
  - `src/views/Login.vue`、`src/views/Dashboard.vue`
  - `src/views/ArticleEdit.vue`、`src/views/ResumeEdit.vue`、`src/views/Settings.vue`
- 不影响：`src/api/*`、`src/stores/*`、`src/router/*`、后端代码

## ADDED Requirements

### Requirement: 深森林绿配色方案
系统 SHALL 在 `tokens.css` 中将主色系从靛蓝/紫色（#6366F1 / #8B5CF6）替换为翡翠/森林绿（#059669 / #14B8A6），中性背景从冷蓝灰切换为暖石色，使整体视觉沉稳、成熟、有辨识度。

#### Scenario: 主色令牌生效
- **WHEN** 任意组件使用 `var(--color-primary)`
- **THEN** 解析为 `#059669`

#### Scenario: Element Plus 组件主题
- **WHEN** 渲染 Element Plus 主按钮、链接、激活态
- **THEN** 使用 `#059669` 主色（通过 `--el-color-primary` 覆盖）

## MODIFIED Requirements

### Requirement: 设计令牌系统
令牌架构不变，仅色值更新：主色 → 森林绿系，中性色 → 暖石色系，渐变 → 翡翠绿渐变。

### Requirement: 硬编码色值统一
项目中所有散落的硬编码靛蓝/紫色 rgba 与 hex 值 SHALL 全部替换为对应的森林绿色值，确保无靛蓝残留。

#### Scenario: 全局搜索无靛蓝残留
- **WHEN** 在 `src/` 目录搜索 `6366F1` 或 `rgba(99, 102, 241`
- **THEN** 返回零结果
