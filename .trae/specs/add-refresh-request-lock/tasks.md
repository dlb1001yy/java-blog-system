# Tasks

- [x] Task 1: blog-admin 请求拦截器增加刷新请求锁
  - [x] SubTask 1.1: `refreshTokenOnce()` 中给 `request.post('/auth/refresh')` 的 config 标记 `_isRefresh: true`，使刷新请求自身豁免锁
  - [x] SubTask 1.2: 将请求拦截器回调改为 `async`，在附加 token 前检查 `refreshing`：若非 null 且 `!config._isRefresh`，则 `await refreshing` 获取新 token 并覆盖到 `config.headers['Authorization']`
  - [x] SubTask 1.3: 确保 `await refreshing` 失败时静默 catch（不中断请求），让请求用当前旧 token 照常发出，由响应拦截器处理

- [x] Task 2: blog-app request 函数增加刷新请求锁
  - [x] SubTask 2.1: 在 `request()` 函数内部、调用 `uni.request` 之前增加锁等待逻辑：若 `refreshing` 非 null，先 `await refreshing` 获取新 token
  - [x] SubTask 2.2: 将新 token 用于构建 `Authorization` 头（覆盖从 storage 读取的旧值）
  - [x] SubTask 2.3: 锁等待失败时静默 catch，继续用旧 token 发出请求
  - [x] SubTask 2.4: 确认 `refreshTokenOnce()` 内部使用 `uni.request` 直连（不走 `request()` 函数），天然豁免锁，不会死锁

# Task Dependencies

- Task 1 和 Task 2 相互独立，可并行实施
