# Docker 环境 Actuator 端点加固 Spec

## Why
Docker 生产环境中 `application-docker.yaml` 未覆盖 `management` 配置，Actuator 完全继承主配置的 `include: health,info,prometheus` 白名单，但缺少显式的敏感端点 `exclude`，建议在 docker profile 中显式声明生产端点策略，防止后续主配置变动导致敏感端点（env/beans/configprops）意外暴露。

## What Changes
- 在 `blog-backend/src/main/resources/application-docker.yaml` 中新增 `management` 配置段：
  - `endpoints.web.exposure.include: health,info,prometheus`
  - `endpoints.web.exposure.exclude: env,beans,configprops`（显式排除敏感端点）
- 本地开发（application.yaml）行为不变

## Impact
- Affected specs: add-metrics-monitoring（其 Task 1.2 定义了主配置的 management 段，本次为 docker profile 补充覆盖，不修改其内容）
- Affected code: `blog-backend/src/main/resources/application-docker.yaml`

## ADDED Requirements
### Requirement: Docker 环境 Actuator 端点最小暴露
系统 SHALL 在 docker profile 激活时仅暴露 health、info、prometheus 端点，并显式排除 env、beans、configprops 敏感端点。

#### Scenario: Docker 环境访问 Actuator 端点
- **WHEN** 应用以 `--spring.profiles.active=docker` 启动
- **THEN** `/api/actuator/health`、`/api/actuator/info`、`/api/actuator/prometheus` 可用
- **THEN** `/api/actuator/env`、`/api/actuator/beans`、`/api/actuator/configprops` 返回 404（未暴露）

#### Scenario: 本地开发不受影响
- **WHEN** 应用以默认 profile 启动
- **THEN** Actuator 行为与现状一致（application.yaml 的 management 配置不变）
