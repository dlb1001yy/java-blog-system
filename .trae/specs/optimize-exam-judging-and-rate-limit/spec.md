# 优化考试判分与限流模块 Spec

## Why
当前考试判分（ExamServiceImpl）在大批量并发交卷时同步执行 JSON 解析与多次数据库读写，可能耗尽 Tomcat 线程池；作弊判定仅依赖前端上报的切屏次数，易被绕过；限流 Lua 脚本已通过 Bean 预加载，但可在判分/限流两个模块进一步做健壮性优化。

## What Changes
- 作弊判定强化：在 `submitPaper` 中增加服务端侧的可疑耗时校验——当试卷总题目数 ≥ 8 且 `durationSeconds` 小于 `题目数 × 10 秒` 时，将 `cheatFlag` 置 1（与切屏超限判定取或）。
- 判分性能保护：交卷接口的判分与落库流程改为异步执行，新增专用线程池 `examJudgeExecutor`（复用 AsyncConfig 模式），接口立即返回记录 ID（先插入状态为 0 的占位 ExamRecord，判分在异步线程完成更新）。
- 主观题批改草稿批量插入：将逐条 `examMarkingMapper.insert` 改为 MyBatis-Plus 批量插入（SQLSessionFactory 开启 `rewriteBatchedStatements`），减少网络往返。
- 限流切面：无功能性改动，仅补充日志中输出剩余可用次数上下文（Lua 返回放行数）。
- **BREAKING**：交卷接口 `POST /exam/submit` 语义调整——立即返回 recordId，但 `objectiveScore`/`answers` 在异步完成后才可查询到（通常毫秒级）。前端轮询成绩详情时需容忍占位状态。

## Impact
- Affected specs: 考试判分流程、防作弊判定
- Affected code:
  - `blog-backend/src/main/java/com/dlbyy/blog/service/impl/ExamServiceImpl.java`
  - `blog-backend/src/main/java/com/dlbyy/blog/config/AsyncConfig.java`
  - `blog-backend/src/main/java/com/dlbyy/blog/aspect/RateLimitAspect.java`
  - 前端交卷结果页（blog-frontend）对异步判分结果的轮询/展示

## ADDED Requirements

### Requirement: 服务端作弊判定强化
系统 SHALL 在交卷时结合服务端数据（题目数、耗时）独立判定可疑交卷，不再完全信任前端上报。

#### Scenario: 疑似瞬答
- **WHEN** 试卷题目数 ≥ 8 且 `durationSeconds < 题目数 × 10`
- **THEN** `cheatFlag` 置 1，即使 `switchCount < 3`

#### Scenario: 切屏超限仍生效
- **WHEN** `switchCount >= 3`
- **THEN** `cheatFlag` 置 1（原有行为保持不变）

### Requirement: 交卷异步判分
系统 SHALL 在接收交卷请求后立即持久化占位答卷记录并返回 recordId，将判分与批改草稿生成放入专用线程池异步执行。

#### Scenario: 高并发交卷
- **WHEN** 大量用户同时交卷
- **THEN** 请求线程仅执行占位记录插入，判分在 `examJudgeExecutor` 中完成，Tomcat 线程不被长时间占用

#### Scenario: 异步判分失败
- **WHEN** 异步判分抛出异常
- **THEN** 记录状态保持/回退为可重试状态并记录错误日志，不影响已返回的 recordId

### Requirement: 主观题批改草稿批量插入
系统 SHALL 使用 MyBatis-Plus 批量插入一次性写入所有主观题批改草稿，替代循环单条 insert。

## MODIFIED Requirements

### Requirement: 限流切面日志（原：仅记录触发）
`RateLimitAspect` 在超限拦截时 SHALL 记录 key、方法名与窗口阈值等上下文信息，便于排查。Lua 脚本预加载机制（RateLimitConfig + DefaultRedisScript Bean）保持不变。
