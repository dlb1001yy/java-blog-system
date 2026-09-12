# 定时任务新增能力 + cron 可视化编辑 Spec

## Why
现有定时任务页（add-database-backup-scheduler 交付）只能对内置任务改 cron/启停，无法新增任务；cron 编辑是手写文本框 + 正则，易写错。需要支持在管理后台新增/编辑任务（当前仅同步已有任务 Bean，Bean 未注册的任务提示不可执行），并用社区流行的 `@vue-js-cron/element-plus` 组件替换为可视化编辑。

## What Changes
- 前端引入 `@vue-js-cron/element-plus`（npm 依赖），「编辑 cron」弹窗改为可视化 cron 组件（支持面板配置与手动输入），保留手输模式
- 后端 `AdminTaskController` 新增任务管理接口：`GET /admin/tasks/spis`（返回可用的任务类型：容器内全部 ScheduledTaskSpi 的 taskKey/名称/描述/默认 cron）、`POST /admin/tasks`（新增任务记录）
- `DynamicTaskManager` 放开「必须有 SPI Bean 才能操作」限制：新增 `create(taskKey, taskName, description, cron, status)`，插入记录并注册调度；对无 Bean 的任务，`schedule` 注册照常、`runWithRecord` 执行时回写 failed（「未找到任务实现」）；`reschedule/pause/resume/triggerOnce` 移除 `requireSpi` 硬校验，改为记录校验
- 前端「定时任务」页新增「新增任务」按钮 + 弹窗（选择任务类型下拉（数据来自 /spis）、任务名、描述、cron 可视化组件）；列表行操作增加「编辑」（复用新增弹窗，改任务名/描述/cron）
- 后端新增/编辑接口接入操作日志（`@Admin`）与 cron 校验（CronExpression）

## Impact
- Affected specs: add-database-backup-scheduler（MODIFIED：任务列表页新增「新增任务/编辑」能力；任务管理接口新增 2 个）
- Affected code:
  - `blog-admin/package.json`（新增依赖 @vue-js-cron/element-plus）
  - `blog-admin/src/views/TaskManage.vue`（新增/编辑弹窗 + cron 可视化组件替换文本框）
  - `blog-admin/src/api/task.js`（新增 spiOptions/createTask/updateTask）
  - `blog-backend/.../task/DynamicTaskManager.java`（create 方法、放开 requireSpi）
  - `blog-backend/.../controller/admin/AdminTaskController.java`（新增 2 接口 + 编辑接口）
- 不涉及数据库表结构变更（复用 sys_scheduled_task）

## ADDED Requirements

### Requirement: 任务类型查询
系统 SHALL 提供任务类型列表接口，返回容器内全部 ScheduledTaskSpi 注册的任务类型元信息。

#### Scenario: 查询成功
- **WHEN** 调用 `GET /admin/tasks/spis`
- **THEN** 返回 [{taskKey, taskName, description, defaultCron}] 列表

### Requirement: 新增定时任务
管理员 SHALL 可在后台新增定时任务。

#### Scenario: 新增成功
- **WHEN** 调用 `POST /admin/tasks`，body 含 taskKey（从 /spis 选择的任务类型）、taskName、description、cronExpression、status
- **THEN** 校验 taskKey 对应 SPI 存在、cron 合法（CronExpression 校验，非法 400）、taskKey 未重复（重复提示）；插入记录并按 status 注册调度；操作写入操作日志（@Admin("新增定时任务")）

#### Scenario: 新增失败
- **WHEN** taskKey 已存在或 cron 非法
- **THEN** 返回明确业务错误，不插入记录

### Requirement: 编辑任务
管理员 SHALL 可编辑任务的名称/描述/cron。

#### Scenario: 编辑成功
- **WHEN** 调用 `PUT /admin/tasks/{id}`，body 含 taskName、description、cronExpression
- **THEN** 更新记录；若任务启用则按新 cron 重新调度；操作写入操作日志（@Admin("编辑定时任务")）

### Requirement: cron 可视化编辑
管理后台 SHALL 使用 `@vue-js-cron/element-plus` 组件编辑 cron 表达式（新增与编辑弹窗共用）。

#### Scenario: 可视化配置
- **WHEN** 管理员在弹窗中打开 cron 编辑
- **THEN** 默认以可视化标签页（秒/分/时/日/月/周）配置；可切换 manual 模式直接输入表达式；表达式为 6 段（不含年），与后端 Spring CronExpression 兼容；未生成合法表达式前保存按钮禁用

### Requirement: 新增任务弹窗
管理后台 SHALL 提供「新增任务」弹窗：任务类型（下拉，选项来自 /spis，选中后自动填充任务名/描述/默认 cron）、任务名、描述、cron（可视化组件）、状态（默认启用）；保存成功刷新列表。

### Requirement: 生命周期兼容
系统 SHALL 允许对「表中存在但 SPI Bean 缺失」的任务记录执行改 cron/启停/立即执行操作。

#### Scenario: 无 Bean 任务操作
- **WHEN** 管理员对无 Bean 的任务任务执行暂停/恢复/改 cron
- **THEN** 正常更新记录与调度状态；「立即执行」或定时触发时执行结果回写 failed，错误信息「未找到任务实现: {taskKey}」

## MODIFIED Requirements

### Requirement: 任务列表（原：add-database-backup-scheduler）
管理员可分页查看定时任务（任务名、taskKey、描述、cron、状态、下次触发时间、最近执行信息，按 taskKey 排序）；**新增**：页面顶部提供「新增任务」按钮；行操作新增「编辑」（改任务名/描述/cron）。

### Requirement: Docker Compose / 构建配置
blog-admin 新增 npm 依赖后，`package.json`/lock 更新随前端构建自然生效（Dockerfile 构建阶段拉取依赖），无 compose 变更。
