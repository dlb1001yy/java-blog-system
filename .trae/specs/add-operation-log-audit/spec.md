# 后台操作日志审计 Spec

## Why
后台所有管理操作（文章增删改、用户管理、配置修改等）无任何审计记录，一旦出现误操作或安全问题无法追溯"谁在什么时间从什么 IP 做了什么"。

## What Changes
- 新增 `@Admin` 注解（含操作描述属性），标注于后台管理接口的写操作方法上
- 新增 `OperationLogAspect` 切面（`@Aspect`），拦截 `@Admin` 方法，异步落库到 `sys_operation_log` 表
- 新增 `sys_operation_log` 表（建表 SQL + 幂等迁移，兼容已有旧库部署）
- 新增实体 `OperationLog`、Mapper、`OperationLogService`（异步写入）
- 为 `controller/admin` 下所有非 GET 接口添加 `@Admin("操作描述")` 注解
- 新增后台分页查询接口 `GET /admin/operation-logs` 用于审计回溯
- **BREAKING**：无（纯增量功能，不影响现有接口行为）

## Impact
- Affected specs: 无已有 spec 受影响（纯新增能力）
- Affected code:
  - `blog-backend/sql/01-create_sql.sql`（新增表）
  - `blog-backend/src/main/java/com/dlbyy/blog/annotation/Admin.java`（新增）
  - `blog-backend/src/main/java/com/dlbyy/blog/aspect/OperationLogAspect.java`（新增）
  - `blog-backend/src/main/java/com/dlbyy/blog/entity/OperationLog.java`（新增）
  - `blog-backend/src/main/java/com/dlbyy/blog/mapper/OperationLogMapper.java`（新增）
  - `blog-backend/src/main/java/com/dlbyy/blog/service/OperationLogService.java` + impl（新增）
  - `blog-backend/src/main/java/com/dlbyy/blog/controller/admin/AdminOperationLogController.java`（新增）
  - `blog-backend/src/main/java/com/dlbyy/blog/config/AsyncConfig.java`（新增 `opLogExecutor` 线程池）
  - `blog-backend/src/main/java/com/dlbyy/blog/mapper/SchemaMapper.java` + `DataInitializer.java`（幂等建表迁移）
  - `controller/admin/*` 各控制器（添加 `@Admin` 注解）

## ADDED Requirements

### Requirement: 操作日志表
系统 SHALL 提供 `sys_operation_log` 表存储后台操作审计记录，字段包含：id、username（操作人）、operation（操作描述）、method（HTTP 方法）、uri（请求路径）、params（请求参数/请求体，截断存储）、ip（客户端 IP）、status（1 成功 / 0 失败）、error_msg（异常信息，截断存储）、cost_ms（耗时毫秒）、create_time。

#### Scenario: 新库初始化
- **WHEN** 全新环境执行 `01-create_sql.sql` 初始化数据库
- **THEN** `sys_operation_log` 表被创建

#### Scenario: 旧库升级
- **WHEN** 应用在已有旧数据的库上启动（DataInitializer 执行）
- **THEN** 通过 `CREATE TABLE IF NOT EXISTS` 幂等补建该表，不影响已有数据

### Requirement: @Admin 注解与审计切面
系统 SHALL 提供 `@Admin` 注解（`value` 属性为操作描述），并 SHALL 提供 `OperationLogAspect` 切面拦截所有标注 `@Admin` 的方法，记录操作日志。

#### Scenario: 写操作成功
- **WHEN** 已登录管理员调用任一标注 `@Admin` 的后台写接口且执行成功
- **THEN** 切面异步写入一条日志：username 取自 SecurityContext，operation 取注解描述，记录 HTTP 方法、URI、脱敏后的参数、IP、status=1、耗时

#### Scenario: 写操作失败
- **WHEN** 标注 `@Admin` 的方法抛出异常
- **THEN** 切面记录 status=0 及截断后的 error_msg，并原样抛出异常（不影响原有异常处理链）

#### Scenario: 日志写入失败不干扰业务
- **WHEN** 日志落库过程自身发生异常
- **THEN** 仅打印 WARN 日志，业务请求结果不受影响

#### Scenario: 敏感信息脱敏
- **WHEN** 请求参数中包含名为 password（不区分大小写）的字段
- **THEN** 序列化参数时该字段值以 `******` 替换后存储

#### Scenario: 异步写入
- **WHEN** 切面完成日志组装
- **THEN** 通过专用线程池 `opLogExecutor` 异步落库，不阻塞业务请求线程

### Requirement: 后台写操作全量覆盖
所有 `controller/admin` 包下的非 GET（POST/PUT/DELETE）接口方法 SHALL 标注 `@Admin` 并给出中文操作描述。

#### Scenario: 覆盖检查
- **WHEN** 检查 admin 控制器
- **THEN** 所有写操作方法均带 `@Admin` 注解，GET 查询接口不记录（避免噪音）

### Requirement: 审计查询接口
系统 SHALL 提供分页查询操作日志的后台接口，支持按操作人、操作描述关键字过滤。

#### Scenario: 分页查询
- **WHEN** 管理员调用 `GET /admin/operation-logs?current=1&size=10`
- **THEN** 返回按 create_time 倒序的分页日志列表

#### Scenario: 条件过滤
- **WHEN** 传入 `username` 或 `operation` 关键字参数
- **THEN** 返回匹配过滤条件的日志分页
