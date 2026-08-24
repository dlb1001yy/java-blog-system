# 面试题 Markdown 编辑与刷题端渲染 Spec

## Why
管理后台（blog-admin）新增/编辑面试题时，"解题思路"和"参考答案"仅为普通 textarea，无法编辑/预览 Markdown；前台（blog-frontend）刷题相关页面需要确认对这两个字段的 Markdown 渲染。

## What Changes
- blog-admin `InterviewQuestionList.vue`：对话框中"解题思路"（tips）与"参考答案"（answer）改为带工具栏 + 预览的 Markdown 编辑器（复用 ArticleEdit.vue 的 markdown-it 方案），不再使用纯 textarea。
- blog-frontend `Interview.vue`：已使用 `md.render` 渲染 answer/tips —— 校验并保持（必要时补全）Markdown 渲染。
- blog-frontend `Scores.vue`：已使用 `renderMd` 渲染解析/参考答案 —— 校验并保持。

## Impact
- Affected code:
  - `blog-admin/src/views/InterviewQuestionList.vue`
  - `blog-frontend/src/views/Interview.vue`（校验）
  - `blog-frontend/src/views/Scores.vue`（校验）
- 依赖：markdown-it、github-markdown-css（admin 已有）。

## ADDED Requirements
### Requirement: 面试题 Markdown 编辑
管理后台新增/编辑面试题时，"解题思路"和"参考答案"字段 SHALL 提供 Markdown 编辑器，包含常用格式工具栏（标题/粗斜体/代码/列表/引用/代码块/链接等）与实时预览切换。

#### Scenario: 编辑并预览
- **WHEN** 管理员在新增/编辑面试题对话框中编辑"解题思路"或"参考答案"
- **THEN** 可通过工具栏插入 Markdown 语法，并可切换预览查看渲染效果
- **WHEN** 保存
- **THEN** 原始 Markdown 文本正常提交，不改变后端接口

### Requirement: 前台刷题 Markdown 渲染
前台面试题刷题页展开题目时，"解题思路"和"参考答案" SHALL 以 Markdown 渲染展示；考试成绩页的解析/参考答案 SHALL 以 Markdown 渲染展示。

#### Scenario: 展开题目
- **WHEN** 用户在刷题页展开一道面试题
- **THEN** 答案与思路以渲染后的 HTML（markdown-body 样式）展示

## MODIFIED Requirements
### Requirement: 面试题表单字段
原"解题思路"为普通 textarea、"参考答案"为等宽字体 textarea；现两者均改为 Markdown 编辑器组件形式，字段名与数据结构不变。
