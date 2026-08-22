# Tasks

- [x] Task 1: 新建 MarkdownEditor.vue 组件
  - [x] SubTask 1.1: 实现工具栏（加粗/斜体/H2/无序列表/有序列表/链接/代码块）对 textarea 选中文本的包裹插入逻辑
  - [x] SubTask 1.2: 实时预览（md 实例渲染）+ 预览开关、左右分栏布局与样式
- [x] Task 2: ProfileResume.vue 替换 5 个描述输入框为 MarkdownEditor
  - [x] SubTask 2.1: form.summary、form.selfEvaluation
  - [x] SubTask 2.2: work.description、project.description、edu.description（动态条目内）
- [x] Task 3: 验证
  - [x] SubTask 3.1: GetDiagnostics 无错误
  - [x] SubTask 3.2: 确认 v-model 双向绑定、保存数据仍为 Markdown 字符串

# Task Dependencies
- Task 2 依赖 Task 1
- Task 3 依赖 Task 1、Task 2
