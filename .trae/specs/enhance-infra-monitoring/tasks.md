# Tasks

- [x] Task 1: Prometheus 新增基础设施抓取 job
  - [x] 1.1 `monitoring/prometheus/prometheus.yml` 追加 `mysql-exporter`（mysql-exporter:9104）、`redis-exporter`（redis-exporter:9121）、`elasticsearch-exporter`（elasticsearch-exporter:9114）三个 scrape_configs job，并更新顶部注释（移除"仅抓取后端"说明）
  - [x] 1.2 验证：复读文件确认 YAML 缩进与现有 job 一致
- [x] Task 2: docker-compose 新增 3 个 exporter 服务
  - [x] 2.1 新增 `mysql-exporter` 服务：prom/mysqld-exporter:v0.15.1，command 指定 `--mysqld.address=mysql:3306 --mysqld.username=root`，环境变量 `MYSQLD_EXPORTER_PASSWORD: ${MYSQL_ROOT_PASSWORD:-123456}`，profiles ["monitor"]，加入 blog-net，不发布端口，depends_on mysql healthy
  - [x] 2.2 新增 `redis-exporter` 服务：oliver006/redis_exporter:v1.58.0，环境变量 `REDIS_ADDR: redis://redis:6379`、`REDIS_PASSWORD: ${REDIS_PASSWORD:-123456}`，profiles ["monitor"]，加入 blog-net，不发布端口，depends_on redis healthy
  - [x] 2.3 新增 `elasticsearch-exporter` 服务：quay.io/prometheuscommunity/elasticsearch-exporter:v1.8.0，command `--es.uri=http://elasticsearch:9200`，profiles ["monitor"]，加入 blog-net，不发布端口，depends_on elasticsearch（condition service_healthy + required: false）
  - [x] 2.4 验证：复读 docker-compose.yml 全文确认 YAML 语法、缩进、profiles、网络正确（本机无 docker，按既有预案目测复核）
- [x] Task 3: Grafana 仪表盘追加基础设施面板
  - [x] 3.1 `blog-monitor.json` 追加 MySQL 行：连接数（mysql_threads_connected / mysql_threads_running）、QPS（rate(mysql_com_select 等)）、慢查询（mysql_slow_queries）、InnoDB 缓冲池（mysql_innodb_buffer_pool_reads 与 read_requests 命中率）
  - [x] 3.2 追加 Redis 行：连接客户端（redis_connected_clients）、内存（redis_memory_used_bytes / maxmemory）、命中率（redis_keyspace_hits_total / misses_total）、命令速率（rate(redis_commands_processed_total)）、驱逐（rate(redis_evicted_keys_total)）
  - [x] 3.3 追加 Elasticsearch 行：集群状态（elasticsearch_cluster_status 单值面板 green=1/yellow=0.5/red=0）、JVM 堆使用率（elasticsearch_jvm_memory_used_bytes/max）、搜索与索引速率（rate(elasticsearch_search_query_total) 等），PromQL 过滤 `job="elasticsearch-exporter"`
  - [x] 3.4 验证：JSON 可被解析（幂等校验），新面板 gridPos 不与现有面板重叠，uid 不变（保证升级后仍是同一仪表盘）
- [x] Task 4: 文档同步
  - [x] 4.1 README.md「监控体系」小节补充基础设施监控说明（3 个 exporter、面板内容、ES 需同时启用 search profile 的提示）
  - [x] 4.2 部署操作手册.md 监控章节同步补充，并注明未启用 search 时 ES 目标 DOWN 属预期
- [x] Task 5: 整体验证
  - [x] 5.1 Grep 复核：prometheus.yml 含 3 个新 job、compose 含 3 个 exporter 服务且均无 ports 段、dashboard JSON 含 mysql_/redis_/elasticsearch_ 指标查询
  - [x] 5.2 确认未启用 monitor profile 时 compose 行为与现状一致（新增服务均在 profile 内，非破坏性）

# Task Dependencies
- Task 1、Task 2、Task 3 相互独立，可并行
- Task 4 依赖 Task 2/3（文档需引用最终服务与面板）
- Task 5 依赖 Task 1-4 全部完成
