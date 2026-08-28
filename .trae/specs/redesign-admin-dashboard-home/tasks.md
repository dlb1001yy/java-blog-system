# Tasks

- [x] Task 1: 后端新增聚合统计接口
  - [x] 1.1 在 `AdminDashboardController` 中注入所需 Service（InterviewQuestionService、ExamQuestionService、ExamPaperService、ResumeInfoService、MusicSongService、UserService、LinkService、TagService）
  - [x] 1.2 新增 `GET /admin/dashboard/overview`：一次性返回统计卡片全部指标（articleCount/publishedCount/totalViews/totalLikes/commentCount/messageCount/interviewQuestionCount/examQuestionCount/examPaperCount/pendingMarkingCount/userCount/resumeCount/musicCount/categoryCount/tagCount/linkCount/pendingCommentCount/pendingMessageCount/todayArticleCount）
  - [x] 1.3 新增 `GET /admin/dashboard/module-stats`：返回 [{ name, value }] 形式的模块内容分布数据（文章、面试题、试题、试卷、简历、音乐、友链、留言、评论）
  - [x] 1.4 扩展 `GET /admin/dashboard/system-status`：新增 uptime（毫秒）、jdkVersion、osName、osVersion、appVersion、buildTime 字段
  - [x] 1.5 后端代码完成后静态自查（导入、字段命名、Result 包装正确）；mvn 编译由用户手动执行

- [x] Task 2: 前端 API 层扩展
  - [x] 2.1 在 `blog-admin/src/api/dashboard.js` 新增 `getOverview()`、`getModuleStats()` 方法（getSystemStatus 已存在）

- [x] Task 3: 重构 Dashboard.vue 首页
  - [x] 3.1 新增「平台概览」欢迎区块：平台定位介绍（博客/面试题库/在线考试/音乐/简历五大模块）、登录用户问候语 + 当日日期、模块快捷入口（跳转对应管理页）
  - [x] 3.2 统计卡片扩展为 8 张：文章总数、总浏览量（副文本已发布数）、评论总数、留言总数、面试题总数、试题总数、待阅卷数、注册用户数；数据源切换为 overview 接口
  - [x] 3.3 「文章类型分布」饼图替换为「模块内容分布」环形图（module-stats 接口），沿用主题色适配与主题切换重建逻辑
  - [x] 3.4 保留近 7 天文章趋势图与分类统计图（沿用现有接口与主题适配）
  - [x] 3.5 待处理事项沿用 todo 接口，草稿箱、今日新增等条目保持可用，新增待审核简历入口
  - [x] 3.6 服务状态卡片增补：系统运行时长（格式化为 天/时/分）、JDK 版本、操作系统、后端版本/构建时间
  - [x] 3.7 样式沿用现有设计令牌（--space-*/--radius-*/--font-*/明暗主题变量），响应式布局（xs/sm/lg 断点）不劣化

- [x] Task 4: 验证
  - [x] 4.1 静态核对代码：接口字段前后端一致、无明显语法错误（IDE 诊断为空；图标 import 已对照 node_modules 确认存在；构建命令由用户手动执行：mvn compile / npm run build）
  - [x] 4.2 核对 checklist.md 各检查项并勾选可静态验证部分，其余留待用户手动构建后确认

# Task Dependencies
- Task 2、Task 3 依赖 Task 1（接口字段需先定义）
- Task 4 依赖 Task 1-3 全部完成
