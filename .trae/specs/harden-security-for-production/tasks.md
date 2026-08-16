# Tasks

- [x] Task 1: CORS 来源白名单化
  - [x] SubTask 1.1: 新增 `CorsProperties.java`（`security.cors` 前缀绑定，`allowedOrigins` 默认 `["*"]`；注：因 SecurityProperties 实际前缀为 `security.login`（扁平结构、8 处注入），遵循项目既有独立属性类模式，效果与计划一致）
  - [x] SubTask 1.2: `SecurityConfig.java` 的 `corsConfigurationSource()` 改读该配置：值为 `*` 时保持 `setAllowedOriginPatterns(list)` 现行为；否则 `setAllowedOrigins(list)` 精确注入
  - [x] SubTask 1.3: `application.yaml` 增加 `security.cors.allowed-origins: ${CORS_ALLOWED_ORIGINS:*}` 及注释（生产填精确域名）
  - [x] SubTask 1.4: `docker-compose.yml` backend environment 注入 `CORS_ALLOWED_ORIGINS: ${CORS_ALLOWED_ORIGINS:-*}`；`.env.example` 新增该项（★生产必改）
- [x] Task 2: MySQL 专用账号与库名对齐
  - [x] SubTask 2.1: `docker-compose.yml` mysql 服务 environment 增加 `MYSQL_USER: ${MYSQL_USERNAME:-blog_app}`、`MYSQL_PASSWORD: ${MYSQL_PASSWORD:-123456}`，`MYSQL_DATABASE` 改为 `${MYSQL_DATABASE:-dlbyy_z}`，附老部署回退注释
  - [x] SubTask 2.2: backend environment 的 `MYSQL_USERNAME` 改为 `${MYSQL_USERNAME:-blog_app}`、`MYSQL_PASSWORD: ${MYSQL_PASSWORD:-123456}`（与 mysql 侧同源默认）
  - [x] SubTask 2.3: `application.yaml` 与 `application-docker.yaml` 数据源 URL 库名占位化为 `${MYSQL_DATABASE:dlbyy_zp_blog}`（本地默认不变）
  - [x] SubTask 2.4: `01-create_sql.sql` 建库/USE 行改为 `dlbyy_z`；追加修正 `02-article_evolution.sql` 的 USE 行
  - [x] SubTask 2.5: `.env.example` 数据库段改为 `MYSQL_DATABASE=dlbyy_z`、`MYSQL_USERNAME=blog_app`、`MYSQL_PASSWORD=<强密码>`、`MYSQL_PORT`，注明仅首次建库生效
- [x] Task 3: Swagger 生产关闭
  - [x] SubTask 3.1: `SwaggerConfig.java` 增加 `@ConditionalOnProperty(name="swagger.enabled", havingValue="true", matchIfMissing=true)`
  - [x] SubTask 3.2: `SecurityConfig.java` 文档放行项按 `swaggerEnabled`（@Value 默认 true）条件注册，块式 lambda 重构，其余规则不变
  - [x] SubTask 3.3: `application.yaml` 增加 `swagger.enabled: ${SWAGGER_ENABLED:true}`；compose backend 注入 `SWAGGER_ENABLED: ${SWAGGER_ENABLED:-false}`；`.env.example` 新增该项
- [x] Task 4: MySQL 端口收敛
  - [x] SubTask 4.1: `docker-compose.yml` mysql `ports` 改为 `"${MYSQL_PORT:-127.0.0.1:3306}"`；`.env.example` 注明默认仅回环
- [x] Task 5: 验证
  - [x] SubTask 5.1: `mvn test` 全部通过：**Tests run: 56, Failures: 0, Errors: 0, BUILD SUCCESS**（GraalVM JDK 21 + 项目内临时仓库）
  - [x] SubTask 5.2: 编译随测试通过；SecurityConfig/SwaggerConfig/CorsProperties 改动经全文 Read 复核无遗漏
  - [x] SubTask 5.3: docker-compose.yml 全文复核：插值语法/缩进/mysql 与 backend 默认值同源一致（本机无 docker）
- [x] Task 6: 文档同步
  - [x] SubTask 6.1: `部署操作手册.md` 新增「11. 上线前安全检查」（检查清单+快速验证命令+老库升级提示）；1.5 节变量表补 6 项；wiki 副本同步
  - [x] SubTask 6.2: `.env.example` 全文复核：新增变量均有中文注释与 ★ 标注

# Task Dependencies
- Task 1、2、3、4 相互独立，可并行
- Task 5 依赖 Task 1-4
- Task 6 依赖 Task 1-4（可与 Task 5 并行）
