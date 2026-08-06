# Tasks

- [x] Task 1: 创建 blog-backend 的 Docker 配置文件
  - [x] SubTask 1.1: 创建 `blog-backend/src/main/resources/application-docker.yaml`，配置 MySQL 连接主机为 `mysql`、Redis 主机为 `redis`、上传路径为 `/app/uploads/`
  - [x] SubTask 1.2: 创建 `blog-backend/Dockerfile`，多阶段构建（maven:3.9-eclipse-temurin-17 编译 → eclipse-temurin:17-jre-jammy 运行），启动命令带 `--spring.profiles.active=docker`，暴露 8080 端口

- [x] Task 2: 创建 blog-admin 的 Docker 配置文件
  - [x] SubTask 2.1: 创建 `blog-admin/nginx.conf`，root 指向 `/usr/share/nginx/html`，配置 `/admin/` 路径的 `try_files` 支持 Vue Router history 模式，配置 `/api` 反向代理到 `http://blog-backend:8080`
  - [x] SubTask 2.2: 创建 `blog-admin/Dockerfile`，多阶段构建（node:18-alpine 编译 → nginx:1.25-alpine 运行），将 nginx.conf 与 dist 复制到运行镜像，暴露 80 端口

- [x] Task 3: 创建 blog-frontend 的 Docker 配置文件
  - [x] SubTask 3.1: 创建 `blog-frontend/nginx.conf`，root 指向 `/usr/share/nginx/html`，配置根路径的 `try_files` 支持 Vue Router history 模式，配置 `/api` 反向代理到 `http://blog-backend:8080`
  - [x] SubTask 3.2: 创建 `blog-frontend/Dockerfile`，多阶段构建（node:18-alpine 编译 → nginx:1.25-alpine 运行），将 nginx.conf 与 dist 复制到运行镜像，暴露 80 端口

- [x] Task 4: 创建根目录 docker-compose.yml
  - [x] SubTask 4.1: 定义 `blog-net` 自定义桥接网络
  - [x] SubTask 4.2: 定义 mysql 8.0 服务，挂载 `blog-backend/sql/create_sql.sql` 到 `/docker-entrypoint-initdb.d/` 自动初始化，配置数据卷 `mysql_data`，环境变量设置 root 密码与数据库名，暴露 3306 端口，配置健康检查
  - [x] SubTask 4.3: 定义 redis 7-alpine 服务，配置数据卷 `redis_data`，启动命令带密码 `--requirepass 123456`，暴露 6379 端口，配置健康检查
  - [x] SubTask 4.4: 定义 blog-backend 服务，build context 为 `./blog-backend`，依赖 mysql 与 redis（condition: service_healthy），挂载 `uploads_data` 到 `/app/uploads`，暴露 8080 端口，配置健康检查
  - [x] SubTask 4.5: 定义 blog-admin 服务，build context 为 `./blog-admin`，依赖 blog-backend，暴露 8081:80 端口
  - [x] SubTask 4.6: 定义 blog-frontend 服务，build context 为 `./blog-frontend`，依赖 blog-backend，暴露 8082:80 端口

- [x] Task 5: 创建根目录 .dockerignore
  - [x] SubTask 5.1: 创建 `.dockerignore`，排除 `**/node_modules`、`**/target`、`**/.git`、`**/dist`、`**/*.log`、`.trae/` 等

- [x] Task 6: 修改 README.md 新增 Docker 部署章节
  - [x] SubTask 6.1: 在「部署说明」章节之后新增「Docker 部署（Ubuntu 22）」一级标题
  - [x] SubTask 6.2: 编写环境准备小节（安装 Docker Engine、Docker Compose 插件、Git）
  - [x] SubTask 6.3: 编写从 Gitee 克隆代码小节（含 git clone 命令占位）
  - [x] SubTask 6.4: 编写配置说明小节（端口映射表、数据卷说明、可自定义的环境变量）
  - [x] SubTask 6.5: 编写一键启动小节（`docker compose up -d --build`）
  - [x] SubTask 6.6: 编写访问地址小节（前台门户 8082、管理后台 8081/admin/、后端 API 8080、接口文档）
  - [x] SubTask 6.7: 编写常用运维命令小节（查看日志、重启、停止、清理、重新构建单个服务）
  - [x] SubTask 6.8: 编写数据持久化与备份小节（数据卷位置、MySQL 备份命令、恢复命令）
  - [x] SubTask 6.9: 编写常见问题排查小节（端口占用、数据库连接失败、权限问题、镜像构建慢）

# Task Dependencies

- Task 4 依赖 Task 1、Task 2、Task 3（docker-compose 引用各子项目的 Dockerfile）
- Task 6 依赖 Task 1 ~ Task 5（文档需描述所有已创建的文件）
- Task 1、Task 2、Task 3、Task 5 可并行
