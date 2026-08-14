# 认证模块单元测试覆盖 Spec

## Why
`LoginAttemptService`（IP 限流 + 账户锁定）、`JwtUtils`（双 Token 签发/校验/黑名单/吊销）、`AuthController`（登录/刷新/登出）是系统安全核心链路，目前没有任何单元测试，且 `blog-backend/pom.xml` 缺少测试依赖。回归风险高，无法验证安全逻辑正确性。

## What Changes
- 在 `blog-backend/pom.xml` 中新增 `spring-boot-starter-test`（test scope，提供 JUnit 5 / Mockito / AssertJ / spring-test Mock 对象）
- 新增 `LoginAttemptServiceTest`：纯 Mockito 单元测试，Mock `StringRedisTemplate`/`ValueOperations`/`DefaultRedisScript`/`AlertNotifier`，使用真实 `SecurityProperties`
- 新增 `JwtUtilsTest`：Mock `RedisUtils`/`StringRedisTemplate`/`SetOperations`，通过 `ReflectionTestUtils` 注入 ≥64 字节的 HS512 密钥，真实签发/解析 Token 验证往返
- 新增 `AuthControllerTest`：Mock 全部 5 个协作者（`AuthenticationManager`/`JwtTokenProvider`/`JwtUtils`/`LoginAttemptService`/`CookieUtils`），使用 `MockHttpServletRequest/Response` 直接调用 Controller 方法
- 不修改任何生产代码

## Impact
- Affected specs: prevent-token-replay-attack（Refresh Token 轮换逻辑将被覆盖）
- Affected code: `blog-backend/pom.xml`、`blog-backend/src/test/java/com/dlbyy/blog/**`（仅新增文件）
- 所有测试均为纯单元测试，不启动 Spring 上下文、不依赖 Redis/MySQL

## ADDED Requirements

### Requirement: 测试依赖就绪
构建系统 SHALL 在 `blog-backend` 模块提供 JUnit 5 + Mockito + AssertJ + spring-test 测试能力。

#### Scenario: 依赖添加
- **WHEN** 查看 `blog-backend/pom.xml`
- **THEN** 存在 `spring-boot-starter-test` 依赖且 scope 为 test

### Requirement: LoginAttemptService 单元测试
测试 SHALL 覆盖以下场景（Mock Redis，断言交互与返回值）：

#### Scenario: tryAcquireIp
- **WHEN** Lua 脚本返回 1 → 返回 true 且不触发告警
- **WHEN** Lua 脚本返回 0 → 返回 false 且调用 `notifyIpRateLimited`
- **WHEN** 返回 null → 返回 false
- **WHEN** ip 为 null/空 → 使用 "unknown" 作为 Redis Key

#### Scenario: isLocked
- **WHEN** 锁定 Key 不存在 → false
- **WHEN** 过期时间戳在未来 → true
- **WHEN** 过期时间戳已过去 → false 且删除该 Key

#### Scenario: getRemainingLockMillis
- **WHEN** Key 不存在 → 0；未来 → 正数；过去 → 0

#### Scenario: onLoginFailure
- **WHEN** 失败次数 < 阈值 → 返回 0，不写锁定 Key，不告警
- **WHEN** 首次失败（count==1）→ 为失败计数 Key 设置 TTL（lockMinutes*60+60 秒）
- **WHEN** 失败次数 ≥ 阈值 → 写锁定 Key（值=过期时间戳，TTL=lockMillis），触发 `notifyAccountLocked`，返回 lockMillis > 0
- **WHEN** increment 返回 null → 按 1 处理

#### Scenario: onLoginSuccess
- **WHEN** 登录成功 → 删除失败计数 Key 与锁定 Key

### Requirement: JwtUtils 单元测试
测试 SHALL 使用真实密钥完成 Token 签发-解析往返，覆盖：

#### Scenario: Token 生成与类型区分
- **WHEN** 生成 AccessToken → `isAccessToken` true、`isRefreshToken` false、subject 为用户名、有效期≈accessTokenMinutes
- **WHEN** 生成 RefreshToken → 类型为 refresh，且写入 Redis Set 并设置 TTL

#### Scenario: Token 校验
- **WHEN** 合法 Token → `validateToken` true
- **WHEN** 篡改/乱码/错误密钥 Token → false（不抛异常）
- **WHEN** Token 在黑名单 → false

#### Scenario: Refresh Token 有效性
- **WHEN** 签名有效且在用户 refresh 集合中 → `isValidRefreshToken` true
- **WHEN** 不在集合中（已吊销）→ false
- **WHEN** 传入 AccessToken 或无效 Token → false

#### Scenario: 吊销与黑名单
- **WHEN** `revokeAllRefreshTokens` → 删除 Redis Set Key
- **WHEN** `revokeRefreshToken` → 从 Set 中移除该 Token
- **WHEN** `addToBlacklist` 且剩余有效期 > 0 → 写入黑名单（TTL=剩余毫秒）
- **WHEN** `addToBlacklist` 且已过期 → 不写黑名单

### Requirement: AuthController 单元测试
测试 SHALL 覆盖三个端点的全部分支：

#### Scenario: login
- **WHEN** 用户名为空/空白 → code 400，不触发任何安全检查
- **WHEN** 账户被锁定 → code 423（含分钟数提示），不调用认证
- **WHEN** IP 被限流 → code 429
- **WHEN** 认证成功 → code 200，返回 accessToken/refreshToken/username，调用 `onLoginSuccess`，下发 Cookie
- **WHEN** 抛出 BadCredentialsException 且未达锁定阈值 → code 401，调用 `onLoginFailure`
- **WHEN** 抛出 BadCredentialsException 且返回 lockMillis>0 → code 423
- **WHEN** 抛出其他异常 → code 401
- **WHEN** 请求带 X-Forwarded-For → 取第一个 IP 传给 `tryAcquireIp`

#### Scenario: refresh
- **WHEN** Cookie 与 X-Refresh-Token 头均缺失 → 抛 BusinessException
- **WHEN** Token 无效/已吊销 → 清除 Cookie 并抛 BusinessException
- **WHEN** Cookie 缺失但 Header 存在且有效 → 走 Header 兜底
- **WHEN** Token 有效 → 吊销旧 Token、签发新双 Token、更新 Cookie、code 200

#### Scenario: logout
- **WHEN** 带 Bearer Token → 加入黑名单、吊销该用户全部 refresh token、清除 Cookie
- **WHEN** 不带 Authorization 头 → 仍返回成功并清除 Cookie

### Requirement: 测试全部通过
- **WHEN** 执行 `mvn test -Dtest="LoginAttemptServiceTest,JwtUtilsTest,AuthControllerTest"`
- **THEN** 全部测试通过，构建成功
