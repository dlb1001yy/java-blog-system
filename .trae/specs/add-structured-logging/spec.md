# 结构化日志与日志持久化 Spec

## Why
后端当前无 logback-spring.xml，使用 Spring Boot 默认日志配置（无文件输出、无轮转策略），Docker 部署下日志仅依赖 journald/json-file 默认行为，容器重建即丢失且无大小上限，生产问题无法回溯。

## What Changes
- 新增 `blog-backend/src/main/resources/logback-spring.xml`：结构化日志配置
  - 控制台 + 滚动文件双输出（文件路径通过 `LOG_FILE` 属性注入，默认 `/app/logs/blog-backend.log`，本地开发走临时目录）
  - 按天 + 大小双触发轮转（单文件 100MB，保留 14 天 / 30 个文件），总占用有界
  - ERROR 级别独立文件 `blog-backend-error.log`，便于告警检索
  - 约定日志格式：时间 | 级别 | 线程 | logger | traceId 占位 | 消息（结构化、便于 grep/采集）
  - docker profile 下降低框架噪音（ROOT 保持 INFO，`org.apache.ibatis` stdout 实现在 yaml 中关闭改走 SLF4J）
- `application.yaml`：`mybatis-plus.configuration.log-impl` 由 `StdOutImpl` 改为 `org.apache.ibatis.logging.slf4j.Slf4jImpl`，SQL 日志纳入统一日志管道（级别由 `com.dlbyy.blog.mapper` DEBUG 控制，默认不打印 SQL）
- `application-docker.yaml`：新增 `LOG_FILE` 环境变量占位（默认 `/app/logs/blog-backend.log`）
- `docker-compose.yml`：blog-backend 挂载 `backend_logs:/app/logs` 数据卷实现持久化；新增 `logging` 段配置 json-file 驱动轮转（max-size 50m，max-file 5）；volumes 段新增 `backend_logs`
- **BREAKING**：无（日志行为增强，业务零改动）

## Impact
- Affected specs: deploy-with-docker-compose（backend 服务新增卷与 logging 配置）、add-metrics-monitoring（日志与指标互补，无冲突）
- Affected code: `blog-backend/src/main/resources/logback-spring.xml`（新增）、`application.yaml`（log-impl）、`application-docker.yaml`（LOG_FILE）、`docker-compose.yml`（backend 卷 + logging + volumes）

## ADDED Requirements
### Requirement: 结构化日志输出
系统 SHALL 通过 logback-spring.xml 提供控制台与滚动文件双通道日志，格式统一为「时间 | 级别 | 线程 | logger | 消息」。

#### Scenario: 本地开发
- **WHEN** 默认 profile 启动
- **THEN** 控制台输出 INFO 及以上日志，文件写入 `${LOG_FILE:-java.io.tmpdir}/blog-backend.log`（临时目录，不污染工程目录）

#### Scenario: Docker 部署
- **WHEN** docker profile 启动
- **THEN** 日志写入 /app/logs/blog-backend.log（挂载卷持久化），ERROR 单独写入 blog-backend-error.log

### Requirement: 日志轮转与持久化
#### Scenario: 轮转触发
- **WHEN** 单个日志文件达到 100MB 或跨天
- **THEN** 按天 + 序号归档为 .gz，最多保留 14 天或 30 个归档，超限自动清理

#### Scenario: 容器重建
- **WHEN** blog-backend 容器删除重建
- **THEN** 历史日志保留在 backend_logs 数据卷中不丢失

#### Scenario: Docker json-file 双重轮转
- **WHEN** docker compose 部署
- **THEN** 容器 stdout 日志由 json-file 驱动限制单文件 50MB、最多 5 个，防止 /var/lib/docker 无限膨胀

### Requirement: SQL 日志统一管道
#### Scenario: SQL 日志控制
- **WHEN** 需要排查 SQL 时设置 `logging.level.com.dlbyy.blog.mapper=DEBUG`
- **THEN** MyBatis SQL 经 SLF4J 按统一格式输出（不再 System.out 直排）
