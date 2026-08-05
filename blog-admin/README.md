# blog-admin

> Java码农笔记管理后台 — Vue 3 + Element Plus 后台管理系统。

## 技术栈

| 框架 | 版本 | 用途 |
|------|------|------|
| Vue | 3.3.8 | UI 框架 |
| Vite | 5.0 | 构建工具 |
| Vue Router | 4.2.5 | 路由 |
| Pinia | 2.1.7 | 状态管理 |
| Element Plus | 2.4.3 | UI 组件库 |
| @element-plus/icons-vue | 2.1.0 | 图标 |
| ECharts | 5.4.3 | 仪表盘图表 |
| markdown-it | 13.0.2 | 文章预览 |
| highlight.js | 11.8.0 | 代码高亮 |
| github-markdown-css | 5.2.0 | Markdown 样式 |
| Sass | 1.69.5 | CSS 预处理器 |
| axios | 1.6.2 | HTTP 请求 |

## 环境要求

| 软件 | 版本 |
|------|------|
| Node.js | 18+ |
| npm | 9+ |

## 快速开始

```bash
npm install
npm run dev
```

启动后访问：http://localhost:3001/admin/

> 默认账号：admin / admin123（需先启动后端服务）

## 配置说明

`vite.config.js`：

```js
export default defineConfig({
  base: '/admin/',           // 部署子路径
  server: {
    port: 3001,              // 开发端口
    proxy: {
      '/api': {
        target: 'http://localhost:8080',  // 后端地址
        changeOrigin: true
      }
    }
  }
})
```

## 目录结构

```
blog-admin/
├── src/
│   ├── main.js                       # 入口
│   ├── App.vue                       # 根组件
│   ├── router/index.js               # 路由配置
│   ├── layout/                       # 布局组件
│   │   ├── index.vue                  # 主布局
│   │   ├── Header.vue                 # 顶栏
│   │   ├── Sidebar.vue                # 侧边栏
│   │   └── TagsView.vue               # 标签页
│   ├── views/                        # 页面
│   │   ├── Dashboard.vue              # 数据看板
│   │   ├── ArticleList.vue            # 文章列表
│   │   ├── ArticleEdit.vue            # 文章编辑/新建
│   │   ├── CategoryList.vue           # 分类管理
│   │   ├── TagList.vue                # 标签管理
│   │   ├── CommentList.vue            # 评论管理
│   │   ├── MessageList.vue            # 留言管理
│   │   ├── LinkList.vue               # 友情链接
│   │   ├── ResumeEdit.vue             # 简历编辑
│   │   ├── Settings.vue               # 系统设置
│   │   ├── Login.vue                  # 登录
│   │   └── Redirect.vue               # 路由重定向
│   ├── components/                   # 公共组件
│   │   ├── Editor.vue                 # Markdown 编辑器
│   │   ├── Upload.vue                 # 文件上传
│   │   └── SvgIcon.vue                # SVG 图标
│   ├── api/                          # 接口请求
│   │   ├── request.js                 # axios 封装
│   │   ├── auth.js                    # 登录/登出
│   │   ├── article.js                 # 文章接口
│   │   ├── category.js                # 分类接口
│   │   ├── tag.js                     # 标签接口
│   │   ├── comment.js                 # 评论接口
│   │   ├── message.js                 # 留言接口
│   │   ├── link.js                    # 友链接口
│   │   ├── resume.js                  # 简历接口
│   │   └── dashboard.js               # 仪表盘接口
│   ├── stores/                       # Pinia 状态
│   │   ├── user.js                    # 用户状态（token、userInfo）
│   │   └── app.js                     # 应用状态（侧边栏折叠）
│   ├── assets/
│   │   └── styles/global.css         # 全局样式
│   └── style.css                     # 全局基础样式
├── public/
│   ├── favicon.svg
│   └── icons.svg
├── index.html
└── vite.config.js
```

## 路由表

| 路径 | 名称 | 组件 | 标题 | 图标 |
|------|------|------|------|------|
| /login | Login | Login.vue | 登录 | — |
| /redirect/:path(.*) | Redirect | Redirect.vue | Redirect | — |
| /dashboard | Dashboard | Dashboard.vue | 数据看板 | DataAnalysis |
| /article | ArticleList | ArticleList.vue | 文章管理 | Document |
| /article/create | ArticleCreate | ArticleEdit.vue | 写文章 | — |
| /article/edit/:id | ArticleEdit | ArticleEdit.vue | 编辑文章 | — |
| /category | CategoryList | CategoryList.vue | 分类管理 | Folder |
| /tag | TagList | TagList.vue | 标签管理 | PriceTag |
| /comment | CommentList | CommentList.vue | 评论管理 | ChatDotRound |
| /message | MessageList | MessageList.vue | 留言管理 | Message |
| /resume | ResumeEdit | ResumeEdit.vue | 简历管理 | User |
| /link | LinkList | LinkList.vue | 友情链接 | Link |
| /settings | Settings | Settings.vue | 系统设置 | Setting |

### 路由守卫

`router.beforeEach` 检查 token：
- 未登录访问受保护页面 → 跳转 `/login`
- 已登录访问 `/login` → 跳转 `/dashboard`

## 页面说明

| 页面 | 功能 |
|------|------|
| Dashboard | 站点统计仪表盘，含文章数/评论数/留言数/访问量等卡片与 ECharts 图表 |
| ArticleList | 文章列表，支持分页、搜索、按分类/标签筛选、删除 |
| ArticleEdit | 文章编辑器，支持 Markdown 编辑与实时预览、封面图上传、分类标签选择 |
| CategoryList | 分类管理，支持增删改查 |
| TagList | 标签管理，支持增删改查 |
| CommentList | 评论管理，支持查看、删除、审核 |
| MessageList | 留言管理，支持查看、删除、回复 |
| LinkList | 友情链接管理，支持增删改查 |
| ResumeEdit | 简历信息编辑，含基本信息、技能、工作经历等 |
| Settings | 站点配置，如站点名称、备案号、关于信息等 |
| Login | 登录页 |

## API 模块

`src/api/` 下按业务域拆分：

| 文件 | 模块 | 主要接口 |
|------|------|----------|
| request.js | 基础 | axios 实例封装（拦截器、token、错误处理） |
| auth.js | 鉴权 | login / logout / getInfo |
| article.js | 文章 | 列表/详情/新增/编辑/删除 |
| category.js | 分类 | 列表/新增/编辑/删除 |
| tag.js | 标签 | 列表/新增/编辑/删除 |
| comment.js | 评论 | 列表/删除/审核 |
| message.js | 留言 | 列表/删除/回复 |
| link.js | 友链 | 列表/新增/编辑/删除 |
| resume.js | 简历 | 获取/更新 |
| dashboard.js | 仪表盘 | 统计数据 |

## 状态管理

| Store | 文件 | 状态 |
|-------|------|------|
| user | stores/user.js | token、userInfo |
| app | stores/app.js | sidebarCollapsed（侧边栏折叠状态） |

## 构建打包

```bash
npm run build
```

产物位于 `dist/`，部署到 nginx 时需配置 base 路径 `/admin/`：

```nginx
location /admin/ {
    alias /path/to/dist/;
    try_files $uri $uri/ /admin/index.html;
}

location /api {
    proxy_pass http://localhost:8080;
}
```
