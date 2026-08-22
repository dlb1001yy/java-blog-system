# 简历描述字段 Markdown 编辑器 Spec

## Why
简历描述字段（个人简介、自我评价、工作描述、项目描述、教育背景描述）当前是普通 textarea，仅靠 placeholder 提示"支持 Markdown"，缺少编辑体验；需求是使用真正的 Markdown 编辑器，粘贴 Markdown 格式数据能直接保留并正常渲染展示。

## What Changes
- 新建 `blog-frontend/src/components/MarkdownEditor.vue`（可复用组件，v-model:String）：
  - 工具栏按钮：加粗、斜体、标题（H2）、无序列表、有序列表、链接、代码块，对选中文本包裹/插入对应 Markdown 语法。
  - 编辑区：普通 textarea（等宽字体、Tab 插入两空格），支持粘贴 Markdown 文本原样保留。
  - 实时预览：使用 `@/utils/markdown` 的 md 实例渲染（复用现有 `md-text` 排版思路），编辑/预览左右分栏，可折叠预览。
- `blog-frontend/src/views/ProfileResume.vue`：
  - 5 个描述 textarea（form.summary、form.selfEvaluation、work.description、project.description、edu.description）替换为 `<MarkdownEditor v-model="..." />`（高度约 200px / 150px）。

## Impact
- Affected specs: `add-resume-markdown-fields`（编辑体验增强，展示渲染已在上一变更完成）
- Affected code: 新增 `blog-frontend/src/components/MarkdownEditor.vue`，修改 `blog-frontend/src/views/ProfileResume.vue`
- 兼容性：v-model 为纯 Markdown 字符串，保存/展示链路不变，旧数据正常打开。

## ADDED Requirements

### Requirement: Markdown 编辑器
描述字段 SHALL 通过带工具栏和实时预览的 Markdown 编辑器编辑，粘贴 Markdown 文本原样保留。

#### Scenario: 粘贴 Markdown
- **WHEN** 用户在编辑器中粘贴 `**加粗**`、`- 列表` 等 Markdown 文本
- **THEN** 内容原样保留在编辑框中，预览区实时显示渲染效果

#### Scenario: 工具栏格式化
- **WHEN** 选中文本后点击加粗/列表/链接等工具栏按钮
- **THEN** 选中文本被正确包裹对应 Markdown 语法

#### Scenario: 保存与展示
- **WHEN** 编辑后保存简历并打开预览或分享页
- **THEN** 描述字段按 Markdown 渲染（沿用 ResumePreview 已有 renderMd 渲染）

#### Scenario: 旧数据
- **WHEN** 打开已保存的纯文本描述
- **THEN** 编辑器正常加载内容，预览兼容纯文本
