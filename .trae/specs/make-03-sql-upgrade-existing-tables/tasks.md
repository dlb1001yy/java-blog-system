# Tasks
- [x] Task 1: 改造 03-multi_modules.sql 支持存量表升级
  - [x] 1.1 新增"存量表升级"段：检测旧 category/tags 列则执行加列、名称迁移回填（自动建分类/标签、显式 COLLATE）、关联表迁移、安全删旧列；exam_question 同理
  - [x] 1.2 与 10 号脚本等价幂等：10 号脚本补旧列守卫，任一顺序执行均无副作用；空库行为不变
- [x] Task 2: 验证
  - [x] 2.1 语法与逻辑走查：空库、存量未迁移库、已迁移库三种场景

# Task Dependencies
- Task 2 依赖 Task 1
