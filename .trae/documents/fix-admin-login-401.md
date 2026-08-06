# 修复三端登录 Token 字段不匹配导致 401 / 登录态失效

## Summary

后端已升级为 JWT 双 Token 认证，`/auth/login` 返回 `accessToken` / `refreshToken` 字段，但三个前端项目仍读取旧字段 `token`，导致存入的是 `undefined`，请求头变成 `Authorization: Bearer undefined`（或空），后端校验失败返回 401。

经核查三个前端项目，**全部存在相同问题**，本次一并修复：

| 项目 | 影响程度 | 文件 |
|------|---------|------|
| blog-admin | 🔴 活跃 Bug（登录后被弹回登录页） | `src/stores/user.js` |
| blog-app | 🔴 活跃 Bug（登录态无法保持，getUserInfo 401） | `pages/mine/login.vue`、`pages/mine/index.vue`、`common/config.js`、`common/request.js` |
| blog-frontend | 🟡 潜在 Bug（死代码，无登录 UI，预防性修复） | `src/stores/user.js` |

## Current State Analysis

### 后端登录响应（已确认，三端共用同一接口）
[AuthController.java](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/controller/portal/AuthController.java#L29-L46) `login()` 返回：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "username": "admin"
  }
}
```

### 1. blog-admin（活跃 Bug）
[stores/user.js](file:///d:/my-project/java-blog-system/blog-admin/src/stores/user.js#L10-L17) `login()`：
```js
token.value = res.data.token                        // ❌ undefined
localStorage.setItem('admin_token', res.data.token) // ❌ 存入 "undefined"
```
**失败链路**：登录 200 → 存入 `"undefined"` → 路由守卫放行（truthy 字符串）→ Dashboard 4 个接口带 `Bearer undefined` → 后端校验失败 401 → 响应拦截器清空 token + 跳回 `/login` → 用户看到"登录后不跳首页"。

### 2. blog-app（活跃 Bug）
[login.vue](file:///d:/my-project/java-blog-system/blog-app/pages/mine/login.vue#L88-L91)：
```js
const res = await api.login(form.value)
uni.setStorageSync(TOKEN_KEY, res.data.token)  // ❌ undefined
```
**失败链路**：登录 200 → 弹"登录成功" toast → token 存储失败 → `mine/index.vue` 的 `isLogin = !!uni.getStorageSync(TOKEN_KEY)` 为 false → 仍显示"点击登录" → 即使强制调用 `api.getUserInfo()`（`/user/info` 需认证）也会 401 → request.js 401 处理清空 token + 跳回登录页。

### 3. blog-frontend（潜在 Bug / 死代码）
[stores/user.js](file:///d:/my-project/java-blog-system/blog-frontend/src/stores/user.js#L8-L13) `setUser()`：
```js
token.value = data.token              // ❌ 字段名不匹配
localStorage.setItem('token', data.token)
```
**当前状态**：`setUser` / `authApi.login` 全局未被调用（grep 确认），无登录路由、无登录 UI、无路由守卫。是预留/遗留代码，当前不影响运行，但字段名需与后端对齐以防后续接入登录时踩坑。

### 验证（非阻塞）
- 三端 Vite/uni 代理均指向 `http://localhost:8080/api`，配置正确。
- `getUserInfo` 在 blog-admin 未在登录流程中调用；blog-app 在 `mine/index.vue` onShow 时调用（`/user/info`）。
- blog-frontend 的 `auth.js` / `user.js` 确认为死代码（无调用点）。

## Proposed Changes

### 一、blog-admin — `src/stores/user.js`

**`login()`（第 10-17 行）**：`res.data.token` → `res.data.accessToken`，并持久化 `refreshToken`。
```js
const login = async (loginForm) => {
  const res = await authApi.login(loginForm)
  token.value = res.data.accessToken
  username.value = res.data.username
  localStorage.setItem('admin_token', res.data.accessToken)
  localStorage.setItem('admin_refresh_token', res.data.refreshToken)
  localStorage.setItem('admin_username', res.data.username)
  return res
}
```

**`logout()`（第 19-25 行）**：同步清理 refreshToken。
```js
const logout = () => {
  token.value = ''
  username.value = ''
  userInfo.value = {}
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_refresh_token')
  localStorage.removeItem('admin_username')
}
```

### 二、blog-app

#### 2.1 `common/config.js` — 新增 REFRESH_TOKEN_KEY
```js
export const BASE_URL = 'http://localhost:8080/api'
export const TOKEN_KEY = 'uni_app_token'
export const REFRESH_TOKEN_KEY = 'uni_app_refresh_token'   // 新增
```

#### 2.2 `pages/mine/login.vue` — 修复字段名 + 存储 refreshToken（第 71、88-91 行）
导入 REFRESH_TOKEN_KEY：
```js
import { TOKEN_KEY, REFRESH_TOKEN_KEY } from '@/common/config.js'
```
修改存储逻辑：
```js
const res = await api.login(form.value)
// 保存 Token
uni.setStorageSync(TOKEN_KEY, res.data.accessToken)
uni.setStorageSync(REFRESH_TOKEN_KEY, res.data.refreshToken)
```

#### 2.3 `pages/mine/index.vue` — 退出登录时清理 refreshToken（第 71、129-142 行）
导入 REFRESH_TOKEN_KEY：
```js
import { TOKEN_KEY, REFRESH_TOKEN_KEY } from '@/common/config.js'
```
`handleLogout` 中增加清理：
```js
if (res.confirm) {
  uni.removeStorageSync(TOKEN_KEY)
  uni.removeStorageSync(REFRESH_TOKEN_KEY)
  userInfo.value = {}
  refreshLoginState()
  uni.showToast({ title: '已退出' })
}
```

#### 2.4 `common/request.js` — 401 处理时清理 refreshToken（第 1、45-50 行）
导入 REFRESH_TOKEN_KEY：
```js
import { BASE_URL, TOKEN_KEY, REFRESH_TOKEN_KEY } from './config.js'
```
401 分支中增加清理：
```js
} else if (res.statusCode === 401) {
  uni.removeStorageSync(TOKEN_KEY)
  uni.removeStorageSync(REFRESH_TOKEN_KEY)
  uni.showToast({ title: '登录已过期', icon: 'none' })
  setTimeout(() => uni.reLaunch({ url: '/pages/mine/login' }), 1500)
  reject(res)
}
```

### 三、blog-frontend — `src/stores/user.js`

**`setUser()`（第 8-13 行）**：仅修复字段名（死代码，不增加 refreshToken 独立存储，因为 `data` 整体已存为 `userInfo`，refreshToken 已包含其中）。
```js
const setUser = (data) => {
  token.value = data.accessToken
  userInfo.value = data
  localStorage.setItem('token', data.accessToken)
  localStorage.setItem('userInfo', JSON.stringify(data))
}
```

## Assumptions & Decisions

1. **假设**：后端 `/api/auth/login` 当前可正常返回 200 且 `data` 含 `accessToken`/`refreshToken`（基于 AuthController 源码确认，三端共用）。
2. **决策**：三端均将 `token` 字段名改为 `accessToken`，与后端 `response.put("accessToken", ...)` 严格对齐。
3. **决策**：blog-admin 与 blog-app（均有活跃登录流）同步持久化 `refreshToken`，正确消费后端双 Token 响应；blog-frontend 为死代码，仅修复字段名，不引入 refreshToken 独立存储（其 `setUser` 已将整个 `data` 存为 `userInfo`）。
4. **决策**：不实现 AccessToken 过期自动刷新机制（属于增强项，非本次 bug 修复范围）。
5. **决策**：不改动后端代码——后端响应结构是设计意图（双 Token），应由前端适配。

## Verification Steps

### blog-admin
1. `cd blog-admin && npm run dev`，确认 `http://localhost:3001` 启动。
2. 浏览器打开 `http://localhost:3001/admin/`，用 `admin`/`admin123` 登录。
3. 预期：停留在 `/dashboard`，4 个 dashboard 接口返回 200，看板正常渲染。
4. DevTools → Application → Local Storage：`admin_token` 为 `eyJ...` 开头的 JWT，`admin_refresh_token` 有值。
5. F5 刷新仍保持登录态；退出登录后三个键均被清空。

### blog-app
1. 用 HBuilder X 或 CLI 运行 blog-app 到 H5/小程序。
2. 进入"我的"页 → 点击登录 → 用 `admin`/`admin123` 登录。
3. 预期：toast"登录成功"后返回"我的"页，显示用户头像 + 昵称（来自 `/user/info` 200 响应），不再显示"点击登录"。
4. Storage 检查：`uni_app_token` 为 JWT，`uni_app_refresh_token` 有值。
5. 点击"退出登录"后两个键均被清空，页面恢复"点击登录"。

### blog-frontend
1. `cd blog-frontend && npm run dev`，确认站点正常加载。
2. 无需功能验证（死代码修复）——确认编译无报错即可。
