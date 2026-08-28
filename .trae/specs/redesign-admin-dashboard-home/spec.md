# 重构管理后台数据看板（首页）Spec

## Why
当前 `blog-admin` 首页（数据看板）只展示博客维度的统计（文章/浏览/评论/点赞）与图表，没有传达系统整体定位。整个系统实际上是「博客 + 面试题库 + 在线考试 + 音乐 + 简历」五位一体的学习分享平台，且后台已具备题库、试卷、阅卷、用户简历、面试题、音乐等内容管理能力，但新访客看首页完全无法得知这些功能的存在与规模。需要重构首页，让不了解系统的人一眼就知道"这个系统是干什么的"，并对各业务模块数据规模与服务运行状态有基本了解。

## What Changes
- **新增「平台概览」欢迎区块**：页面顶部展示平台定位介绍（Java 技术学习分享一体化平台：博客、面试题库、在线考试、音乐、简历五大模块）、当前登录人问候语与日期，以及各模块入口快捷跳转（内容/考试/系统管理）
- **统计卡片重构**：由 4 张博客卡片改为 8 张，按五大模块组织 ——
  - 内容：文章总数、总浏览量（含已发布数副文本）
  - 互动：评论总数、留言总数
  - 题库与面试：面试题总数、试题总数
  - 考试：待阅卷数
  - 用户：注册用户数
- **新增「模块内容分布」图表**：以环形饼图展示各内容模块的条目规模（文章/面试题/试题/简历/音乐/友链/留言/评论），替代原「文章类型分布」，体现平台全貌
- **保留并优化既有图表**：近 7 天文章发布趋势、分类文章统计保持不变（沿用现有接口与主题适配逻辑）
- **待处理事项**：沿用现有 todo 接口，补充"待审核简历"入口（若后端返回该字段则展示）
- **服务状态卡片增强**：在现有 Redis / 数据库 / 磁盘 / JVM 基础上，新增系统运行时长（uptime）、JDK 版本、操作系统、后端版本/构建时间等基本服务信息，让"一看就有基本了解"
- **后端扩展**：`AdminDashboardController` 新增/扩展接口（`/overview`、`/module-stats`、`/system-status` 增补字段），复用既有 Service，遵循现有代码风格
- **前端 API 层**：`dashboard.js` 增加对应方法

## Impact
- Affected specs: redesign-admin-ui（首页视觉风格延续其成果，非破坏性）、add-metrics-monitoring / enhance-infra-monitoring（看板服务状态与监控体系互补，不冲突）
- Affected code:
  - blog-admin/src/views/Dashboard.vue（主要重构对象）
  - blog-admin/src/api/dashboard.js（新增 API 方法）
  - blog-backend/src/main/java/com/dlbyy/blog/controller/admin/AdminDashboardController.java（新增 overview/module-stats 接口、扩展 system-status）
  - 可能涉及 blog-backend 对应 Service 注入（InterviewQuestionService、ExamQuestionService、ExamPaperService、ResumeInfoService、MusicSongService、UserService、LinkService、TagService 等，均为已存在的 IService，直接注入 count 即可）
- 纯增量修改，不删除现有接口，已有调用方（Sidebar 待阅卷 badge 等）不受影响（**非破坏性**）

## ADDED Requirements

### Requirement: 平台概览欢迎区块
首页 SHALL 在顶部展示平台定位介绍区块，包含：平台名称与一句话定位（覆盖博客、面试题库、在线考试、音乐、简历五大模块的说明）、当前登录用户问候与当日日期、各模块管理入口的快捷跳转链接。

#### Scenario: 首次进入首页
- **WHEN** 管理员登录后进入数据看板
- **THEN** 页面顶部可见平台定位介绍、问候语与模块快捷入口，无需浏览其他页面即可了解系统是做什么的

### Requirement: 五模块数据统计卡片
首页 SHALL 展示 8 张统计卡片：文章总数、总浏览量（副文本显示已发布数）、评论总数、留言总数、面试题总数、试题总数、待阅卷数、注册用户数，数据来自后端聚合接口。

#### Scenario: 数据加载成功
- **WHEN** 看板接口返回成功
- **THEN** 8 张卡片正确显示对应数值，加载失败时显示 0 且不抛错

### Requirement: 模块内容分布图表
首页 SHALL 以环形饼图展示各内容模块条目规模（文章、面试题、试题、简历、音乐、友链、留言、评论），鼠标悬停显示具体数量与占比。

#### Scenario: 渲染分布图
- **WHEN** 模块统计接口返回数据
- **THEN** 环形图渲染各模块扇区，图例与悬停提示正常，跟随明暗主题切换颜色适配

### Requirement: 服务基本信息展示
首页服务状态卡片 SHALL 在 Redis/数据库/磁盘/JVM 状态基础上，展示系统运行时长、JDK 版本、操作系统名称版本、后端构建版本与时间。

#### Scenario: 查看服务信息
- **WHEN** 打开服务状态卡片
- **THEN** 可见运行时长（如"3 天 5 小时"）、JDK 版本、OS 信息及后端版本信息，服务异常项以红色状态点标识

### Requirement: 后端聚合接口
`/admin/dashboard/overview` SHALL 一次性返回统计卡片所需全部指标（文章数、已发布数、总浏览量、总点赞数、评论数、留言数、面试题数、试题数、试卷数、待阅卷数、用户数、简历数、音乐数、分类数、标签数、友链数、待审核评论/留言数、今日新增文章数）；`/admin/dashboard/module-stats` SHALL 返回模块内容分布数据；`/admin/dashboard/system-status` SHALL 额外返回 uptime、jdkVersion、osName/osVersion、appVersion/buildTime 字段。

#### Scenario: 聚合接口调用
- **WHEN** 前端请求 overview 接口
- **THEN** 返回 Result 包装的完整指标 Map，无需前端多次请求拼装

## MODIFIED Requirements

### Requirement: 数据看板页面布局（原 redesign-admin-ui 相关成果）
数据看板布局调整为：平台概览欢迎区 → 8 张统计卡片 → 趋势图 + 模块分布图 → 分类统计 + 待处理事项 → 最近活动 + 服务状态（含基本信息）。延续现有设计令牌（CSS 变量）、明暗主题切换与 ECharts 主题适配逻辑。

## REMOVED Requirements

### Requirement: 文章类型分布饼图
**Reason**: 单一维度（原创/转载/翻译）无法体现平台全貌，被"模块内容分布"环形图替代。
**Migration**: 原后端 `/admin/dashboard/type-stats` 接口保留不动，仅前端 Dashboard 不再调用；如需恢复可在图表区重新接入。
