# Tasks
- [x] Task 1: JwtUtils 增加启动期密钥强度校验（Fail-Fast）
  - [x] SubTask 1.1: 新增 `@PostConstruct` 初始化校验方法：secret 为空或字节数 < 64 时抛 IllegalStateException，错误信息含「使用 openssl rand -base64 64 生成强随机密钥」指引
  - [x] SubTask 1.2: 类注释补充「启动时强制校验密钥强度（≥64 字节，HS512）」说明
- [x] Task 2: .env.example 弱模板值治理
  - [x] SubTask 2.1: JWT_SECRET 模板值改为 `<openssl rand -base64 64 生成的强随机值>` 占位提示，注释保留生成命令
- [x] Task 3: 既有机制验证（不新增代码）
  - [x] SubTask 3.1: 复核 Token 黑名单 / Redis TTL 同步实现（JwtUtils 黑名单段 + AuthController 登出调用）
  - [x] SubTask 3.2: 复核 GlobalExceptionHandler 兜底不泄露堆栈
- [x] Task 4: 回归验证
  - [x] SubTask 4.1: `mvn test` 全部通过，确认本地默认密钥（74 字符）不受启动校验影响

# Task Dependencies
- Task 1、Task 2 相互独立，可并行
- Task 3 独立（纯审查）
- Task 4 依赖 Task 1（校验逻辑变更后需回归）
