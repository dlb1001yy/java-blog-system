# 计划：滑动续期——每次后台请求自动重置 AccessToken 有效期（并修复 HTTP 下刷新失效）

## 问题分析

用户诉求：**每一次后台请求，都把当前用户的 token 有效期重置为配置的时长**（滑动过期），避免操作中被"token 过期"打断（尤其上传音乐耗时场景）。

现状：
- AccessToken 有效期固定 15 分钟（`security.login.access-token-minutes`），JWT 签发后 exp 固定，请求再多也不会续期。
- 401 后前端走 `/auth/refresh`（RefreshToken 轮换），但 HTTP（`http://192.168.244.130:8082`）环境下 refresh Cookie 配置为 `Secure=true; SameSite=None`，浏览器不存储，刷新必然失败 → 直接跳登录页。

## 方案：后端滑动续期（推荐，改动小且对前端透明）

思路：在 `JwtAuthenticationFilter` 校验 AccessToken 成功后，检查剩余有效期；若低于阈值（如剩余 < 一半时长），**在响应头中下发新 AccessToken**，前端响应拦截器静默替换 localStorage 中的 token。这样任何一次成功请求都会把有效期"重置"为配置时长，且不需要改 JWT exp、不加 Redis 状态。

### 1. 后端：JwtAuthenticationFilter 增加滑动续期
文件：`blog-backend/src/main/java/com/dlbyy/blog/security/JwtAuthenticationFilter.java`

- 注入 `SecurityProperties`（读 `accessTokenMinutes`）。
- 认证成功后计算剩余有效期 `jwtUtils.getExpirationFromToken(token) - now`；若 `剩余 < accessTokenMinutes * 60_000 / 2`，生成新 AccessToken：`jwtUtils.generateAccessToken(username)`，并 `response.setHeader("X-New-Token", newToken)`。
- 新 token 生成失败不影响请求（try-catch 仅记日志）。

### 2. 前端：request.js 响应拦截器静默换 token
文件：`blog-admin/src/api/request.js`（响应拦截器开头）

```js
const newToken = response.headers['x-new-token']
if (newToken) localStorage.setItem('admin_token', newToken)
```

blog-frontend 若有同样的 token 机制则同步（经检查前台 portal 接口多为公开，暂不涉及；如前台也存 token 则同样加两行）。

### 3. 后端配置：修复 HTTP 联调下 refresh Cookie（兜底）
文件：`blog-backend/src/main/resources/application.yaml:86-87`

```yaml
    cookie-secure: false       # 本地/内网 HTTP 联调置 false；生产 HTTPS 必须 true
    cookie-same-site: "Lax"
```

保证即使 token 真过期（如静置超过续期窗口没发请求），refresh 仍可用，不再被踢出。

## 行为效果
- 用户每次操作（任意成功请求）→ token 剩余 < 7.5 分钟时自动换发 → 有效期重置为 15 分钟。
- 活跃用户永不掉线；长时间不操作后回来，首请求 401 → 走 refresh（Cookie 现在可用了）→ 无感续期。
- 生产部署 HTTPS 时把 `cookie-secure` 改回 `true`、`same-site: "None"`。

## 验证
1. 重启后端 + 前端，HTTP 地址重新登录。
2. 正常操作若干请求，DevTools 确认响应偶带 `X-New-Token` 且 localStorage token 更新。
3. 静置 15 分钟后上传音乐 → 应自动刷新成功，不再跳转登录页。

## 改动文件清单
- `blog-backend/src/main/java/com/dlbyy/blog/security/JwtAuthenticationFilter.java`
- `blog-backend/src/main/resources/application.yaml`
- `blog-admin/src/api/request.js`
