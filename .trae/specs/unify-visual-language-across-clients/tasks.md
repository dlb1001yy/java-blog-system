# Tasks

- [x] Task 1: 品牌资产统一（admin + frontend favicon/站名）
  - [x] 1.1 重写 `blog-admin/public/favicon.svg`：以 frontend `LogoIcon.vue` 的咖啡杯+代码符号为基准，翡翠绿渐变（#059669→#14B8A6）；同步替换 `blog-admin/public/icons.svg` 中的紫色残留（5 处 #aa3bff → #10B981，Read 磁盘验证）
  - [x] 1.2 重写 `blog-frontend/public/favicon.svg`：同款咖啡杯图形，将蓝色渐变改为 #059669→#14B8A6
  - [x] 1.3 `blog-admin/src/layout/index.vue`：侧栏站名"Java 博客"改"Java码农笔记"，渐变圆点保留；另统一 Login.vue 标题与 index.html <title>
  - [x] 1.4 验证：admin/frontend favicon 与页内 Logo 图形一致、颜色一致；"Java 博客"变体全项目清零
- [x] Task 2: blog-app 品牌露出与刻度对齐
  - [x] 2.1 Hero 站名确认"Java码农笔记"；新增 .hero-brand 行（48px logo.png + 站名）；GDI+ 重绘 192x192 翡翠绿咖啡杯 logo.png
  - [x] 2.2 `uni.scss` + `common/theme.js`：圆角阶梯 8/12/16/20（radii xxl 24）
  - [x] 2.3 清理 `pages/index/index.vue` 硬编码圆角（10px→$radius-md、14px→$radius-lg；rpx 体系未动）
  - [x] 2.4 统一按压反馈：module-item/latest-item 改 scale(0.98)+opacity .9
- [x] Task 3: frontend 视觉冲突清理与反馈统一
  - [x] 3.1 Hero 紫渐变改 linear-gradient(135deg, #047857, #0D9488)；装饰均为中性白无需改
  - [x] 3.2 模块图标配色品牌化（#059669/#0D9488/#14B8A6/#2DD4BF + 0.1 透明度背景）
  - [x] 3.3 LatestBlogCard：#9b59b6→#14B8A6；hover 阴影改 var(--shadow-hover)
  - [x] 3.4 global.css：阴影令牌 slate 基对齐 admin；补 --container-padding
  - [x] 3.5 SkeletonCard 底色改 var(--border-color)；额外清理 ArticleDetail 进度条、Music/PlayerBar 占位渐变紫色
- [x] Task 4: admin 样式收敛与死代码清理
  - [x] 4.1 global.css 全局表格行 hover（一处定义）；删除 15 个视图的重复 :deep 块
  - [x] 4.2 删除零使用 .hover-lift、与 scoped 冲突的 global.css .slide-fade 纵向定义
  - [x] 4.3 el-empty image-size 统一 80（Dashboard/ExamPaperList 共 3 处 60→80）
- [x] Task 5: app 空态反馈补齐
  - [x] 5.1 首页最新文章：删除静默隐藏 computed；四分支（骨架→失败空态+重试→暂无文章→列表）；修复失败标记不重置与空数组误判
- [x] Task 6: 验证
  - [x] 6.1 blog-frontend `npm run build` 成功（12.86s）
  - [x] 6.2 blog-admin `npm run build` 成功（17.52s）
  - [x] 6.3 grep 验证：紫色系与 #409eff 清零；admin 视图 :deep 重复清零；app 无 10/14px 游离圆角；icons.svg 紫色清零（Read 磁盘验证）
  - [x] 6.4 checklist.md 全项复验通过

# Task Dependencies
- Task 1、2、3、4 相互独立可并行（Task 2.1 依赖 Task 1 的品牌图形基准，可先以 frontend LogoIcon.vue 为准直接开工）
- Task 5 独立
- Task 6 依赖 Task 1-5 全部完成
