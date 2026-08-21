# Tasks

- [x] Task 1: 后端数据库与实体扩展
  - [x] SubTask 1.1: 新建 `blog-backend/sql/08-resume-share-audit.sql`：ALTER TABLE resume_info ADD status/audit_remark；CREATE TABLE resume_share（含 uk_share_token）；并在 `sql/create_sql.sql` 末尾追加同样内容
  - [x] SubTask 1.2: `ResumeInfo.java` 新增 `status` Integer、`auditRemark` String
  - [x] SubTask 1.3: 新建 `ResumeShare.java` 实体（id/resumeId/userId/shareToken/expireTime/createTime）
  - [x] SubTask 1.4: 新建 `ResumeShareMapper extends BaseMapper<ResumeShare>`
  - [x] SubTask 1.5: 不执行 mvn 命令，由用户手动执行 `mvn compile` 验证（仅静态检查代码无误）

- [x] Task 2: 后端 Service 层
  - [x] SubTask 2.1: `ResumeInfoService` 新增 `pageAll(page, size, keyword, status)`：分页查所有用户简历（userId 非空），keyword 匹配用户名/姓名（联 user 表或查后组装 userName），status 过滤
  - [x] SubTask 2.2: `ResumeInfoService` 新增 `audit(id, status, remark)`：更新 status 与 auditRemark
  - [x] SubTask 2.3: `mySave` 修改：upsert 时服务端强制 `setStatus(0)`、清空 auditRemark（不信任客户端传入 status）
  - [x] SubTask 2.4: 新建 `ResumeShareService(+Impl)`：`create(userId, expireMinutes)`（校验简历归属且 status=1 否则 BusinessException「简历未审核通过，无法分享」；RandomUtil.randomString(32) 生成 token；expireMinutes 为 null 则 expireTime=null）；`listMine(userId)`；`revoke(id, userId)`；`viewByToken(token)`（校验存在、未过期、简历 status=1，返回 ResumeInfo，失效抛 404「链接已失效或不存在」）

- [x] Task 3: 后端 Controller 层
  - [x] SubTask 3.1: `PortalResumeController`：`GET /portal/resume/share/{token}`（匿名）调 viewByToken
  - [x] SubTask 3.2: `PortalResumeController`：`POST /portal/resume/mine/share`（body {expireMinutes}）、`GET /portal/resume/mine/shares`、`DELETE /portal/resume/mine/share/{id}`（登录校验同现有 currentUserId 模式）
  - [x] SubTask 3.3: `PortalResumeController` 修改 `GET /portal/resume/{userId}`：仅返回 status=1 的用户简历；站长简历接口（无参 GET）保持返回（userId IS NULL 记录直接返回，不受 status 限制）
  - [x] SubTask 3.4: `AdminResumeController`：`GET /admin/resume/page`、`GET /admin/resume/detail/{id}`、`PUT /admin/resume/audit/{id}`，均加 `@Admin` 注解
  - [x] SubTask 3.5: 不执行 mvn 命令，由用户手动执行 `mvn compile` 验证

- [x] Task 4: 前台 blog-frontend 编辑页分享功能
  - [x] SubTask 4.1: 新建 `src/api/resume.js`：getMyResume/saveMyResume/getResumeByToken/createShare/getMyShares/revokeShare（从 article.js 中迁移简历接口亦可，保持 article.js 原导出不动，ProfileResume/Resume 改引用新文件）
  - [x] SubTask 4.2: `ProfileResume.vue` 顶部审核状态条：el-tag（0待审核 warning/1通过 success/2拒绝 danger）+ status=2 时显示 auditRemark
  - [x] SubTask 4.3: `ProfileResume.vue` 新增「分享链接」卡片：时长 el-select（永久/30分钟/1天/7天/1个月/3个月/1年/自定义）+ 自定义时 el-input-number 天数；「生成分享链接」按钮；生成后显示完整链接 + 一键复制（navigator.clipboard + ElMessage）
  - [x] SubTask 4.4: 「我的分享」列表：token、创建时间、过期时间（永久/时间/已过期 tag）、撤销按钮（ElMessageBox.confirm 后调 revokeShare）

- [x] Task 5: 前台分享查看页与路由
  - [x] SubTask 5.1: `router/index.js` 新增 `/resume/share/:token` → Resume.vue（不需要登录）
  - [x] SubTask 5.2: `Resume.vue`：优先判断 `route.params.token`，调 getResumeByToken；接口 404 时显示「链接已失效或不存在」空状态；token 模式下隐藏编辑相关入口

- [x] Task 6: 后台 blog-admin 简历管理页
  - [x] SubTask 6.1: `api/resume.js` 新增 getPage(params)/getDetail(id)/audit(id, status, remark)
  - [x] SubTask 6.2: 改造 `ResumeEdit.vue` 为列表页：搜索栏（关键词 + 状态 el-select 全部/待审核/已通过/已拒绝）、el-table（ID/用户名/姓名/求职岗位/审核状态 tag/更新时间/操作）、el-pagination、操作列「详情」「通过」「拒绝」（实现为新建 ResumeManage.vue，保留原 ResumeEdit.vue 站长简历编辑）
  - [x] SubTask 6.3: 详情 el-drawer：展示基本信息 + 解析 skills/workExperience/projects/education/certificates JSON 渲染（简单分组文本列表即可）
  - [x] SubTask 6.4: 拒绝弹窗：el-dialog + textarea 填原因，确认调 audit(id, 2, remark)；通过直接 audit(id, 1, '')（ElMessageBox.confirm）
  - [x] SubTask 6.5: `router/index.js` 简历管理路由指向改造后页面（如沿用 resume 路径则无需改）（新增 resumeManage 路由 + Sidebar 菜单项）

- [ ] Task 7: 验证
  - [ ] SubTask 7.1: 不执行任何 mvn/npm 命令；仅确认代码无 VSCode 诊断错误，编译与构建由用户手动执行
  - [ ] SubTask 7.2: 用户执行 `sql/08-resume-share-audit.sql` 后重启后端
  - [ ] SubTask 7.3: 手工冒烟：前台登录用户保存简历（变待审核）→ 生成分享被拒 → 后台审核通过 → 生成 30 分钟分享链接 → 隐身窗口打开可见 → 撤销后打开显示失效

# Task Dependencies
- Task 1 → Task 2 → Task 3（实体/Service/Controller 顺序依赖）
- Task 4、Task 5 依赖 Task 3（前台接口）；Task 6 依赖 Task 3（后台接口）
- Task 4 / Task 5 / Task 6 相互独立，可并行
- Task 7 依赖全部
