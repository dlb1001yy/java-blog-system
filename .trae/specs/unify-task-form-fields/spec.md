# 统一定时任务新增/编辑弹窗字段 Spec

## Why
定时任务页的新增与编辑共用同一弹窗但字段不一致：编辑模式缺少「任务标识」与「状态」两项（均为 `v-if="!isEdit"`），体验不统一，且编辑时无法直接改任务启停状态（只能关闭弹窗后再点行内按钮）。

## What Changes
- 编辑模式补齐与新增一致的完整字段：任务类型（disabled 只读，现状已有）、任务标识（disabled 只读展示，保持唯一标识不可改）、任务名、描述、执行频率（cron）、状态开关（可修改）
- 后端 `DynamicTaskManager.update()` 增加 `status` 参数支持：status 变化时同步调度（0→1 注册调度、1→0 取消调度）；cron 变化且启用时重新调度
- `AdminTaskController` `PUT /admin/tasks/{id}` 请求体 `TaskUpdateDTO` 增加 `status` 字段
- 前端编辑弹窗打开时回填行内 `status`；保存 payload 带 `status`；保存成功刷新列表（状态列同步更新）

## Impact
- Affected specs: enhance-task-manage-frontend（MODIFIED：编辑任务能力扩展为含状态）
- Affected code:
  - `blog-backend/.../task/DynamicTaskManager.java`（update 方法增加 status 逻辑）
  - `blog-backend/.../controller/admin/AdminTaskController.java`（TaskUpdateDTO 增加 status）
  - `blog-admin/src/views/TaskManage.vue`（弹窗两个字段的 `v-if="!isEdit"` 调整）
- 不涉及数据库表结构变更

## MODIFIED Requirements

### Requirement: 编辑任务（原：enhance-task-manage-frontend）
管理员可编辑任务的名称/描述/cron/状态。编辑弹窗字段与新增弹窗完全一致（任务类型、任务标识、任务名、描述、执行频率、状态），其中任务类型与任务标识为只读展示。

#### Scenario: 编辑状态成功
- **WHEN** 管理员在编辑弹窗将状态从「暂停」改为「启用」并保存
- **THEN** 记录 status 更新为 1 并按当前 cron 注册调度；列表状态列刷新为「启用」

#### Scenario: 编辑 cron 与状态
- **WHEN** 管理员同时修改 cron 并将状态改为「暂停」保存
- **THEN** 记录更新，取消调度（不注册新 cron）

#### Scenario: 字段一致性
- **WHEN** 分别打开新增与编辑弹窗
- **THEN** 两者展示相同字段集（仅任务类型/任务标识在编辑时只读、状态开关始终可操作）
