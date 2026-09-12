# Checklist

## 后端
- [x] GET /admin/tasks/spis 返回容器内全部 ScheduledTaskSpi 元信息（taskKey/taskName/description/defaultCron）
- [x] POST /admin/tasks 校验 SPI 存在、cron 合法（非法返回业务错误）、taskKey 唯一；插入记录并按状态注册调度；带 @Admin("新增定时任务")
- [x] PUT /admin/tasks/{id} 更新任务名/描述/cron；启用中的任务按新 cron 重新调度；带 @Admin("编辑定时任务")
- [x] reschedule/pause/resume/triggerOnce 不再要求 SPI Bean 必须存在（表记录存在即可）；无 Bean 任务执行回写 failed（"未找到任务实现: {taskKey}"），不影响调度
- [x] mvn compile 通过

## 前端
- [x] 已安装 @vue-js-cron/element-plus，TaskManage 弹窗使用可视化 cron 组件（中文、6 段），未生成合法表达式时保存禁用
- [x] 「新增任务」弹窗：任务类型下拉（/spis 数据，选中自动填充）、任务名、描述、cron 组件、状态默认启用；保存成功刷新
- [x] 行操作「编辑」复用弹窗：可改任务名/描述/cron（任务类型只读）
- [x] api/task.js 新增 spiOptions/createTask/updateTask
- [x] npm run build 通过
