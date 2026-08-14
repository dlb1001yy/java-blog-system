# 安全功能优化计划：密码加密 + JWT 刷新 + 登录限流

## 概述

经调查，**三项功能均已实现**，且实现质量较高。本次优化针对调查中发现的 6 个问题进行修复。

## 当前状态分析

### 1. 密码加密 — 已实现
- BCrypt 12 轮，所有写入路径均已加密
- 登录通过 Spring Security `AuthenticationManager` 校验

### 2. JWT 刷新 — 已实现
- 双 Token 架构：AccessToken (15min) + RefreshToken (7天)
- Refresh Token 通过 HTTP-only Cookie 下发
- Redis 黑名单 + 活跃 Refresh Token 集合管理

### 3. 登录限流 — 已实现
- IP 维度滑动窗口限流 (Lua 脚本)
- 用户名维度失败计数 + 账户锁定
- 通用 `@RateLimit` AOP 注解

---

## 发现的问题与优化方案

### 问题 1：Refresh Token 未轮换（安全风险）

**位置**: [AuthController.java](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/controller/portal/AuthController.java#L90-L108)

**问题**: `/auth/refresh` 端点签发新 AccessToken 但不轮换 RefreshToken。旧 RefreshToken 在 7 天内始终有效，一旦泄露无法通过轮换检测发现。

**修复方案**:
- `JwtUtils.generateRefreshToken()`: 为 Redis SET 设置 TTL（匹配 RefreshToken 有效期），解决内存泄漏
- `AuthController.refresh()`: 验证旧 RefreshToken 后，吊销旧 Token、签发新 RefreshToken、更新 Cookie
- `AuthController.login()`: 在响应体中同时返回 `refreshToken`（供移动端非浏览器客户端使用）
- `AuthController.refresh()`: 增加 `X-Refresh-Token` 请求头回退读取（兼容移动端无法使用 Cookie 的场景）

**修改文件**:
- `JwtUtils.java` — `generateRefreshToken()` 增加 SET 的 TTL；新增不写 Redis 的 token 构建方法用于轮换流程内部调用
- `AuthController.java` — `login()` 响应体增加 `refreshToken`；`refresh()` 实现轮换逻辑 + Header 回退
- `CookieUtils.java` — 无需修改（已有 `addRefreshCookie`）

### 问题 2：Refresh Token Redis SET 无 TTL（内存泄漏）

**位置**: [JwtUtils.java](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/utils/JwtUtils.java#L72-L76)

**问题**: `generateRefreshToken()` 将 token 加入 Redis SET 但不设置过期时间。用户不主动登出时，过期 token 永久残留在 Redis 中。

**修复方案**: 在 `generateRefreshToken()` 中，添加 token 后对 SET key 设置 `expire`，值为 `refreshTokenDays` 对应的秒数。每次添加新 token 时刷新 TTL。

### 问题 3：SQL 种子密码哈希无效

**位置**: [01-create_sql.sql](file:///d:/my-project/java-blog-system/blog-backend/sql/01-create_sql.sql#L152-L153)

**问题**: 种子管理员密码哈希 `$2a$10$Eblj1bNwQX5fZQvXQxQJZOVpO0ZQ7Q2LqXzQ8Q9Q0Q0Q0Q0Q0Q0Q` 不是合法 BCrypt 哈希（尾部为重复占位字符）。直接导入 SQL 时管理员无法登录。

**修复方案**: 生成 `admin123` 的合法 BCrypt 哈希（strength=12），替换 SQL 中的占位哈希。使用 `PasswordGenerator` 测试工具或代码生成。

### 问题 4：JWT 密钥硬编码（安全风险）

**位置**: [application.yaml](file:///d:/my-project/java-blog-system/blog-backend/src/main/resources/application.yaml#L47-L49)

**问题**: `jwt.secret` 直接硬编码在配置文件中，生产环境存在泄露风险。

**修复方案**: 改为环境变量优先模式：
```yaml
jwt:
  secret: ${JWT_SECRET:ThisIsASecureSecretKeyForJwtTokenGenerationAndValidationMakeItLongEnough123}
```
开发环境使用默认值，生产环境通过 `JWT_SECRET` 环境变量覆盖。

### 问题 5：废弃配置项残留

**位置**: [application.yaml](file:///d:/my-project/java-blog-system/blog-backend/src/main/resources/application.yaml#L50)

**问题**: `jwt.expiration: 86400000` (24小时) 已废弃，实际有效期由 `SecurityProperties` 的 `access-token-minutes` / `refresh-token-days` 控制。

**修复方案**: 删除 `jwt.expiration` 行，保留 `jwt.header` 和 `jwt.prefix`（仍被使用）。

### 问题 6：移动端（blog-app）缺少静默刷新

**位置**: [blog-app/common/request.js](file:///d:/my-project/java-blog-system/blog-app/common/request.js#L45-L51)

**问题**: Access Token 过期（15分钟）后，移动端直接跳转登录页，用户体验差。管理端（blog-admin）已实现静默刷新，但移动端未实现。

**修复方案**:
- 参考 blog-admin 的 `refreshTokenOnce()` 模式，实现并发去重的静默刷新
- 登录成功后，从响应体中提取 `refreshToken` 并存储到 `uni.getStorageSync`
- 收到 401 时，调用 `/auth/refresh`，通过 `X-Refresh-Token` 请求头传递 RefreshToken
- 刷新成功后更新存储的 AccessToken 和 RefreshToken，重试原请求
- 刷新失败时清除存储并跳转登录页

**修改文件**:
- `blog-app/common/request.js` — 增加静默刷新逻辑
- `blog-app/common/config.js` — 确认 `REFRESH_TOKEN_KEY` 常量已定义（已存在）

---

## 实施步骤

### Step 1: 后端 — JwtUtils 优化
1. `generateRefreshToken()`: 添加 Redis SET 的 `expire`（TTL = refreshTokenDays 对应秒数）
2. 确认 `revokeRefreshToken()` 已存在（无需修改）

### Step 2: 后端 — AuthController 优化
1. `login()`: 响应体增加 `refreshToken` 字段
2. `refresh()`: 
   - 增加 `X-Refresh-Token` Header 回退读取逻辑
   - 验证旧 Token → 吊销旧 Token → 签发新 RefreshToken → 更新 Cookie → 返回新 AccessToken + 新 RefreshToken

### Step 3: 后端 — 配置修复
1. `application.yaml`: JWT 密钥改为 `${JWT_SECRET:默认值}`
2. `application.yaml`: 删除废弃的 `jwt.expiration`
3. `sql/01-create_sql.sql`: 替换无效 BCrypt 哈希

### Step 4: 前端 — blog-app 静默刷新
1. `blog-app/common/request.js`: 实现静默刷新 + 并发去重
2. 确保登录页从响应体存储 refreshToken

---

## 假设与决策

1. **Refresh Token 轮换策略**: 采用"每次刷新都轮换"策略（非滑动窗口），旧 Token 立即吊销。安全性最高，且前端已有并发去重机制防止多请求同时刷新。
2. **移动端 Token 传递**: 非浏览器客户端通过 `X-Refresh-Token` Header 传递 RefreshToken，浏览器客户端继续使用 Cookie。双路径共存。
3. **SQL 哈希**: 使用 strength=12 生成（与 `SecurityProperties.bcryptStrength` 一致），版本前缀为 `$2a$12$`。
4. **不做的事**: 不修改 blog-admin 前端（已完善）；不引入第三方限流库（现有 Lua 实现已足够）；不增加密码强度验证（已有 `PasswordStrengthValidator`）。

## 验证步骤

1. **后端编译**: `mvn compile` 确认无编译错误
2. **Refresh Token 轮换测试**: 
   - 登录获取 refreshToken → 用旧 token 刷新 → 验证返回新 refreshToken → 再次用旧 token 刷新应失败
3. **Redis TTL 验证**: 登录后检查 `jwt:refresh:{username}` key 有 TTL
4. **SQL 验证**: 直接导入修复后的 SQL，确认 admin/admin123 可登录
5. **移动端刷新**: blog-app 中触发 401 后确认自动刷新不跳转登录页
