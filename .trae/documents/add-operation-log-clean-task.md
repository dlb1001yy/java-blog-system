# 新增定时清理任务（操作日志清理）计划

## 背景：任务类型下拉的维护机制

任务类型下拉框的数据**不是数据库维护的字典**，而是来自 Spring 容器中的代码实现：

- 下拉数据源：前端 [task.js](d:/my-project/java-blog-system/blog-admin/src/api/task.js) 调用 `GET /admin/tasks/spis` → [AdminTaskController.spis()](d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/controller/admin/AdminTaskController.java#L108-L111) → `DynamicTaskManager.listSpiOptions()`
- `listSpiOptions()` 遍历容器内所有 [ScheduledTaskSpi](d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/task/ScheduledTaskSpi.java) 实现 Bean，返回 `taskKey/taskName/description/defaultCron`

**因此新增一个任务类型 = 新建一个 Java 类实现 `ScheduledTaskSpi` + `@Component`，重启后端即可**：
1. 自动出现在下拉框（无需改前端）
2. 自动幂等播种任务记录到 `sys_scheduled_task` 并按 `defaultCron()` 注册调度（无需改管理器）
3. 支持后台改 cron、暂停/恢复、手动触发

现有唯一实现是 [DatabaseBackupTask.java](d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/task/DatabaseBackupTask.java)（数据库备份）。

## 本次要新增的任务

用户选择「定时清理类」。分析系统内可清理数据：

| 数据 | 现状 | 结论 |
|---|---|---|
| `sys_operation_log` 后台操作日志 | 只增不删，**无任何清理机制**，无限增长 | ✅ 本次目标 |
| `sys_backup_record` + 备份文件 | 每次备份后 `BackupService.deleteExpired()` 已按 `retention-days=30` 清理 | 已覆盖 |

→ 新增**操作日志清理任务**，删除超过保留天数的操作日志。

## 改动内容（共 3 处，均在 blog-backend）

### 1. 新建 `OperationLogCleanTask.java`（核心改动）

路径：`blog-backend/src/main/java/com/dlbyy/blog/task/OperationLogCleanTask.java`

仿照 `DatabaseBackupTask` 的写法：

```java
package com.dlbyy.blog.task;

import com.baomidou.mybatisplus.core.conditions.delete.LambdaDeleteWrapper;
import com.dlbyy.blog.entity.OperationLog;
import com.dlbyy.blog.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 操作日志定时清理任务。
 * 每天凌晨 3 点清理超过保留天数的后台操作日志（sys_operation_log），
 * 保留天数由 blog.cleanup.retention-days 配置（默认 90 天）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogCleanTask implements ScheduledTaskSpi {

    private final OperationLogMapper operationLogMapper;

    /** 操作日志保留天数（超期自动清理） */
    @Value("${blog.cleanup.retention-days:90}")
    private int retentionDays;

    @Override
    public String taskKey() {
        return "operationLogClean";
    }

    @Override
    public String taskName() {
        return "操作日志清理";
    }

    @Override
    public String description() {
        return "每天凌晨清理超过保留天数的后台操作日志";
    }

    @Override
    public String defaultCron() {
        return "0 30 3 * * ?"; // 每天 3:30（错开 4:00 的数据库备份）
    }

    @Override
    public void run() {
        LocalDateTime expireBefore = LocalDateTime.now().minusDays(retentionDays);
        int deleted = operationLogMapper.delete(new LambdaDeleteWrapper<OperationLog>()
                .lt(OperationLog::getCreateTime, expireBefore));
        log.info("[OperationLogCleanTask] 已清理 {} 天前的操作日志 {} 条", retentionDays, deleted);
    }
}
```

要点：
- `taskKey` 为 `operationLogClean`，符合 `^[a-zA-Z][a-zA-Z0-9-]*$` 校验
- cron 定 3:30，避开 databaseBackup 的 4:00，且二者共用 2 线程的调度池也不冲突
- 清理逻辑简单，直接在任务类内实现（不需要新建 Service）；异常由 `DynamicTaskManager.runWithRecord` 统一捕获回写

### 2. 追加配置 `application.yaml`

文件：[application.yaml](d:/my-project/java-blog-system/blog-backend/src/main/resources/application.yaml#L140-L143)

在 `blog.backup` 配置块之后追加：

```yaml
  # 操作日志清理配置（配合定时任务 operationLogClean 使用）
  cleanup:
    retention-days: 90    # 操作日志保留天数，超期自动清理
```

### 3. 追加种子 SQL（可选，保持一致性）

文件：[11-scheduled-task-backup.sql](d:/my-project/java-blog-system/blog-backend/sql/11-scheduled-task-backup.sql#L46-L54)

仿照 databaseBackup 的幂等写法，在种子段后追加：

```sql
INSERT INTO `sys_scheduled_task`
    (`task_key`, `task_name`, `description`, `cron_expression`, `status`)
SELECT 'operationLogClean', '操作日志清理', '每天凌晨清理超过保留天数的后台操作日志', '0 30 3 * * ?', 1
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_scheduled_task` WHERE `task_key` = 'operationLogClean'
);
```

（应用启动时 `DynamicTaskManager.syncAndRegister()` 也会自动补插，此处仅为 docker 首次建库时预置。）

### 无需改动的部分

- **前端 TaskManage.vue**：下拉框渲染 `spiList`，接口返回新类型后自动出现
- **DynamicTaskManager / AdminTaskController**：SPI 架构自动收集，无需注册

## 决策与假设

- 清理目标选操作日志：它是系统内唯一只增不删、无清理机制的表；备份文件已有保留策略
- 保留天数做成可配置（默认 90 天），与 `blog.backup.retention-days` 模式对齐；单属性用 `@Value` 即可，不新建 Properties 类
- 同类型可建多实例：后台新增任务时选「操作日志清理」类型，任务标识填 `operationLogClean-xxx` 即可建多个不同 cron 的清理任务（现有 `resolveSpi` 前缀匹配已支持）

## 验证步骤

1. 启动后端，观察日志出现 `[DynamicTaskManager] 已注册定时任务: operationLogClean [操作日志清理] cron=0 30 3 * * ?`
2. 查询数据库：`SELECT * FROM sys_scheduled_task WHERE task_key='operationLogClean'` 应有一条启用记录
3. 打开后台「定时任务」页面 → 新增任务 → 任务类型下拉应出现「操作日志清理(operationLogClean)」
4. 在任务列表对「操作日志清理」点「执行」手动触发一次，确认最近执行状态为 success，且超过保留期的日志被删除
