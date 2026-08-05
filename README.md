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
├── sql/create_sql.sql                # 数据库初始化脚本
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
mysql -uroot -p < blog-backend/sql/create_sql.sql
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

```js
export const BASE_URL = 'http://localhost:8080/api' // 生产环境请更换为实际域名
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

产物位于 `dist/`，使用 nginx 部署时配置 `/api` 反向代理到后端：

```nginx
location /api {
    proxy_pass http://localhost:8080;
}
```

### 移动端打包

使用 HBuilderX：
- 发行 → 网站-PC Web 或手机 H5
- 发行 → 小程序-微信
- 发行 → 原生 App-云打包

打包前需修改 `blog-app/common/config.js` 中的 `BASE_URL` 为生产域名。

## License

MIT
