# Docker Compose 部署文档 Spec

## Why

用户已将 blog-admin、blog-backend、blog-frontend 三个项目推送到 Gitee，需要通过 Gitee 拉取代码并在本地 Ubuntu 22 的 Docker 环境中一键部署整套博客系统。当前项目仅有手动启动说明，缺少容器化部署方案，需要补充 Dockerfile、docker-compose.yml 以及完整的部署文档。

## What Changes

- **新增** blog-backend 的 `Dockerfile`（多阶段构建：Maven 编译 + JRE 运行）
- **新增** blog-backend 的 `application-docker.yaml`（适配 Docker 网络环境的数据库/Redis 连接配置与 Linux 上传路径）
- **新增** blog-admin 的 `Dockerfile`（多阶段构建：Node 编译 + nginx 托管静态资源）
- **新增** blog-admin 的 `nginx.conf`（托管 `/admin/` 路径下的静态文件，并反向代理 `/api` 到后端）
- **新增** blog-frontend 的 `Dockerfile`（多阶段构建：Node 编译 + nginx 托管静态资源）
- **新增** blog-frontend 的 `nginx.conf`（托管根路径下的静态文件，并反向代理 `/api` 到后端）
- **新增** 根目录 `docker-compose.yml`（编排 mysql、redis、blog-backend、blog-admin、blog-frontend 五个服务，含数据卷、网络、健康检查、依赖顺序）
- **新增** 根目录 `.dockerignore`（排除 node_modules、target、.git 等）
- **修改** 根目录 `README.md`，在现有「部署说明」章节之后新增完整的「Docker 部署（Ubuntu 22）」章节，包含：
  - 环境前提（Docker、Docker Compose、Git 安装）
  - 从 Gitee 克隆代码步骤
  - 目录结构说明
  - 配置文件说明（环境变量、端口映射、数据卷）
  - 一键启动命令
  - 服务访问地址
  - 常用运维命令（日志、重启、停止、清理）
  - 数据持久化与备份说明
  - 常见问题排查

## Impact

- Affected specs: 无（新增部署能力）
- Affected code:
  - `blog-backend/Dockerfile`（新建）
  - `blog-backend/src/main/resources/application-docker.yaml`（新建）
  - `blog-admin/Dockerfile`（新建）
  - `blog-admin/nginx.conf`（新建）
  - `blog-frontend/Dockerfile`（新建）
  - `blog-frontend/nginx.conf`（新建）
  - `docker-compose.yml`（新建）
  - `.dockerignore`（新建）
  - `README.md`（修改，新增 Docker 部署章节）

## ADDED Requirements

### Requirement: Docker 化后端服务

系统 SHALL 提供 blog-backend 的 Dockerfile，使用多阶段构建（Maven 3.9 + JDK 17 编译，Eclipse Temurin JRE 17 运行），构建产物为可执行 JAR，运行时通过 `--spring.profiles.active=docker` 激活 Docker 专属配置，容器对外暴露 8080 端口，上传目录通过数据卷持久化。

#### Scenario: 后端容器构建与启动
- **WHEN** 执行 `docker compose up -d blog-backend`
- **THEN** 容器使用 Maven 编译源码生成 JAR，以 JRE 启动，连接 mysql 与 redis 容器，监听 8080 端口

### Requirement: Docker 化管理后台

系统 SHALL 提供 blog-admin 的 Dockerfile，使用多阶段构建（Node 18 编译，nginx 1.25 托管），构建产物为 `dist/` 静态文件，nginx 配置 `base: /admin/` 路径，并将 `/api` 反向代理到 blog-backend:8080，容器对外暴露 80 端口。

#### Scenario: 管理后台访问
- **WHEN** 浏览器访问 `http://<服务器IP>:8081/admin/`
- **THEN** nginx 返回管理后台静态页面，API 请求经 `/api` 反向代理到后端容器

### Requirement: Docker 化前台门户

系统 SHALL 提供 blog-frontend 的 Dockerfile，使用多阶段构建（Node 18 编译，nginx 1.25 托管），构建产物为 `dist/` 静态文件，nginx 将 `/api` 反向代理到 blog-backend:8080，容器对外暴露 80 端口。

#### Scenario: 前台门户访问
- **WHEN** 浏览器访问 `http://<服务器IP>:8082/`
- **THEN** nginx 返回前台门户静态页面，API 请求经 `/api` 反向代理到后端容器

### Requirement: Docker Compose 编排

系统 SHALL 提供根目录 `docker-compose.yml`，定义 mysql（8.0）、redis（7-alpine）、blog-backend、blog-admin、blog-frontend 五个服务，使用自定义桥接网络 `blog-net` 通信，配置数据卷持久化 MySQL 数据、Redis 数据与上传文件，设置服务依赖顺序（mysql/redis 先于 backend，backend 先于两个前端），MySQL 容器启动时自动执行 `blog-backend/sql/create_sql.sql` 初始化数据库。

#### Scenario: 一键启动全部服务
- **WHEN** 在项目根目录执行 `docker compose up -d --build`
- **THEN** 按依赖顺序启动 MySQL → Redis → 后端 → 管理后台 → 前台门户，数据库自动初始化，全部服务健康运行

### Requirement: 部署文档

系统 SHALL 在根目录 `README.md` 中新增「Docker 部署（Ubuntu 22）」章节，包含环境准备、Gitee 克隆、配置说明、启动命令、访问地址、运维命令、备份与排障等完整内容，使用中文编写，命令可直接复制执行。

#### Scenario: 用户按文档部署
- **WHEN** 用户在 Ubuntu 22 服务器上按照 README.md 的 Docker 部署章节逐步执行
- **THEN** 能够成功克隆代码、构建镜像、启动服务，并通过浏览器访问前台门户与管理后台

## MODIFIED Requirements

### Requirement: 部署说明

在 `README.md` 现有「部署说明」章节之后，新增「Docker 部署（Ubuntu 22）」章节作为推荐的容器化部署方式，保留原有手动部署说明作为备选。

## REMOVED Requirements

无
