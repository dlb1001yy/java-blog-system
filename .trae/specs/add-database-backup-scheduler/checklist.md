# Checklist

## 定时任务框架
- [x] 后端启用 @EnableScheduling，动态任务基于 TaskScheduler 运行期管理，改 cron/启停即时生效、无需重启
- [x] sys_scheduled_task 表由 DataInitializer 幂等创建；SPI Bean 与表记录启动时自动同步（缺记录插种子，缺 Bean 告警跳过）
- [x] 管理接口 GET /admin/tasks/page、PUT /admin/tasks/{id}/cron（服务端 CronExpression 校验，非法返回 400）、pause、resume、POST run 全部可用
- [x] 每次执行（定时或手动）后回写 last_execute_time / last_execute_status / last_execute_error；执行异常不影响下次调度
- [x] 立即执行为异步触发，接口立即返回
- [x] 任务相关写操作均带 @Admin 注解并写入操作日志
- [x] 管理后台「定时任务」页面：表格展示任务名/描述/cron/状态 Tag/下次触发/最近执行；编辑 cron 弹窗、启停（二次确认）、立即执行（二次确认）；操作后刷新

## 数据库备份
- [x] 内置任务 databaseBackup（默认 cron `0 0 4 * * ?`，默认启用，种子幂等写入）
- [x] 备份为纯 Java JDBC 导出（不依赖 mysqldump），gzip 压缩 .sql.gz，Windows 与 Docker 均可运行
- [x] 备份文件名 backup_yyyyMMdd_HHmmss.sql.gz，内容含 DROP TABLE IF EXISTS / CREATE TABLE / INSERT，头部含 SET NAMES utf8mb4 与 SET FOREIGN_KEY_CHECKS=0
- [x] sys_backup_record 表幂等创建，记录文件名/大小/类型(auto|manual)/状态/错误信息/备份时间
- [x] 备份成功后自动清理超过保留期（默认 30 天，blog.backup.retention-days 可配置）的文件与记录
- [x] 备份执行中有并发保护，重叠触发时跳过并告警
- [x] 备份失败记录 status=failed 与错误信息，不残留损坏的部分文件
- [x] 管理接口 GET /admin/backups/page、POST /admin/backups、DELETE /admin/backups/{id}、POST /admin/backups/{id}/restore 全部可用
- [x] 还原接口校验文件存在且 status=success；还原失败返回明确错误（含失败位置）；还原有覆盖当前数据的明确提示（前端危险二次确认）
- [x] 备份写操作均带 @Admin 注解并写入操作日志
- [x] 管理后台「数据备份」页面：分页表格（文件名/大小格式化/时间/类型 Tag/状态）、立即备份、还原（危险二次确认）、删除（二次确认）；成功刷新、失败提示

## 部署与配置
- [x] docker-compose.yml 为 blog-backend 挂载 backup_data 卷（/app/backups），重建容器备份不丢失
- [x] application.yaml / application-docker.yaml 备份配置项齐全且有默认值
- [x] 根 .gitignore 忽略本地 backups 目录

## 构建
- [x] 后端 mvn clean package 编译通过
- [x] 前端 blog-admin 构建通过
