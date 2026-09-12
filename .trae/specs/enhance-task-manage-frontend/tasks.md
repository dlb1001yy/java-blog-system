# Tasks

- [x] Task 1: 后端任务管理能力扩展
  - [x] 1.1 `DynamicTaskManager`：新增 `create(taskKey, taskName, description, cron, status)`（校验 taskKey 对应 SPI 存在、cron 合法、taskKey 唯一；插入记录；启用则注册调度）；新增 `update(id, taskName, description, cron)`（更新记录；启用中则重新调度）；将 `reschedule/pause/resume/triggerOnce` 的 `requireSpi` 硬校验放开为「记录存在即可」，SPI 缺失时调度照常注册、执行回写 failed（错误「未找到任务实现: {taskKey}」）
  - [x] 1.2 `AdminTaskController`：新增 `GET /admin/tasks/spis`（返回容器内 SPI 元信息列表）、`POST /admin/tasks`（@Admin("新增定时任务")）、`PUT /admin/tasks/{id}`（@Admin("编辑定时任务")）
- [x] Task 2: 前端 cron 组件接入
  - [x] 2.1 `blog-admin` 安装 `@vue-js-cron/element-plus`（v3.2.1 安装成功；导出为 CronElementPlus，样式 dist/element-plus.css）
  - [x] 2.2 `TaskManage.vue` cron 编辑改用可视化组件（`format="spring"` 6 段无年、`locale="zh"`、`:periods` 过滤 year），表达式非法/为空禁用保存
- [x] Task 3: 前端新增/编辑任务弹窗
  - [x] 3.1 `api/task.js` 新增 `spiOptions()`、`createTask(data)`、`updateTask(id, data)`
  - [x] 3.2 `TaskManage.vue` 新增「新增任务」按钮与弹窗（任务类型下拉（/spis 数据，选中自动填充名称/描述/默认 cron）、任务名、描述、cron 可视化组件、状态开关默认启用）；行操作「编辑」复用同一弹窗（任务类型只读）
  - [x] 3.3 操作成功刷新列表，失败提示后端错误信息
- [x] Task 4: 构建验证
  - [x] 4.1 后端 `mvn compile` 通过
  - [x] 4.2 前端 `npm run build` 通过（16.88s；chunk >500kB 警告为既有问题非本次引入）

# Task Dependencies
- Task 1、Task 2 相互独立可并行
- Task 3 依赖 Task 2（cron 组件）与 Task 1（接口契约）
- Task 4 依赖全部完成
