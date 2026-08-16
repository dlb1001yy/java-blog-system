# JWT 安全增强与启动密钥校验 Spec

## Why
当前 JWT_SECRET 链路（代码外置 + 文档指引 `openssl rand -base64 64`）已具备，Token 黑名单/Redis TTL 同步已在 [JwtUtils.java](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/utils/JwtUtils.java) 实现，全局异常处理已在 [GlobalExceptionHandler.java](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/common/exception/GlobalExceptionHandler.java) 实现（兜底返回 500「系统内部错误」，不泄露堆栈）。但存在残余风险：应用启动时若 JWT_SECRET 为空或短于 64 字节仍可启动上线（HS512 弱密钥被猜解），`.env.example` 的 JWT_SECRET 模板值仍是弱示例。

## What Changes
- JwtUtils 增加启动期密钥强度自检：secret 为空或字节数 < 64 时快速失败（Fail-Fast），本地开发保留内置默认值不受影响
- `.env.example` 的 JWT_SECRET 模板值改为 `<openssl rand -base64 64 生成的强随机值>` 占位提示
- 代码级 Token 黑名单 / Redis 过期同步、全局异常处理**已存在**，仅纳入验证，不重复实现

## Impact
- Affected specs: externalize-sensitive-config（.env.example 模板值调整）、harden-security-for-production（启动校验为其「上线前安全检查」的代码级兜底）
- Affected code: `blog-backend/src/main/java/com/dlbyy/blog/utils/JwtUtils.java`、`.env.example`

## ADDED Requirements
### Requirement: JWT 密钥启动期强度校验
应用 SHALL 在启动阶段校验 jwt.secret，长度不足 64 字节（HS512 安全要求）时立即失败并给出明确错误信息。

#### Scenario: 弱密钥启动失败
- **WHEN** 应用启动且 jwt.secret 字节数 < 64（或为空）
- **THEN** 启动失败，日志明确提示「JWT 密钥长度不足，使用 openssl rand -base64 64 生成」
- **AND** 本地开发默认密钥（74 字符）不受影响，正常启动

### Requirement: 既有安全机制验证（不新增代码）
- Token 黑名单：登出/改密时 Token 进入 Redis 黑名单，剩余 TTL 自动过期（[JwtUtils.java:184-203](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/utils/JwtUtils.java#L184-L203)）
- 全局异常兜底：未预期异常返回统一 `Result.error(500, "系统内部错误，请联系管理员")`，堆栈仅写日志（[GlobalExceptionHandler.java:71-75](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/common/exception/GlobalExceptionHandler.java#L71-L75)）

#### Scenario: 验证通过
- **WHEN** 审查 JwtUtils 黑名单实现与 GlobalExceptionHandler 兜底逻辑
- **THEN** 与上述描述一致，无需修改
