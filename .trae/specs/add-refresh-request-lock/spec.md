# 前端刷新拦截器请求锁（防并发）Spec

## Why

当前 blog-admin（axios）和 blog-app（uni-app）的刷新令牌逻辑仅在**响应拦截器**中通过 `refreshing` Promise 单例做去重——多个 401 共享同一次刷新请求。但在刷新进行期间，**新发出的业务请求仍然携带已过期的旧 token**，这些请求注定返回 401，然后才排队等待刷新完成。

这带来三个问题：
1. **无效网络往返**：刷新期间每个新请求都白白走一次"发送 → 401 → 排队等待"的流程
2. **签名资源浪费**：每个请求都生成 `X-Nonce` 并写入 Redis，但请求本身注定失败，Nonce 被无意义消耗
3. **延迟叠加**：用户感知到的响应时间 = 刷新等待 + 401 往返 + 重试往返，而非仅刷新等待 + 重试往返

需要在**请求发送阶段**增加请求锁：刷新进行中时暂停所有业务请求，刷新完成后自动附加新 token 再发送，从源头杜绝无效 401。

## What Changes

- **blog-admin（axios）请求拦截器增加请求锁**：刷新进行中时，业务请求 `await refreshing`，完成后用新 token 重发
- **blog-app（uni-app）request 函数增加请求锁**：刷新进行中时，业务请求等待刷新完成后再发送
- **刷新请求自身豁免**：`/auth/refresh` 请求标记 `_isRefresh`，跳过请求锁，避免死锁
- **刷新失败时请求照常发出**：锁释放后请求用当前 token 发出，由响应拦截器正常处理 401 → 跳转登录

## Impact

- Affected code:
  - `blog-admin/src/api/request.js`：请求拦截器增加锁等待逻辑；`refreshTokenOnce()` 标记 `_isRefresh`
  - `blog-app/common/request.js`：`request()` 函数增加锁等待逻辑（`refreshTokenOnce()` 已用 `uni.request` 直连，天然豁免）
- **不影响** blog-frontend：该端未实现刷新令牌，不在本次范围内
- **不影响** 后端代码：纯前端改动
- **不影响** 现有签名逻辑、错误处理、登录跳转等流程

## ADDED Requirements

### Requirement: 刷新期间请求锁

系统应在 token 刷新进行期间暂停所有业务请求的发送，待刷新完成后自动附加新 token 再发出。

#### Scenario: 刷新进行中新请求自动等待

- **WHEN** 刷新令牌请求已发出（`refreshing` 非 null）
- **AND** 新的业务请求进入请求拦截器
- **THEN** 该请求被暂停（`await refreshing`），不立即发送
- **AND** 刷新成功后，请求自动附加新 token 并发出
- **AND** 请求正常返回业务数据，不产生 401

#### Scenario: 刷新期间多个新请求并发

- **WHEN** 刷新进行中，同时有 3 个新业务请求进入拦截器
- **THEN** 3 个请求全部暂停等待同一个 `refreshing` Promise
- **AND** 刷新完成后，3 个请求各自附加新 token 并行发出
- **AND** 不产生任何无效 401 请求

#### Scenario: 刷新请求自身豁免锁

- **WHEN** `/auth/refresh` 请求（标记 `_isRefresh`）进入请求拦截器
- **AND** 此时 `refreshing` 非 null（理论上不会发生，因单例去重）
- **THEN** 该请求跳过锁等待，直接发出，不产生死锁

#### Scenario: 无刷新进行时请求正常发出

- **WHEN** `refreshing` 为 null（无刷新进行中）
- **AND** 业务请求进入请求拦截器
- **THEN** 请求立即附加当前 token 并发出，无任何等待

#### Scenario: 刷新失败后锁释放

- **WHEN** 刷新请求失败（refresh token 也已过期）
- **AND** 有被锁暂停的业务请求正在等待
- **THEN** 等待的请求被释放，使用当前（旧的）token 照常发出
- **AND** 这些请求收到 401 后由响应拦截器正常处理（跳转登录页）

### Requirement: axios 请求拦截器锁实现（blog-admin）

blog-admin 的 axios 请求拦截器应在附加 token 前检查刷新状态。

#### Scenario: axios 拦截器锁等待

- **WHEN** 请求进入 `request.interceptors.request.use` 回调
- **AND** `refreshing` 非 null 且该请求未标记 `_isRefresh`
- **THEN** 拦截器 `await refreshing` 获取新 token
- **AND** 将新 token 写入 `config.headers['Authorization']`
- **AND** 返回 config 继续后续签名和发送流程

### Requirement: uni-app 请求函数锁实现（blog-app）

blog-app 的 `request()` 函数应在构造 `uni.request` 前检查刷新状态。

#### Scenario: uni-app 函数锁等待

- **WHEN** 调用 `request(options)` 且 `refreshing` 非 null
- **THEN** 先 `await refreshing` 获取新 token
- **AND** 用新 token 构建 `Authorization` 头
- **AND** 然后再调用 `uni.request` 发送请求
