# Tasks
- [x] Task 1: 模板题型下拉与示例改为名称
  - [x] AdminExamQuestionController：题型下拉选项改为 单选题/多选题/判断题/填空题/简答题/编程题
  - [x] 表头说明改为"题型(单选题/多选题/判断题/填空题/简答题/编程题)"
  - [x] 示例行题型值改为"单选题"/"简答题"
- [x] Task 2: 导入解析支持题型名称
  - [x] ExamQuestionServiceImpl：新增题型名称→数字映射（含"单选"等简写与数字 1-6 兼容），解析方法供 validateRow 与 setType 复用
  - [x] 错误提示更新为名称格式
- [x] Task 3: 验证 mvn compile 通过
