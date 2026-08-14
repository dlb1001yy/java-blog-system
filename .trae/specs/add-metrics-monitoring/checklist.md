# Checklist

- [x] pom.xml 包含 spring-boot-starter-actuator 与 micrometer-registry-prometheus 依赖，且未写死版本号（随 Boot 3.1.5 BOM）
- [ ] application.yaml 含 spring.application.name: blog-backend 与 management 配置段，仅暴露 health,info,prometheus
- [ ] management 配置中 metrics.tags.application 生效，指标带 application 标签
- [ ] monitoring/prometheus/prometheus.yml 抓取地址为 http://blog-backend:8080/api/actuator/prometheus，interval 15s
- [x] monitoring/grafana/provisioning/ 下 datasource 与 dashboard provider 配置齐全（自动预配，无需手工导入）
- [x] monitoring/grafana/dashboards/blog-monitor.json 为合法 JSON，包含 HTTP/JVM/CPU/HikariCP 面板，PromQL 过滤 application="blog-backend"
- [ ] docker-compose.yml 含 prometheus（9090）与 grafana（3000）服务，均挂 profiles ["monitor"]，加入 blog-net 网络
- [ ] docker-compose.yml 新增 prometheus_data、grafana_data 数据卷（命名 blog_prometheus_data / blog_grafana_data）
- [x] 未启用 monitor profile 时 compose 行为与改动前完全一致（非破坏性）
- [ ] 未修改任何安全相关代码（SecurityConfig / JwtAuthenticationFilter / RequestSignatureFilter 零改动）
- [ ] .env.example 追加 GRAFANA_ADMIN_USER / GRAFANA_ADMIN_PASSWORD，带生产必改提示
- [ ] README.md 与 部署操作手册.md 含监控部署与访问说明
- [ ] mvn test 全部通过（现有 56 用例无回归）
- [x] 敏感端点（env/beans/threaddump 等）未暴露（include 白名单验证）
