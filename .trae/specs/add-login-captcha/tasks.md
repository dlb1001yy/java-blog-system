# Tasks
- [x] Task 1: 后端 CaptchaService 与配置项
  - [x] SubTask 1.1: 新增 `service/CaptchaService.java`：Hutool `CaptchaUtil.createLineCaptcha(130, 40, 4, 8)` 生成，UUID 作 captchaId，`captcha:{id}` 存 Redis（TTL 60s，值 `captchaEnabled` 可配），`generate()` 返回 captchaId+base64 data URI，`verify(captchaId, code)` 用 `opsForValue().getAndDelete()` 一次性消费后忽略大小写比对；`captchaEnabled=false` 时 verify 直接返回 true
  - [x] SubTask 1.2: `SecurityProperties` 增加 `captchaEnabled=true` 字段；`application.yaml` 与 `application-docker.yaml` 的 `security.login` 块增加 `captcha-enabled: true`（docker 文件无 security 块，继承主配置，无需添加）
- [x] Task 2: AuthController 集成验证码
  - [x] SubTask 2.1: 新增 `GET /auth/captcha` 端点（已确认 RequestSignatureFilter 仅校验含 /user/、/admin/ 的 URI，匿名 GET 放行，无需改动）
  - [x] SubTask 2.2: `LoginRequest` 增加 `captchaId`、`captchaCode` 字段
  - [x] SubTask 2.3: login 方法在锁定检查（isLocked 之后）与 IP 限流（tryAcquireIp 之前）之间插入 `captchaService.verify`，失败返回 `Result.error(400, "验证码错误或已过期")`
- [x] Task 3: 后端单元测试
  - [x] SubTask 3.1: 新增 `CaptchaServiceTest`（Mock StringRedisTemplate/ValueOperations + 真实 SecurityProperties）：generate 返回 base64 前缀与 Redis setex 60s；verify 正确通过、大小写不敏感、错误拒绝、Key 不存在拒绝、getAndDelete 一次性消费；开关关闭 verify 恒 true 且不触 Redis（共 8 用例）
  - [x] SubTask 3.2: 更新 `AuthControllerTest`：新增 `@Mock CaptchaService` 并在 6 个走到限流/认证的用例中 stub verify 返回 true；新增"验证码错误→400 且 tryAcquireIp/onLoginFailure 不被调用"与"锁定 423 优先于验证码"用例（14→16 用例）
- [x] Task 4: blog-admin 登录页验证码
  - [x] SubTask 4.1: `api/auth.js` 增加 `getCaptcha()`（GET /auth/captcha）
  - [x] SubTask 4.2: `Login.vue` 密码框与登录按钮之间插入验证码 el-form-item（输入框 + img 点击刷新），loginForm 增加 captchaId/captchaCode，rules 增加必填校验；onMounted 拉取验证码；登录失败后刷新验证码并清空输入（store 透传无需改）
- [x] Task 5: blog-app 登录页验证码
  - [x] SubTask 5.1: `common/api.js` 增加 `getCaptcha()`
  - [x] SubTask 5.2: `pages/mine/login.vue`：表单区域增加验证码输入框 + image 组件（:src 绑定 base64，点击刷新），form 增加 captchaId/captchaCode，提交前非空校验，登录失败后刷新验证码
- [x] Task 6: 验证与文档
  - [x] SubTask 6.1: blog-backend 执行 `mvn test`，全部通过（Tests run: 56, Failures: 0, Errors: 0，BUILD SUCCESS）
  - [x] SubTask 6.2: blog-admin 执行 `npm run build` 确认编译通过（built in 16.13s）
  - [x] SubTask 6.3: README.md 接口约定表补充 `/auth/captcha` 与登录验证码说明；「单元测试」章节覆盖范围表补充 `CaptchaServiceTest`（56 用例）；项目开发文档合集.md 新增「十二、登录图形验证码」章节

# Task Dependencies
- Task 2 依赖 Task 1（CaptchaService 先存在）
- Task 3 依赖 Task 1+2；Task 4/5 依赖 Task 2（接口定型）但与 Task 3 可并行
- Task 6 依赖全部前置任务
