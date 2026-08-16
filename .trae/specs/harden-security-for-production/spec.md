# 上线前安全加固 Spec

## Why
系统即将上线，当前存在多项上线前必须处理的安全缺口：后端 CORS 允许任意来源（`setAllowedOriginPatterns(Arrays.asList("*"))` 且 `allowCredentials=true`），任何恶意站点都可携带 Cookie 凭证跨域调用 API；MySQL 一直使用 root 账号连接，权限过大；数据库名 `dlbyy_zp_blog` 与用户提供的 `dlbyy_z` 不一致；`.env.example` 与部署文档缺少生产域名类配置说明。

## What Changes
- **CORS 收敛（SecurityConfig.java）**：新增 `security.cors.allowed-origins` 配置（支持环境变量 `CORS_ALLOWED_ORIGINS`），默认 `*` 保持本地开发兼容，生产通过环境变量注入精确域名列表；`SecurityProperties` 增加对应绑定字段
- **MySQL 专用账号**：`docker-compose.yml` 为 mysql 服务注入 `MYSQL_USER=blog_app` / `MYSQL_PASSWORD` / `MYSQL_DATABASE=dlbyy_z`（首启自动建库建用户）；后端 compose environment 注入 `MYSQL_USERNAME=blog_app`；`application*.yaml` 数据源 URL 数据库名占位化 `${MYSQL_DATABASE:dlbyy_zp_blog}`；`.env.example` 同步新增三个变量并标注"首次部署后改库需手工迁移"
- **本地 SQL 脚本**：`01-create_sql.sql` 的 `CREATE DATABASE` 行同步为 `dlbyy_z`（与容器首启建库一致，避免本地与 Docker 库名漂移）
- **Swagger 生产关闭**：`SwaggerConfig` 按 `swagger.enabled` 开关注册（默认 true），docker-compose 后端注入 `SWAGGER_ENABLED=${SWAGGER_ENABLED:-false}`，`.env.example` 增加该项；SecurityConfig 放行路径同步按开关控制
- **数据库端口收敛**：mysql 端口映射 `${MYSQL_PORT:-127.0.0.1:3306}`，生产默认仅本机回环可达，需要外连时在 .env 显式放开
- **文档同步**：`.env.example` 注释、`部署操作手册.md` 上线检查清单补充 CORS 域名 / 专用账号 / Swagger 关闭 / 端口收敛说明

## Impact
- Affected specs: deploy-with-docker-compose（新增环境变量）、externalize-sensitive-config（.env 变量扩展）
- Affected code:
  - blog-backend：`config/SecurityConfig.java`、`config/SwaggerConfig.java`、`properties/SecurityProperties.java`、`resources/application.yaml`、`resources/application-docker.yaml`、`sql/01-create_sql.sql`
  - 部署：`docker-compose.yml`、`.env.example`
  - 文档：`部署操作手册.md`
- **BREAKING（仅 Docker 生产新部署的库初始化）**：`MYSQL_DATABASE` 由 `dlbyy_zp_blog` 变更为 `dlbyy_z`。已有数据卷（首次启动过旧版本）不受影响——MYSQL_USER/MYSQL_DATABASE 仅在数据目录为空时生效，老库继续用 root/原库名运行；全新部署自动创建 `dlbyy_z` 与 `blog_app` 用户。本地开发默认值仍是 `dlbyy_zp_blog` + root（零改动）
- 非 BREAKING：CORS/Swagger/端口映射均带安全默认开关，未配置环境变量时本地与现有 Docker 行为不变（CORS 默认 `*`、Swagger 本地默认开、Docker 默认关）

## ADDED Requirements

### Requirement: CORS 来源白名单化
系统 SHALL 通过配置 `security.cors.allowed-origins`（逗号分隔）控制允许的跨域来源，支持环境变量 `CORS_ALLOWED_ORIGINS` 覆盖；未配置时默认 `*`（开发兼容），配置后仅白名单来源可携带凭证跨域访问。

#### Scenario: 本地开发未配置
- **WHEN** 未设置 `CORS_ALLOWED_ORIGINS`
- **THEN** CORS 允许所有来源（与现状一致），本地前后端联调不受影响

#### Scenario: 生产白名单
- **WHEN** 设置 `CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://admin.yourdomain.com`
- **THEN** 仅这两个来源的跨域请求被放行，其余来源浏览器侧被拦截（无 CORS 响应头）

### Requirement: MySQL 专用应用账号
Docker 部署 SHALL 使用专用账号 `blog_app`（由 MYSQL_USER/MYSQL_PASSWORD 首启自动创建）连接数据库，root 仅保留维护用途；数据库名 SHALL 由 `MYSQL_DATABASE` 控制（新部署默认 `dlbyy_z`）。

#### Scenario: 全新部署
- **WHEN** 首次 `docker compose up` 且数据卷为空
- **THEN** MySQL 自动创建库 `dlbyy_z`、用户 `blog_app` 并授予该库全部权限；后端以 `blog_app` 连接

#### Scenario: 存量数据卷
- **WHEN** 使用已初始化的数据卷重新部署
- **THEN** 建库/建用户语句不执行，后端按 .env 中配置的账号密码连接原库，数据不丢

#### Scenario: 本地开发
- **WHEN** 本地直连启动后端
- **THEN** 仍用 root + `dlbyy_zp_blog` 默认值，无需任何改动

### Requirement: Swagger 生产可关闭
API 文档（doc.html / swagger-ui / v3/api-docs / webjars）SHALL 可通过 `swagger.enabled` 开关控制注册与放行；Docker 部署默认关闭，本地开发默认开启。

#### Scenario: Docker 默认
- **WHEN** Docker 部署且未设置 `SWAGGER_ENABLED`
- **THEN** 访问 `/api/doc.html` 返回 404/不可用，接口结构不对外暴露

#### Scenario: 需要联调排查
- **WHEN** .env 设置 `SWAGGER_ENABLED=true` 后重启
- **THEN** 文档恢复可访问

### Requirement: MySQL 端口收敛
Docker 部署时 MySQL 宿主机端口映射 SHALL 默认绑定 `127.0.0.1`（`${MYSQL_PORT:-127.0.0.1:3306}`），避免生产库直接暴露公网。

#### Scenario: 默认部署
- **WHEN** 不配置 `MYSQL_PORT`
- **THEN** 3306 仅本机可达，公网扫描无法连接数据库

#### Scenario: 需要外部连接
- **WHEN** .env 设置 `MYSQL_PORT=3306`
- **THEN** 端口对全网开放（配合强密码/安全组使用）

## MODIFIED Requirements

### Requirement: .env 模板与部署文档（原 externalize-sensitive-config 定义）
`.env.example` SHALL 新增：`CORS_ALLOWED_ORIGINS`、`MYSQL_DATABASE=dlbyy_z`、`MYSQL_USER=blog_app`、`MYSQL_PASSWORD=<强密码>`、`MYSQL_PORT`、`SWAGGER_ENABLED`，并注明 root 密码仅维护用；`部署操作手册.md` SHALL 补充上线前安全检查清单（换强密码、CORS 域名、Swagger 关闭、端口收敛、专用账号语义）。
