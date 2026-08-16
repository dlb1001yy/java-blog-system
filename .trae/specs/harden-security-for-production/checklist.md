# 上线前安全加固 Checklist

- [x] SecurityConfig CORS 来源改为读 `security.cors.allowed-origins` 配置（CorsProperties 绑定），未配置时默认 `*` 与现状一致（SecurityConfig.java 已复核）
- [x] 设置 `CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://admin.yourdomain.com` 后仅白名单来源可跨域携带凭证访问（setAllowedOrigins 精确匹配分支；compose/-.env 注入链路完整）
- [x] docker-compose mysql 服务注入 MYSQL_USER=blog_app / MYSQL_PASSWORD / MYSQL_DATABASE=dlbyy_z，首启自动建库建用户（含老部署回退注释）
- [x] backend 以 blog_app 账号连接 MySQL（compose environment 默认值与 mysql 侧同源 ${MYSQL_USERNAME:-blog_app}）
- [x] application.yaml / application-docker.yaml 数据源 URL 库名支持 `${MYSQL_DATABASE:dlbyy_zp_blog}` 覆盖，本地默认 dlbyy_zp_blog 不变（已 Read 复核）
- [x] 01-create_sql.sql 建库名与 MYSQL_DATABASE 默认值（dlbyy_z）一致；02-article_evolution.sql USE 行同步（Grep 确认 sql 目录无 dlbyy_zp_blog 残留）
- [x] Swagger 按 `swagger.enabled` 开关注册（@ConditionalOnProperty）与放行（条件 requestMatchers）：本地默认开、Docker 默认关
- [x] Docker 部署未设 SWAGGER_ENABLED 时 /api/doc.html 不可访问（SWAGGER_ENABLED:-false + 放行规则不注册）
- [x] MySQL 宿主机端口默认绑定 127.0.0.1（${MYSQL_PORT:-127.0.0.1:3306}）
- [x] .env.example 新增 CORS_ALLOWED_ORIGINS / MYSQL_DATABASE / MYSQL_USERNAME / MYSQL_PASSWORD / MYSQL_PORT / SWAGGER_ENABLED 且注释完整（已全文 Read 复核）
- [x] 部署操作手册含上线前安全检查清单（第 11 章：域名、专用账号、Swagger、端口、密钥 + 验证命令 + 老库升级提示；wiki 副本同步）
- [x] `mvn test` 全部通过（Tests run: 56, Failures: 0, Errors: 0, BUILD SUCCESS）
- [x] 本地开发（无任何环境变量）行为与改造前完全一致（CORS 默认 * / Swagger 默认开 / root + dlbyy_zp_blog / compose 默认值兜底）
