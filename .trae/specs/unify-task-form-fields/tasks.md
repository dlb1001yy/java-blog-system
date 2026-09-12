# Tasks

- [x] Task 1: 后端 update 支持 status
  - [x] 1.1 `DynamicTaskManager.update()` 增加 `Integer status` 参数：status 非空时更新记录状态；调度同步——(旧状态=0 且 新状态=1) 注册调度（按最终 cron）；(旧状态=1 且 新状态=0) 取消调度；cron 变更且最终状态=1 时重新调度（注意与启停组合后的最终效果，避免重复注册/漏取消）
  - [x] 1.2 `AdminTaskController.TaskUpdateDTO` 增加 `Integer status` 字段并透传
- [x] Task 2: 前端弹窗字段统一
  - [x] 2.1 `TaskManage.vue` 弹窗：「任务标识」编辑模式显示但 disabled 只读；「状态」开关去掉 `v-if="!isEdit"`（新增/编辑均可操作）
  - [x] 2.2 编辑保存 payload 增加 `status: form.status`（后端 null 不更新，故始终传值）
- [x] Task 3: 构建验证
  - [x] 3.1 后端 `mvn compile` 通过（exit 0）
  - [x] 3.2 前端 `npm run build` 通过（16.60s，exit 0）

# Task Dependencies
- Task 1 与 Task 2 相互独立可并行（契约简单：PUT body 增加 status）
- Task 3 依赖全部完成
