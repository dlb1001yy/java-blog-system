# 生成项目根目录 README.md 计划

## Summary
在 `D:\my-project\java-blog-system\README.md` 创建一份完整的项目说明文档，覆盖四个子模块（blog-backend / blog-admin / blog-frontend / blog-app）的架构、技术栈、目录结构、启动方式、接口约定与部署说明。当前根目录无 README.md，四个子模块的 README 均为 Vite 模板默认内容，无参考价值。

## Current State Analysis
- 根目录 `D:\my-project\java-blog-system\` 无 README.md
- `blog-admin/README.md` 与 `blog-frontend/README.md` 均为 Vite 默认模板（"Vue 3 + Vite"），无实际内容
- `blog-backend/` 无 README.md
- `blog-app/` 无 README.md
- 项目无统一文档入口

## 技术栈（基于 pom.xml / package.json / manifest.json 实测）

### blog-backend（Spring Boot 单体后端）
- Spring Boot 3.1.5 / Java 17
- MyBatis Plus 3.5.5 / MySQL（数据库名 `dlbyy_zp_blog`）
- Spring Security + JWT（jjwt 0.11.5）
- Redis（数据库索引 5）
- Knife4j 4.3.0（OpenAPI 3 / Swagger）
- Hutool 5.8.22 / Lombok
- context-path: `/api`，端口 8080
- 包结构：`com.dlbyy.blog.{controller,service,mapper,entity,config,security,common,utils}`

### blog-admin（管理后台）
- Vue 3.3.8 + Vite 5 + Vue Router 4 + Pinia 2
- Element Plus 2.4.3 + @element-plus/icons-vue
- ECharts 5.4.3（仪表盘）
- markdown-it 13 + highlight.js 11 + github-markdown-css（文章编辑/预览）
- Sass 1.69.5
- 入口：`src/main.js`，路由：`src/router/index.js`，布局：`src/layout/`
- 视图：Dashboard / ArticleList / ArticleEdit / CategoryList / TagList / CommentList / MessageList / LinkList / ResumeEdit / Settings / Login

### blog-frontend（前台门户）
- Vue 3.3.8 + Vite 5 + Vue Router 4 + Pinia 2
- Element Plus 2.4.3 + @element-plus/icons-vue
- markdown-it 13 + highlight.js 11 + github-markdown-css（文章渲染）
- Sass 1.69.5
- 入口：`src/main.js`，路由：`src/router/index.js`
- 视图：Home / ArticleList / ArticleDetail / Category / Tags / Archives / MessageBoard / Resume / About
- 组件：AppHeader / AppFooter / AppSidebar / ArticleCard / BackToTop / CommentSection / LogoIcon

### blog-app（uni-app 移动端）
- uni-app + Vue 3（`vueVersion: "3"`）
- 多端目标：H5 / 微信小程序 / 支付宝小程序 / 百度小程序 / 头条小程序 / App
- 自定义 TabBar（components/TabBar.vue，SVG 图标，uni.reLaunch 切换）
- 设计令牌系统：`common/theme.js` + `uni.scss`
- 页面：index（首页）/ article/detail（文章详情）/ resume（简历）/ mine（我的）/ mine/login（登录）
- 组件：ArticleItem / TabBar / SearchBar / CategoryChips / Skeleton / Icon / Loading
- 请求封装：`common/request.js`（含 token、防重复、loading）+ `common/api.js`
- BASE_URL: `http://localhost:8080/api`

## 后端接口分组（基于 controller 包结构）
- `/auth/**` — 登录/登出（公开）
- `/portal/**` — 前台公开接口（文章/分类/标签/评论/简历/留言/统计）
- `/admin/**` — 管理后台接口（需鉴权）
- `/user/**` — 当前用户信息（需鉴权）
- `/uploads/**` — 静态文件访问（公开）

## 数据库（基于 blog-backend/sql/create_sql.sql）
- 数据库名：`dlbyy_zp_blog`（utf8mb4）
- 主要表：sys_user / blog_category / blog_tag / blog_article / blog_article_tag / blog_comment / blog_message / blog_link / blog_resume_info / blog_config

## Proposed Changes

### 文件：`D:\my-project\java-blog-system\README.md`（新建）

README.md 结构（中文，GitHub-flavored markdown）：

1. **项目标题与简介**
   - 项目名：Java码农笔记
   - 一句话描述：基于 Spring Boot + Vue 3 的全栈博客系统，含管理后台、前台门户与移动端 App

2. **项目架构图（ASCII）**
   - 展示四个模块与后端 API 的关系

3. **技术栈表格**
   - 后端 / 管理后台 / 前台门户 / 移动端 四列，列出框架、版本、用途

4. **模块说明**
   - 对每个子模块单独一节，含：定位、目录结构、关键文件

5. **环境要求**
   - JDK 17 / Node 18+ / MySQL 8 / Redis 7 / Maven 3.8+

6. **快速开始**
   - 后端启动：导入 SQL → 改 application.yaml → `mvnw spring-boot:run`
   - 管理后台：`npm install` → `npm run dev`
   - 前台门户：`npm install` → `npm run dev`
   - 移动端：用 HBuilderX 打开 blog-app → 运行到 H5/小程序

7. **配置说明**
   - 后端 application.yaml 关键项（端口 8080、context-path /api、数据库、Redis）
   - 前端 vite.config.js 代理
   - 移动端 common/config.js 的 BASE_URL

8. **接口约定**
   - 路径分组（/auth /portal /admin /user /uploads）
   - 鉴权方式（Bearer Token）
   - 统一返回格式（Result<T>，code/message/data）

9. **默认账号**
   - admin / admin123

10. **目录结构总览**
    - 根目录树形结构（一级 + 二级）

11. **部署说明**
    - 后端打包：`mvnw clean package` → `java -jar`
    - 前端打包：`npm run build` → nginx 部署
    - 移动端：HBuilderX 云打包

12. **License**
    - MIT

## Assumptions & Decisions
- 假设用户希望 README 为中文（与项目内现有中文注释一致）
- 假设用户希望 README 详细而非简略（任务要求"详细创建"）
- 决定不在 README 中包含截图占位符（避免无效链接）
- 决定保留各子模块自己的 README 不动（用户只要求在根目录创建）
- 接口列表只列分组与代表性接口，不逐一列举全部端点（避免文档冗长且易过期）

## Verification Steps
1. 检查 `D:\my-project\java-blog-system\README.md` 是否创建成功
2. 用 Read 工具读取文件，确认内容完整覆盖四个模块
3. 确认所有文件路径引用正确（与 LS 结果对照）
4. 确认版本号与 pom.xml / package.json / manifest.json 一致
5. 确认 markdown 语法正确（标题层级、表格、代码块）
