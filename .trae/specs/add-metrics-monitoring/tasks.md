# Tasks

- [x] Task 1: 后端接入 Actuator + Prometheus 指标暴露
  - [x] 1.1 pom.xml 新增 `spring-boot-starter-actuator`、`micrometer-registry-prometheus` 依赖（版本随 BOM，不写死）
  - [x] 1.2 application.yaml 顶部新增 `spring.application.name: blog-backend`，新增 `management` 段：endpoints.web.exposure.include=health,info,prometheus；endpoint.health.show-details=when-authorized；metrics.tags.application=${spring.application.name}；不单独配置 management.server.port（与业务同端口，经 context-path 暴露为 /api/actuator/**）
  - [x] 1.3 验证：`mvn compile` 通过；Grep 确认 yaml 无语法问题
- [x] Task 2: 编写 monitoring/ 监控配置
  - [x] 2.1 新建 `monitoring/prometheus/prometheus.yml`：scrape_interval 15s，job_name blog-backend 指向 `http://blog-backend:8080/api/actuator/prometheus`（容器网络内服务名互访），metrics_path 精确指定
  - [x] 2.2 新建 `monitoring/grafana/provisioning/datasources/datasource.yml`：Prometheus 数据源 `http://prometheus:9090`，设为默认
  - [x] 2.3 新建 `monitoring/grafana/provisioning/dashboards/provider.yml`：从 `/var/lib/grafana/dashboards` 加载 JSON
  - [x] 2.4 新建 `monitoring/grafana/dashboards/blog-monitor.json`：预置仪表盘（HTTP QPS、p95 延迟、5xx 错误率、JVM 堆内存、线程数、GC 暂停、CPU、HikariCP 面板），PromQL 均带 `application="blog-backend"` 过滤
- [x] Task 3: docker-compose 集成监控服务
  - [x] 3.1 新增 `prometheus` 服务（prom/prometheus 镜像，挂载 prometheus.yml 与 prometheus_data 卷，端口 9090，profiles ["monitor"]，加入 blog-net 网络）
  - [x] 3.2 新增 `grafana` 服务（grafana/grafana 镜像，挂载 provisioning 目录与 dashboard 目录、grafana_data 卷，端口 3000，GF_SECURITY_ADMIN_* 环境变量，depends_on prometheus，profiles ["monitor"]）
  - [x] 3.3 volumes 段新增 `prometheus_data`（name: blog_prometheus_data）、`grafana_data`（name: blog_grafana_data）
  - [x] 3.4 验证：全文复读 docker-compose.yml 确认 YAML 语法与缩进正确（本机无 docker，按既有预案目测复核）
- [x] Task 4: .env.example 与文档同步
  - [x] 4.1 .env.example 追加 `GRAFANA_ADMIN_USER`/`GRAFANA_ADMIN_PASSWORD`（带 ★ 生产必改注释）、COMPOSE_PROFILES 注释补 monitor 说明
  - [x] 4.2 README.md 新增「监控体系」小节：架构说明、访问地址、启用方式
  - [x] 4.3 部署操作手册.md 新增监控部署与使用步骤（启用 profile、访问 Grafana、看仪表盘）
- [x] Task 5: 整体验证
  - [x] 5.1 `mvn test`（blog-backend 目录，JAVA_HOME=graalvm-jdk-21.0.7）全部通过（56 用例，不因 actuator 引入破坏现有测试）
  - [x] 5.2 Grep 验证：pom 含两个新依赖、yaml 含 management 段、compose 含 prometheus/grafana 服务、dashboard JSON 可解析

# Task Dependencies
- Task 1、Task 2 相互独立，可并行
- Task 3 依赖 Task 2（compose 挂载路径需与 monitoring/ 目录结构对应）
- Task 4 依赖 Task 3（文档需引用最终端口与服务名）
- Task 5 依赖 Task 1-4 全部完成
