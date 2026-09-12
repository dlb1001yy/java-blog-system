# Checklist

- [x] 编辑弹窗与新增弹窗字段集一致：任务类型（编辑只读）、任务标识（编辑只读）、任务名、描述、执行频率、状态开关（两种模式均可操作）
- [x] 后端 update 支持 status：0→1 注册调度、1→0 取消调度、cron 变更 + 启用时重调度，组合场景（如暂停态改 cron）不误注册
- [x] PUT /admin/tasks/{id} 请求体支持 status 字段（null 不更新）
- [x] 前端编辑保存 payload 携带 status，保存后列表状态刷新
- [x] 后端 mvn compile 通过
- [x] 前端 npm run build 通过
