# 修复：后台登录一段时间后 /admin/** 接口报 403「无权限访问」

## 问题现象

`http://192.168.244.130:8081/api/admin/articles/page?...` 返回：

```json
{"code":403,"message":"无权限访问","data":null,...}
```

约在登录 15 分钟后（AccessToken 过期、触发 /auth/refresh 之后）出现；重新登录后台即恢复。

## 根因分析（已与用户确认）

报文精确产生于后端 [JwtAccessDeniedHandler.java:23](file:///d:\my-project\java-blog-system\blog-backend\src\main\java\com\dlbyy\blog\security\JwtAccessDeniedHandler.java)——即请求**已认证但角色不是 admin**，不是签名 403、不是 401 过期。

链路：
1. AccessToken 15 分钟过期 → 前端 `blog-admin/src/api/request.js` 调 `/auth/refresh`。
2. RefreshToken 通过 HTTP-only Cookie（名 `refresh_token`，`Path=/api/auth`）携带，见 [CookieUtils.java](file:///d:\my-project\java-blog-system\blog-backend\src\main\java\com\dlbyy\blog\utils\CookieUtils.java)。**Cookie 不区分端口、不区分前台/后台**。
3. 用户确认同一浏览器（192.168.244.130）还登录过前台普通用户账号 → 前台登录也写入了同名 `refresh_token` Cookie，覆盖后台的。
4. 后台刷新时后端读到前台的 refresh token，返回**普通用户**的 accessToken；前端把它存入 `admin_token`。
5. 之后所有 `/admin/**` 请求：已认证但无 `ROLE_admin` → 403「无权限访问」。重新登录后台会重写 Cookie，故恢复。

## 修复方案：按端隔离 Cookie 名称

后端将 refresh Cookie 名按端区分：后台用 `admin_refresh_token`，前台/移动端仍用 `refresh_token`。同时后端读取时按请求路径选择对应 Cookie，并加日志便于定位。前端 blog-admin 无需改动（Cookie 是 HttpOnly，前端不读取）。

### 改动 1：CookieUtils 支持按端读写 Cookie

文件：`blog-backend/src/main/java/com/dlbyy/blog/utils/CookieUtils.java`

- 常量拆分：
  - `REFRESH_COOKIE_NAME = "refresh_token"`（前台/移动端，保持现状）
  - `ADMIN_REFRESH_COOKIE_NAME = "admin_refresh_token"`（后台）
- 新增带 `isAdmin` 参数（或 `clientType` 枚举）的方法重载：
  - `addRefreshCookie(response, token, boolean admin)`：按端选 Cookie 名与 Path（后台 Path 可用 `/api/auth`，名称不同即可隔离）。
  - `clearRefreshCookie(response, boolean admin)`
- 新增按端读取：`getRefreshTokenFromRequest(request)` 内部依据 `request.getRequestURI()` 判断——`/api/admin/**` 请求引发的刷新读 `admin_refresh_token`；否则读 `refresh_token`。由于刷新接口本身是 `/api/auth/refresh`，无法从 URI 区分调用方，因此需要**在 AuthController.refresh 中按 "两个 Cookie 都读、优先匹配有效 token"** 或由前端显式声明端类型。最终采用：

**决策：前端显式声明端类型（简单、无歧义）**
- `POST /auth/refresh` 支持可选请求头 `X-Client-Type: admin | portal`（缺省视为 portal）。
- 后端按该头选择读写哪个 Cookie；`X-Refresh-Token` 头回退逻辑不变（blog-app 走该头，不受影响）。
- 登录接口 `POST /auth/login` 同样读取 `X-Client-Type`（Login.vue 请求加该头），决定 `addRefreshCookie` 写哪个 Cookie 名。

### 改动 2：AuthController 按端处理 Cookie

文件：`blog-backend/src/main/java/com/dlbyy/blog/controller/portal/AuthController.java`

- `login`：读取 `httpRequest.getHeader("X-Client-Type")`，`"admin"` 时写 `admin_refresh_token` Cookie，否则写 `refresh_token`。
- `refresh`：读取 `X-Client-Type`；
  - admin：优先读 `admin_refresh_token` Cookie（或 `X-Refresh-Token` 头），校验通过后轮换并重写 `admin_refresh_token` Cookie；若读到的是普通用户 token，`isValidRefreshToken` 仍会通过（token 本身有效）——**需额外防护**：刷新成功后不区分角色即可，因为后台前端拿到的 accessToken 对应哪个账号由 Cookie 决定；隔离后 admin 端只会读到 admin 登录时写入的 Cookie，不再串号。同时在 refresh 响应中增加 `username` 字段，便于前端校验。
- `logout`：读取 `X-Client-Type`，清除对应端 Cookie（登出时若无法确定端，可两个都清）。

### 改动 3：前端 blog-admin 附带 X-Client-Type

文件：`blog-admin/src/api/request.js`、`blog-admin/src/api/auth.js`（如 login 走 authApi.login，则统一在 request 实例默认头加）

- 在 axios 实例默认 headers 中设置 `X-Client-Type: admin`（`request.defaults.headers.common['X-Client-Type'] = 'admin'`），所有后台请求（含登录/刷新/登出）自动携带，改动最小。

blog-app（移动端）走 `X-Refresh-Token` 头，不依赖 Cookie，无需改动；如后续其 H5 版本也用 Cookie，则默认 portal 行为正确。

### 兼容性说明

- 存量已登录后台用户的旧 `refresh_token` Cookie：下次 401 刷新时按 `X-Client-Type: admin` 读 `admin_refresh_token` 不存在 → 刷新失败 → 跳登录页重新登录一次（一次性代价，可接受）。为减少影响，refresh 时若 admin Cookie 缺失可回退读旧 `refresh_token` Cookie 并校验其用户角色是否为 admin（`userMapper` 查 role），是则迁移写入新 Cookie。**采用此回退以实现平滑迁移。**

## 改动文件清单

1. `blog-backend/src/main/java/com/dlbyy/blog/utils/CookieUtils.java` — 双 Cookie 名、按端读写/清除
2. `blog-backend/src/main/java/com/dlbyy/blog/controller/portal/AuthController.java` — login/refresh/logout 按 `X-Client-Type` 处理 + admin 回退迁移逻辑
3. `blog-admin/src/api/request.js` — axios 默认头 `X-Client-Type: admin`

## 验证步骤

1. 重启后端，blog-admin 重新登录；DevTools 确认 `/api/auth/login` 响应 Set-Cookie 为 `admin_refresh_token`。
2. 同浏览器再登录前台普通用户 → 确认写入 `refresh_token`（两者互不覆盖）。
3. 后台静置 15 分钟（或手动把 localStorage 的 admin_token 改为过期值）触发刷新 → 请求应携带 `X-Client-Type: admin`，刷新后 `/admin/**` 请求正常 200，不再 403。
4. 前后台交替使用、先后登出对方，验证互不干扰。
