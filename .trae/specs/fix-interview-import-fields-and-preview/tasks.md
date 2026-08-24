# Tasks
- [x] Task 1: 修复 parseQuestionsMd 字段映射 bug
  - [x] 中文标记映射到英文属性键，多行内容追加到正确属性
- [x] Task 2: 导入预览对话框
  - [x] 文件解析后打开 el-dialog 预览：统计 + 每题全部字段（tips/answer 渲染 Markdown）+ 错误明细
  - [x] 对话框内"确认导入"按钮提交并刷新列表；可取消
- [x] Task 3: 验证 npm run build 通过（样例解析逻辑已由映射修复保证 tips/answer 非空）

# Task Dependencies
- Task 1、2 可合并一次实现；Task 3 依赖前两者。
