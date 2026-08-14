# 登录图形验证码 Spec

## Why
当前登录链路仅有 IP 限流 + 账户锁定，均为"事后计数"型防护，自动化脚本可持续尝试直至触发阈值。在限流之前增加一层图形验证码人机验证，可在消耗限流额度之前拦截绝大多数自动化攻击。

## What Changes
- 后端新增 `CaptchaService`：基于 Hutool `CaptchaUtil`（hutool-all 5.8.22 已有依赖，零新增）生成图形验证码，存 Redis（TTL 60s，一次性消费）
- `AuthController` 新增 `GET /auth/captcha` 接口，返回 `{captchaId, image(base64 data URI)}`
- `AuthController.login` 校验顺序调整为：①用户名空检查 → ②账户锁定检查 → **③验证码校验（新增）** → ④IP 限流 → ⑤认证
- `LoginRequest` 扩展 `captchaId`、`captchaCode` 字段
- `SecurityProperties` 新增 `captchaEnabled`（默认 true）开关，`application.yaml` 同步配置
- blog-admin 登录页（Login.vue）增加验证码输入框 + 图片（点击刷新）
- blog-app 登录页（pages/mine/login.vue）增加验证码输入框 + 图片
- 新增 `CaptchaServiceTest`；更新 `AuthControllerTest`（登录链路插入验证码后的 stub 与新场景）
- blog-frontend 无登录页，不改动

## Impact
- Affected specs: add-auth-unit-tests（AuthControllerTest 需同步更新）；prevent-token-replay-attack（登录链路顺序变化，不影响 Token 逻辑）
- Affected code:
  - 后端：新增 `service/CaptchaService.java`；修改 `controller/portal/AuthController.java`、`properties/SecurityProperties.java`、`resources/application.yaml`、`application-docker.yaml`
  - 前端：`blog-admin/src/views/Login.vue`、`blog-admin/src/api/auth.js`；`blog-app/pages/mine/login.vue`、`blog-app/common/api.js`
  - 测试：新增 `CaptchaServiceTest`，修改 `AuthControllerTest`
- **BREAKING**（接口层面）：`POST /auth/login` 请求体新增必填字段 `captchaId`/`captchaCode`（`captcha-enabled=false` 时可不传）；所有旧客户端登录将收到 400
- 需确认 `RequestSignatureFilter` 对未登录 GET 请求（/auth/captcha）的放行行为（现有 /portal GET 接口可匿名访问，预期无阻塞；实现时验证）

## ADDED Requirements

### Requirement: 验证码生成接口
系统 SHALL 提供 `GET /auth/captcha` 接口，返回 `{captchaId, image}`，image 为 `data:image/png;base64,...` 格式。

#### Scenario: 获取验证码
- **WHEN** 客户端请求 `GET /auth/captcha`
- **THEN** 生成 UUID 作为 captchaId，生成 4 位字符图形验证码图片（130x40）
- **AND** 以 `captcha:{captchaId}` 为 Key 将验证码文本存入 Redis，TTL 60 秒
- **AND** 返回 code=200 与 captchaId、base64 图片

### Requirement: 登录验证码校验
`POST /auth/login` SHALL 在账户锁定检查之后、IP 限流之前校验验证码。

#### Scenario: 验证码校验通过
- **WHEN** captchaId 对应 Redis 记录存在且 captchaCode 匹配（忽略大小写）
- **THEN** 消费该验证码（删除 Key），继续执行 IP 限流与认证

#### Scenario: 验证码错误或过期
- **WHEN** code 为空 / captchaId 不存在（过期或已使用）/ 文本不匹配
- **THEN** 返回 `Result.error(400, "验证码错误或已过期")`，不执行 IP 限流、不累加失败计数

#### Scenario: 一次性消费
- **WHEN** 同一 captchaId 提交第二次（无论第一次成功与否）
- **THEN** 校验失败（Key 已删除）

#### Scenario: 开关关闭
- **WHEN** `security.login.captcha-enabled=false`
- **THEN** 跳过验证码校验，登录行为与现状一致

### Requirement: 前端登录页验证码交互
blog-admin 与 blog-app 登录页 SHALL 展示验证码图片，支持点击刷新，登录请求携带 captchaId/captchaCode。

#### Scenario: 加载与刷新
- **WHEN** 登录页挂载或用户点击验证码图片 / 登录失败后
- **THEN** 重新请求 `GET /auth/captcha` 更新图片与 captchaId，清空已输入的验证码

#### Scenario: 提交登录
- **WHEN** 用户点击登录
- **THEN** 请求体含 username、password、captchaId、captchaCode
- **AND** 收到"验证码错误或已过期"提示后自动刷新验证码

### Requirement: 单元测试
- 新增 `CaptchaServiceTest`：生成（图片格式/Redis 写入 TTL）、校验（正确/大小写不敏感/错误/过期/一次性消费）、开关关闭
- 更新 `AuthControllerTest`：登录链路 stub `captchaService.verify` 通过；新增验证码失败 400（不触限流）场景；空用户名 400 与锁定 423 场景不受验证码影响
- `mvn test` 全部通过

## MODIFIED Requirements

### Requirement: 登录接口请求体（原 username/password 两字段）
`LoginRequest` SHALL 包含 `username`、`password`、`captchaId`、`captchaCode` 四个字段；后两个字段仅在 `captcha-enabled=true` 时必填。
