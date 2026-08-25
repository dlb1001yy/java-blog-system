# Tasks
- [x] Task 1: 数据库迁移脚本
  - [x] 1.1 新增 SQL：interview_question 增加 category_id 并按 name 迁移；新建 interview_question_tag 关联表并从 tags 字符串迁移；exam_question 增加 category_id 并迁移；迁移后删除旧列，脚本幂等
- [x] Task 2: 后端面试题改造
  - [x] 2.1 InterviewQuestion 实体：category→categoryId，tags→tagIds（非表字段）+ tagNames/categoryName（返回字段）；新增 InterviewQuestionTag 实体与 Mapper
  - [x] 2.2 InterviewQuestionServiceImpl：adminSave 按 id 校验落库、维护关联表；导入时 name→id（getOrCreateByName）；分页查询按 categoryId 筛选并填充名称返回；listEnabledCategories 改为返回分类对象
  - [x] 2.3 AdminInterviewQuestionController / PortalInterviewController：参数与返回调整（portal 支持逗号分隔多选 categoryId）
- [x] Task 3: 后端题库（exam_question）改造
  - [x] 3.1 ExamQuestion 实体与 Service：category→categoryId，保存校验、查询填充 categoryName
  - [x] 3.2 AdminExamQuestionController / PortalExamController：参数与返回调整；组卷/成绩明细同步填充 categoryName
- [x] Task 4: 前端 blog-admin 改造
  - [x] 4.1 InterviewQuestionList.vue：下拉/多选 value 改 id，提交 categoryId+tagIds，回显与展示改名称字段，Markdown 导入保持传名称由后端转换
  - [x] 4.2 ExamQuestionList.vue：分类下拉与筛选改 id
- [x] Task 5: 前台门户兼容
  - [x] 5.1 blog-frontend Interview.vue 改为 categoryId 多选（后端兼容 List）；blog-app 无相关调用无需改动
- [x] Task 6: 验证
  - [x] 6.1 后端编译、blog-admin/blog-frontend 构建通过；核对各页面字段一致性

# Task Dependencies
- Task 1 先行；Task 2、3 依赖 Task 1（可并行）；Task 4、5 依赖后端接口定型；Task 6 最后
