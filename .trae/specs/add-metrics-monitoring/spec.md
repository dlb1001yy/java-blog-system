# 添加 Actuator + Prometheus + Grafana 监控体系 Spec

## Why
项目目前没有任何可观测性手段：线上故障（接口变慢、连接池耗尽、OOM 前兆）只能靠翻日志排查，无法量化系统健康度。引入 Spring Boot Actuator 指标暴露 + Prometheus 抓取存储 + Grafana 可视化后，可实时掌握 JVM、HTTP 请求、数据库连接池等核心指标。

## What Changes
- 后端新增依赖 `spring-boot-starter-actuator` + `micrometer-registry-prometheus`（版本由 Boot 3.1.5 BOM 管理，不写死版本号）
- `application.yaml` 新增 `spring.application.name` 与 `management` 配置段，仅暴露 `health,info,prometheus` 三个端点（实际路径为 `/api/actuator/**`，因 context-path=/api）
- 新建 `monitoring/` 配置目录：Prometheus 抓取配置（job=blog-backend，抓取路径 `/api/actuator/prometheus`，15s 间隔）+ Grafana 自动预配（数据源 + 仪表盘 Provider）+ 预置仪表盘 JSON（HTTP QPS/p95 延迟/5xx 错误率、JVM 内存/线程/GC、CPU、HikariCP 连接池）
- `docker-compose.yml` 新增 `prometheus`、`grafana` 两个服务，挂 `profiles: ["monitor"]`（与现有 search profile 同机制，不开启则完全不影响现有部署），新增 `prometheus_data`、`grafana_data` 数据卷；端口 9090（Prometheus）、3000（Grafana）
- `.env.example` 追加监控相关变量：`GRAFANA_ADMIN_USER`、`GRAFANA_ADMIN_PASSWORD`
- 文档同步：`README.md` 新增监控小节，`部署操作手册.md` 新增监控部署步骤

## Impact
- Affected specs: externalize-sensitive-config（.env.example 追加变量，向后兼容）、deploy-with-docker-compose（compose 新增可选服务组）
- Affected code:
  - blog-backend/pom.xml（+2 依赖）
  - blog-backend/src/main/resources/application.yaml（+management 段）
  - docker-compose.yml（+2 服务 +2 数据卷）
  - monitoring/**（全部新建）
  - .env.example、README.md、部署操作手册.md
- 安全性说明：`/api/actuator/**` 经核实由 `SecurityConfig` 的 `anyRequest().permitAll()` 匿名放行，JwtAuthenticationFilter 无 token 直接放行，RequestSignatureFilter 仅拦截含 `/user/`、`/admin/` 的 URI —— **无需修改任何安全代码**；仅暴露三个端点、不暴露 env/beans/threaddump 等敏感端点，风险可控

## ADDED Requirements

### Requirement: 指标暴露
系统 SHALL 通过 Spring Boot Actuator 在 `/api/actuator/prometheus` 暴露 Prometheus 文本格式指标（涵盖 JVM、HTTP 请求 `http.server.requests`、HikariCP 连接池），并在 `/api/actuator/health`、`/api/actuator/info` 提供健康检查。

#### Scenario: 匿名访问指标端点
- **WHEN** 任意客户端 GET `/api/actuator/prometheus`（无需 token、无需签名）
- **THEN** 返回 200 与 Prometheus 格式文本（含 `jvm_`、`http_server_requests_`、`hikaricp_` 前缀指标）

#### Scenario: 敏感端点不暴露
- **WHEN** GET `/api/actuator/env` 或 `/api/actuator/beans`
- **THEN** 返回 404（include 仅 health,info,prometheus）

### Requirement: Prometheus 抓取
`monitor` profile 启用时，Prometheus 容器 SHALL 每 15 秒抓取一次后端指标，指标带 `application=blog-backend` 公共标签。

#### Scenario: 抓取成功
- **WHEN** `COMPOSE_PROFILES` 含 `monitor` 且后端健康
- **THEN** Prometheus Targets 页面显示 job `blog-backend` 状态 UP

### Requirement: Grafana 可视化
Grafana 容器 SHALL 通过 provisioning 机制自动完成数据源（指向 Prometheus）与预置仪表盘加载，首次启动零手工配置；仪表盘至少包含：HTTP QPS、p95 延迟、5xx 错误率、JVM 堆内存、JVM 线程数、GC 暂停、CPU 使用率、Hikari 连接池面板。

#### Scenario: 首次启动即有仪表盘
- **WHEN** `docker compose --profile monitor up -d` 后访问 `http://<IP>:3000`
- **THEN** 用 .env 配置的管理员账密登录后，可直接打开「Java 博客系统监控」仪表盘并看到实时曲线（后端有流量时）

### Requirement: 环境变量管理
Grafana 管理员账号密码 SHALL 通过 `GRAFANA_ADMIN_USER`（默认 admin）/ `GRAFANA_ADMIN_PASSWORD`（默认 admin123，生产必改）环境变量注入，与现有 .env 机制一致。

## MODIFIED Requirements

### Requirement: Docker Compose 服务编排（原 deploy-with-docker-compose spec）
compose 在原有 mysql/redis/elasticsearch(可选)/backend/admin/frontend 基础上，新增可选服务组 `monitor`（prometheus + grafana）与 `prometheus_data`/`grafana_data` 数据卷；未启用 `monitor` profile 时部署行为与现状完全一致（**非破坏性变更**）。

## REMOVED Requirements
无
