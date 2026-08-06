# Java 码农笔记

> 基于 Spring Boot 3 + Vue 3 的全栈博客系统，含管理后台、前台门户与 uni-app 移动端。

## 项目架构

```
┌─────────────────────────────────────────────────────────────┐
│                       浏览器 / 移动端                        │
└──────────────┬──────────────────┬──────────────┬────────────┘
               │                  │              │
       ┌───────▼──────┐   ┌───────▼──────┐  ┌────▼─────────┐
       │ blog-admin   │   │blog-frontend │  │   blog-app   │
       │ 管理后台      │   │ 前台门户      │  │  移动端       │
       │ Vue3+Vite5   │   │ Vue3+Vite5   │  │  uni-app+Vue3│
       │ Element Plus │   │ Element Plus │  │  多端         │
       └───────┬──────┘   └───────┬──────┘  └────┬─────────┘
               │                  │              │
               │     HTTP/REST    │              │
               └────────┬─────────┴──────────────┘
                        │
                ┌───────▼────────┐
                │  blog-backend  │
                │  Spring Boot 3 │
                │  端口 8080     │
                │  context-path  │
                │     /api       │
                └───┬────────┬───┘
                    │        │
              ┌─────▼──┐  ┌──▼─────┐
              │ MySQL  │  │ Redis  │
              │ 8.x    │  │ 7.x    │
              └────────┘  └────────┘
```

## 技术栈

| 层级 | 框架 | 版本 |
|------|------|------|
| 后端 | Spring Boot | 3.1.5 |
| 后端 | MyBatis Plus | 3.5.5 |
| 后端 | Spring Security + JWT | 6.x / 0.11.5 |
| 后端 | Knife4j | 4.3.0 |
| 前端/后台 | Vue 3 + Vite 5 | 3.3.8 / 5.0 |
| 前端/后台 | Element Plus | 2.4.3 |
| 移动端 | uni-app + Vue 3 | — |
| 数据库 | MySQL 8.x + Redis 7.x | — |

## 模块说明

| 模块 | 说明 | 端口 |
|------|------|------|
| blog-backend | Spring Boot 后端服务，提供 REST API | 8080 |
| blog-admin | Vue3 管理后台 | 8081 |
| blog-frontend | Vue3 前台门户 | 8082 |
| blog-app | uni-app 移动端（H5/小程序） | — |

## 快速开始

### 本地开发

1. **后端**：导入 IDEA，配置 MySQL 与 Redis，运行 `JavaBlogApplication.java`
2. **管理后台**：`cd blog-admin && npm install && npm run dev`
3. **前台门户**：`cd blog-frontend && npm install && npm run dev`
4. **移动端**：用 HBuilderX 打开 `blog-app` 目录

### Docker 部署

```bash
git clone https://gitee.com/<你的用户名>/<仓库名>.git
cd java-blog-system
docker compose up -d --build
```

访问地址：

| 服务 | 地址 |
|------|------|
| 前台门户 | http://localhost:8082 |
| 管理后台 | http://localhost:8081/admin/ |
| 后端 API | http://localhost:8080/api |
| 接口文档 | http://localhost:8080/api/doc.html |

默认账号：`admin` / `admin123`

## Wiki 文档导航

- [Docker环境安装](Docker环境安装) — Ubuntu 22.04 上安装 Docker 的完整步骤
- [部署操作手册](部署操作手册) — 从镜像拉取到访问测试的完整部署流程
- [项目开发文档](项目开发文档) — 架构设计、后端/前端/移动端完整代码与问题修复

## 相关文档

- [README.md](../blob/master/README.md) — 项目完整说明
- [Docker环境安装指南.md](../blob/master/Docker环境安装指南.md) — Docker 安装指南
- [部署操作手册.md](../blob/master/部署操作手册.md) — 部署操作手册
- [项目开发文档合集.md](../blob/master/项目开发文档合集.md) — 开发文档合集
