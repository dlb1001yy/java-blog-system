# Tasks
- [x] Task 1: 修复 UserMapper.countByRole 表名错误：SQL 中 `user` 改为 `sys_user`（注：sys_user 表无 is_deleted 列，User 实体不继承 BaseEntity 无逻辑删除，故不加过滤）
- [x] Task 2: 验证修复：直接执行修正后的 SQL（mysql 客户端）确认可查询且返回按角色分组计数；确认无其他地方引用 `FROM user`

# Task Dependencies
- Task 2 depends on Task 1
