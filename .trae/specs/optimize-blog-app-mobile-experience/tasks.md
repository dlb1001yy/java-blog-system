# Tasks

## 阶段一：基础设施（可并行）
- [ ] Task 1: CSS 变量令牌层与暗黑模式基础设施
  - [ ] SubTask 1.1: `App.vue` 全局样式在 `page` 选择器定义 `--app-*` 变量（bg/bg-card/text/text-secondary/text-tertiary/border/divider/primary 等，值与 theme.js 一致），定义 `.theme-dark` 覆盖（暗色板：bg #0B1220、bg-card #1E293B、text #E2E8F0 等），定义 `--app-safe-bottom: env(safe-area-inset-bottom)`；`page` 背景改用变量
  - [ ] SubTask 1.2: `common/theme.js` 新增 `darkColors` 色板、响应式 `currentTheme`（light/dark/system）、`initTheme()`（读 storage + 系统主题）、`setTheme()/toggleTheme()`（写 storage）、`themeVars` 供页面根节点绑定 class；`uni.onThemeChange` 跟随系统（system 模式下）
  - [ ] SubTask 1.3: 新建 `theme.json`（light/dark 导航栏与背景色），`manifest.json` app-plus 增加 `"darkmode": true`，`pages.json` 增加 `"themeLocation": "theme.json"` 并将 globalStyle 颜色改为 `@` 引用
- [ ] Task 2: 图片 URL 工具与封面懒加载
  - [ ] SubTask 2.1: 新建 `common/imageUrl.js`：`optimizeImageUrl(url, width)` —— URL 为配置的 CDN host（config.js 新增 `IMG_CDN_HOSTS` 数组，默认空）时追加 `?x-oss-process=image/resize,w_{width}/format,webp`，否则原样返回
  - [ ] SubTask 2.2: `ArticleItem.vue` 封面图加 `lazy-load`、`@load` 渐显（默认 opacity 0 + 底色占位，加载后过渡到 1）、src 接 `optimizeImageUrl`
- [ ] Task 3: SwipeCell 侧滑组件（自研，不引入 uni-ui）
  - [ ] SubTask 3.1: 新建 `components/SwipeCell.vue`：touch 事件实现左滑露出右侧操作插槽（translateX + 过渡），暴露 open/close 方法与 click 事件，点击外部/滑动其他条目自动收起（父级提供 collapse-all 机制或组件内处理）
- [x] Task 4: QR 编码器
  - [ ] SubTask 4.1: 新建 `utils/qrcode.js`：内联零依赖 QR Code 生成器（MIT 协议的精简实现），输出二维码模块矩阵，提供 `createQrMatrix(text)` 与 `drawQrToCanvas(ctx, matrix, x, y, size)` 辅助函数

## 阶段二：页面接入主题与导航（依赖 Task 1）
- [x] Task 5: 全部页面/组件接入暗黑模式
  - [ ] SubTask 5.1: index/detail/resume/mine/login 页面根节点绑定 `:class="['page-root', isDark ? 'theme-dark' : '']"`（来自 theme.js 响应式状态）
  - [ ] SubTask 5.2: 各页面与组件（ArticleItem/TabBar/SearchBar/CategoryChips/Skeleton）中 surface 颜色（背景/卡片/文字/边框/分割线）改用 `var(--app-*, 兜底值)`；品牌主色等非主题色保持 SCSS 变量
  - [ ] SubTask 5.3: 各页面 onShow 时按当前主题调用 `uni.setNavigationBarColor` 同步原生导航栏（H5 无效时静默）
  - [ ] SubTask 5.4: 我的页新增"外观"设置项（跟随系统/亮色/暗色三态），接入 setTheme
- [ ] Task 6: 自定义 NavBar 组件与登录页接入
  - [ ] SubTask 6.1: 新建 `components/NavBar.vue`：`uni.getWindowInfo().statusBarHeight` 顶部内边距、返回按钮（uni.navigateBack，首页级页面可隐藏）、标题插槽、背景色 `var(--app-bg-card)`、底部投影可选
  - [ ] SubTask 6.2: `pages/mine/login.vue` 接入 NavBar 替代手写状态栏留白（如有）

## 阶段三：首页手势与离线（依赖 Task 5；index/detail 同文件串行）
- [ ] Task 7: 首页自定义下拉刷新与加载动画 + 触控反馈
  - [ ] SubTask 7.1: index 列表区改为固定高度 scroll-view（`refresher-enabled` + 自定义 refresher 插槽：品牌色三点脉冲动画），`@scrolltolower` 触底加载，移除 onPullDownRefresh/onReachBottom 与 pages.json 对应配置
  - [ ] SubTask 7.2: 加载更多状态渲染三点跳动动画组件（内联于页面或抽 `components/LoadingDots.vue`）
  - [ ] SubTask 7.3: ArticleItem / detail 相关文章条目 / mine 菜单项添加 `:active` 缩放触控反馈（scale 0.98 + opacity 0.9，transition 0.15s）
- [x] Task 8: 离线缓存基础设施与页面降级
  - [ ] SubTask 8.1: 新建 `common/offline.js`：`watchNetwork()`（uni.onNetworkStatusChange 维护响应式 `offlineMode`）、`cacheArticleList(records)`/`getCachedArticleList()`、`cacheArticleDetail(article)`/`getCachedArticleDetail(id)`（带缓存时间戳）、稍后阅读 `toggleReadLater(article)/isReadLater(id)/getReadLaterList()/removeReadLater(id)`（storage 持久化）
  - [ ] SubTask 8.2: index `fetchData` 失败（网络类错误）时降级：首次加载读缓存列表 + toast"已进入离线阅读模式"；offlineMode 时跳过请求直接读缓存
  - [ ] SubTask 8.3: detail 成功加载后 `cacheArticleDetail`；加载失败读 `getCachedArticleDetail(id)` 渲染（评论/相关文章可缺省）+ 离线提示；标题区下方新增"稍后阅读"按钮（toggleReadLater，已加入态高亮）

## 阶段四：详情页图片预览与海报（依赖 Task 5；detail 同文件串行）
- [ ] Task 9: 文章图片分段渲染与预览
  - [ ] SubTask 9.1: `utils/markdown.js` 新增 `splitHtmlImages(html)`：将 HTML 按 `<img>` 切分为 `[{type:'html'|'img', ...}]` 段数组
  - [ ] SubTask 9.2: detail 正文区按段交替渲染 rich-text 与 image（image 加 lazy-load、@load 渐显、`optimizeImageUrl`），点击调用 `uni.previewImage(urls, current)` 传入本文全部图片
- [x] Task 10: Canvas 分享海报（依赖 Task 4）
  - [ ] SubTask 10.1: 新建 `components/SharePoster.vue`：canvas（旧版 createCanvasContext API 保证跨端）绘制品牌渐变头部 + 站点名、文章标题（自动换行截断）、摘要（3 行截断）、日期/浏览数、底部 QR 码（utils/qrcode.js 绘制）+ "扫码阅读全文"；绘制完成 `canvasToTempFilePath` 生成临时图
  - [ ] SubTask 10.2: 弹层交互：展示海报预览 + "保存到相册"（App/MP：`uni.saveImageToPhotosAlbum`，处理权限拒绝引导设置；H5：提示长按保存）+ "复制链接"（`uni.setClipboardData`，链接为 H5 端文章地址或站点地址兜底）
  - [ ] SubTask 10.3: detail 新增分享按钮（Navbar 不可用时放操作条：稍后阅读 | 分享海报），点赞 FAB 保持

## 阶段五：稍后阅读页与分包（依赖 Task 3、8）
- [x] Task 11: 稍后阅读分包页
  - [ ] SubTask 11.1: 新建 `subpkg/pages/readlater/index.vue`：列出稍后阅读文章（离线可读），SwipeCell 包裹条目，右侧"删除/置顶或详情"操作，空状态展示，点击进详情（缓存的 id 传给 detail，detail 断网走缓存）
  - [ ] SubTask 11.2: mine 菜单新增"稍后阅读"入口（带已存数量角标），navigateTo 分包路径
- [x] Task 12: 分包加载与路径迁移（最后执行，避免与前期页面改动冲突）
  - [ ] SubTask 12.1: `pages.json` 配置 `subPackages`（root `subpkg`），将 `pages/mine/index`、`pages/mine/login`、`pages/resume/index` 移动至 `subpkg/pages/...`，主包保留 index 与 article/detail；subpkg 页面注册含稍后阅读页
  - [ ] SubTask 12.2: 更新引用路径：`TabBar.vue`（resume/mine 路径）、`request.js`（登录跳转）、`mine/index.vue`（login/resume/readlater 跳转）、`resume/index.vue`（TabBar current）；easycom 规则不变
- [x] Task 13: 验证
  - [ ] SubTask 13.1: 静态检查：grep 确认无残留 `/pages/mine`、`/pages/resume` 引用；surface 颜色无硬编码遗漏（bg-card/text/border 类）；分包页面均注册
  - [ ] SubTask 13.2: 输出 HBuilderX 手动验证清单（H5 运行）：暗黑切换/跟随系统、下拉刷新动画、触底加载动画、卡片按压反馈、断网（DevTools offline）首页/详情降级与提示、稍后阅读增删与侧滑、海报生成与保存、登录/简历/我的分包页可达

# Task Dependencies
- Task 5 依赖 Task 1；Task 6 依赖 Task 1
- Task 7、8 依赖 Task 5（index/detail 同文件改动，7 与 8 串行执行）
- Task 9、10 依赖 Task 5（detail 同文件改动，8→9→10 串行执行；Task 10 另依赖 Task 4）
- Task 11 依赖 Task 3、8；Task 12 依赖 Task 5-11（最后执行）
- Task 13 依赖 Task 12
- 可并行组：Task 1 / 2 / 3 / 4 相互独立可并行
