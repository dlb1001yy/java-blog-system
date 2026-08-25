# 03 脚本兼容存量表字段变更 Spec

## Why
`03-multi_modules.sql` 使用 `CREATE TABLE IF NOT EXISTS`，对已存在数据的老表（interview_question、exam_question 仍带旧 category/tags 列）不生效，需依赖单独的 10 号迁移脚本；用户希望 03 脚本自身即可在保留数据的前提下完成字段增删。

## What Changes
- 将 `03-multi_modules.sql` 中 interview_question、exam_question 的结构升级逻辑改为幂等双路径：
  - 新装环境：CREATE TABLE IF NOT EXISTS 建新结构表（保持现状）
  - 存量环境：对已存在的表执行幂等 ALTER（新增 category_id + 索引、按名称迁移数据回填 id、未匹配的分类/标签自动插入、tags 逗号串迁入 interview_question_tag、回填完成后删除旧 category/tags 列）
- 复用 10 号迁移脚本的存储过程思路（add_column_if_not_exists / 幂等数据迁移 / 安全删列），在 03 内以「表已存在时」执行；已执行过 10 号脚本的库再跑 03 不产生任何变更
- 示例数据部分保持幂等不变

## Impact
- Affected code: `blog-backend/sql/03-multi_modules.sql`（不新增文件、不改动 10 号脚本）

## ADDED Requirements

### Requirement: 03 脚本对存量表幂等升级字段
03-multi_modules.sql SHALL 在表已存在且有数据时，安全完成 category→category_id、tags→关联表的结构变更。

#### Scenario: 存量表升级
- **WHEN** 在已有数据的库上执行 03 脚本
- **THEN** 数据保留；category_id 按名称回填（未匹配自动建分类/标签）；tags 迁入 interview_question_tag；回填完成后删除旧列；重复执行无副作用

#### Scenario: 已跑过 10 号迁移脚本
- **WHEN** 库已通过 10 号脚本完成迁移后执行 03
- **THEN** 无任何变更、无报错

#### Scenario: 新装环境
- **WHEN** 空库执行 01→03
- **THEN** 直接建新结构表，示例数据正常插入
