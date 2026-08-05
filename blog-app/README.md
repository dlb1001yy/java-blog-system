# blog-app

> Java码农笔记移动端 — uni-app + Vue 3 多端应用，支持 H5、微信小程序、支付宝小程序、百度小程序、头条小程序与 App。

## 技术栈

| 框架 | 用途 |
|------|------|
| uni-app | 跨端框架 |
| Vue 3 | UI 框架（vueVersion: "3"） |
| Sass | CSS 预处理器 |

## 环境要求

| 软件 | 版本 | 说明 |
|------|------|------|
| HBuilderX | 最新版 | uni-app 官方 IDE，用于运行与打包 |
| Node.js | 18+ | H5 端运行依赖 |
| 微信开发者工具 | 最新 | 微信小程序调试（如需） |

## 快速开始

1. 打开 HBuilderX
2. 文件 → 导入 → 从本地项目导入 → 选择 `blog-app` 目录
3. 修改 `common/config.js` 中的 `BASE_URL` 为后端地址
4. 运行 → 运行到浏览器 → Chrome（H5 调试）
5. 或运行 → 运行到小程序模拟器 → 微信开发者工具

> 需先启动后端服务

## 配置说明

`common/config.js`：

```js
export const BASE_URL = 'http://localhost:8080/api'  // 后端 API 地址
export const TOKEN_KEY = 'uni_app_token'              // 本地存储 token 的 key
```

> 生产环境请将 `BASE_URL` 更换为实际域名，并配置 HTTPS。

`manifest.json`：应用名称、图标、各平台配置（appid、权限等）。

`pages.json`：页面路由、窗口样式、tabBar 配置（已移除原生 tabBar，改用自定义组件）。

## 目录结构

```
blog-app/
├── App.vue                           # 应用入口（生命周期）
├── main.js                           # Vue 入口
├── pages.json                        # 页面路由配置
├── manifest.json                     # 应用配置
├── uni.scss                          # 全局 SCSS 变量
├── uni.promisify.adaptor.js          # Promise 化适配
├── index.html                        # H5 入口
├── config.js                         # 旧配置文件（保留）
├── common/
│   ├── config.js                     # 全局配置（BASE_URL、TOKEN_KEY）
│   ├── request.js                    # 请求封装
│   ├── api.js                        # 接口定义
│   └── theme.js                      # 设计令牌（颜色/间距/圆角/阴影）
├── components/
│   ├── TabBar.vue                    # 自定义底部导航
│   ├── ArticleItem.vue               # 文章卡片
│   ├── SearchBar.vue                 # 搜索栏
│   ├── CategoryChips.vue            # 横向分类筛选
│   ├── Skeleton.vue                 # 骨架屏
│   ├── Icon.vue                      # SVG 图标库
│   ├── Loading.vue                   # 加载动画
├── pages/
│   ├── index/index.vue               # 首页
│   ├── article/detail.vue            # 文章详情
│   ├── resume/index.vue              # 简历
│   ├── mine/index.vue                # 我的
│   └── mine/login.vue                # 登录
├── utils/
│   └── markdown.js                   # Markdown 解析
└── static/
    └── logo.png                     # 应用图标
```

## 页面说明

| 页面 | 路径 | 功能 |
|------|------|------|
| 首页 | pages/index/index | Hero 区 + 搜索栏 + 分类筛选 + 文章列表（含骨架屏、下拉刷新、上拉加载） |
| 文章详情 | pages/article/detail | Markdown 渲染、代码高亮、浮动点赞、相关文章、评论区 |
| 简历 | pages/resume/index | Hero 头像区 + 基本信息 + 技能标签云 + 卡片化时间线 |
| 我的 | pages/mine/index | 渐变 Hero + 统计网格 + SVG 菜单（未登录跳转登录） |
| 登录 | pages/mine/login | Mesh 渐变背景 + 玻璃拟态登录卡片 |

## 组件说明

| 组件 | 用途 |
|------|------|
| TabBar | 自定义底部导航，SVG 图标，高亮当前页，固定定位 + safe-area 适配 |
| ArticleItem | 文章卡片，展示封面/标题/摘要/分类/标签/统计 |
| SearchBar | 搜索输入框，支持防抖与回车搜索 |
| CategoryChips | 横向滚动的分类筛选 chips |
| Skeleton | 骨架屏加载占位 |
| Icon | SVG 图标库（内置 14 个图标：home/article/mine/search/like/comment/share/back/eye/clock/category/tag/user/settings/logout） |
| Loading | 加载动画 |

## 设计令牌

`common/theme.js` 定义统一的设计令牌：

```js
export const colors = {
  primary: '#4F46E5',      // 靛蓝主色
  secondary: '#06B6D4',    // 青色辅色
  gradientHero: '...',      // hero 渐变
  gradientButton: '...',    // 按钮渐变
  // ...
}

export const spacing = { xs: 8, sm: 12, md: 16, lg: 24, xl: 32 }
export const radius = { sm: 8, md: 12, lg: 16, xl: 24, full: 9999 }
export const shadow = { sm: '...', md: '...', lg: '...' }
```

`uni.scss` 同步导出 SCSS 变量供样式使用。

## 请求封装

### common/request.js

- 自动携带 token（从本地存储读取）
- 防重复请求（同一 url+method 进行中不重复发起）
- 全局 loading 蒙层（可按请求关闭）
- 统一错误处理（401 跳转登录、业务错误 toast）

### common/api.js

按业务域组织的接口方法：

| 方法 | 接口 | 说明 |
|------|------|------|
| getArticleList | GET /portal/articles/page | 文章列表 |
| searchArticles | GET /portal/articles/search | 搜索文章 |
| getArticleDetail | GET /portal/articles/{id} | 文章详情 |
| getRelatedArticles | GET /portal/articles/{id}/related | 相关文章 |
| getHotArticles | GET /portal/articles/hot | 热门文章 |
| getCategories | GET /portal/categories | 分类列表 |
| getTags | GET /portal/tags | 标签列表 |
| getComments | GET /portal/comments/{articleId} | 评论列表 |
| postComment | POST /portal/comments | 发表评论 |
| getResume | GET /portal/resume | 简历 |
| getMessages | GET /portal/messages | 留言列表 |
| postMessage | POST /portal/messages | 提交留言 |
| getStats | GET /portal/stats | 站点统计 |
| login | POST /auth/login | 登录 |
| getUserInfo | GET /user/info | 当前用户信息 |

## 多端打包

使用 HBuilderX 发行：

### H5

发行 → 网站-PC Web 或手机 H5 → 填写域名 → 发布

### 微信小程序

1. 在 `manifest.json` → 微信小程序配置中填写 appid
2. 发行 → 小程序-微信 → 填写版本号 → 发布
3. 上传后在微信公众平台提交审核

### App

发行 → 原生 App-云打包 → 选择 Android/iOS → 打包

## 开发约定

| 约定 | 说明 |
|------|------|
| TabBar 图标 | 内联 SVG，24x24 viewBox，2px stroke width |
| TabBar 颜色 | 默认 #909399，激活 #409eff |
| TabBar 定位 | fixed bottom，padding-bottom: env(safe-area-inset-bottom) |
| Tab 切换 | 使用 uni.reLaunch 避免页面栈累积 |
| 页面生命周期 | `<script setup>` 中需从 `@dcloudio/uni-app` 显式 import onLoad/onShow 等 |
| 页面底部留白 | TabBar 高度 56px + safe-area，页面底部需留相应 padding |
| 配色 | 主色 #4F46E5，辅色 #06B6D4，统一使用 theme.js 令牌 |
