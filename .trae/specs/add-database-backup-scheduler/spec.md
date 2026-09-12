# 通用定时任务管理 + 数据库定时备份 Spec

## Why
项目当前没有任何定时任务能力，也没有自动备份（仅 README 记录手动 mysqldump 命令）。需要一套通用的、可在管理后台维护的定时任务框架（动态 cron、启用/暂停、立即执行），并以「每天凌晨 4 点全库备份、滚动保留一个月」作为第一个内置任务；同时提供备份管理页面（列表/手动备份/删除/还原）。

## What Changes
- 后端启用 `@EnableScheduling`，新增**动态定时任务框架**：任务以 Spring Bean 形式实现统一 SPI 接口注册，`sys_scheduled_task` 表持久化每个任务的 cron、状态、执行情况；运行期由 `DynamicTaskManager`（基于 `TaskScheduler`）支持动态注册/取消/改 cron/暂停恢复/立即执行，改配置即时生效、无需重启
- 新增内置任务「数据库备份」：默认 cron `0 0 4 * * ?`，纯 Java JDBC 逻辑备份（`SHOW CREATE TABLE` + `SELECT` 导出 SQL，gzip 压缩 `.sql.gz`），不依赖 mysqldump，Windows 开发与 Docker 均可运行；备份后自动清理超过保留期（默认 30 天）的旧备份
- 新增备份记录表 `sys_backup_record`；备份/还原核心服务与管理接口
- 新增后台接口：`AdminTaskController`（任务分页/修改 cron/启用暂停/立即执行）与 `AdminBackupController`（备份分页/手动备份/删除/还原），写操作全部接入操作日志审计（`@Admin`）
- 管理后台新增两个页面：「定时任务」（管理任务）与「数据备份」（管理备份文件），含路由与侧边栏菜单
- Docker：`docker-compose.yml` 为 blog-backend 挂载 `backup_data` 卷持久化 `/app/backups`
- `.env.example` / `application*.yaml` 增加备份与任务相关配置项

## Impact
- Affected specs: 无既有 spec 直接受影响；与 `add-operation-log-audit`（操作日志）、`deploy-with-docker-compose`（compose 编排）有衔接
- Affected code:
  - `blog-backend/src/main/java/com/dlbyy/blog/JavaBlogApplication.java`（启用调度）
  - 新增 `task/` 包：`ScheduledTaskSpi` 接口、`DynamicTaskManager`、`task/DatabaseBackupTask`
  - 新增 `service/BackupService.java`、`controller/admin/AdminTaskController.java`、`controller/admin/AdminBackupController.java`、`entity/ScheduledTaskRecord.java`、`entity/BackupRecord.java`、`mapper/ScheduledTaskMapper.java`、`mapper/BackupRecordMapper.java`
  - `blog-backend/src/main/java/com/dlbyy/blog/config/DataInitializer.java`、`mapper/SchemaMapper.java`（幂等建表 + 内置任务种子数据）
  - `blog-backend/src/main/resources/application.yaml`、`application-docker.yaml`
  - `docker-compose.yml`（新增 backup_data 卷）
  - `blog-admin/src/router/index.js`、`layout/Sidebar.vue`、新增 `src/api/task.js`、`src/api/backup.js`、`src/views/TaskManage.vue`、`src/views/BackupManage.vue`
  - `.gitignore`（忽略本地 backups 目录）

## ADDED Requirements

### Requirement: 定时任务 SPI 注册
系统 SHALL 提供统一接口（如 `ScheduledTaskSpi`：taskKey / taskName / description / run()），所有内置任务以 Spring Bean 实现该接口自动注册；新增任务只需实现接口并在种子数据中登记。

#### Scenario: 启动时同步
- **WHEN** 应用启动
- **THEN** `sys_scheduled_task` 表幂等创建；已注册 SPI Bean 但表中无记录的任务自动插入默认记录（status=1 启用）；表中存在但 Bean 缺失的任务跳过执行并告警日志

### Requirement: 动态调度管理
系统 SHALL 基于 `TaskScheduler` 在运行期管理任务生命周期，配置变更即时生效、无需重启。

#### Scenario: 修改 cron
- **WHEN** 管理员调用 `PUT /admin/tasks/{id}/cron` 传入新 cron（服务端用 `CronExpression.isValidExpression` 校验）
- **THEN** 校验失败返回 400；成功则更新 `sys_scheduled_task.cron_expression`，取消旧调度并按新 cron 重新注册，操作写入操作日志

#### Scenario: 暂停/恢复
- **WHEN** 调用 `PUT /admin/tasks/{id}/pause` 或 `/resume`
- **THEN** 更新状态并取消/恢复对应调度，操作写入操作日志

#### Scenario: 立即执行
- **WHEN** 调用 `POST /admin/tasks/{id}/run`
- **THEN** 在调度线程池中异步触发一次 run()，立即返回「已触发」；执行结果（成功/失败、错误信息、结束时间）回写 `last_execute_*` 字段

#### Scenario: 执行记录回写
- **WHEN** 任意一次任务执行（定时或手动）结束
- **THEN** 回写 last_execute_time / last_execute_status / last_execute_error；执行异常仅记日志不影响下次调度

### Requirement: 任务列表
管理员 SHALL 可分页查看定时任务。

#### Scenario: 分页查询
- **WHEN** 调用 `GET /admin/tasks/page`
- **THEN** 返回任务名、taskKey、描述、cron、状态（启用/暂停）、下次触发时间、最近执行时间/状态/错误信息，按 taskKey 排序

### Requirement: 内置任务——数据库每日备份
系统 SHALL 内置「数据库备份」任务（taskKey=databaseBackup，默认 cron `0 0 4 * * ?`，默认启用，种子数据幂等写入）。

#### Scenario: 定时触发成功
- **WHEN** 到达 cron 触发时间
- **THEN** 遍历数据库所有表导出 `DROP TABLE IF EXISTS` + `CREATE TABLE` + `INSERT`，gzip 压缩写入备份目录，文件名 `backup_yyyyMMdd_HHmmss.sql.gz`
- **THEN** 在 `sys_backup_record` 写入 type=auto 的成功记录（含文件大小），任务执行状态回写 success

#### Scenario: 备份失败
- **WHEN** 备份过程异常
- **THEN** 写入 type=auto、status=failed 记录与错误信息，清理残留的部分文件，任务执行状态回写 failed，不影响下次调度

#### Scenario: 防止并发
- **WHEN** 备份执行中再次触发（定时与手动重叠）
- **THEN** 后一次直接跳过并告警日志

### Requirement: 备份滚动保留一个月
系统 SHALL 在每次备份完成后删除超过保留期（默认 30 天，`blog.backup.retention-days` 可调）的备份文件及其记录。

### Requirement: 手动备份
管理员 SHALL 可通过 `POST /admin/backups` 立即触发一次备份，type=manual，操作写入操作日志。

### Requirement: 备份列表
管理员 SHALL 可分页查看备份记录（`GET /admin/backups/page`）：文件名、大小、类型、状态、错误信息，按备份时间倒序。

### Requirement: 删除备份
管理员 SHALL 可删除指定备份（`DELETE /admin/backups/{id}`）：同时删除文件（文件缺失时仅删记录并提示）与记录，操作写入操作日志。

### Requirement: 还原备份
管理员 SHALL 可将数据库还原到指定备份时点（`POST /admin/backups/{id}/restore`）。

#### Scenario: 还原成功
- **WHEN** 备份文件存在且 status=success
- **THEN** 解压并按语句顺序执行 SQL（dump 头部含 `SET NAMES utf8mb4`、`SET FOREIGN_KEY_CHECKS=0`），成功后操作写入操作日志（`@Admin("还原数据库")`）

#### Scenario: 还原失败
- **WHEN** 还原中途 SQL 执行失败
- **THEN** 停止执行、返回明确错误（含失败语句位置）并记日志；已执行部分保持该时刻状态（MySQL DDL 无事务，属预期）

### Requirement: 备份配置
系统 SHALL 支持以下配置（均有默认值）：`blog.backup.dir`（本地 `./backups`，docker 为 `/app/backups`）、`blog.backup.retention-days`（默认 30）。任务开关与 cron 统一由 `sys_scheduled_task` 管理，不再单独配置 enabled/cron。

### Requirement: 管理后台——定时任务页面
管理后台 SHALL 新增「定时任务」页面。

#### Scenario: 页面功能
- **WHEN** 管理员进入页面
- **THEN** 展示任务表格（任务名、描述、cron、状态 Tag、下次触发、最近执行时间/结果）；行内操作：编辑 cron（弹窗，前端也做格式校验）、启用/暂停开关（二次确认）、「立即执行」（二次确认，提示异步执行）；执行后刷新列表

### Requirement: 管理后台——数据备份页面
管理后台 SHALL 新增「数据备份」页面。

#### Scenario: 页面功能
- **WHEN** 管理员进入页面
- **THEN** 展示备份记录表格（文件名、大小格式化、备份时间、类型 Tag、状态），「立即备份」按钮；每行「还原」（红色危险操作，二次确认并明确提示会覆盖当前数据）与「删除」（二次确认）；成功刷新列表，失败提示后端错误信息

### Requirement: Docker 持久化
系统 SHALL 在 Docker 部署时将备份目录持久化到 `backup_data` 卷（`/app/backups`），重建容器不丢失备份。

## MODIFIED Requirements

### Requirement: Docker Compose 编排
在 `docker-compose.yml` 的 blog-backend 服务新增 `backup_data:/app/backups` 卷挂载，并在 volumes 顶层声明 `blog_backup_data` 命名卷。
