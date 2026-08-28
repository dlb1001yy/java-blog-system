# 计划：重新生成前端「关于站点」页面的「关于本站」内容

## Summary
将 `blog-frontend` 前台「关于站点」页面（`/about-site`）中静态、过时的「关于本站」介绍文案（"这是一个专注于Java技术分享的个人博客"）替换为按项目实际功能重新撰写的内容：准确介绍「Java码农笔记」是集 **博客、面试刷题、在线考试、音乐、简历** 五大模块于一体的 Java 技术学习分享平台，并增加模块介绍、技术栈、联系方式三个小节，让访客通过该页面全面了解站点功能。改动仅限 `AboutSite.vue` 一个文件的内容文案与少量配套样式，留言板功能保持不变。

## Current State Analysis（现状分析）
- 页面文件：[AboutSite.vue](d:/my-project/java-blog-system/blog-frontend/src/views/AboutSite.vue)，路由 `/about-site`（[router/index.js](d:/my-project/java-blog-system/blog-frontend/src/router/index.js#L75-L79)），导航入口「关于站点」（[AppHeader.vue](d:/my-project/java-blog-system/blog-frontend/src/components/AppHeader.vue#L18)）。
- 现状问题：文案停留在"个人博客"阶段，只提到博客/文章，未提及站点已具备的面试刷题、在线考试、音乐、简历模块；排版为纯文本段落（p + h2 + ul），信息量少。
- 项目实际功能（依据前台导航与路由）：
  - 个人博客：`/articles`、`/article/:id`、`/category`、`/tags`、`/archives`（分类/标签/归档/搜索）
  - 面试刷题：`/interview`（登录）
  - 在线考试：`/exam`、`/exam/:paperId`、`/scores` 成绩查询（登录）
  - 音乐：`/music`（含播放器 PlayerBar）
  - 简历：`/profile/resume` 我的简历编辑、`/resume/share/:token` 简历分享
  - 互动：评论、留言板 `/messages`
- 首页 [Home.vue](d:/my-project/java-blog-system/blog-frontend/src/views/Home.vue#L120-L126) 已有各模块名称/描述/路径/图标定义，可作为文案与跳转路径的权威参考（保持口径一致）。
- 站点统计接口已存在：`articleApi.getStats()` → `/portal/stats`（首页用于文章/分类/标签统计）。
- 样式沿用前台变量体系（`--text-regular`、`--text-secondary`、`.card`、`.container` 等，见 [variables.css](d:/my-project/java-blog-system/blog-frontend/src/assets/styles/variables.css) 与 global.css）。

## Proposed Changes（改动内容）

### 文件：`blog-frontend/src/views/AboutSite.vue`（唯一改动文件）

**1. 重写「关于本站」卡片内容（替换第 5–16 行）**

结构：
- 站点标题：Java码农笔记
- 站点定位段（2 段）：
  - 第一段：欢迎语 + 一句话定位——「Java 技术学习分享一体化平台，集博客、面试刷题、在线考试、音乐、简历五大模块于一体」
  - 第二段：平台理念——记录 Java 学习之路（Spring、数据库、前端、DevOps 等），从输入（读文章）到练习（刷题）再到检验（在线考试/成绩查询），并提供简历制作与分享能力，覆盖"学、练、测、展"完整学习闭环
- 「功能模块」小节：5 个模块项（名称 + 一句话描述，文案与首页 modules 口径一致）：
  | 模块 | 描述 | 对应路径 |
  |---|---|---|
  | 个人博客 | 记录学习笔记与技术心得，支持分类、标签、归档与全文搜索 | `/articles` |
  | 面试刷题 | 精选面试题库，助力求职准备 | `/interview` |
  | 在线考试 | 模拟真实考试环境，自动判卷，支持成绩查询 | `/exam` |
  | 音乐放松 | 学习之余聆听音乐放松身心 | `/music` |
  | 我的简历 | 在线制作个人简历，一键生成分享链接 | `/profile/resume` |
  模块项使用简洁列表/网格布局（纯 CSS，不引入新组件库依赖），每项可点击 `router.push` 跳转。
- 「技术栈」小节（新增，帮助了解项目构成）：前端 Vue 3 + Element Plus + Pinia + ECharts；后端 Spring Boot + MyBatis-Plus + MySQL + Redis + Elasticsearch；部署 Docker + Nginx。
- 「联系方式」小节：保留现有邮箱 1310471544@qq.com 与 Gitee 仓库链接 https://gitee.com/dlbyy/java-blog-system.git（Gitee 链接改为可点击的 `<a target="_blank" rel="noopener">`）。

**2. 留言板区块（第 18–42 行）保持不变**：表单逻辑、`articleApi.addMessage` 提交、校验提示均不动。

**3. 样式补充（scoped，约 30–40 行）**：
- 模块网格：`.module-list`（网格 `repeat(auto-fill, minmax(240px, 1fr))`，间距沿用页面 `gap` 惯例），`.module-item`（card 内浅背景块、圆角、hover 提亮 + cursor pointer）
- 模块名/描述文字使用现有变量 `--text-regular` / `--text-secondary`
- 链接颜色沿用 `--primary-color`
- 技术栈标签：`.tech-tags` 简单行内标签样式（浅底圆角小标签）
- 移动端适配：网格在小屏自动单列（auto-fill 自适应，无需额外媒体查询；必要时补一条 `@media (max-width: 768px)`）

**4. 脚本部分**：新增 `useRouter()`（模块项跳转用）；其余不变。

### 不做的事（明确边界）
- 不改动后端任何接口/Controller
- 不改动路由、导航、About.vue（`/about` 旧页面，无导航入口，保持原样）
- 不改动留言板功能与接口调用
- 不引入新依赖、不新增组件文件

## Assumptions & Decisions（假设与决策）
1. **文案口径与首页对齐**：模块名称/描述直接沿用 Home.vue 中 modules 定义，避免同一站点两处说法不一。
2. **联系方式保留现有值**：邮箱与 Gitee 仓库地址取自现有页面，视为站点真实联系方式，不做虚构。
3. **技术栈描述基于项目实际**（pom.xml 依赖、frontend package.json、Dockerfile/nginx.conf 均已存在），只列主键，不做版本罗列。
4. **简历模块说明写法**：因前台导航中「简历」入口为登录后的「我的简历」，路由注释显示简历展示功能暂时屏蔽，故描述侧重"在线制作与分享"，不承诺公开展示。
5. **纯前端改动**，无需后端配合；构建验证由用户手动执行（沿用此前约定：mvn/npm 命令用户手动跑）。

## Verification（验证步骤）
1. 静态检查：AboutSite.vue 模板无语法错误（IDE 诊断为空），新引入的 `useRouter` 已正确 import。
2. 样式核对：新增样式类均使用项目现有 CSS 变量，不硬编码颜色（hover 态可除外）。
3. 功能核对：留言板提交逻辑与原实现一致（字段、接口、提示语不变）；5 个模块项跳转路径与 router/index.js 中路由一一对应。
4. 构建验证（用户手动执行）：`cd blog-frontend && npm run build` 通过。
5. 页面验收：访问 `/blog/#/about-site`，确认新文案展示五大模块、技术栈与联系方式，点击模块项可正确跳转，留言板可正常提交。
