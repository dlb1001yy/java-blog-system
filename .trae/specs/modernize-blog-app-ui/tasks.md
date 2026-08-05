# Tasks

## 后端接口（先行，前端依赖）
- [x] Task 1: 新增文章搜索接口 `GET /portal/articles/search`
  - [ ] SubTask 1.1: 在 `PortalArticleController` 添加 `search` 方法，支持 keyword/current/size 参数
  - [ ] SubTask 1.2: 复用 `ArticleService.page` + LambdaQueryWrapper（title/summary LIKE）
  - [ ] SubTask 1.3: 仅返回已发布文章，按 createTime 倒序，调用 `fillArticleInfo` 填充分类名
- [x] Task 2: 新增相关文章接口 `GET /portal/articles/{id}/related`
  - [ ] SubTask 2.1: 在 `PortalArticleController` 添加 `related` 方法
  - [ ] SubTask 2.2: 查询同 categoryId 且 id != 当前、已发布的文章，LIMIT 3
  - [ ] SubTask 2.3: 调用 `fillArticleInfo` 填充分类名
- [x] Task 3: 新增站点统计接口 `GET /portal/stats`
  - [ ] SubTask 3.1: 新建 `PortalStatsController`（位于 controller/portal）
  - [ ] SubTask 3.2: 注入 ArticleService、TagService、CategoryService
  - [ ] SubTask 3.3: 统计已发布文章数、所有文章 viewCount 求和、标签数、分类数，返回 Map
- [x] Task 4: 新增公开留言列表接口 `GET /portal/messages`
  - [ ] SubTask 4.1: 在 `PortalMessageController` 添加 `list` 方法
  - [ ] SubTask 4.2: 查询 status=1 的留言，按 createTime 倒序
- [x] Task 5: 新增当前用户信息接口 `GET /user/info`
  - [ ] SubTask 5.1: 新建 `UserController`（位于 controller/portal，路径 `/user`）
  - [ ] SubTask 5.2: 从 SecurityContext 获取当前 Authentication，构造 UserDetailsService 查询
  - [ ] SubTask 5.3: 返回 id/username/nickname/avatar/email，路径在 SecurityConfig 中改为 authenticated

## 前端基础设施
- [x] Task 6: 建立设计令牌系统 `blog-app/common/theme.js`
  - [ ] SubTask 6.1: 定义 colors（primary #4F46E5、secondary #06B6D4、bg #F1F5F9、text #0F172A、textSecondary #64748B、border #E2E8F0、success/warning/danger）
  - [ ] SubTask 6.2: 定义 spacing（4/8/12/16/20/24/32）、radii（4/8/12/16/999）、shadows（card/floating）、fontSize（xs/sm/base/lg/xl/2xl）
- [x] Task 7: 在 `uni.scss` 中暴露 scss 变量供页面使用，与 theme.js 保持一致
- [x] Task 8: 升级 `common/api.js` 新增接口方法：searchArticles、getRelatedArticles、getStats、getMessages、getUserInfo

## 前端组件
- [x] Task 9: 重构 `components/ArticleItem.vue`
  - [ ] SubTask 9.1: 卡片样式：12px 圆角、柔和阴影、padding 16px
  - [ ] SubTask 9.2: 类型徽章改为胶囊形
  - [ ] SubTask 9.3: 底部统计用内联 SVG 图标（eye、folder）替代 emoji
  - [ ] SubTask 9.4: 标题最多两行省略，摘要最多两行省略
- [x] Task 10: 新建 `components/Skeleton.vue` 骨架屏组件
  - [ ] SubTask 10.1: 支持 type="article" 与 type="detail" 两种预设
  - [ ] SubTask 10.2: 灰色背景 + shimmer 动画
- [x] Task 11: 新建 `components/Icon.vue` SVG 图标组件
  - [ ] SubTask 11.1: 内置常用图标（search、eye、heart、folder、clock、chevron-right、user、mail、phone、location）
  - [ ] SubTask 11.2: 通过 name prop 选择图标，size/color 可配置
- [x] Task 12: 重构 `components/TabBar.vue`
  - [ ] SubTask 12.1: 高度 56px，图标 24x24，描边 1.8px
  - [ ] SubTask 12.2: 激活态添加顶部圆点指示器
  - [ ] SubTask 12.3: 切换时 0.2s 颜色过渡（保留 uni.reLaunch 行为）
- [x] Task 13: 新建 `components/SearchBar.vue` 搜索栏组件
  - [ ] SubTask 13.1: 圆角输入框 + 搜索图标 + placeholder
  - [ ] SubTask 13.2: confirm 事件触发搜索回调
- [x] Task 14: 新建 `components/CategoryChips.vue` 横向分类 chips 组件
  - [ ] SubTask 14.1: props 接收 list 与 active，emit change
  - [ ] SubTask 14.2: 激活态填充主色，非激活态浅色背景

## 前端页面
- [x] Task 15: 重构 `pages/index/index.vue`
  - [ ] SubTask 15.1: 顶部 hero 区（站点名 + 副标题 + 统计数据来自 /portal/stats）
  - [ ] SubTask 15.2: 搜索栏接入 SearchBar 组件
  - [ ] SubTask 15.3: 分类筛选接入 CategoryChips（包含"全部"+真实分类）
  - [ ] SubTask 15.4: 文章列表使用新 ArticleItem，加载时用 Skeleton
  - [ ] SubTask 15.5: 保留类型筛选（全部/原创/转载/翻译）作为二级 chips
- [x] Task 16: 重构 `pages/article/detail.vue`
  - [ ] SubTask 16.1: 标题区与 meta 用现代排版，相对时间显示
  - [ ] SubTask 16.2: 内容区字号 15px、行高 1.8、段落间距 16px
  - [ ] SubTask 16.3: 浮动点赞按钮 fixed 右下角，圆形主色背景
  - [ ] SubTask 16.4: 评论区带头像/首字母 avatar、相对时间、卡片样式
  - [ ] SubTask 16.5: 文末"相关文章"区块调用 /portal/articles/{id}/related
- [x] Task 17: 重构 `pages/resume/index.vue
  - [ ] SubTask 17.1: hero 卡片（头像 + 姓名 + 职位 + 联系方式用 SVG 图标）
  - [ ] SubTask 17.2: 技能以彩色标签云展示（按 level 区分颜色）
  - [ ] SubTask 17.3: 工作经历与教育背景卡片化时间线
  - [ ] SubTask 17.4: 项目经验卡片网格（每行 1 个，大卡片）
- [x] Task 18: 重构 `pages/mine/index.vue
  - [ ] SubTask 18.1: 渐变 hero（新配色）+ 头像 + 昵称 + 简介
  - [ ] SubTask 18.2: 统计数据网格（2 列，调用 /portal/stats）
  - [ ] SubTask 18.3: 菜单列表用 Icon 组件 SVG 图标
  - [ ] SubTask 18.4: 未登录时显示登录入口卡片
- [x] Task 19: 重构 `pages/mine/login.vue`
  - [ ] SubTask 19.1: mesh gradient 背景（多层径向渐变叠加）
  - [ ] SubTask 19.2: 玻璃拟态登录卡片（backdrop-filter blur + 半透明白底）
  - [ ] SubTask 19.3: 输入框 focus 微动画（边框颜色过渡）
  - [ ] SubTask 19.4: 登录按钮主色填充 + 圆角

## 验证
- [x] Task 20: 启动后端验证所有新接口可正常访问（Postman/curl）
- [x] Task 21: 启动 H5 端验证各页面样式与交互

# Task Dependencies
- Task 8 依赖 Task 1-5（接口路径需后端先确定）
- Task 15 依赖 Task 9、10、13、14
- Task 16 依赖 Task 9、10、11
- Task 17 依赖 Task 11
- Task 18 依赖 Task 11
- Task 20 依赖 Task 1-5
- Task 21 依赖 Task 15-19
- Task 1-5 可并行
- Task 9-14 可并行
- Task 15-19 可并行（依赖前置组件完成）
