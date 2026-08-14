# blog-backend

> Java码农笔记后端服务 — Spring Boot 3 单体后端，为前台门户、管理后台与移动端提供 REST API。

## 技术栈

| 框架 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.1.5 | Web 框架 |
| Java | 17 | 运行时 |
| MyBatis Plus | 3.5.5 | ORM |
| Spring Security | 6.x | 鉴权 |
| jjwt | 0.11.5 | JWT Token |
| Redis | — | 缓存/会话 |
| Knife4j | 4.3.0 | 接口文档（OpenAPI 3） |
| Hutool | 5.8.22 | 工具库 |
| Lombok | — | 简化 POJO |
| MySQL Connector/J | 8.0.33 | 数据库驱动 |

## 环境要求

| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 必须为 17，Java 21 需调整部分依赖 |
| Maven | 3.8+ | 构建工具 |
| MySQL | 8.0+ | 数据库 |
| Redis | 6.0+ | 缓存 |

## 快速开始

### 1. 初始化数据库

```bash
mysql -uroot -p < sql/01-create_sql.sql
```

数据库名：`dlbyy_zp_blog`（utf8mb4）

### 2. 修改配置

编辑 `src/main/resources/application.yaml`，配置数据库与 Redis 连接信息。

### 3. 启动

```bash
./mvnw spring-boot:run
```

启动后访问：
- API 根路径：http://localhost:8080/api
- 接口文档（Knife4j）：http://localhost:8080/api/doc.html

## 配置说明

`src/main/resources/application.yaml` 关键项：

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dlbyy_zp_blog?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的密码
  data:
    redis:
      host: localhost
      port: 6379
      password: 你的Redis密码
      database: 5

jwt:
  # JWT 签名密钥（HS512 算法，至少 64 字符）
  # 生产环境通过 JWT_SECRET 环境变量覆盖
  secret: ${JWT_SECRET:ThisIsASecureSecretKeyForJwtTokenGenerationAndValidationMakeItLongEnough123}

# 安全认证增强配置
security:
  login:
    ip-limit-count: 5            # 单 IP 每分钟最大登录尝试次数
    ip-window-seconds: 60        # IP 限流窗口（秒）
    user-fail-threshold: 5       # 连续失败达到该次数锁定账户
    lock-minutes: 15             # 账户锁定时长（分钟）
    bcrypt-strength: 12          # BCrypt 加密强度（盐值轮数）
    access-token-minutes: 15     # AccessToken 有效期（分钟）
    refresh-token-days: 7        # RefreshToken 有效期（天）
    cookie-secure: true          # refresh Cookie 是否带 Secure（HTTP 联调置 false）
    cookie-same-site: "None"     # refresh Cookie SameSite 策略
  signing:
    enabled: true                # 是否开启请求签名验证（false 时完全跳过）
    secret: ${API_SIGNING_SECRET:BlogApiSigningSecret2024!}  # 签名密钥
    timestamp-window-seconds: 60 # 请求时间戳有效窗口（秒）

file:
  upload-path: ./uploads/  # 文件上传目录
```

## 目录结构

```
blog-backend/
├── src/main/java/com/dlbyy/blog/
│   ├── JavaBlogApplication.java          # 启动类
│   ├── common/                           # 通用类
│   │   ├── Result.java                    # 统一响应封装
│   │   ├── PageResult.java                # 分页响应封装
│   │   ├── Constants.java                 # 常量
│   │   └── exception/                     # 异常处理
│   │       ├── BusinessException.java
│   │       └── GlobalExceptionHandler.java
│   ├── annotation/                       # 自定义注解
│   │   └── RateLimit.java                 # 通用限流注解（AOP + Redis Lua）
│   ├── aspect/                           # AOP 切面
│   │   └── RateLimitAspect.java           # @RateLimit 限流切面
│   ├── config/                           # 配置类
│   │   ├── SecurityConfig.java            # Spring Security 配置（过滤器链）
│   │   ├── MybatisPlusConfig.java         # MyBatis Plus 配置
│   │   ├── RedisConfig.java               # Redis 配置
│   │   ├── RateLimitConfig.java           # 限流 Lua 脚本 Bean
│   │   ├── DataInitializer.java           # 启动时初始化管理员账号
│   │   ├── WebMvcConfig.java              # 静态资源映射
│   │   └── SwaggerConfig.java             # Knife4j 配置
│   ├── controller/
│   │   ├── admin/                         # 管理后台接口（需鉴权）
│   │   │   ├── AdminArticleController.java
│   │   │   ├── AdminCategoryController.java
│   │   │   ├── AdminCommentController.java
│   │   │   ├── AdminDashboardController.java
│   │   │   ├── AdminFileController.java
│   │   │   ├── AdminConfigController.java        # 系统配置（站点/上传）
│   │   │   ├── AdminLinkController.java
│   │   │   ├── AdminMessageController.java
│   │   │   ├── AdminResumeController.java
│   │   │   └── AdminTagController.java
│   │   └── portal/                        # 前台公开接口
│   │       ├── AuthController.java         # 登录/登出
│   │       ├── PortalArticleController.java
│   │       ├── PortalCategoryController.java
│   │       ├── PortalCommentController.java
│   │       ├── PortalMessageController.java
│   │       ├── PortalResumeController.java
│   │       ├── PortalStatsController.java
│   │       ├── PortalTagController.java
│   │       └── UserController.java
│   ├── entity/                           # 实体类
│   │   ├── Article.java
│   │   ├── ArticleTag.java
│   │   ├── Category.java
│   │   ├── Comment.java
│   │   ├── Config.java
│   │   ├── Link.java
│   │   ├── Message.java
│   │   ├── ResumeInfo.java
│   │   ├── Tag.java
│   │   └── User.java
│   ├── mapper/                           # MyBatis Mapper 接口
│   │   ├── ArticleMapper.java
│   │   ├── ArticleTagMapper.java
│   │   ├── CategoryMapper.java
│   │   ├── CommentMapper.java
│   │   ├── ConfigMapper.java
│   │   ├── LinkMapper.java
│   │   ├── MessageMapper.java
│   │   ├── ResumeInfoMapper.java
│   │   ├── TagMapper.java
│   │   └── UserMapper.java
│   ├── properties/                       # 配置属性类
│   │   ├── SecurityProperties.java        # 安全配置（限流/锁定/BCrypt/Token有效期/Cookie）
│   │   └── SignatureProperties.java       # 请求签名配置（开关/密钥/时间窗口）
│   ├── security/                         # JWT 安全模块
│   │   ├── JwtTokenProvider.java          # Token 生成（委托 JwtUtils）
│   │   ├── JwtAuthenticationFilter.java   # JWT 过滤器（提取 Bearer Token → SecurityContext）
│   │   ├── RequestSignatureFilter.java    # 请求签名过滤器（HMAC-SHA256 防重放）
│   │   ├── JwtAuthenticationEntryPoint.java
│   │   └── CustomUserDetailsService.java
│   ├── service/                         # 业务接口
│   │   ├── ArticleService.java
│   │   ├── CategoryService.java
│   │   ├── CommentService.java
│   │   ├── ConfigService.java
│   │   ├── LinkService.java
│   │   ├── MessageService.java
│   │   ├── ResumeInfoService.java
│   │   ├── TagService.java
│   │   ├── UserService.java
│   │   ├── LoginAttemptService.java       # 登录限流 + 账户锁定（Redis）
│   │   ├── AlertNotifier.java             # 安全告警通知（扩展点）
│   │   └── impl/                          # 业务实现
│   └── utils/                           # 工具类
│       ├── FileUtils.java
│       ├── JwtUtils.java                  # JWT 双Token工具（AccessToken + RefreshToken）
│       ├── CookieUtils.java               # Refresh Token HTTP-only Cookie 工具
│       ├── PasswordStrengthValidator.java # 密码强度校验
│       └── RedisUtils.java
├── src/main/resources/
│   ├── application.yaml                  # 配置文件
│   └── mapper/                           # MyBatis XML
│       ├── ArticleMapper.xml
│       ├── ArticleTagMapper.xml
│       ├── CategoryMapper.xml
│       ├── CommentMapper.xml
│       ├── ConfigMapper.xml
│       ├── LinkMapper.xml
│       ├── MessageMapper.xml
│       ├── ResumeInfoMapper.xml
│       ├── TagMapper.xml
│       └── UserMapper.xml
├── sql/01-create_sql.sql                    # 数据库初始化脚本
├── uploads/                              # 上传文件目录
└── pom.xml
```

## 包结构说明

| 包 | 职责 |
|----|------|
| `common` | 统一响应封装 `Result<T>`、分页封装 `PageResult`、常量、业务异常 |
| `common.exception` | `BusinessException` 业务异常、`GlobalExceptionHandler` 全局异常处理、`RateLimitException` 限流异常 |
| `annotation` | `@RateLimit` 自定义限流注解 |
| `aspect` | `RateLimitAspect` 限流切面（AOP + Redis Lua 滑动窗口） |
| `config` | Spring Security、MyBatis Plus、Redis、限流、WebMvc、Swagger 配置 |
| `controller.admin` | 管理后台接口，路径前缀 `/admin/**`，需 JWT 鉴权 + 请求签名 |
| `controller.portal` | 前台公开接口，路径前缀 `/portal/**`、`/auth/**`、`/user/**` |
| `entity` | MyBatis Plus 实体类，对应数据库表 |
| `mapper` | MyBatis Mapper 接口，继承 BaseMapper |
| `properties` | `SecurityProperties` 安全配置、`SignatureProperties` 签名配置 |
| `security` | JWT Token 生成与校验、JWT 过滤器、请求签名过滤器、UserDetailsService |
| `service` | 业务接口与实现、`LoginAttemptService` 登录限流/锁定、`AlertNotifier` 告警 |
| `utils` | JWT 双Token工具、Cookie 工具、密码强度校验、Redis 工具、文件操作 |

## 接口分组

| 前缀 | 鉴权 | 说明 |
|------|------|------|
| `/auth/**` | 公开 | 登录、登出 |
| `/portal/**` | 公开 | 前台接口（文章/分类/标签/评论/简历/留言/统计） |
| `/admin/**` | 需 Token | 管理后台接口 |
| `/admin/config/**` | 需 Token | 系统配置（站点配置、上传配置） |
| `/user/**` | 需 Token | 当前用户信息 |
| `/uploads/**` | 公开 | 上传文件静态访问 |

### 鉴权方式

除 `/auth` 与 `/portal` 外，其余接口需在请求头携带 JWT：

```
Authorization: Bearer <token>
X-Timestamp: <毫秒时间戳>
X-Nonce: <随机字符串>
X-Signature: <HMAC-SHA256 签名>
```

### 安全架构

系统采用三层安全防护，JWT Token 代码与签名验证相互独立：

#### 第一层：JWT 双 Token 认证

| Token | 有效期 | 用途 | 传递方式 |
|-------|--------|------|----------|
| AccessToken | 15 分钟 | 接口鉴权 | `Authorization: Bearer <token>` |
| RefreshToken | 7 天 | 刷新 AccessToken | HTTP-only Cookie（浏览器）/ `X-Refresh-Token` 头（移动端） |

- 登录返回 AccessToken + RefreshToken，RefreshToken 通过 HTTP-only Cookie 下发
- AccessToken 过期后，前端自动调用 `/auth/refresh` 获取新 Token
- **Refresh Token 轮换**：每次刷新都签发新的 RefreshToken，旧 Token 立即吊销
- Redis 黑名单：登出时将 Token 加入黑名单，剩余有效期内自动失效
- Redis SET：记录每用户活跃的 RefreshToken，便于批量吊销（带 TTL 自动清理）

#### 第二层：请求签名防重放

防止从浏览器 DevTools 复制 Token 到 Apifox/Postman 等外部工具直接调用接口。

**签名算法**：
```
stringToSign = HTTP方法 + "\n" + 请求URI + "\n" + 时间戳 + "\n" + 随机数
signature = HMAC-SHA256(签名密钥, stringToSign) → 十六进制
```

**验证流程**（`RequestSignatureFilter`）：
1. 检查 `X-Timestamp` / `X-Nonce` / `X-Signature` 三个请求头是否齐全
2. 校验时间戳：与服务端时间差超过 60 秒则拒绝（防延迟重放）
3. 校验 Nonce：Redis `SETNX` 去重，同一随机数只能用一次（防立即重放）
4. 校验签名：服务端重新计算 HMAC-SHA256，与请求头比对

> 签名验证仅作用于 `/user/**` 和 `/admin/**`，`/auth/**` 和 `/portal/**` 不受影响。
> 设置 `security.signing.enabled = false` 可临时关闭（开发调试用）。

#### 第三层：登录限流与账户锁定

| 防护 | 规则 | 实现 |
|------|------|------|
| IP 限流 | 单 IP 每分钟最多 5 次登录尝试 | Redis 滑动窗口（Lua 脚本） |
| 账户锁定 | 同一用户名连续失败 5 次锁定 15 分钟 | Redis 计数器 + 锁定 Key |
| 通用限流 | `@RateLimit` 注解可标注任意接口 | AOP + Redis 滑动窗口 |

### 安全架构对比：本项目 vs 大厂

> 本节帮助理解当前安全方案在行业中的定位，以及与大厂方案的差距。

#### 大厂 Token 安全的 6 层体系

| 层级 | 大厂做法 | 本项目状态 |
|------|----------|------------|
| HTTPS 全链路加密 | 强制 HTTPS，Token 在传输中加密 | 部署时需启用（Nginx 配置 SSL 证书） |
| HttpOnly + Secure Cookie | Token 完全不暴露给 JavaScript | RefreshToken 已用 HttpOnly Cookie；AccessToken 在 localStorage（对 JS 可见） |
| 服务端 Session | Token 只是查找 ID，真正权限在服务端 | 采用"有状态 JWT"（Redis 黑名单 + RefreshToken SET），可主动吊销 |
| 设备指纹绑定 | Token 绑定浏览器/设备指纹，换工具失效 | 未实现（需要收集 User-Agent/IP/Canvas 指纹等） |
| 风控引擎 | 实时行为分析（IP 漂移、异常频率、并发检测） | 有基础限流（IP + 用户名维度），无综合风控 |
| 硬件级密钥管理 | 密钥存放在 KMS/HSM 硬件中，永不出硬件 | 密钥在环境变量/配置文件（个人项目可接受） |

#### 请求签名方案的安全边界

本项目的前端请求签名（`signing.js`）是一个**门槛/威慑**，不是真正的安全屏障：

| 攻击者水平 | 能否突破签名 | 说明 |
|------------|-------------|------|
| 只会复制 Authorization 头 | **被挡住** (403) | 缺少签名头，直接拒绝 |
| 懂前端、能读 JS 代码 | 能突破 | 可从 `signing.js` 读出密钥和算法，自行计算签名 |
| 复制全部请求头重放 | 60 秒内有效 | 超过后 Nonce 去重生效 |

> 前端密钥必然暴露（JS 是明文），这是浏览器架构决定的，无法避免。
> 真正的安全保障靠 **JWT 短期过期（15分钟）+ RefreshToken 轮换 + HTTPS**。

#### 本项目实际安全分工

| 机制 | 角色 | 重要性 |
|------|------|--------|
| JWT AccessToken 15 分钟过期 | 核心保障——泄露窗口极短 | ⭐⭐⭐⭐⭐ |
| RefreshToken 每次轮换 | 核心保障——旧 Token 立即吊销 | ⭐⭐⭐⭐⭐ |
| 登录限流 + 账户锁定 | 暴力破解防护 | ⭐⭐⭐⭐ |
| Redis 黑名单 | 主动吊销能力 | ⭐⭐⭐⭐ |
| 前端请求签名 | 门槛/威慑——挡住随手复制 Token | ⭐⭐ |
| **生产环境 HTTPS** | **传输加密——一切的基础** | ⭐⭐⭐⭐⭐ |

### 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

## 系统配置接口

后台「系统设置」页面通过以下接口读写站点配置与上传配置，配置以键值形式持久化到 `sys_config` 表（见 `Config` 实体 / `ConfigService`）。

| 方法 | 路径 | 说明 | 请求体 |
|------|------|------|--------|
| PUT | `/admin/config/site` | 保存网站配置 | `{ blogName, blogDescription, blogDomain }` |
| GET | `/admin/config/site` | 获取网站配置 | — |
| PUT | `/admin/config/upload` | 保存上传配置 | `{ uploadPath, allowedTypes, maxSize }` |
| GET | `/admin/config/upload` | 获取上传配置 | — |

说明：

- 所有接口均需 JWT 鉴权（`/admin/**` 受保护）。
- `maxSize` 单位为 **MB**，保存时自动转换为字节写入数据库与运行时 `StorageProperties.LocalConfig`。
- 保存上传配置后，会**动态改写运行时的本地存储参数**（`uploadPath` / `allowedTypes` / `maxSize`），使上传校验立即生效，无需重启（仅对 `storage.type=local` 生效）。
- 字段说明：
  - `blogName`：站点名称
  - `blogDescription`：站点描述
  - `blogDomain`：站点域名
  - `uploadPath`：上传文件存储路径
  - `allowedTypes`：允许上传的文件类型，逗号分隔（如 `jpg,png,gif,webp`）
  - `maxSize`：上传文件大小上限（MB）

## 接口文档

启动后端后访问 Knife4j：

```
http://localhost:8080/api/doc.html
```

## 数据库表

| 表名 | 说明 |
|------|------|
| sys_user | 用户表 |
| blog_category | 文章分类 |
| blog_tag | 文章标签 |
| blog_article | 文章 |
| blog_article_tag | 文章-标签关联 |
| blog_comment | 评论 |
| blog_message | 留言 |
| blog_link | 友情链接 |
| blog_resume_info | 简历信息 |
| blog_config | 站点配置 |

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |

> 密码使用 BCrypt 加密存储，如需重置可参考 `src/test/java/com/dlbyy/blog/PasswordGenerator.java` 生成新密码。

## 构建打包

```bash
./mvnw clean package -DskipTests
java -jar target/blog-backend-1.0.0.jar
```

生产环境可通过环境变量覆盖配置：

```bash
java -jar blog-backend-1.0.0.jar \
  --spring.datasource.password=$DB_PASSWORD \
  --spring.data.redis.password=$REDIS_PASSWORD \
  --jwt.secret=$JWT_SECRET \
  --security.signing.secret=$API_SIGNING_SECRET
```

| 环境变量 | 用途 | 默认值 |
|----------|------|--------|
| `DB_PASSWORD` | MySQL 密码 | `123456` |
| `REDIS_PASSWORD` | Redis 密码 | `123456` |
| `JWT_SECRET` | JWT 签名密钥（至少 64 字符） | 内置开发密钥 |
| `API_SIGNING_SECRET` | 请求签名密钥 | `BlogApiSigningSecret2024!` |
