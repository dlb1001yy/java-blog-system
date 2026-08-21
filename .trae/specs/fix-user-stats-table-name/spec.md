# Fix User Stats Table Name Spec

## Why
用户管理页查询统计时后端报 `Table 'dlbyy_zp_blog.user' doesn't exist`。根因是 `UserMapper.countByRole()` 的 SQL 写错了表名：实体 `User` 映射的是 `sys_user`，SQL 却用了 `user`。

## What Changes
- 修正 `UserMapper.countByRole()` SQL：表名 `user` → `sys_user`，并补 `WHERE is_deleted = 0` 与实体 `@TableLogic` 行为一致
- 后端重启后验证 `/admin/user/stats` 与用户管理页统计正常

## Impact
- Affected specs: refactor-to-multi-function-site（用户管理模块）
- Affected code: `blog-backend/src/main/java/com/dlbyy/blog/mapper/UserMapper.java`

## ADDED Requirements

### Requirement: 用户统计查询使用正确表名
`countByRole()` 查询 SHALL 在 `sys_user` 表上执行，并排除逻辑删除用户（`is_deleted = 0`）。

#### Scenario: 管理端查询用户统计
- **WHEN** 管理员打开用户管理页或 Dashboard 触发 `userStats()`
- **THEN** 后端按角色分组统计返回，无 SQLSyntaxErrorException，结果与用户列表口径一致（不含已删除用户）
