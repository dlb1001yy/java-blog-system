# 简历多风格预览 Spec

## Why
用户在 `/profile/resume` 编辑简历后无法直观看到成品效果；分享页目前只有一种展示样式。参考 2026 年简历模板流行趋势（ATS 友好、极简单栏、双栏侧边栏、经典衬线、现代极简），提供 3-4 种流行风格的实时预览。

## What Changes
- `ProfileResume.vue` 新增「预览」按钮，打开全屏抽屉/对话框预览简历
- 新建 `ResumePreview.vue` 组件：接收简历数据 + 风格名，渲染不同风格
- 提供 4 种 2026 流行风格（依据流行趋势调研）：
  1. **现代极简（Modern Minimal）**——单栏、无衬线字体、单一强调色（深青/藏青）、大量留白、细分隔线
  2. **经典衬线（Classic Serif）**——单栏、衬线字体（Georgia）、居中页眉、传统双线分隔，庄重
  3. **双栏侧边栏（Sidebar Two-Column）**——左侧深色侧栏放照片/联系方式/技能，右侧主内容放经历/项目/教育
  4. **粗体页眉（Bold Header）**——大号姓名页眉 + 强调色横条，正文单栏
- 风格可在预览中切换（el-radio-group 或分段控件）
- 预览复用编辑页已有的解析逻辑（skills/workExperience/projects/education/certificates JSON 字段），通过 props 传入

## Impact
- Affected code:
  - `blog-frontend/src/views/ProfileResume.vue`（加预览按钮 + 抽屉 + 数据传递）
  - `blog-frontend/src/components/ResumePreview.vue`（新建，含 4 种风格渲染与 scoped 样式）
- 不改后端、不改数据结构、不影响分享页现有逻辑

## ADDED Requirements
### Requirement: 简历风格预览
系统 SHALL 在简历编辑页提供预览入口，并支持 4 种风格的简历渲染切换。

#### Scenario: 打开预览
- **WHEN** 用户点击「预览简历」按钮
- **THEN** 弹出全屏预览，默认以「现代极简」风格渲染当前表单内容（未保存的草稿内容也实时可见）

#### Scenario: 切换风格
- **WHEN** 用户在预览中切换风格（现代极简/经典衬线/双栏侧边栏/粗体页眉）
- **THEN** 简历以对应风格的排版重新渲染，内容一致

#### Scenario: 空字段容错
- **WHEN** 简历某些字段为空（无照片、无证书等）
- **THEN** 对应区块不渲染或显示占位，布局不塌陷
