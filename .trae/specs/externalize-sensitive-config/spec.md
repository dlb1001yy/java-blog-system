# 敏感配置外部化（环境变量）Spec

## Why
敏感信息当前硬编码且随 git 仓库分发：MySQL/Redis 密码 `123456` 明文在 [application.yaml](d:/my-project/java-blog-system/blog-backend/src/main/resources/application.yaml)、application-docker.yaml 与 docker-compose.yml 中；JWT 密钥与 API 签名密钥仅"支持环境变量但有硬编码默认值"；前端签名密钥 `BlogApiSigningSecret2024!` 硬编码在 blog-admin/blog-app 源码中且随构建产物公开。需要建立统一的环境变量外部化机制。

## What Changes
- 后端 yaml：数据库/Redis 账密改为 `${ENV_VAR:本地开发默认值}` 占位（保留本地开发开箱即用）；JWT_SECRET/API_SIGNING_SECRET 去除 yaml 中的弱默认值、改为 `${ENV_VAR:开发默认}` 并在 docker 链路强制注入；OSS 密钥改为 `${OSS_*:占位符}` 占位
- `SignatureProperties.java` 删除硬编码默认密钥（改为空串，由 yaml/环境变量提供）
- docker-compose.yml：MYSQL_ROOT_PASSWORD/Redis 密码/healthcheck 全部改为 `${VAR:-默认}` 插值；backend environment 段注入全部敏感变量
- 新增 `.env.example`（安全模板，入 git）；`.gitignore` 忽略 `.env`
- 前端签名密钥接入构建期环境变量：blog-admin 用 `import.meta.env.VITE_API_SIGNING_SECRET`（vite .env 机制），blog-app 用条件编译 + 编译期常量文件（uni-app 无 vite env），均保留原值为开发默认
- blog-admin 新增 `.env.development`/`.env.production`；blog-app 新增 `common/env.js`（编译期替换）；各模块 .gitignore 同步忽略本地覆盖文件
- 文档同步：README 配置说明表、部署操作手册补充 .env 配置说明

## Impact
- Affected specs: deploy-with-docker-compose（compose 密码插值化）、prevent-token-replay-attack（签名密钥通道变化，签名算法不变）
- Affected code:
  - blog-backend：`application.yaml`、`application-docker.yaml`、`properties/SignatureProperties.java`
  - 部署：`docker-compose.yml`、新增 `.env.example`、根 `.gitignore`
  - blog-admin：`src/api/signing.js`、新增 `.env.development`/`.env.production`、`.gitignore`
  - blog-app：`common/signing.js`、新增 `common/env.js`（或改造 config.js）、`.gitignore`
  - 文档：`README.md`、`部署操作手册.md`
- **非 BREAKING**：所有变量均带本地开发默认值，现有本地/Docker 部署行为不变（除非主动注入新值）
- 范围说明：不修改 git 历史、不轮换已泄露密钥值（默认值仍为原值，生产通过环境变量覆盖实现"换钥"）、不处理文档中已展示的示例密码（文档描述改为引导配置）

## ADDED Requirements

### Requirement: 后端配置占位化
application.yaml / application-docker.yaml 中所有敏感字段 SHALL 使用 `${ENV:default}` 占位。

#### Scenario: 本地开发
- **WHEN** 未设置任何环境变量启动后端
- **THEN** 使用占位默认值（localhost/123456/原密钥），行为与现状完全一致

#### Scenario: 生产覆盖
- **WHEN** 设置 `MYSQL_PASSWORD`、`REDIS_PASSWORD`、`JWT_SECRET`、`API_SIGNING_SECRET`、`OSS_ACCESS_KEY_ID` 等变量
- **THEN** 配置取环境变量值

#### Scenario: 签名密钥默认值收敛
- **WHEN** 查看 `SignatureProperties.java`
- **THEN** 字段默认值为空串，无硬编码密钥；实际值来自 yaml（`${API_SIGNING_SECRET:开发默认}`）

### Requirement: docker-compose 环境变量插值
docker-compose.yml 中 MySQL 密码、Redis 密码、healthcheck 命令 SHALL 使用 `${VAR:-default}` 插值；backend 服务 environment 段 SHALL 注入 `MYSQL_PASSWORD`、`REDIS_PASSWORD`、`JWT_SECRET`、`API_SIGNING_SECRET`、`BLOG_SEARCH_ENABLED`。

#### Scenario: 无 .env 部署
- **WHEN** 不提供 .env 直接 `docker compose up`
- **THEN** 使用插值默认值，与现状一致

#### Scenario: .env 覆盖
- **WHEN** .env 中定义 `MYSQL_ROOT_PASSWORD=强密码` 等
- **THEN** MySQL/Redis/backend 同步使用新值（数据库与后端密码一致）

### Requirement: .env 机制
仓库根 SHALL 提供 `.env.example`（含全部变量与说明注释，占位值非真实密钥）；`.gitignore` SHALL 忽略 `.env`。

#### Scenario: 新环境部署
- **WHEN** 按 `.env.example` 复制为 `.env` 并填写真实值
- **THEN** compose 与后端读取到真实值，`.env` 不入 git

### Requirement: 前端签名密钥环境变量化
blog-admin SHALL 通过 `import.meta.env.VITE_API_SIGNING_SECRET` 读取签名密钥（vite `.env.*` 文件机制）；blog-app SHALL 通过编译期 env 常量读取。均保留 `BlogApiSigningSecret2024!` 为开发默认值。

#### Scenario: blog-admin 开发/构建
- **WHEN** 未配置 VITE_API_SIGNING_SECRET
- **THEN** 使用默认值，签名行为与现状一致
- **WHEN** `.env.production` 配置新密钥后 build
- **THEN** 构建产物使用新密钥

#### Scenario: blog-app
- **WHEN** 修改 `common/env.js` 中的常量（或 HBuilderX 自定义条件编译）
- **THEN** 签名使用新密钥；默认仍为原值

## MODIFIED Requirements

### Requirement: 部署文档环境变量说明（原仅 BLOG_SEARCH_ENABLED）
`部署操作手册.md` SHALL 补充 `.env` 完整变量表（MYSQL_ROOT_PASSWORD/REDIS_PASSWORD/JWT_SECRET/API_SIGNING_SECRET/BLOG_SEARCH_ENABLED）与"复制 .env.example"操作步骤；`README.md` 配置说明表同步更新为环境变量优先的表述。
