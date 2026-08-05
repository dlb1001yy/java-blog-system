# 修复「我的」页面登录/退出后不刷新问题

## Summary
`blog-app/pages/mine/index.vue` 的 `isLogin` 用 `computed` 读取 `uni.getStorageSync(TOKEN_KEY)`，但 storage 是非响应式的，computed 不会在 token 变化后重新求值。导致登录成功返回我的页仍显示「点击登录」、退出后仍显示用户名。修复方案：将 `isLogin` 改为 `ref`，通过 `onShow` 生命周期在每次页面显示时重新读取 token，并在 `handleLogout` 内同步刷新状态。

## Current State Analysis
- [pages/mine/index.vue](file:///d:\my-project\java-blog-system\blog-app\pages\mine\index.vue) 第 43 行：`const isLogin = computed(() => !!uni.getStorageSync(TOKEN_KEY))` —— 非响应式数据源 + computed，永远不会重算。
- 第 38 行 `import { onLoad } from '@dcloudio/uni-app'` 已导入但未调用 —— 页面没有任何生命周期钩子去刷新登录态。
- 第 42 行 `const userInfo = ref({})` —— 从未被赋值，登录后显示的是模板里的 '管理员' / 'admin@javalog.com' 兜底文案（用户对此无异议，本次不改）。
- [pages/mine/login.vue](file:///d:\my-project\java-blog-system\blog-app\pages\mine\login.vue#L72-L76) 登录成功后 `uni.navigateBack()` 返回上一页（即我的页）。`navigateBack` 不会触发目标页的 `onLoad`，但会触发 `onShow` —— 因此用 `onShow` 是刷新时机的正确选择。
- 第 53-65 行 `handleLogout`：清 token + 重置 `userInfo`，但停留在当前页（无导航），`onShow` 不会触发，必须手动调用刷新函数。

## Proposed Changes

### 仅修改 `blog-app/pages/mine/index.vue` 的 `<script setup>`

**1. 调整 import**：将 `onLoad` 替换为 `onShow`（或同时导入，但 `onLoad` 当前未使用，直接替换更干净）。
```js
import { onShow } from '@dcloudio/uni-app'
```

**2. `isLogin` 由 computed 改为 ref**：
```js
const isLogin = ref(false)
```

**3. 新增 `refreshLoginState` 函数**：从 storage 读取 token 并更新 `isLogin.value`。
```js
const refreshLoginState = () => {
  isLogin.value = !!uni.getStorageSync(TOKEN_KEY)
}
```

**4. 注册 `onShow` 钩子**：每次页面显示（含首次加载、从登录页 navigateBack 返回、从其他页返回）都刷新登录态。
```js
onShow(() => {
  refreshLoginState()
})
```

**5. `handleLogout` 内同步刷新**：清 token 后立即调用 `refreshLoginState()`，确保退出后视图马上切换到「点击登录」。
```js
const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        uni.removeStorageSync(TOKEN_KEY)
        userInfo.value = {}
        refreshLoginState()   // 新增：立即刷新视图
        uni.showToast({ title: '已退出' })
      }
    }
  })
}
```

模板部分（`v-if="isLogin"` / `v-else`）无需改动，ref 与 computed 在模板中用法一致。

## Assumptions & Decisions
- **不改 `userInfo` 数据来源**：用户对登录后显示 '管理员' 兜底文案无异议，且后端 `/user/info` 接口在 api.js 中标注「假设后端有此接口」，是否可用未确认。本次仅修复刷新问题，不引入对未知接口的依赖。
- **不改 login.vue**：登录页的 `navigateBack` 流程正常，配合我的页 `onShow` 即可刷新。`switchTab` 分支虽因之前移除 tabBar 而失效，但不在本次「我的页刷新」范围，不涉猎。
- **不用全局状态库/Pinia**：用 `onShow` + 本地 ref 是 uni-app 跨端最简方案，避免引入额外依赖与架构改动。
- **不处理 mine 页头像 404**（`/static/default-avatar.png` 不存在）：属其他问题，本次不涉猎。
- **`computed` 移除后无残留引用**：模板中 `isLogin` 仅作为值使用，ref 与 computed 行为一致，无需改模板。

## Verification Steps
1. 启动 blog-app，进入「我的」页（未登录态）：头部显示「点击登录」，无退出按钮。
2. 点击「点击登录」→ 输入 admin/admin123 → 登录成功 → `navigateBack` 返回我的页：头部应立即切换为「管理员 / admin@javalog.com」，并出现「退出登录」按钮。
3. 点击「退出登录」→ 确认：头部应立即切换回「点击登录」，退出按钮消失。
4. 在未登录态从我的页切到首页再切回我的页：仍显示「点击登录」（onShow 重新读取，状态正确）。
5. 上述每步无需手动刷新页面，视图均自动更新。
