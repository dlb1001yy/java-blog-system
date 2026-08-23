# Tasks
- [x] Task 1: 前端 ExamQuestionList.vue 表单分类改为下拉选择
  - [x] 引入 categoryApi 并在页面加载时调用 `getAll()` 获取分类列表
  - [x] 表单"分类"由 `el-input` 改为 `el-select`，选项 label/value 均为分类 `name`
  - [x] 校验规则改为 required + change 触发，提示"请选择分类"
- [x] Task 2: 前端 ExamPaperList.vue 题目筛选分类改为可清空下拉选择
  - [x] 引入 categoryApi 获取分类列表
  - [x] 筛选"分类"由 `el-input` 改为可清空 `el-select`
- [x] Task 3: 后端模板下载增加分类下拉数据验证
  - [x] AdminExamQuestionController.template 注入 CategoryService，获取全部分类名称
  - [x] 使用 POI DataValidation 为"分类"列（第 3 列，示例行范围）设置下拉列表
  - [x] 示例行分类值改用实际存在的分类名称
- [x] Task 4: 后端批量导入校验分类
  - [x] ExamQuestionServiceImpl.importFromExcel 获取全部分类名称集合
  - [x] 校验每行分类必填且在集合中，不通过则记录错误"分类必须为分类管理中已有的分类"
- [x] Task 5: 验证
  - [x] 前端构建通过；后端编译通过（mvn compile）
  - [ ] 手动验证：表单/试卷筛选分类下拉、模板分类列下拉、导入无效分类报错

# Task Dependencies
- Task 1、2 可并行；Task 3、4 可并行；Task 5 依赖全部
