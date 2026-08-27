# Tasks

- [x] Task 1: 修复 ExamTaking.vue 选项解析
  - [x] 1.1 在 `loadPaper` 中对 `res.data` 每道题的 `options` 字段做安全 JSON 解析（JSON.parse + try/catch），非数组或失败时回退 `[]`
  - [x] 1.2 验证单选（type=1）/多选（type=2）渲染 A/B/C/D 选项文本，与管理端一致
  - [x] 1.3 验证判断/填空/简答/编程题渲染不受影响，options 异常时页面不报错

# Task Dependencies
- 无（单任务）
