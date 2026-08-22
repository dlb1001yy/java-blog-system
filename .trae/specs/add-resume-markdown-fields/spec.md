# 简历描述字段支持 Markdown Spec

## Why
我的简历编辑页（ProfileResume.vue）中，个人简介、自我评价、工作描述、项目描述、教育背景描述均为纯文本 textarea，用户无法使用 Markdown 排版；简历预览（ResumePreview 组件）和分享的简历页（Resume.vue 分享模式，复用同一组件）展示这些字段时也仅按纯文本渲染，换行与格式丢失。

## What Changes
- `blog-frontend/src/components/ResumePreview.vue`：
  - 引入 `@/utils/markdown` 的 md 实例，新增 `renderMd(text)` 辅助函数（空值返回 ''）。
  - 四种风格（modern / classic / sidebar / bold）中以下字段由 `{{ text }}` 改为 `v-html="renderMd(...)"`，并加 `md-text` 类：
    - `resume.summary`（个人简介）
    - `resume.selfEvaluation`（自我评价）
    - `w.description`（工作描述）
    - `p.description`（项目描述）
    - `e.description`（教育背景描述，仅 sidebar/classic 等展示处；modern/bold 风格未展示该字段则无需改）
  - 为 `.md-text` 添加 scoped 样式（`p { margin: 4px 0; }`、`ul/ol { padding-left: 20px; }` 等，保证 Markdown 元素排版紧凑）。
- `blog-frontend/src/views/ProfileResume.vue`：
  - 上述 5 个 textarea 输入框的 placeholder 补充"支持 Markdown"提示。

## Impact
- Affected specs: `refactor-resume-management`、`fix-resume-description-format`（格式演进的后续）
- Affected code: `blog-frontend/src/components/ResumePreview.vue`、`blog-frontend/src/views/ProfileResume.vue`
- 兼容性：markdown-it 对纯文本（含换行）渲染正常，旧数据无需迁移。
- 注意：markdown.js 实例开启 `html: true`，内容来自本人编辑的简历，与现有文章渲染策略一致。

## ADDED Requirements

### Requirement: 简历描述字段 Markdown 编辑与渲染
个人简介、自我评价、工作描述、项目描述、教育背景描述 SHALL 以 Markdown 语法编辑，并在简历预览与分享页以渲染后的 HTML 展示。

#### Scenario: 编辑输入 Markdown
- **WHEN** 用户在"我的简历"页的简介/自我评价/工作描述/项目描述/教育描述输入 Markdown 文本（如 `- 列表项`、`**加粗**`）
- **THEN** 输入框 placeholder 提示支持 Markdown，保存后原样存储

#### Scenario: 预览渲染 Markdown
- **WHEN** 打开简历预览抽屉（任一风格）或访问分享链接 `/resume/share/:token`
- **THEN** 上述字段按 Markdown 渲染（列表、加粗、链接等生效），纯文本内容显示不受影响

#### Scenario: 字段为空
- **WHEN** 某描述字段为空
- **THEN** 对应区域不渲染多余内容，布局不变
