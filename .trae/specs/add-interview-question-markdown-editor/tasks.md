# Tasks
- [x] Task 1: blog-admin 面试题对话框接入 Markdown 编辑器
  - [x] 1.1 在 InterviewQuestionList.vue 中引入 markdown-it 与 github-markdown-css（参照 ArticleEdit.vue）
  - [x] 1.2 为"解题思路"（tips）与"参考答案"（answer）实现带工具栏（标题/粗斜体/代码/列表/引用/代码块/链接/图片）的编辑区
  - [x] 1.3 为两个字段分别提供编辑/预览切换与渲染预览（markdown-body 样式）
  - [x] 1.4 保持表单字段名、校验与提交逻辑不变
- [x] Task 2: blog-frontend 刷题端 Markdown 渲染校验
  - [x] 2.1 校验 Interview.vue 展开题目时 tips/answer 均通过 md.render 渲染且样式为 markdown-body（通过，无需修改）
  - [x] 2.2 校验 Scores.vue 成绩详情中解析/参考答案通过 renderMd 渲染（发现正确答案行遗漏，已修复为 Markdown 渲染）
  - [x] 2.3 修复遗漏：Scores.vue 正确答案/参考答案行由纯文本改为 renderMd + v-html 渲染

# Task Dependencies
- Task 1 与 Task 2 相互独立，可并行。
