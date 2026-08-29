# Blog-App 功能对齐重构 Spec

## Why
blog-app（uni-app 移动端）目前仅有 6 个页面（首页/文章详情/登录/我的/简历展示/稍后阅读），而 blog-frontend（Web 门户）拥有完整功能：文章体系（列表/分类/标签/归档）、音乐播放、面试刷题、在线考试（答题/成绩）、留言板、注册、简历编辑等。需以 blog-frontend 为功能基准、以 blog-admin 为后端 API/数据结构参考，对 blog-app 进行重构补齐，并按移动端交互习惯合理布局。**管理端（blog-admin）功能不移植进 App**，仅作接口与数据结构参考。

## What Changes
- **TabBar 重构**：底部导航改为 4 个 tab——首页 / 刷题 / 音乐 / 我的（原"简历" tab 移除，简历入口收进"我的"）
- **首页改造**：Hero 统计扩展（文章/分类/标签/题量 4 项）+ 模块入口网格（文章/刷题/考试/成绩/音乐/留言/简历/关于）+ 最新文章区（3 篇 + 查看全部）
- **新增文章模块分包 `subpkg-article`**：文章列表页（搜索/分类/标签筛选/滚动分页）、分类页、标签云页、归档时间线页
- **新增学习模块分包 `subpkg-study`**：面试刷题页（方向/难度/收藏/错题筛选 + 展开看答案）、试卷列表页、全屏答题页（倒计时/答题卡/防作弊/6 种题型）、成绩查询页（记录列表 + 详情回顾 + 题型得分图表）
- **新增音乐分包 `subpkg-music`**：音乐页（正在播放卡/歌曲列表/推荐歌单）+ 全局播放器单例（`common/player.js`，`uni.createInnerAudioContext`）+ 迷你播放条组件（`PlayerBar.vue`）
- **新增个人中心页面**：注册页、留言反馈页、关于我们页（含站点介绍/模块/技术栈/联系方式）；登录页支持 `redirect` 参数登录后回跳
- **新增简历编辑页**（对标 blog-frontend ProfileResume）：审核状态/基本信息/求职意向/技能/经历/项目/教育/证书动态表单 + 保存 + 分享链接管理（生成/列表/撤销）
- **文章详情增强**：上一篇/下一篇导航、阅读进度条
- **基础设施扩展**：`common/api.js` 补齐全部新接口、新增 `common/upload.js`（uni.uploadFile 封装，携带 token+签名头）、新增登录守卫工具（requiresAuth 页面未登录跳登录页并回跳）、`Icon.vue` 图标扩充

## Impact
- Affected specs:
  - modernize-blog-app-ui（首页/详情页布局在其设计令牌体系上演进，不推翻）
  - optimize-blog-app-mobile-experience（保留暗黑模式/离线缓存/分包架构，新页面遵循同一体系）
- Affected code:
  - `blog-app/pages.json`（新分包注册、页面标题）
  - `blog-app/common/api.js`（新增约 25 个接口函数）、新增 `upload.js`、新增 `player.js`、新增 `auth.js`（守卫）
  - `blog-app/components/`：TabBar.vue（4 tab）、Icon.vue（图标扩充）、新增 PlayerBar.vue
  - `blog-app/pages/index/index.vue`（首页改造）、`pages/article/detail.vue`（上下篇/进度条）
  - `blog-app/subpkg/pages/mine/index.vue`（菜单更新）、`mine/login.vue`（redirect 支持）、新增 `mine/register.vue`、`message/index.vue`、`about/index.vue`、`resume/edit.vue`
  - 新增分包目录 `subpkg-article/`、`subpkg-study/`、`subpkg-music/`
- 不改动 blog-frontend 与 blog-backend（后端接口已齐备，全部复用）

## ADDED Requirements

### Requirement: 四 Tab 底部导航
The system SHALL provide a custom TabBar with 4 tabs: 首页（/pages/index/index）、刷题（/subpkg-study/pages/interview/index）、音乐（/subpkg-music/pages/index）、我的（/subpkg/pages/mine/index），切换使用 uni.reLaunch。

#### Scenario: Tab 切换
- **WHEN** 用户点击任一 tab
- **THEN** reLaunch 到对应页面，tab 高亮与指示器正确

### Requirement: 首页模块化布局
The system SHALL provide a home page with hero stats (文章数/分类数/标签数/面试题量), a module entry grid (文章/刷题/考试/成绩/音乐/留言/简历/关于), and latest articles section (3 items + 查看全部按钮).

#### Scenario: 浏览首页
- **WHEN** 用户打开首页
- **THEN** Hero 显示 4 项统计（来自 GET /portal/stats 与 GET /portal/interview/questions?page=1&size=1 的 total）
- **AND** 模块入口网格 8 项可点击跳转对应页面
- **AND** 最新文章区展示 3 篇（GET /portal/articles/latest），点击进详情，"查看全部"进文章列表页

### Requirement: 文章列表页
The system SHALL provide an article list page (subpkg-article/pages/list) with search bar, category filter chips, scroll-to-load-more pagination, supporting keyword/categoryId/tagId filter modes.

#### Scenario: 筛选浏览
- **WHEN** 用户从分类页/标签页/首页"查看全部"进入
- **THEN** 列表按对应条件加载（GET /portal/articles/page 或 /portal/articles/search），页面标题显示筛选来源
- **AND** 触底自动加载下一页，无更多数据时显示提示

### Requirement: 分类页
The system SHALL provide a category page (subpkg-article/pages/category) showing all categories as a grid; tapping one navigates to the article list filtered by categoryId.

#### Scenario: 分类导航
- **WHEN** 用户点击某分类卡片
- **THEN** 跳转文章列表页并展示该分类文章（GET /portal/categories）

### Requirement: 标签云页
The system SHALL provide a tags page (subpkg-article/pages/tags) rendering all tags as a cloud; tapping a tag navigates to the article list filtered by tagId.

#### Scenario: 标签筛选
- **WHEN** 用户点击某标签
- **THEN** 跳转文章列表页并展示含该标签的文章（GET /portal/tags）

### Requirement: 归档时间线页
The system SHALL provide an archives page (subpkg-article/pages/archives) grouping articles by month (YYYY-MM) in a timeline; tapping an entry opens article detail.

#### Scenario: 按月归档
- **WHEN** 用户打开归档页
- **THEN** 展示按月分组的时间线（月份头 + 当月文章行"日期 + 标题"）（GET /portal/articles/archives）

### Requirement: 面试刷题页
The system SHALL provide an interview page (subpkg-study/pages/interview) with filters (技术方向多选、难度、刷题状态：全部/我的收藏/错题本、关键词搜索) and question cards that expand to load and render the answer (markdown) lazily, with favorite/wrong-book toggle buttons.

#### Scenario: 刷题
- **WHEN** 用户（已登录）进入刷题页并展开某题
- **THEN** 懒加载答案（GET /portal/interview/questions/{id}/answer）并以 markdown 渲染，同时展示解题思路
- **WHEN** 用户点击收藏/错题本按钮
- **THEN** 调用 toggle 接口（POST /portal/interview/favorites/{qid}、/portal/interview/wrong/{qid}）并更新按钮状态
- **WHEN** 未登录用户进入
- **THEN** 被登录守卫拦截跳转登录页，登录后回跳

### Requirement: 试卷列表页
The system SHALL provide an exam papers page (subpkg-study/pages/exam) listing papers with 总分/时长/题数 and a start button; tapping starts the full-screen taking page.

#### Scenario: 选择试卷
- **WHEN** 已登录用户点击"开始考试"
- **THEN** 跳转答题页并携带 paperId（GET /portal/exam/papers）

### Requirement: 全屏答题页
The system SHALL provide an exam taking page (subpkg-study/pages/exam/taking) with countdown timer (auto-submit at zero), switch-count anti-cheat (onHide 计时，离开超 10 秒或累计切屏 ≥3 次强制交卷), answer card grid (已答/标记/当前三态), and support for 6 question types (单选/多选/判断/填空/简答/编程——编程题为语言选择 + 多行 textarea).

#### Scenario: 答题与交卷
- **WHEN** 用户作答并点击交卷（确认弹窗提示未答题数）
- **THEN** 提交 POST /portal/exam/papers/{paperId}/submit（body 含 answers/switchCount/durationSeconds），成功后跳转成绩页
- **WHEN** 倒计时归零或切屏超限
- **THEN** 自动强制交卷

### Requirement: 成绩查询页
The system SHALL provide a scores page (subpkg-study/pages/scores) with a records list (试卷/得分/状态/用时/切屏/提交时间) and a detail view (得分 hero 卡/及格与作弊标记/题型得分分布与掌握度纯 CSS 图表/逐题答题回顾含我的答案、正确答案、解析、评语)，支持"判分中"轮询（最多 10 次 × 2s）。

#### Scenario: 查看成绩
- **WHEN** 用户点击某条考试记录
- **THEN** 进入详情态，加载 GET /portal/exam/records/{id} 并渲染图表与答题回顾
- **WHEN** 记录处于判分中
- **THEN** 自动轮询直至判分完成

### Requirement: 全局音乐播放器
The system SHALL provide a singleton player store (common/player.js) using uni.createInnerAudioContext surviving page navigation, with playlist/currentIndex/isPlaying/currentTime/duration state, play/pause/next/prev/seek/toggleRepeat(none/all/one)/toggleShuffle methods, volume/repeat/shuffle preferences persisted to storage, and play-count reporting (POST /portal/music/songs/{id}/play，静默失败).

#### Scenario: 跨页连续播放
- **WHEN** 用户在音乐页开始播放后切换到其他页面
- **THEN** 播放不中断，迷你播放条（PlayerBar.vue）在挂载了该组件的页面展示当前曲目并可控制播放/暂停/上下曲

### Requirement: 音乐页
The system SHALL provide a music page (subpkg-music/pages/index) with a now-playing card (cover/title/artist/progress slider/play controls/循环与随机切换)、歌曲列表（分页加载、当前曲高亮、行内播放）和推荐歌单网格（封面+歌曲数角标，点击载入歌单并播放第一首）。

#### Scenario: 播放音乐
- **WHEN** 用户点击歌曲行
- **THEN** 设置播放列表并播放该曲；再点当前曲则切换播放/暂停（GET /portal/music/songs、/portal/music/playlists、/portal/music/playlists/{id}）

### Requirement: 注册页
The system SHALL provide a register page (subpkg/pages/mine/register) with username/email/password/confirm fields; password must be ≥8 chars containing letters and digits; on success navigate to login with username prefilled.

#### Scenario: 注册
- **WHEN** 用户填写合法信息提交
- **THEN** 调用 POST /auth/register 成功后跳转登录页（POST /auth/register）

### Requirement: 登录回跳
The system SHALL support a `redirect` query param on the login page; after successful login the app navigates to that path instead of the default.

#### Scenario: 守卫拦截后登录
- **WHEN** 未登录用户访问 requiresAuth 页面被拦截 → 登录成功
- **THEN** 自动跳回原目标页面

### Requirement: 留言反馈页
The system SHALL provide a message page (subpkg/pages/message/index) with 昵称（必填）/邮箱（选填）/内容（必填）表单，提交 POST /portal/messages 成功后提示"留言成功，等待审核"并清空。

### Requirement: 关于我们页
The system SHALL provide an about page (subpkg/pages/about/index) combining site intro, clickable module grid, tech-stack tags (前端/后端/部署), and contact info.

### Requirement: 简历编辑页
The system SHALL provide a resume edit page (subpkg/pages/resume/edit.vue, requiresAuth) showing audit status (0 待审核/1 已通过/2 已拒绝 + 拒绝原因), full form (基本信息/求职意向/简介与自我评价/技能动态行含等级与熟练度/工作经历/项目经验含技术栈标签/教育背景/证书/兴趣), photo upload via uni.chooseImage + upload util, save (PUT /portal/resume/mine), preview entry (复用简历展示页数据渲染), and share-link management (有效期选择 0 永久~自定义天数、生成 POST /portal/resume/mine/share、列表 GET /portal/resume/mine/shares、撤销 DELETE /portal/resume/mine/share/{id}、复制链接).

#### Scenario: 编辑保存
- **WHEN** 用户填写姓名（必填校验）并保存
- **THEN** 动态列表字段序列化为 JSON 字符串提交，成功后提示

#### Scenario: 生成分享链接
- **WHEN** 简历审核已通过且用户选择有效期生成
- **THEN** 返回分享 token，拼接 `{SERVER_ORIGIN}/blog/resume/share/{token}` 可复制；未通过审核时生成按钮禁用并说明

### Requirement: 文件上传工具
The system SHALL provide common/upload.js wrapping uni.uploadFile to POST /v1/storage/upload with FormData field `file`, injecting Bearer token and signing headers, returning data.url; max size 10MB pre-check.

### Requirement: 登录守卫
The system SHALL provide a guard utility (common/auth.js) `requireLogin()` returning false and redirecting to login with `redirect` param when no token exists; requiresAuth pages (刷题/考试/成绩/简历编辑) call it on onLoad/onShow.

### Requirement: 文章详情上下篇与进度条
The article detail page SHALL show prev/next navigation cards (from detail API prev/next fields) and a top reading progress bar driven by page scroll.

#### Scenario: 上下篇导航
- **WHEN** 用户点击上一篇/下一篇
- **THEN** 跳转对应文章详情并回到顶部

## MODIFIED Requirements

### Requirement: TabBar 组件（原 optimize-blog-app-mobile-experience）
由 3 tab（首页/简历/我的）改为 4 tab（首页/刷题/音乐/我的），保持 reLaunch 切换、激活指示器与暗黑适配。

### Requirement: 首页（原 modernize-blog-app-ui / optimize-blog-app-mobile-experience）
保留搜索栏/分类 chips/文章流/离线降级；Hero 统计扩为 4 项；新增模块入口网格与"最新文章"区（首页文章流保留在其下方或以"最新文章 3 篇 + 查看全部"替代——采用后者：首页展示最新 3 篇，完整文章流移入文章列表页）。

### Requirement: 我的页菜单（原 modernize-blog-app-ui）
菜单项调整为：我的简历（编辑）、简历预览、稍后阅读、留言反馈、外观设置、关于我们、退出登录；统计卡保留。

### Requirement: common/api.js（原 modernize-blog-app-ui）
在既有 15 个接口基础上新增约 25 个：register、hot/latest/archives 文章接口、interview 全套（questions/categories/answer/favorites/wrong toggle 与列表）、exam 全套（papers/paper/submit/records/record）、music 全套（songs/playlists/playlist/play 上报）、sendMessage、resume mine 全套（get/save/share create/list/revoke）。

### Requirement: Icon 组件（原 modernize-blog-app-ui）
图标字典由 15 个扩充，新增：play/pause/next/prev/repeat/repeat-one/shuffle/music/star/book(错题)/chevron-down/chevron-left/calendar/trophy/check/close/share/image/upload/trash 等，统一 24x24 stroke 1.8 风格。

## REMOVED Requirements

### Requirement: TabBar"简历" tab
**Reason**: tab 位让位给刷题与音乐两大高频模块；简历为低频功能收进"我的"菜单。
**Migration**: 我的页菜单提供"简历预览"入口，简历编辑提供"我的简历"入口；简历展示页保留原有路由（subpkg/pages/resume/index）。
