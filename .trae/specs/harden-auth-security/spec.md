# Harden Auth & Security Spec

## Why
需求文档 §2.1 要求的权限与安全基线经审计有 3 项缺口：无公开注册接口（用户名/邮箱）、`/admin/**` 仅登录校验无 Admin 角色隔离（普通用户可越权调用全部管理接口，最严重）、XSS 过滤未闭环（文章 Markdown 内容、考试答案、简历内容入库不过滤，前端 html:true 直接 v-html）。

## What Changes
- **注册**：新增 `POST /portal/auth/register`（username/email/password/confirmPassword），密码格式沿用 PasswordStrengthValidator，邮箱+用户名唯一校验，BCrypt 入库，默认角色 user
- **登录增强**：支持用户名或邮箱登录
- **管理接口角色隔离**：SecurityConfig 中 `/admin/**` 从 `authenticated()` 收紧为 `hasRole("ADMIN")`（统一入口校验，一处配置覆盖所有管理接口）
- **XSS 过滤闭环**：文章内容（管理端保存与修改）、简历内容、考试主观题答案在入库前经 JsoupXssUtil（Markdown 场景用保留代码块/语法的宽松白名单）清洗；JsoupXssUtil Safelist 去掉 `style` 属性
- **门户前端注册页**：blog-frontend 新增注册页（用户名/邮箱/密码/确认密码），登录页加"去注册"入口
- 不改：JWT refresh 机制（已达标）、BCrypt（已达标）、门户数据归属校验（已达标）

## Impact
- Affected specs: align-eduhub-requirements、refactor-to-multi-function-site
- Affected code:
  - 后端：`AuthController`、`UserService/Impl`（register、邮箱登录）、`SecurityConfig`（/admin/** hasRole）、`AdminArticleController`、`PortalResumeController` 或 Service（内容清洗）、`ExamServiceImpl`（主观题答案清洗）、`JsoupXssUtil`
  - 前端：blog-frontend `Register.vue`（新增）、`Login.vue`、`router/index.js`、api
- 无数据库变更（sys_user 已有 username/email 字段）

## ADDED Requirements

### Requirement: 公开注册
系统 SHALL 提供公开注册接口 `POST /portal/auth/register`，接受用户名/邮箱/密码/确认密码；用户名与邮箱全库唯一；密码经 PasswordStrengthValidator 校验且 BCrypt 存储；默认角色 user；注册成功可直接登录。

#### Scenario: 注册成功
- **WHEN** 提交合法的用户名、邮箱、两次一致的强密码
- **THEN** 创建 user 角色账号，返回成功，登录可用

#### Scenario: 邮箱已存在
- **WHEN** 使用已被占用的邮箱注册
- **THEN** 返回"邮箱已被注册"，不创建账号

#### Scenario: 两次密码不一致
- **WHEN** confirmPassword != password
- **THEN** 返回明确错误提示

### Requirement: 邮箱登录
登录接口 SHALL 接受用户名或邮箱作为账号字段。

#### Scenario: 邮箱登录
- **WHEN** 用户以注册邮箱 + 密码登录
- **THEN** 认证成功并签发双 Token

### Requirement: 管理接口 Admin 角色强制校验
`/admin/**` SHALL 在 SecurityConfig 强制 `hasRole("ADMIN")`，普通用户（含已登录）访问返回 403。

#### Scenario: 普通用户越权
- **WHEN** role=user 的登录用户请求任意 `/admin/**` 接口
- **THEN** 返回 403，无副作用

### Requirement: 富文本 XSS 过滤闭环
文章内容、简历富文本字段、考试主观题答案 SHALL 在入库前经 Jsoup 白名单清洗，去除 script/iframe/on* 事件/style 等危险内容，同时保留 Markdown 所需的常规标签与代码块。

#### Scenario: 存储型 XSS
- **WHEN** 文章内容含 `<script>alert(1)</script>` 或 `<img onerror=...>`
- **THEN** 入库内容中 script 与事件属性被移除，前端渲染不执行

### Requirement: 门户注册页
blog-frontend SHALL 提供注册页（路由 `/register`），登录页有入口互跳，注册成功后引导登录。

## MODIFIED Requirements

### Requirement: JsoupXssUtil（既有）
Safelist 移除 `style` 属性白名单，防 CSS 注入；其余保持。
