# Tasks
- [x] Task 1: 创建 sys_operation_log 表与实体层
  - [x] SubTask 1.1: 在 `sql/01-create_sql.sql` 末尾新增 `sys_operation_log` 建表语句（含索引 idx_username、idx_create_time）
  - [x] SubTask 1.2: 新增实体 `entity/OperationLog.java`（继承 BaseEntity 风格，@TableName("sys_operation_log")）
  - [x] SubTask 1.3: 新增 `mapper/OperationLogMapper.java`（继承 BaseMapper）
- [x] Task 2: 幂等表迁移
  - [x] SubTask 2.1: `SchemaMapper` 新增 `countOperationLogTable()` 与 `createOperationLogTable()`（CREATE TABLE IF NOT EXISTS）
  - [x] SubTask 2.2: `DataInitializer.ensureUserColumns()` 流程中加入操作日志表的幂等建表调用
- [x] Task 3: 实现异步日志写入服务
  - [x] SubTask 3.1: `AsyncConfig` 新增 `opLogExecutor` 线程池（core=2, max=4, queue=1000, CallerRunsPolicy）
  - [x] SubTask 3.2: 新增 `service/OperationLogService.java` 接口与 `service/impl/OperationLogServiceImpl.java`，提供 `@Async("opLogExecutor")` 的 `asyncSave(OperationLog)` 方法（内部 try-catch，失败仅 WARN）
- [x] Task 4: 实现 @Admin 注解与审计切面
  - [x] SubTask 4.1: 新增 `annotation/Admin.java`（value 为操作描述）
  - [x] SubTask 4.2: 新增 `aspect/OperationLogAspect.java`：`@Around("@annotation(Admin)")`，从 SecurityContext 取用户名，序列化参数（password 字段脱敏、长度截断），记录 HTTP 方法/URI/IP/耗时/成功失败与异常信息，异步落库
- [x] Task 5: 为后台控制器添加 @Admin 注解
  - [x] SubTask 5.1: 为 `controller/admin` 下全部 11 个控制器的非 GET 接口方法添加 `@Admin("中文操作描述")`（GET 查询不加）
- [x] Task 6: 审计查询接口
  - [x] SubTask 6.1: 新增 `controller/admin/AdminOperationLogController.java`，`GET /admin/operation-logs` 分页查询，支持 username 精确 / operation 模糊过滤，按 create_time 倒序
- [x] Task 7: 验证
  - [x] SubTask 7.1: 执行 `mvn compile` 确认编译通过（沙箱限制 Maven 仓库写入，改用 IDE Java 编译诊断逐文件验证，全部通过）
  - [x] SubTask 7.2: 检查所有 admin 控制器写接口均已标注 @Admin 且描述为中文（28/28 全覆盖）

# Task Dependencies
- Task 3 依赖 Task 1（需要实体与 Mapper）
- Task 4 依赖 Task 1、Task 3（切面调用异步保存服务）
- Task 5 依赖 Task 4（需要 @Admin 注解存在）
- Task 6 依赖 Task 1
- Task 7 依赖 Task 1-6 全部完成
