# 一次性 Token（Per-Request Token Rotation）实施计划

## 概述

将 Access Token 从"15分钟内可重复使用"改为"每次请求后立即作废，响应头返回新 Token"。
一个 Token 只能用于一次请求，用过即失效。

## 原理

```
客户端发送请求 (Authorization: Bearer T1)
         │
         ▼
JwtAuthenticationFilter:
  1. 校验 T1 → 有效？ → 否 → 401
  2. T1 已用过？(Redis 黑名单) → 是 → 401
  3. 设置 SecurityContext（认证通过）
  4. 将 T1 加入黑名单（立即作废）
  5. 生成新 Token T2，写入响应头 X-New-Token
         │
         ▼
Controller 正常处理业务，返回响应
         │
         ▼
前端响应拦截器:
  读取 X-New-Token → 更新 localStorage / Storage
  下一次请求使用 T2
```

## 修改文件清单

### 后端（3 个文件）

#### 1. `JwtUtils.java` — 新增 Token 轮换方法
路径: `blog-backend/src/main/java/com/dlbyy/blog/utils/JwtUtils.java`

新增方法：
```java
/**
 * 将已使用的 Token 加入黑名单（复用现有黑名单机制）
 */
public void markTokenUsed(String token) {
    addToBlacklist(token);  // 复用现有方法，TTL = token 剩余有效期
}
```

#### 2. `JwtAuthenticationFilter.java` — 核心逻辑
路径: `blog-backend/src/main/java/com/dlbyy/blog/security/JwtAuthenticationFilter.java`

在 `doFilterInternal` 中，认证通过后：
1. 将当前 Token 加入黑名单（立即作废）
2. 生成新 Access Token
3. 写入响应头 `X-New-Token`

需要注入 `HttpServletResponse`（已有参数）和 `JwtUtils`（已有）。

关键改动位置：第 57 行 `SecurityContextHolder.getContext().setAuthentication(authenticationToken);` 之后。

#### 3. `SecurityConfig.java` — CORS 暴露响应头
路径: `blog-backend/src/main/java/com/dlbyy/blog/config/SecurityConfig.java`

在 `corsConfigurationSource()` 中添加：
```java
configuration.addExposedHeader("X-New-Token");
```
否则浏览器前端的 axios 拿不到该响应头。

### 前端（2 个文件）

#### 4. `blog-admin/src/api/request.js` — 响应拦截器读取新 Token
路径: `blog-admin/src/api/request.js`

在响应拦截器的成功分支（第 42-48 行）中，读取 `X-New-Token` 并更新 localStorage：
```js
response => {
    const newToken = response.headers['x-new-token']
    if (newToken) {
        localStorage.setItem('admin_token', newToken)
    }
    // ...原有逻辑
}
```

#### 5. `blog-app/common/request.js` — 同上
路径: `blog-app/common/request.js`

在 `success` 回调中，读取响应头并更新 Storage。

## 并发请求限制说明

**重要**：此方案下并发请求会失败。例如页面同时发起 3 个 API 请求：
- 请求 A 用 T1 → 成功，T1 作废，响应返回 T2
- 请求 B 也用 T1（已发出）→ T1 已作废 → 401
- 请求 C 也用 T1 → 同上 → 401

前端已有的 401 重试机制（`refreshTokenOnce`）可以处理这种情况：
- 收到 401 后先检查 localStorage 中是否有更新的 Token（被兄弟请求的响应更新过）
- 如果有，用新 Token 重试
- 如果没有（所有并发请求的 Token 都一样），调用 `/auth/refresh` 获取新 Token

## 假设与决策

1. **复用黑名单机制**：不新建 Redis 结构，直接用 `addToBlacklist()`，TTL 自动匹配 Token 剩余有效期（最多 15 分钟后自动清理）。
2. **Refresh Token 不受影响**：`/auth/refresh` 端点使用 RefreshToken（Cookie/Header），不经过 `JwtAuthenticationFilter` 的轮换逻辑（`/auth/**` 是 permitAll）。
3. **Token 生成方式不变**：新 Token 仍通过 `jwtUtils.generateAccessToken(username)` 生成，格式和有效期不变。
4. **`X-New-Token` 响应头**：所有认证接口的响应都会携带此头。前端无条件读取并更新。

## 验证步骤

1. **后端编译**：`mvn compile`
2. **单请求测试**：登录 → 获取 T1 → 请求 /user/info (T1) → 成功 + 响应头有 T2 → 再用 T1 请求 → 401
3. **前端测试**：blog-admin 登录后操作页面，确认每次请求后 Token 自动更新
4. **并发测试**：确认页面正常加载（依赖 401 重试 + localStorage 更新）
