---
name: blog-frontend-static-asset-fix
overview: 修复 blog-frontend 因 base 改为 /blog/ 后导致的静态资源（favicon）引用路径问题，并清理遗留死代码。
todos:
  - id: fix-index-favicon
    content: 修改 blog-frontend/index.html 的 favicon 引用为 /blog/favicon.svg
    status: completed
  - id: add-nginx-favicon
    content: 在 blog-frontend/nginx.conf 增加 /favicon.ico 兜底 alias
    status: completed
  - id: delete-helloworld
    content: 删除 blog-frontend/src/components/HelloWorld.vue 死代码
    status: completed
---

## 用户需求
因 Nginx 改动，`blog-frontend` 的 Vite base 从默认的 `/` 改为 `/blog/`（路由也改为 `createWebHistory('/blog/')`）。需要把受此改动影响、但仍未对齐的"相关代码"也一并修改，确保静态资源在子路径部署下能正确加载。

## 产品概述
`blog-frontend` 将以 `/blog/` 为根路径部署在 Docker 容器中（外部 Nginx 通过 `location /blog` 代理到容器 8082）。由于 base 变为子路径，原本使用根路径绝对引用的静态资源（favicon、公共 svg 图标）会解析到网站根而非 `/blog/`，导致 404。需修正这些资源引用并清理遗留死代码。

## 核心功能
- 修正 `blog-frontend/index.html` 的 favicon 引用，使其指向 `/blog/favicon.svg`，落在 `/blog` location 由容器 nginx 正常返回
- 在 `blog-frontend/nginx.conf` 增加 favicon 兜底规则，避免 `/favicon.ico` 请求产生 404 噪音日志
- 删除 `blog-frontend/src/components/HelloWorld.vue` 死代码（引用根路径 `/icons.svg`，base 改动后会 404 且组件从未被使用）


## 技术栈
- 前端框架：Vue 3 + Vite（uni-app 之外的标准 Vue SPA）
- 部署：Docker + Nginx（容器内部 nginx 1.25-alpine）
- 路由：Vue Router `createWebHistory('/blog/')`

## 实现方案

### 整体策略
基于 code-explorer 的全面扫描结论：业务代码（路由跳转、axios `/api` 代理、跨项目链接、blog-admin 配置）均已正确，**唯一受 base 子路径改动影响的是"根路径绝对引用"的静态资源**。因此本次只做静态资源对齐与死代码清理，不触碰业务逻辑。

### 关键技术决策
1. **favicon 引用改为 `/blog/favicon.svg`**：Vite 的 `base` 只影响 JS/CSS 等资源注入，HTML 中的 `<link rel="icon">` 由开发者手写、不受 base 自动改写。改为 `/blog/favicon.svg` 后，浏览器请求 `/blog/favicon.svg`，被容器 nginx 的 `location /blog` 命中并经 `alias /usr/share/nginx/html/blog` 正确返回 `favicon.svg`。相比在外部 nginx 给 `/favicon.ico` 加兜底，此方案让请求自然落在 `/blog` 链路，更干净且不依赖额外配置。
2. **nginx.conf 增加 favicon 兜底**：为消除老旧浏览器/外部爬虫直接请求 `/favicon.ico` 时的 404 日志噪音，在容器 nginx 增加 `location = /favicon.ico` 显式 alias 到 `blog/favicon.svg`。此项为防御性增强，与 blog-admin 既有做法一致（admin 已对 `/admin/favicon.ico` 做 alias）。
3. **删除 HelloWorld.vue**：该组件引用根路径 `/icons.svg#...`（共 6 处），但全项目无任何 import（搜索命中 0），属脚手架遗留死代码。base 改为 `/blog/` 后其引用会 404，且本身不可达，直接删除避免误导与潜在 404。

### 实现注意事项
- **不修改业务代码**：vue-router 在 `createWebHistory('/blog/')` 下自动为 `router.push`/`router-link` 拼接前缀；axios `baseURL: '/api'` 由外部 Nginx `location /api/` 代理，与前端 base 完全解耦。这些保持不动，避免引入回归。
- **向后兼容**：base 为子路径后，直接访问容器 8082 根路径（`location = /` 已 301 到 `/blog/`）行为不变。
- **性能**：favicon 兜底为精确 `location =` 匹配，开销可忽略；删除死代码缩小构建产物体积。
- **校验**：修改后 `npm run build` 确认产物中 `index.html` 引用 `/blog/favicon.svg`，且 `dist/blog/` 下存在 `favicon.svg`。

## 架构设计
本次改动不改变系统架构，仅修正资源引用与清理死代码，保持与 blog-admin 既有的子路径部署模式（`base + 路由 history + Dockerfile 子目录 + nginx location`）一致。

## 目录结构
```
blog-frontend/
├── index.html                                  # [MODIFY] 第5行 favicon 引用改为 /blog/favicon.svg
├── nginx.conf                                  # [MODIFY] 增加 location = /favicon.ico 兜底 alias 到 /usr/share/nginx/html/blog/favicon.svg
└── src/
    └── components/
        └── HelloWorld.vue                      # [DELETE] 删除死代码组件（引用根路径 /icons.svg，从未被 import）
```

