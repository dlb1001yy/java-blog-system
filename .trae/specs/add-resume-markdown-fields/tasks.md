# Tasks

- [x] Task 1: ResumePreview.vue 支持 Markdown 渲染
  - [x] SubTask 1.1: 引入 `@/utils/markdown`，新增 `renderMd` 辅助函数
  - [x] SubTask 1.2: 四种风格中将 summary、selfEvaluation、工作描述、项目描述、教育描述改为 `v-html="renderMd(...)"` 并加 `md-text` 类
  - [x] SubTask 1.3: 为 `.md-text` 添加 scoped 样式（p/ul/ol 等紧凑排版）
- [x] Task 2: ProfileResume.vue 输入提示
  - [x] SubTask 2.1: 5 个描述类 textarea 的 placeholder 加"支持 Markdown"提示
- [x] Task 3: 验证
  - [x] SubTask 3.1: GetDiagnostics 检查两个文件无错误
  - [x] SubTask 3.2: 核对 4 种风格均完成替换、无遗漏 `{{ }}` 描述字段

# Task Dependencies
- Task 1、Task 2 独立，可并行
- Task 3 依赖 Task 1、Task 2
