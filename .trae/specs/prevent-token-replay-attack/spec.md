# 请求签名防重放（API Signing）Spec

## Why

当前 JWT Token 可以从浏览器 DevTools Network 面板直接复制，粘贴到 Apifox/Postman 等外部工具即可访问任意接口。这是因为 Token 本身就是完整的身份凭证，不绑定任何额外上下文。

需要在不改变 JWT Token 代码的前提下，增加一层**请求签名验证**，使仅复制 Token 无法在外部工具中成功调用接口。

## What Changes

- **新增请求签名过滤器** `RequestSignatureFilter`：对 `/user/**` 和 `/admin/**` 路由验证请求签名，不通过则返回 403
- **签名算法**：`HMAC-SHA256(secret, method + uri + timestamp + nonce)` → Hex 编码
- **防重放**：时间戳超过 60 秒拒绝；Nonce 存入 Redis，重复使用拒绝
- **前端签名拦截器**：blog-admin (axios) 和 blog-app (uni-app) 在请求拦截器中自动添加 `X-Timestamp`、`X-Nonce`、`X-Signature` 头
- **新增配置项**：`security.signing.secret`、`security.signing.enabled`、`security.signing.timestamp-window-seconds`

## Impact

- Affected code:
  - 后端：新增 `RequestSignatureFilter.java`、`SignatureProperties.java`；修改 `SecurityConfig.java` 注册过滤器；修改 `application.yaml` 增加配置
  - 前端 admin：修改 `request.js` 增加签名拦截器
  - 前端 app：修改 `request.js` 增加签名逻辑
- **不影响 JWT Token 代码**：`JwtUtils.java`、`JwtAuthenticationFilter.java`、`AuthController.java` 零改动
- **不影响公开接口**：`/auth/**`、`/portal/**` 不需要签名

## ADDED Requirements

### Requirement: 请求签名验证

系统应对所有需要认证的接口（`/user/**`、`/admin/**`）验证请求签名，防止 Token 被提取后在非授权客户端中使用。

#### Scenario: 正常请求（前端自动签名）
- **WHEN** 前端发起请求，携带 `Authorization: Bearer <token>` + `X-Timestamp` + `X-Nonce` + `X-Signature`
- **AND** 时间戳在有效窗口内（默认 60 秒）
- **AND** Nonce 未被使用过
- **AND** 签名验证通过
- **THEN** 请求正常通过，返回业务数据

#### Scenario: 仅复制 Token 到外部工具
- **WHEN** 攻击者从浏览器复制 `Authorization` 头到 Apifox，但不携带 `X-Timestamp` / `X-Nonce` / `X-Signature`
- **THEN** 返回 HTTP 403，消息："请求签名缺失，拒绝访问"

#### Scenario: 签名错误
- **WHEN** 请求携带了签名相关头，但 `X-Signature` 与服务端计算的值不匹配（使用了错误的密钥或篡改了参数）
- **THEN** 返回 HTTP 403，消息："请求签名验证失败"

#### Scenario: 时间戳过期
- **WHEN** `X-Timestamp` 与服务端当前时间差超过配置的窗口（默认 60 秒）
- **THEN** 返回 HTTP 403，消息："请求已过期"

#### Scenario: Nonce 重放
- **WHEN** 攻击者复制了浏览器请求的全部头（含签名），再次发送
- **AND** 该 Nonce 已在 Redis 中存在（已被原请求消费）
- **THEN** 返回 HTTP 403，消息："重复请求，拒绝访问"

#### Scenario: 签名功能关闭
- **WHEN** `security.signing.enabled = false`
- **THEN** 过滤器跳过所有验证，行为与当前完全一致（用于开发调试或临时降级）

### Requirement: 前端自动签名

前端应在每个需要认证的请求中自动附加签名头，对用户透明。

#### Scenario: axios 请求拦截器签名
- **WHEN** blog-admin 发起请求
- **THEN** 请求拦截器自动计算并添加 `X-Timestamp`、`X-Nonce`、`X-Signature` 头

#### Scenario: uni-app 请求签名
- **WHEN** blog-app 发起请求
- **THEN** 请求方法自动计算并添加签名相关头
