# Checklist

## 后端 Docker 配置

- [x] `blog-backend/src/main/resources/application-docker.yaml` 存在，且 MySQL host 为 `mysql`、Redis host 为 `redis`、`file.upload-path` 为 Linux 路径 `/app/uploads/`
- [x] `blog-backend/Dockerfile` 使用多阶段构建，第一阶段 Maven 编译，第二阶段 JRE 运行
- [x] `blog-backend/Dockerfile` 启动命令包含 `--spring.profiles.active=docker`
- [x] `blog-backend/Dockerfile` `EXPOSE 8080`

## 管理后台 Docker 配置

- [x] `blog-admin/nginx.conf` 配置了 `/api` 反向代理到 `http://blog-backend:8080`
- [x] `blog-admin/nginx.conf` 配置了 `/admin/` 路径的 `try_files $uri $uri/ /admin/index.html`
- [x] `blog-admin/Dockerfile` 使用多阶段构建，第一阶段 Node 编译，第二阶段 nginx 运行
- [x] `blog-admin/Dockerfile` 将 `dist/` 复制到 nginx html 目录，将 `nginx.conf` 复制到 `/etc/nginx/conf.d/default.conf`
- [x] `blog-admin/Dockerfile` `EXPOSE 80`

## 前台门户 Docker 配置

- [x] `blog-frontend/nginx.conf` 配置了 `/api` 反向代理到 `http://blog-backend:8080`
- [x] `blog-frontend/nginx.conf` 配置了根路径的 `try_files $uri $uri/ /index.html`
- [x] `blog-frontend/Dockerfile` 使用多阶段构建，第一阶段 Node 编译，第二阶段 nginx 运行
- [x] `blog-frontend/Dockerfile` 将 `dist/` 复制到 nginx html 目录，将 `nginx.conf` 复制到 `/etc/nginx/conf.d/default.conf`
- [x] `blog-frontend/Dockerfile` `EXPOSE 80`

## docker-compose.yml

- [x] 定义了 `blog-net` 自定义桥接网络
- [x] mysql 服务使用 `mysql:8.0` 镜像，挂载 `create_sql.sql` 到 `/docker-entrypoint-initdb.d/`
- [x] mysql 服务配置了 `mysql_data` 数据卷与健康检查
- [x] redis 服务使用 `redis:7-alpine` 镜像，配置了 `redis_data` 数据卷与健康检查
- [x] blog-backend 服务 build context 为 `./blog-backend`，依赖 mysql 与 redis 的 `service_healthy`
- [x] blog-backend 服务挂载 `uploads_data` 到 `/app/uploads`
- [x] blog-admin 服务暴露 `8081:80`，依赖 blog-backend
- [x] blog-frontend 服务暴露 `8082:80`，依赖 blog-backend
- [x] 所有服务都加入 `blog-net` 网络

## .dockerignore

- [x] 排除了 `**/node_modules`、`**/target`、`**/.git`、`**/dist`
- [x] 排除了 `.trae/` 目录

## README.md Docker 部署章节

- [x] 新增了「Docker 部署（Ubuntu 22）」一级标题章节
- [x] 包含环境准备小节（Docker、Docker Compose、Git 安装命令）
- [x] 包含从 Gitee 克隆代码的命令
- [x] 包含端口映射表（8080/8081/8082/3306/6379）
- [x] 包含数据卷说明
- [x] 包含一键启动命令 `docker compose up -d --build`
- [x] 包含访问地址（前台门户 `:8082`、管理后台 `:8081/admin/`、接口文档 `:8080/api/doc.html`）
- [x] 包含常用运维命令（日志、重启、停止、清理、重建单服务）
- [x] 包含 MySQL 备份与恢复命令
- [x] 包含常见问题排查（端口占用、数据库连接失败、镜像构建慢等）
- [x] 原有「部署说明」手动部署内容保留未被删除
