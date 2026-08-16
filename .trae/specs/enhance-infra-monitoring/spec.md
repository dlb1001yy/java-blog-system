# 完善基础设施监控体系（MySQL / Redis / Elasticsearch）Spec

## Why
当前监控栈（Actuator + Prometheus + Grafana）仅采集 blog-backend 应用指标，数据库慢查询、Redis 内存打满、ES 集群状态异常等基础设施问题完全不可见，故障定位仍需逐台进容器排查。

## What Changes
- Prometheus 抓取配置新增 3 个 job：`mysql-exporter`、`redis-exporter`、`elasticsearch-exporter`（容器网络内服务名互访，15s 间隔）
- `docker-compose.yml` 新增 3 个 exporter 服务，均挂 `profiles: ["monitor"]`、加入 `blog-net` 网络、**不发布宿主机端口**（仅容器网络内供 Prometheus 抓取，避免指标数据外泄）：
  - `mysql-exporter`（prom/mysqld-exporter:v0.15.1，端口 9104）：`--mysqld.address=mysql:3306 --mysqld.username=root`，密码经 `MYSQLD_EXPORTER_PASSWORD` 注入（复用现有 `MYSQL_ROOT_PASSWORD` 变量，与 mysql healthcheck 同源）；depends_on mysql healthy
  - `redis-exporter`（oliver006/redis_exporter:v1.58.0，端口 9121）：`REDIS_ADDR=redis://redis:6379`，密码复用 `REDIS_PASSWORD`；depends_on redis healthy
  - `elasticsearch-exporter`（quay.io/prometheuscommunity/elasticsearch-exporter:v1.8.0，端口 9114）：`--es.uri=http://elasticsearch:9200`；depends_on elasticsearch（`required: false`，因 ES 属可选 search profile，未启用时不应阻塞 monitor profile 启动）
- Grafana 预置仪表盘 `blog-monitor.json` 追加三行基础设施面板（复用同一 Prometheus 数据源与现有 dashboard，不新建文件）：
  - MySQL：连接数（threads_connected/线程运行）、QPS（Com_select/insert/update/delete 速率）、慢查询、InnoDB 缓冲池命中率/使用量
  - Redis：连接客户端数、已用内存/峰值、keyspace hits/misses（命中率）、每秒执行命令数、驱逐 key 速率
  - Elasticsearch：集群状态（green=1/yellow=0.5/red=0 单值面板）、节点 JVM 堆使用率、搜索/索引速率（search/query 与 index/index ops）
  - PromQL 均按 `job="mysql-exporter"` / `"redis-exporter"` / `"elasticsearch-exporter"` 过滤
- 文档同步：`README.md` 监控体系小节、`部署操作手册.md` 监控章节补充基础设施监控说明，并注明：未启用 `search` profile 时 ES exporter 目标在 Prometheus Targets 页显示 DOWN、ES 面板无数据，属预期

## Impact
- Affected specs: add-metrics-monitoring（在其成果上扩展采集面与仪表盘，非破坏性）、deploy-with-docker-compose（compose 新增 3 个可选服务，均在 monitor profile 内）
- Affected code:
  - monitoring/prometheus/prometheus.yml（+3 scrape job）
  - docker-compose.yml（+3 exporter 服务）
  - monitoring/grafana/dashboards/blog-monitor.json（+3 行面板）
  - README.md、部署操作手册.md
- 无新增 .env 变量（复用 MYSQL_ROOT_PASSWORD、REDIS_PASSWORD）；未启用 monitor profile 时部署行为与现状完全一致（**非破坏性**）
- 安全说明：exporter 不发布宿主端口，仅在 blog-net 内可达；MySQL 导出用 root 账号与既有 healthcheck 做法一致（内网容器，风险可控）

## ADDED Requirements

### Requirement: MySQL 指标采集
monitor profile 启用时，mysqld-exporter SHALL 连接 mysql:3306 采集 MySQL 指标（连接线程、QPS、慢查询、InnoDB 缓冲池等），由 Prometheus 每 15 秒以 job=`mysql-exporter` 抓取。

#### Scenario: 抓取成功
- **WHEN** `COMPOSE_PROFILES` 含 `monitor` 且 mysql 健康
- **THEN** Prometheus Targets 页显示 `mysql-exporter` 状态 UP，Grafana MySQL 面板出现实时数据

### Requirement: Redis 指标采集
monitor profile 启用时，redis_exporter SHALL 以 .env 中 `REDIS_PASSWORD` 认证连接 redis:6379 采集 Redis 指标（客户端连接、内存、命中率、命令速率、驱逐），由 Prometheus 每 15 秒以 job=`redis-exporter` 抓取。

#### Scenario: 抓取成功
- **WHEN** `COMPOSE_PROFILES` 含 `monitor` 且 redis 健康
- **THEN** Prometheus Targets 页显示 `redis-exporter` 状态 UP，Grafana Redis 面板出现实时数据

### Requirement: Elasticsearch 指标采集
monitor profile 启用且 search profile 同时启用时，elasticsearch-exporter SHALL 连接 elasticsearch:9200 采集 ES 指标（集群状态、JVM 堆、搜索/索引速率），由 Prometheus 每 15 秒以 job=`elasticsearch-exporter` 抓取。

#### Scenario: 抓取成功
- **WHEN** `COMPOSE_PROFILES` 含 `monitor,search` 且 ES 健康
- **THEN** Prometheus Targets 页显示 `elasticsearch-exporter` 状态 UP，Grafana ES 面板出现实时数据

#### Scenario: 未启用 search profile
- **WHEN** 仅启用 `monitor`（无 ES 容器）
- **THEN** monitor profile 其余服务正常启动（depends_on `required: false` 不阻塞），Prometheus 中该目标显示 DOWN、ES 面板无数据，属预期且已写入文档

### Requirement: 基础设施面板可视化
现有「Java 博客系统监控」仪表盘 SHALL 追加 MySQL、Redis、Elasticsearch 三行面板，首次启动随 provisioning 自动加载，无需手工导入。

#### Scenario: 仪表盘自动更新
- **WHEN** 重新 `docker compose --profile monitor up -d` 后打开 Grafana
- **THEN** 同一仪表盘内可见三行新面板并渲染曲线

## MODIFIED Requirements

### Requirement: Prometheus 抓取（原 add-metrics-monitoring spec）
Prometheus 除每 15 秒抓取 job `blog-backend` 外，SHALL 额外抓取 `mysql-exporter`、`redis-exporter`、`elasticsearch-exporter` 三个静态目标（均位于 blog-net 容器网络，无需宿主端口）。

### Requirement: Grafana 可视化（原 add-metrics-monitoring spec）
预置仪表盘在原有 HTTP/JVM/CPU/HikariCP 面板基础上，增加 MySQL（连接/QPS/慢查询/缓冲池）、Redis（连接/内存/命中率/命令速率/驱逐）、Elasticsearch（集群状态/JVM 堆/搜索与索引速率）三行面板。

## REMOVED Requirements
无
