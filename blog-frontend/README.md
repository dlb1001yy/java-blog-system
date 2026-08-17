# blog-frontend

> Java码农笔记前台门户 — Vue 3 + Element Plus 前台门户，提供文章浏览、分类、标签、归档、留言、简历展示等功能。

## 技术栈

| 框架 | 版本 | 用途 |
|------|------|------|
| Vue | 3.3.8 | UI 框架 |
| Vite | 5.0 | 构建工具 |
| Vue Router | 4.2.5 | 路由 |
| Pinia | 2.1.7 | 状态管理 |
| Element Plus | 2.4.3 | UI 组件库 |
| @element-plus/icons-vue | 2.1.0 | 图标 |
| markdown-it | 13.0.2 | Markdown 渲染 |
| highlight.js | 11.8.0 | 代码高亮 |
| github-markdown-css | 5.2.0 | Markdown 样式 |
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

启动后访问：http://localhost:3000/

> 需先启动后端服务

## 配置说明

`vite.config.js`：

```js
export default defineConfig({
  server: {
    port: 3000,              // 开发端口
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
blog-frontend/
├── src/
│   ├── main.js                       # 入口
│   ├── App.vue                       # 根组件
│   ├── router/index.js               # 路由配置
│   ├── views/                        # 页面
│   │   ├── Home.vue                   # 首页
│   │   ├── ArticleList.vue            # 文章列表
│   │   ├── ArticleDetail.vue          # 文章详情
│   │   ├── Category.vue               # 分类
│   │   ├── Tags.vue                   # 标签
│   │   ├── Archives.vue               # 归档
│   │   ├── Resume.vue                 # 简历
│   │   ├── About.vue                  # 关于
│   │   └── MessageBoard.vue           # 留言板
│   ├── components/                   # 公共组件
│   │   ├── AppHeader.vue              # 顶部导航
│   │   ├── AppFooter.vue              # 底部信息
│   │   ├── AppSidebar.vue             # 侧边栏
│   │   ├── ArticleCard.vue            # 文章卡片
│   │   ├── BackToTop.vue              # 返回顶部
│   │   ├── CommentSection.vue         # 评论区
│   │   └── LogoIcon.vue               # Logo 图标
│   ├── api/                          # 接口请求
│   │   ├── request.js                 # axios 封装
│   │   ├── article.js                 # 文章接口
│   │   └── auth.js                    # 鉴权接口
│   ├── stores/                       # Pinia 状态
│   │   └── user.js                    # 用户状态
│   ├── utils/
│   │   └── markdown.js               # Markdown 解析
│   ├── assets/
│   │   ├── styles/
│   │   │   ├── variables.css          # CSS 变量
│   │   │   └── global.css             # 全局样式
│   │   ├── hero.png                   # 首页配图
│   │   ├── vite.svg
│   │   └── vue.svg
│   └── style.css                     # 全局基础样式
├── public/
│   ├── favicon.svg
│   └── icons.svg
├── index.html
└── vite.config.js
```

## 路由表

| 路径 | 名称 | 组件 | 说明 |
|------|------|------|------|
| / | Home | Home.vue | 首页 |
| /articles | ArticleList | ArticleList.vue | 文章列表 |
| /article/:id | ArticleDetail | ArticleDetail.vue | 文章详情 |
| /category | Category | Category.vue | 分类总览 |
| /category/:id | CategoryArticles | ArticleList.vue | 分类下文章 |
| /tags | Tags | Tags.vue | 标签云 |
| /archives | Archives | Archives.vue | 归档 |
| /resume | Resume | Resume.vue | 简历 |
| /about | About | About.vue | 关于 |
| /messages | Messages | MessageBoard.vue | 留言板 |

### 路由特性

- 使用 `createWebHistory()` history 模式
- `scrollBehavior`：前进/后退时恢复滚动位置，新导航回到顶部

## 页面说明

| 页面 | 功能 |
|------|------|
| Home | 首页，展示英雄区、最新文章、侧边栏（热门文章/分类/标签） |
| ArticleList | 文章列表，支持分页、按分类/标签筛选 |
| ArticleDetail | 文章详情，Markdown 渲染、代码高亮、评论区、相关文章 |
| Category | 分类总览，展示所有分类及文章数 |
| Tags | 标签云，按标签筛选文章 |
| Archives | 时间轴归档，按年月分组展示 |
| Resume | 简历展示，含基本信息、技能、工作经历 |
| About | 关于页面 |
| MessageBoard | 留言板，支持发表留言 |

## 组件说明

| 组件 | 用途 |
|------|------|
| AppHeader | 顶部导航栏，含 Logo、菜单、登录入口 |
| AppFooter | 底部信息栏，含版权、备案号 |
| AppSidebar | 侧边栏，展示热门文章、分类、标签 |
| ArticleCard | 文章卡片，用于列表展示 |
| BackToTop | 返回顶部按钮 |
| CommentSection | 评论区组件，支持发表评论与回复 |
| LogoIcon | 站点 Logo 图标 |

## 状态管理

| Store | 文件 | 状态 |
|-------|------|------|
| user | stores/user.js | token、userInfo（仅前台浏览用户可选登录） |

## 样式

| 文件 | 用途 |
|------|------|
| assets/styles/variables.css | CSS 自定义属性（颜色、间距等设计令牌） |
| assets/styles/global.css | 全局样式（重置、布局、工具类） |

## 构建打包

```bash
npm run build
```

产物位于 `dist/`，nginx 部署配置：

```nginx
location / {
    root /path/to/dist;
    try_files $uri $uri/ /index.html;
}

location /api {
    proxy_pass http://localhost:8080;
}
```
