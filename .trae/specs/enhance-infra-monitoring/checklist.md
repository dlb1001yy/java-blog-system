# Checklist

- [x] prometheus.yml 新增 mysql-exporter、redis-exporter、elasticsearch-exporter 三个 job，目标端口分别为 9104/9121/9114
- [x] docker-compose.yml 含 3 个 exporter 服务，均挂 profiles ["monitor"]、加入 blog-net、不发布宿主机端口
- [x] mysql-exporter 密码经 MYSQLD_EXPORTER_PASSWORD 注入，复用 MYSQL_ROOT_PASSWORD 变量，depends_on mysql healthy
- [x] redis-exporter 使用 REDIS_ADDR 与 REDIS_PASSWORD 环境变量，depends_on redis healthy
- [x] elasticsearch-exporter 的 depends_on 对 elasticsearch 设 required: false，未启用 search profile 不阻塞 monitor 启动
- [x] blog-monitor.json 追加 MySQL / Redis / Elasticsearch 三行面板，PromQL 分别按 job="mysql-exporter"/"redis-exporter"/"elasticsearch-exporter" 过滤
- [x] 仪表盘 uid 不变，gridPos 无重叠，JSON 可解析
- [x] README.md 与 部署操作手册.md 已补充基础设施监控说明及 ES 目标 DOWN 属预期的提示
- [x] 未新增 .env 变量（复用 MYSQL_ROOT_PASSWORD、REDIS_PASSWORD）
- [x] 未启用 monitor profile 时 compose 行为与改动前完全一致（非破坏性）
- [x] 未修改任何后端 Java 代码与安全相关代码
