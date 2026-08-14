# Tasks

- [x] Task 1: 后端新增签名配置类 `SignatureProperties`
  - [x] SubTask 1.1: 创建 `SignatureProperties.java`，字段：`enabled`(默认 true)、`secret`(从环境变量读取)、`timestampWindowSeconds`(默认 60)
  - [x] SubTask 1.2: 在 `application.yaml` 中增加 `security.signing` 配置块

- [x] Task 2: 后端新增请求签名过滤器 `RequestSignatureFilter`
  - [x] SubTask 2.1: 创建 `RequestSignatureFilter.java`，继承 `OncePerRequestFilter`
  - [x] SubTask 2.2: 实现 `shouldNotFilter()` — 仅对 `/user/` 和 `/admin/` 路径生效
  - [x] SubTask 2.3: 实现签名验证逻辑：检查头完整性 → 校验时间戳 → 校验 Nonce 防重放（Redis SETNX）→ 校验 HMAC-SHA256 签名
  - [x] SubTask 2.4: 验证失败时直接写入 HTTP 403 JSON 响应（不经过 Controller）

- [x] Task 3: 后端注册过滤器到 SecurityConfig
  - [x] SubTask 3.1: 在 `SecurityConfig.filterChain()` 中，将 `RequestSignatureFilter` 添加在 `JwtAuthenticationFilter` 之前
  - [x] SubTask 3.2: CORS 配置中 `AllowedHeaders` 已配置 `*` 通配，覆盖自定义签名头

- [x] Task 4: blog-admin 前端增加签名拦截器
  - [x] SubTask 4.1: 在 `src/api/request.js` 请求拦截器中，为需要认证的请求自动添加签名头
  - [x] SubTask 4.2: 签名密钥放在独立文件 `src/api/signing.js` 中，导出 `signRequest(method, url)` 函数

- [x] Task 5: blog-app 前端增加签名逻辑
  - [x] SubTask 5.1: 在 `common/request.js` 中，为需要认证的请求自动添加签名头
  - [x] SubTask 5.2: 签名密钥放在 `common/signing.js` 中（纯 JS HMAC-SHA256 实现，兼容 uni-app 全平台）

- [x] Task 6: 编译验证
  - [x] SubTask 6.1: `mvn compile` 确认后端无编译错误
  - [x] SubTask 6.2: checklist 14/14 全部通过

# Task Dependencies
- [Task 3] depends on [Task 2]
- [Task 4] and [Task 5] depend on [Task 2]（需要与后端签名算法一致）
- [Task 6] depends on [Task 1] [Task 2] [Task 3] [Task 4] [Task 5]
