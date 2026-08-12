---
name: docs-sync-blog-deploy-changes
overview: 将本次三轮回合的前端部署改动（blog-frontend 子路径 /blog/、blog-app 条件编译 BASE_URL、外部 Nginx 80 端口统一代理）同步更新到项目全部 7 个文档中。
todos:
  - id: update-readme
    content: 更新 README.md 的移动端配置、前端/移动端打包、Docker 访问地址与 404 排查章节
    status: completed
  - id: update-deploy-manual
    content: 更新 部署操作手册.md 与 wiki/部署操作手册.md 的前台门户访问路径、地址汇总与构建产物目录
    status: completed
  - id: update-wiki-home-docker
    content: 更新 wiki/Home.md 与 wiki/Docker环境安装.md 的访问地址表
    status: completed
  - id: update-dev-docs
    content: 更新 项目开发文档合集.md 与 wiki/项目开发文档.md 的 BASE_URL 示例、Nginx 配置示例与移动端说明
    status: completed
---

## 用户需求
将前几轮针对 blog-frontend 子路径改造、blog-app 环境切换、外部 Nginx 统一代理等代码改动，同步更新到项目全部文档中，保证文档与代码一致。

## 产品概述
本次任务为纯文档同步更新，涉及 README、部署手册、Wiki 等 7 个 Markdown 文件。需要同步的代码事实包括：
- blog-frontend 部署路径从根 `/` 改为 `/blog/`（Vite base、路由、Dockerfile 产物目录、容器 nginx 均对齐，favicon 引用同步修正）
- blog-app 的 `common/config.js` 改用 uni-app 条件编译区分 H5（localhost:8080/api）与 APP-PLUS（http://gz.aeert.com:19612/api）
- 外部 Nginx 80 端口通过 `/admin`、`/blog`、`/api/` 三个 location 统一代理到 8081/8082/8080 容器端口

## 核心功能
- 更新 README.md：移动端配置代码示例、前端打包说明、Docker 访问地址、404 排查路径
- 更新 部署操作手册.md 与 wiki/部署操作手册.md：前台门户访问路径加 `/blog/`、访问地址汇总、构建产物目录
- 更新 wiki/Home.md 与 wiki/Docker环境安装.md：访问地址表中的前台门户
- 更新 项目开发文档合集.md 与 wiki/项目开发文档.md：移动端 BASE_URL 条件编译示例、Nginx 配置示例、移动端部署说明


## 技术栈
- Markdown 文档维护（无代码逻辑改动）

## 实现方案
### 策略
按文档内容逐项同步代码事实，保持文档与已落地的代码改动一致：
- 所有访问地址中前台门户由 `http://<IP>:8082` 更新为 `http://<IP>:8082/blog/`，并补充外部 Nginx 80 端口统一入口（`http://<IP>/blog/`、`http://<IP>/admin/`）
- blog-app 配置示例更新为条件编译双分支写法，并说明 APK 打包无需再手动修改 BASE_URL
- Nginx 配置示例更新为容器代理方式（`/blog` → 8082、`/admin` → 8081、`/api/` → 8080）
- 排查章节中构建产物目录由 `html/` 更新为 `html/blog`

### 文档对应关系（更新时注意差异）
| 根目录文档 | wiki 对应文档 | 差异 |
|------|------|------|
| 部署操作手册.md | wiki/部署操作手册.md | 章节编号不同（根目录 7.x/8.x，wiki 版 6.x/7.x），需分别定位 |
| 项目开发文档合集.md | wiki/项目开发文档.md | 内容几乎一致，替换文本相同 |
| Docker环境安装指南.md | wiki/Docker环境安装.md | 仅 wiki 版需更新访问表（根目录版无访问表） |
| README.md | wiki/Home.md | 独立文档，各自更新 |

### 实现注意事项
- 仅更新与本次代码改动直接相关的段落，不做无关修订
- 保持文档原有行文风格与格式（表格、代码块、标题层级）
- 移动端配置代码块需完整展示 `#ifdef H5` / `#ifdef APP-PLUS` 条件编译写法

