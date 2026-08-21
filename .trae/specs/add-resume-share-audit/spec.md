# 简历分享与审核功能 Spec

## Why
当前每个用户已有一份简历（`resume_info` 表，`uk_resume_user` 唯一键），前台 `/portal/resume/{userId}` 匿名可查任意用户简历且无可见性控制；无分享链接机制，也无管理端审核。需求：用户可生成带过期时间的分享链接（永久/30分钟/数天/数月/数年等自定义时长），且简历须经管理员审核通过后才能分享；`blog-admin` 需新增简历列表、详情与审核功能。

## What Changes

### 后端 `blog-backend`
- **数据库（增量 SQL，追加到 `sql/create_sql.sql` 末尾，并提供独立迁移文件 `sql/08-resume-share-audit.sql`）**：
  - `resume_info` 新增 `status` tinyint DEFAULT 0（0待审核 1通过 2拒绝）、`audit_remark` varchar(200)（审核备注/拒绝原因）
  - 新表 `resume_share`：
    ```sql
    CREATE TABLE resume_share (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      resume_id BIGINT NOT NULL,
      user_id BIGINT NOT NULL,
      share_token VARCHAR(64) NOT NULL,
      expire_time DATETIME NULL COMMENT 'NULL=永久',
      create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
      UNIQUE KEY uk_share_token (share_token),
      KEY idx_share_resume (resume_id)
    ) COMMENT '简历分享链接';
    ```
- **实体**：`ResumeInfo` 加 `status`/`auditRemark`；新增 `ResumeShare` 实体（@TableName("resume_share")）。
- **Service**：`ResumeInfoService` 新增 `pageAll(page,size,keyword,status)`（联查用户名）、`audit(id,status,remark)`；新增 `ResumeShareService`：`create(userId, expireMinutes)`（校验简历属于该用户且 `status=1`，否则 403「简历未审核通过，无法分享」；token 用 Hutool `RandomUtil.randomString(32)`）、`viewByToken(token)`（校验 token 存在、未过期、简历 status=1，返回简历数据；过期/不存在抛 404）。
- **PortalResumeController**：
  - `GET /portal/resume/share/{token}`（匿名，放行路径已覆盖 `/portal/**`）按 token 查看简历
  - `POST /portal/resume/mine/share`（登录）body `{ expireMinutes: number|null }`（null=永久）→ 返回 `{ token, url, expireTime }`
  - `GET /portal/resume/mine/shares`（登录）我的分享列表（含过期状态）
  - `DELETE /portal/resume/mine/share/{id}`（登录）撤销分享
  - **保存/更新简历后自动将 status 重置为 0（待审核）**（`mySave` 内服务端强制，不信任客户端）
- **AdminResumeController**：
  - `GET /admin/resume/page?page=&size=&keyword=&status=`（@Admin）分页列表（返回 userName）
  - `GET /admin/resume/detail/{id}`（@Admin）简历详情
  - `PUT /admin/resume/audit/{id}?status=&remark=`（@Admin）审核通过/拒绝
- **Dashboard**（可选，本轮不做，避免扩散）。

### 前台 `blog-frontend`
- **ProfileResume.vue（我的简历编辑页）**：
  - 顶部显示当前审核状态 el-tag（待审核/已通过/已拒绝+拒绝原因）
  - 新增「分享」区：过期时长选择（el-select：永久、30分钟、1天、7天、1个月、3个月、1年、自定义天数 el-input-number）→「生成分享链接」按钮
  - 生成后展示链接（`${origin}/resume/share/${token}`）+ 复制按钮 + 我的分享列表（token、创建时间、过期时间/永久、状态、撤销按钮）
- **路由**：`/resume/share/:token` → Resume.vue（新增分支：有 token 时调 `getResumeByToken`）
- **api/article.js**（或新建 api/resume.js 更清晰）：新增 `getResumeByToken(token)`、`createShare(data)`、`getMyShares()`、`revokeShare(id)`
- **Resume.vue**：支持按 token 加载并隐藏编辑入口。

### 后台 `blog-admin`
- **api/resume.js**：新增 `getPage(params)`、`getDetail(id)`、`audit(id,status,remark)`
- **ResumeEdit.vue 改造为 ResumeManage.vue 列表页**（或新增列表页 + 复用详情抽屉）：
  - 表格列：ID、用户名、姓名、求职岗位、审核状态 tag、更新时间、操作（详情、通过、拒绝）
  - 搜索：关键词（用户名/姓名）、状态筛选
  - 详情：el-drawer 展示简历完整内容（复用现有结构化展示或 JSON 字段渲染）
  - 拒绝时 el-dialog 填写拒绝原因
- 路由菜单标题改为「简历管理」，含列表页。

## Impact
- Affected specs: `refactor-resume-management`（复用其实体与结构化字段，无破坏）
- Affected code:
  - 后端：`ResumeInfo.java`、新增 `ResumeShare.java`、`ResumeInfoService(+Impl)`、新增 `ResumeShareService(+Impl)`、`PortalResumeController`、`AdminResumeController`、`sql/08-resume-share-audit.sql`
  - 前台：`ProfileResume.vue`、`Resume.vue`、`router/index.js`、`api/article.js`
  - 后台：`resume.js`、`ResumeEdit.vue`→列表+详情、`router/index.js`
- **BREAKING（行为变更）**：`/portal/resume/{userId}` 任意匿名可查改为需 `status=1` 且仅返回审核通过的简历（未通过返回 null/404），站长简历（userId 为 null）保持原逻辑返回。
- 兼容：resume_info 增量加列默认 0；存量站长简历（userId NULL）在查询逻辑中按 status=1 处理以保证首页简历仍可展示。

## ADDED Requirements

### Requirement: 简历审核
系统 SHALL 对用户简历实施审核管控：保存后 status 重置为待审核；仅管理员可审核；未通过审核的简历无法分享、无法被匿名查看。

#### Scenario: 保存后重新进入待审核
- **WHEN** 已通过审核的用户修改并保存简历
- **THEN** status 被服务端重置为 0，前台显示「待审核」

#### Scenario: 管理员审核
- **WHEN** 管理员在后台简历列表点击「通过」或「拒绝」并填写原因
- **THEN** status 更新为 1/2，用户侧同步显示状态与拒绝原因

### Requirement: 简历分享链接
系统 SHALL 支持用户为已审核通过的简历生成带过期时间的分享链接，token 不可猜测，过期或撤销后链接失效。

#### Scenario: 生成限时分享
- **WHEN** 简历 status=1 时用户选择「30分钟」生成分享
- **THEN** 返回唯一 token 与链接 `/resume/share/{token}`，30 分钟后访问返回 404/失效提示

#### Scenario: 未审核不可分享
- **WHEN** 简历 status≠1 时用户点击生成分享链接
- **THEN** 返回错误提示「简历未审核通过，无法分享」

#### Scenario: 匿名访问分享
- **WHEN** 未登录访客打开分享链接且未过期
- **THEN** 免登录看到完整简历内容；过期/已撤销则显示「链接已失效」

#### Scenario: 撤销分享
- **WHEN** 用户在我的分享列表点「撤销」
- **THEN** 该 token 立即失效

### Requirement: 管理端简历管理
blog-admin SHALL 提供所有用户简历的分页列表（关键词/状态筛选）、详情查看与审核操作。

#### Scenario: 查看简历列表
- **WHEN** 管理员打开简历管理页
- **THEN** 分页展示各用户简历（用户名、姓名、岗位、状态、更新时间）

## MODIFIED Requirements

### Requirement: 简历匿名查看可见性
`GET /portal/resume/{userId}` SHALL 仅返回审核通过（status=1）的用户简历；站长简历（userId 为 NULL）保持公开。新增 `GET /portal/resume/share/{token}` 匿名分享入口。

## REMOVED Requirements
无
