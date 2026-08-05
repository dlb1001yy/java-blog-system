# 简历描述字段格式统一 Spec

## Why
`blog-frontend` 简历页中，个人简介使用 `v-html` + `summary-text` 类（含 `white-space: pre-line`）能正确渲染换行和富文本，但工作经历的描述、项目经验的描述仍用 `{{ }}` 纯文本 + 无 `pre-line` 的 CSS 类，导致用户在后台输入的换行符丢失、多段文本挤成一行。需要把这两个描述字段统一为与个人简介一致的显示格式。

## What Changes
- `blog-frontend/src/views/Resume.vue` 中：
  - 工作经历描述：`<p class="timeline-desc">{{ work.description }}</p>` → `<p class="summary-text" v-html="work.description"></p>`
  - 项目经验描述：`<p class="project-desc">{{ project.description }}</p>` → `<p class="summary-text" v-html="project.description"></p>`
- 自我评价已使用 `summary-text` + `v-html`，无需改动。

## Impact
- Affected specs: `refactor-resume-management`（简历重构的后续格式微调）
- Affected code: `blog-frontend/src/views/Resume.vue`（2 行模板改动）
- 兼容性：纯文本内容在 `v-html` + `pre-line` 下显示正常（换行符生效）；若内容含 HTML 标签也会被渲染。

## ADDED Requirements

### Requirement: 描述字段统一显示格式
工作经历描述和项目经验描述 SHALL 与个人简介使用相同的显示格式（`v-html` 渲染 + `summary-text` CSS 类，支持换行和富文本）。

#### Scenario: 工作经历描述含换行
- **WHEN** 后台填写的工作描述为多段文本（含 `\n` 换行）
- **THEN** 前台工作经历卡片中描述按多段显示，换行符生效，与个人简介格式一致

#### Scenario: 项目经验描述含换行
- **WHEN** 后台填写的项目描述为多段文本（含 `\n` 换行）
- **THEN** 前台项目经验卡片中描述按多段显示，换行符生效，与个人简介格式一致

#### Scenario: 描述为空
- **WHEN** 工作描述或项目描述为空字符串
- **THEN** 该 `<p>` 标签渲染为空内容，不影响布局（与当前行为一致）
