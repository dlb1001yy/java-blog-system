# 计划：前台登录过期后右上角恢复为“登录/注册”

## 问题分析
- 前台 401 时 [request.js:34-37](d:/my-project/java-blog-system/blog-frontend/src/api/request.js#L34-L37) 只清了 `localStorage.token`，但 Pinia store（[user.js](d:/my-project/java-blog-system/blog-frontend/src/stores/user.js)）中的 `token` / `userInfo` 响应式状态没清。
- [AppHeader.vue:32](d:/my-project/java-blog-system/blog-frontend/src/components/AppHeader.vue#L32) 依据 `userStore.token` 判断登录态，store 里 token 仍在 → 右上角继续显示用户名。

## Proposed Changes

文件：`blog-frontend/src/api/request.js`

在 401 分支中，清除 localStorage 的同时同步清除 Pinia store 状态。为避免模块加载顺序问题（request.js 被 store 之外的模块引入），在 401 处理内**动态 import** store 并调用已有的 `logout()`：

```js
case 401:
  ElMessage.error('登录已过期，请重新登录')
  localStorage.removeItem('token')
  // 同步清空 Pinia 登录态，让右上角恢复“登录/注册”
  import('@/stores/user').then(({ useUserStore }) => {
    try { useUserStore().logout() } catch { /* pinia 未初始化时忽略 */ }
  })
  break
```

说明：
- 不做强制跳转登录页（保持现有行为，仅修 UI 状态）。
- `logout()` 已同时清 localStorage 与 store，无需重复 removeAll。
- 若后续该前端也接入滑动续期（X-New-Token），另议；本计划不涉及。

## 验证
1. 登录前台 → 手动把 localStorage token 改成无效值（或等 15 分钟）→ 触发任意需登录接口 → 提示“登录已过期”，右上角立即变为“登录/注册”。
2. 正常登录/退出流程不受影响。

## 改动文件
- `blog-frontend/src/api/request.js`
