# Docker 环境隐藏 API 文档 Spec

## Why
Docker 生产环境中，虽然 `swagger.enabled` 开关（docker-compose 默认注入 `SWAGGER_ENABLED=false`）已移除自定义 OpenAPI Bean 并由 SecurityConfig 拦截文档路径，但 SpringDoc / Knife4j 的自动配置仍会注册 `/v3/api-docs`、`/swagger-ui/**` 等端点，存在接口信息泄露风险，需在配置层面彻底关闭。

## What Changes
- 在 `blog-backend/src/main/resources/application-docker.yaml` 中新增 `springdoc.api-docs.enabled: false` 与 `springdoc.swagger-ui.enabled: false`，docker profile 下彻底禁用 SpringDoc 文档端点
- 本地开发（默认 profile 的 `application.yaml`）行为不变，Knife4j 文档仍默认开启

## Impact
- Affected specs: 无（部署安全加固，与 harden-security-for-production 方向一致但不修改其内容）
- Affected code: `blog-backend/src/main/resources/application-docker.yaml`

## ADDED Requirements
### Requirement: Docker 环境关闭 SpringDoc API 文档端点
系统 SHALL 在 docker profile 激活时禁用 SpringDoc 的 api-docs 与 swagger-ui 端点。

#### Scenario: Docker 环境访问文档端点
- **WHEN** 应用以 `--spring.profiles.active=docker` 启动
- **THEN** `/api/v3/api-docs` 与 `/api/swagger-ui/**` 不再由 SpringDoc 提供内容（结合 SecurityConfig 已拦截文档路径，文档彻底不可用）

#### Scenario: 本地开发不受影响
- **WHEN** 应用以默认 profile 启动
- **THEN** Knife4j 文档（/api/doc.html）仍可正常访问
