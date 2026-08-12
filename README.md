# Java码农笔记

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

| 层级 | 模块 | 框架 | 版本 | 用途 |
|------|------|------|------|------|
| 后端 | blog-backend | Spring Boot | 3.1.5 | Web 框架 |
| 后端 | blog-backend | MyBatis Plus | 3.5.5 | ORM |
| 后端 | blog-backend | Spring Security | 6.x | 鉴权 |
| 后端 | blog-backend | jjwt | 0.11.5 | JWT |
| 后端 | blog-backend | Redis | — | 缓存/会话 |
| 后端 | blog-backend | Knife4j | 4.3.0 | 接口文档 |
| 后端 | blog-backend | Hutool | 5.8.22 | 工具库 |
| 后端 | blog-backend | Java | 17 | 运行时 |
| 管理后台 | blog-admin | Vue | 3.3.8 | UI 框架 |
| 管理后台 | blog-admin | Vite | 5.0 | 构建 |
| 管理后台 | blog-admin | Element Plus | 2.4.3 | UI 组件库 |
| 管理后台 | blog-admin | Pinia | 2.1.7 | 状态管理 |
| 管理后台 | blog-admin | Vue Router | 4.2.5 | 路由 |
| 管理后台 | blog-admin | ECharts | 5.4.3 | 仪表盘图表 |
| 管理后台 | blog-admin | markdown-it | 13.0.2 | 文章预览 |
| 前台门户 | blog-frontend | Vue | 3.3.8 | UI 框架 |
| 前台门户 | blog-frontend | Vite | 5.0 | 构建 |
| 前台门户 | blog-frontend | Element Plus | 2.4.3 | UI 组件库 |
| 前台门户 | blog-frontend | Pinia | 2.1.7 | 状态管理 |
| 前台门户 | blog-frontend | markdown-it | 13.0.2 | 文章渲染 |
| 移动端 | blog-app | uni-app | — | 跨端框架 |
| 移动端 | blog-app | Vue | 3 | UI 框架 |

## 模块说明

### blog-backend（后端服务）

Spring Boot 单体后端，统一为前台门户、管理后台与移动端提供 REST API。

**包结构**：`com.dlbyy.blog`

```
blog-backend/
├── src/main/java/com/dlbyy/blog/
│   ├── JavaBlogApplication.java      # 启动类
│   ├── common/                       # 通用类
│   │   ├── Result.java                # 统一响应封装
│   │   ├── PageResult.java            # 分页响应封装
│   │   ├── Constants.java             # 常量
│   │   └── exception/                 # 全局异常处理
│   ├── config/                       # 配置类
│   │   ├── SecurityConfig.java        # Spring Security
│   │   ├── MybatisPlusConfig.java     # MyBatis Plus
│   │   ├── RedisConfig.java           # Redis
│   │   ├── WebMvcConfig.java          # 静态资源映射
│   │   └── SwaggerConfig.java         # 接口文档
│   ├── controller/
│   │   ├── admin/                     # 管理后台接口（需鉴权）
│   │   └── portal/                    # 前台公开接口
│   ├── entity/                       # 实体类
│   ├── mapper/                       # MyBatis Mapper
│   ├── service/                     # 业务接口
│   │   └── impl/                      # 业务实现
│   ├── security/                     # JWT 安全
│   │   ├── JwtTokenProvider.java      # Token 生成/校验
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtAuthenticationEntryPoint.java
│   │   └── CustomUserDetailsService.java
│   └── utils/                        # 工具类
├── src/main/resources/
│   ├── application.yaml              # 配置文件
│   └── mapper/                       # MyBatis XML
├── sql/01-create_sql.sql                # 数据库初始化脚本
├── uploads/                          # 上传文件目录
└── pom.xml
```

### blog-admin（管理后台）

Vue 3 + Element Plus 后台管理系统，用于管理文章、分类、标签、评论、留言、友情链接、简历与站点配置。

```
blog-admin/
├── src/
│   ├── main.js                       # 入口
│   ├── App.vue
│   ├── router/index.js               # 路由配置
│   ├── layout/                       # 布局组件
│   │   ├── Header.vue
│   │   ├── Sidebar.vue
│   │   ├── TagsView.vue
│   │   └── index.vue
│   ├── views/                        # 页面
│   │   ├── Dashboard.vue              # 仪表盘
│   │   ├── ArticleList.vue            # 文章列表
│   │   ├── ArticleEdit.vue            # 文章编辑
│   │   ├── CategoryList.vue           # 分类管理
│   │   ├── TagList.vue                # 标签管理
│   │   ├── CommentList.vue            # 评论管理
│   │   ├── MessageList.vue            # 留言管理
│   │   ├── LinkList.vue               # 友链管理
│   │   ├── ResumeEdit.vue             # 简历编辑
│   │   ├── Settings.vue               # 站点设置
│   │   └── Login.vue                  # 登录
│   ├── components/                   # 公共组件
│   │   ├── Editor.vue                 # Markdown 编辑器
│   │   ├── Upload.vue                 # 文件上传
│   │   └── SvgIcon.vue                # SVG 图标
│   ├── api/                          # 接口请求
│   ├── stores/                       # Pinia 状态
│   └── assets/styles/global.css      # 全局样式
├── public/
│   ├── favicon.svg
│   └── icons.svg
└── vite.config.js
```

### blog-frontend（前台门户）

Vue 3 + Element Plus 前台门户，提供文章浏览、分类、标签、归档、留言、简历展示等功能。

```
blog-frontend/
├── src/
│   ├── main.js                       # 入口
│   ├── App.vue
│   ├── router/index.js               # 路由配置
│   ├── views/                        # 页面
│   │   ├── Home.vue                   # 首页
│   │   ├── ArticleList.vue            # 文章列表
│   │   ├── ArticleDetail.vue          # 文章详情
│   │   ├── Category.vue               # 分类
│   │   ├── Tags.vue                   # 标签
│   │   ├── Archives.vue               # 归档
│   │   ├── MessageBoard.vue           # 留言板
│   │   ├── Resume.vue                 # 简历
│   │   └── About.vue                  # 关于
│   ├── components/                   # 公共组件
│   │   ├── AppHeader.vue
│   │   ├── AppFooter.vue
│   │   ├── AppSidebar.vue
│   │   ├── ArticleCard.vue
│   │   ├── BackToTop.vue
│   │   ├── CommentSection.vue
│   │   └── LogoIcon.vue
│   ├── api/                          # 接口请求
│   ├── stores/                       # Pinia 状态
│   ├── utils/markdown.js             # Markdown 解析
│   └── assets/styles/
│       ├── variables.css              # CSS 变量
│       └── global.css                 # 全局样式
├── public/
│   ├── favicon.svg
│   └── icons.svg
└── vite.config.js
```

### blog-app（移动端）

uni-app + Vue 3 多端应用，目标平台包括 H5、微信小程序、支付宝小程序、百度小程序、头条小程序与 App。

```
blog-app/
├── App.vue                           # 应用入口
├── main.js                           # Vue 入口
├── pages.json                        # 页面路由配置
├── manifest.json                     # 应用配置
├── uni.scss                          # 全局 SCSS 变量
├── common/
│   ├── config.js                     # 配置（BASE_URL、TOKEN_KEY）
│   ├── request.js                    # 请求封装（token、防重复、loading）
│   ├── api.js                        # 接口定义
│   └── theme.js                      # 设计令牌（颜色/间距/圆角/阴影）
├── components/
│   ├── TabBar.vue                    # 自定义底部导航（SVG 图标）
│   ├── ArticleItem.vue               # 文章卡片
│   ├── SearchBar.vue                 # 搜索栏
│   ├── CategoryChips.vue            # 横向分类筛选
│   ├── Skeleton.vue                 # 骨架屏
│   ├── Icon.vue                      # SVG 图标库（14 个内置图标）
│   └── Loading.vue                   # 加载动画
├── pages/
│   ├── index/index.vue               # 首页（hero+搜索+chips+列表）
│   ├── article/detail.vue            # 文章详情
│   ├── resume/index.vue              # 简历
│   ├── mine/index.vue                # 我的
│   └── mine/login.vue                # 登录
├── utils/markdown.js                 # Markdown 解析
└── static/logo.png                   # 应用图标
```

## 环境要求

| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 后端运行时 |
| Maven | 3.8+ | 后端构建 |
| Node.js | 18+ | 前端构建 |
| MySQL | 8.0+ | 数据库 |
| Redis | 6.0+ | 缓存 |
| HBuilderX | 最新 | uni-app 移动端运行/打包 |

## 快速开始

### 1. 初始化数据库

```bash
# 登录 MySQL，执行初始化脚本
mysql -uroot -p < blog-backend/sql/01-create_sql.sql
```

数据库名：`dlbyy_zp_blog`（utf8mb4）

### 2. 启动后端

修改 `blog-backend/src/main/resources/application.yaml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dlbyy_zp_blog?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的密码
  data:
    redis:
      host: localhost
      port: 6379
      password: 你的Redis密码
      database: 5
```

启动：

```bash
cd blog-backend
./mvnw spring-boot:run
```

后端启动后访问：
- API：http://localhost:8080/api
- 接口文档：http://localhost:8080/api/doc.html

### 3. 启动管理后台

```bash
cd blog-admin
npm install
npm run dev
```

默认访问：http://localhost:5173

### 4. 启动前台门户

```bash
cd blog-frontend
npm install
npm run dev
```

默认访问：http://localhost:5174

### 5. 启动移动端

使用 HBuilderX 打开 `blog-app` 目录：
- 运行 → 运行到浏览器 → H5
- 运行 → 运行到小程序模拟器 → 微信小程序
- 运行 → 运行到手机或模拟器 → App

## 配置说明

### 后端配置（application.yaml）

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| server.port | 8080 | 服务端口 |
| server.servlet.context-path | /api | 接口前缀 |
| spring.datasource | — | MySQL 数据源 |
| spring.data.redis | — | Redis 连接 |
| jwt.secret | — | JWT 密钥（≥64 字符） |
| jwt.expiration | 86400000 | Token 有效期 24h |
| file.upload-path | — | 文件上传目录 |

### 前端配置（vite.config.js）

`blog-admin` 与 `blog-frontend` 的 `vite.config.js` 中配置了 `/api` 代理到后端 `http://localhost:8080`。

### 移动端配置（blog-app/common/config.js）

使用 uni-app 条件编译自动区分环境，H5 调试与 APK 打包无需手动改地址：

```js
// #ifdef H5
export const BASE_URL = 'http://localhost:8080/api'
// #endif

// #ifdef APP-PLUS
export const BASE_URL = 'http://gz.aeert.com:19612/api'
// #endif

export const TOKEN_KEY = 'uni_app_token'
```

## 接口约定

### 路径分组

| 前缀 | 鉴权 | 说明 |
|------|------|------|
| `/auth/**` | 公开 | 登录、登出 |
| `/portal/**` | 公开 | 前台接口（文章/分类/标签/评论/简历/留言/统计） |
| `/admin/**` | 需 Token | 管理后台接口 |
| `/user/**` | 需 Token | 当前用户信息 |
| `/uploads/**` | 公开 | 上传文件静态访问 |

### 鉴权方式

除 `/auth` 与 `/portal` 外，其余接口需在请求头携带 JWT：

```
Authorization: Bearer <token>
```

登录接口 `POST /auth/login` 返回 token 后，前端将其存入本地存储。

### 统一响应格式

所有接口返回 `Result<T>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

- `code`：200 成功，其他为失败
- `message`：提示信息
- `data`：业务数据

分页接口返回 MyBatis Plus 的 `Page<T>`：

```json
{
  "records": [ ... ],
  "total": 100,
  "size": 10,
  "current": 1,
  "pages": 10
}
```

### 前台接口（/portal）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /portal/articles/page | 分页查询文章 |
| GET | /portal/articles/search | 关键字搜索文章 |
| GET | /portal/articles/{id} | 文章详情 |
| GET | /portal/articles/{id}/related | 相关文章推荐 |
| GET | /portal/articles/hot | 热门文章 |
| GET | /portal/articles/latest | 最新文章 |
| GET | /portal/articles/archives | 文章归档 |
| POST | /portal/articles/{id}/like | 点赞文章 |
| GET | /portal/categories | 分类列表 |
| GET | /portal/tags | 标签列表 |
| GET | /portal/comments/{articleId} | 文章评论 |
| POST | /portal/comments | 发表评论 |
| GET | /portal/resume | 获取简历 |
| GET | /portal/messages | 公开留言列表 |
| POST | /portal/messages | 提交留言 |
| GET | /portal/stats | 站点统计 |

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |

## 数据库表

| 表名 | 说明 |
|------|------|
| sys_user | 用户表 |
| blog_category | 文章分类 |
| blog_tag | 文章标签 |
| blog_article | 文章 |
| blog_article_tag | 文章-标签关联 |
| blog_comment | 评论 |
| blog_message | 留言 |
| blog_link | 友情链接 |
| blog_resume_info | 简历信息 |
| blog_config | 站点配置 |

## 目录结构

```
java-blog-system/
├── blog-backend/         # Spring Boot 后端
├── blog-admin/           # Vue 3 管理后台
├── blog-frontend/        # Vue 3 前台门户
├── blog-app/             # uni-app 移动端
├── .trae/                # Trae IDE 配置
│   ├── documents/        # 计划文档
│   └── specs/            # 规格文档
└── README.md             # 本文件
```

## 部署说明

### 后端打包

```bash
cd blog-backend
./mvnw clean package -DskipTests
java -jar target/blog-backend-1.0.0.jar
```

### 前端打包

```bash
cd blog-admin    # 或 blog-frontend
npm run build
```

- `blog-admin` 的 Vite `base` 为 `/admin/`，产物需部署到 nginx 的 `html/admin/` 子目录
- `blog-frontend` 的 Vite `base` 为 `/blog/`，产物需部署到 nginx 的 `html/blog/` 子目录

两个项目均通过 nginx 代理 `/api` 到后端，容器内部代理到 `blog-backend:8080`，外部统一入口代理到后端 8080 端口：

```nginx
location /api/ {
    proxy_pass http://localhost:8080/api/;
}
```

### 移动端打包

使用 HBuilderX：
- 发行 → 网站-PC Web 或手机 H5
- 发行 → 小程序-微信
- 发行 → 原生 App-云打包

无需手动修改配置：`blog-app/common/config.js` 已通过 uni-app 条件编译自动区分环境——H5 调试走 `http://localhost:8080/api`，APK 云打包自动使用 `http://gz.aeert.com:19612/api`。

## Docker 部署（Ubuntu 22）

通过 Docker Compose 一键部署 blog-backend、blog-admin、blog-frontend 三个项目，含 MySQL 与 Redis，适用于 Ubuntu 22.04 LTS 服务器。

> **部署操作文档**：从镜像拉取到访问测试的完整操作步骤，请参考 [部署操作手册.md](部署操作手册.md)

### 1. 环境准备

> 详细的 Docker 安装步骤（含前置检查、多种安装方式、镜像加速、日志轮转、常见问题排查等）请参考 [Docker环境安装指南.md](Docker环境安装指南.md)

在 Ubuntu 22.04 服务器上安装 Docker Engine、Docker Compose 插件与 Git：

```bash
# 更新包索引
sudo apt update

# 安装 Docker Engine
sudo apt install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 将当前用户加入 docker 组（免 sudo，需重新登录生效）
sudo usermod -aG docker $USER

# 安装 Git
sudo apt install -y git

# 验证安装
docker --version
docker compose version
git --version
```

### 2. 从 Gitee 克隆代码

```bash
# 替换为你的 Gitee 仓库地址
git clone https://gitee.com/<你的用户名>/<仓库名>.git java-blog-system
cd java-blog-system
```

> 如果仓库为私有，克隆时会提示输入 Gitee 用户名与密码（或 Access Token）。

### 3. 目录结构

克隆后项目根目录应包含以下 Docker 相关文件：

```
java-blog-system/
├── docker-compose.yml          # Docker Compose 编排文件
├── .dockerignore               # 根级忽略规则
├── blog-backend/
│   ├── Dockerfile              # 后端多阶段构建
│   ├── .dockerignore
│   └── src/main/resources/
│       └── application-docker.yaml  # Docker 环境配置
├── blog-admin/
│   ├── Dockerfile              # 管理后台多阶段构建
│   ├── .dockerignore
│   └── nginx.conf              # nginx 配置
└── blog-frontend/
    ├── Dockerfile              # 前台门户多阶段构建
    ├── .dockerignore
    └── nginx.conf              # nginx 配置
```

### 4. 配置说明

#### 端口映射

| 服务 | 容器端口 | 宿主端口 | 说明 |
|------|----------|----------|------|
| blog-frontend | 80 | 8082 | 前台门户 |
| blog-admin | 80 | 8081 | 管理后台（路径 /admin/） |
| blog-backend | 8080 | 8080 | 后端 API |
| mysql | 3306 | 3306 | 数据库（可选关闭） |
| redis | 6379 | 6379 | 缓存（可选关闭） |

> 如需修改端口，编辑 `docker-compose.yml` 中对应服务的 `ports` 配置。

#### 数据卷

| 卷名 | 容器路径 | 说明 |
|------|----------|------|
| blog_mysql_data | /var/lib/mysql | MySQL 数据持久化 |
| blog_redis_data | /data | Redis 数据持久化 |
| blog_uploads_data | /app/uploads | 上传文件持久化 |

#### 默认凭据

| 项目 | 用户名 | 密码 |
|------|--------|------|
| MySQL | root | 123456 |
| Redis | — | 123456 |
| 管理后台登录 | admin | admin123 |

> 生产环境请务必修改 `docker-compose.yml` 中的 MySQL/Redis 密码，以及 `blog-backend/src/main/resources/application-docker.yaml` 中对应的连接密码。

### 5. 一键启动

在项目根目录执行：

```bash
# 构建镜像并后台启动全部服务
docker compose up -d --build
```

首次构建需要下载基础镜像并编译后端与前端，耗时较长（约 5-15 分钟，取决于网络与服务器性能）。后续启动会利用缓存，速度较快。

查看启动进度：

```bash
# 查看全部服务状态
docker compose ps

# 实时查看日志（等待后端启动完成）
docker compose logs -f blog-backend
```

当 blog-backend 日志出现 `Started JavaBlogApplication` 字样时，说明后端已就绪。

### 6. 访问地址

启动完成后，通过浏览器访问（将 `<服务器IP>` 替换为实际 IP，本机部署使用 `localhost`）：

| 服务 | 地址 |
|------|------|
| 前台门户 | http://\<服务器IP\>:8082/blog/ |
| 管理后台 | http://\<服务器IP\>:8081/admin/ |
| 后端 API | http://\<服务器IP\>:8080/api |
| 接口文档 | http://\<服务器IP\>:8080/api/doc.html |

如配置了外部 Nginx（80 端口）统一入口，也可通过以下地址访问：

| 服务 | 地址 |
|------|------|
| 前台门户 | http://\<服务器IP\>/blog/ |
| 管理后台 | http://\<服务器IP\>/admin/ |

管理后台默认账号：`admin` / `admin123`

### 7. 常用运维命令

```bash
# 查看所有服务状态
docker compose ps

# 查看某服务日志（实时跟随）
docker compose logs -f blog-backend
docker compose logs -f blog-frontend
docker compose logs -f blog-admin

# 重启单个服务
docker compose restart blog-backend

# 停止全部服务（保留数据）
docker compose down

# 停止并删除数据卷（⚠️ 清空所有数据）
docker compose down -v

# 重新构建并启动单个服务（修改代码后）
docker compose up -d --build blog-backend

# 重新构建并启动全部服务
docker compose up -d --build

# 进入容器排查问题
docker exec -it blog-backend bash
docker exec -it blog-mysql mysql -uroot -p123456
docker exec -it blog-redis redis-cli -a 123456
```

### 8. 数据持久化与备份

#### 数据卷位置

Docker 数据卷默认存储在 `/var/lib/docker/volumes/` 下：

```bash
# 查看数据卷列表
docker volume ls | grep blog

# 查看数据卷详情
docker volume inspect blog_mysql_data
```

#### MySQL 备份与恢复

```bash
# 备份数据库（导出到当前目录）
docker exec blog-mysql mysqldump -uroot -p123456 --single-transaction dlbyy_zp_blog > backup_$(date +%Y%m%d_%H%M%S).sql

# 恢复数据库（从备份文件导入）
docker exec -i blog-mysql mysql -uroot -p123456 dlbyy_zp_blog < backup_20260101_120000.sql
```

#### 上传文件备份

```bash
# 备份上传文件目录
docker run --rm -v blog_uploads_data:/data -v $(pwd):/backup alpine tar czf /backup/uploads_backup_$(date +%Y%m%d).tar.gz -C /data .

# 恢复上传文件目录
docker run --rm -v blog_uploads_data:/data -v $(pwd):/backup alpine tar xzf /backup/uploads_backup_20260101.tar.gz -C /data
```

### 9. 常见问题排查

#### 端口被占用

```bash
# 查看占用端口的进程
sudo lsof -i :8080
sudo lsof -i :8081
sudo lsof -i :8082

# 停止占用进程或修改 docker-compose.yml 中的端口映射
```

#### 后端无法连接数据库

1. 确认 MySQL 容器已健康启动：`docker compose ps mysql`
2. 查看后端日志：`docker compose logs blog-backend`
3. 确认 `application-docker.yaml` 中数据库 host 为 `mysql`（容器名）
4. 如修改了 MySQL 密码，需同步修改 `application-docker.yaml` 与 `docker-compose.yml`

#### 镜像构建缓慢（Maven/npm 下载慢）

- 后端 Dockerfile 已配置 Maven 依赖预下载缓存层，重复构建会加速
- 前端 Dockerfile 已配置使用 npmmirror 镜像源加速 npm 安装
- 可配置 Docker 镜像加速器加速基础镜像拉取：

```bash
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<'EOF'
{
  "registry-mirrors": [
    "https://docker.xuanyuan.me",
    "https://docker.1ms.run",
    "https://docker.m.daocloud.io",
    "https://docker.1panel.live",
    "https://dockerproxy.link"
  ]
}
EOF
sudo systemctl daemon-reload
sudo systemctl restart docker
```

也可一键配置：`sudo bash scripts/configure-docker-mirror.sh`

#### 离线镜像部署（服务器下载慢或无外网时）

如果服务器拉取镜像仍然很慢，可在**另一台有外网的 Linux/macOS 机器**上提前导出镜像，再拷贝到服务器加载：

1. 外网机器上（需已安装 Docker），在项目目录执行导出：

   ```bash
   bash scripts/export-images.sh
   ```

   会在项目根目录生成 `images/` 目录（内含 mysql、redis、node、nginx、temurin 等基镜像 tar 包）。
2. 将 `images/` 目录拷贝到服务器项目根目录（scp / U盘）：

   ```bash
   scp -r images user@服务器IP:/项目路径/
   ```
3. 服务器上加载镜像：

   ```bash
   sudo bash scripts/load-images.sh
   ```
4. 正常启动：

   ```bash
   docker compose up -d --build
   ```

> 说明：此方式只解决镜像层下载；构建时的 Maven/npm 依赖下载仍走阿里云/npmmirror 国内镜像（体积小）。

#### 前端页面空白或 404

1. 确认后端健康检查通过：`docker compose ps blog-backend`（状态应为 healthy）
2. 确认 nginx 配置正确：`docker exec blog-admin cat /etc/nginx/conf.d/default.conf`
3. 管理后台必须访问 `http://<IP>:8081/admin/`（含尾部 `/admin/` 路径）
4. 前台门户必须访问 `http://<IP>:8082/blog/`（含 `/blog/` 子路径，根路径会自动重定向）

#### 数据库未自动初始化

MySQL 仅在数据卷为空（首次启动）时执行 `/docker-entrypoint-initdb.d/` 中的脚本。如需重新初始化：

```bash
# 停止并删除数据卷（⚠️ 清空所有数据）
docker compose down -v
# 重新启动
docker compose up -d --build
```

#### 上传文件无法访问

确认上传目录数据卷已正确挂载，且后端 `application-docker.yaml` 中 `file.upload-path` 为 `/app/uploads/`：

```bash
# 检查挂载
docker exec blog-backend ls -la /app/uploads/
# 检查上传接口
curl -I http://localhost:8080/api/uploads/
```

### 10. 更新部署

当 Gitee 仓库有新代码时，拉取并重新构建：

```bash
# 拉取最新代码
git pull origin main

# 重新构建并启动（仅重建有变更的服务）
docker compose up -d --build
```

## License

MIT
