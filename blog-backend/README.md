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
  secret: 你的JWT密钥（至少64字符）
  expiration: 86400000  # 24小时

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
│   ├── config/                           # 配置类
│   │   ├── SecurityConfig.java            # Spring Security 配置
│   │   ├── MybatisPlusConfig.java         # MyBatis Plus 配置
│   │   ├── RedisConfig.java               # Redis 配置
│   │   ├── WebMvcConfig.java              # 静态资源映射
│   │   └── SwaggerConfig.java             # Knife4j 配置
│   ├── controller/
│   │   ├── admin/                         # 管理后台接口（需鉴权）
│   │   │   ├── AdminArticleController.java
│   │   │   ├── AdminCategoryController.java
│   │   │   ├── AdminCommentController.java
│   │   │   ├── AdminDashboardController.java
│   │   │   ├── AdminFileController.java
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
│   ├── security/                         # JWT 安全模块
│   │   ├── JwtTokenProvider.java          # Token 生成/校验
│   │   ├── JwtAuthenticationFilter.java   # JWT 过滤器
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
│   │   └── impl/                          # 业务实现
│   └── utils/                           # 工具类
│       ├── FileUtils.java
│       ├── JwtUtils.java
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
| `common.exception` | `BusinessException` 业务异常、`GlobalExceptionHandler` 全局异常处理 |
| `config` | Spring Security、MyBatis Plus、Redis、WebMvc、Swagger 配置 |
| `controller.admin` | 管理后台接口，路径前缀 `/admin/**`，需 JWT 鉴权 |
| `controller.portal` | 前台公开接口，路径前缀 `/portal/**`、`/auth/**`、`/user/**` |
| `entity` | MyBatis Plus 实体类，对应数据库表 |
| `mapper` | MyBatis Mapper 接口，继承 BaseMapper |
| `security` | JWT Token 生成与校验、Security 过滤器链 |
| `service` | 业务接口与实现，封装业务逻辑 |
| `utils` | 文件操作、JWT、Redis 工具类 |

## 接口分组

| 前缀 | 鉴权 | 说明 |
|------|------|------|
| `/auth/**` | 公开 | 登录、登出 |
| `/portal/**` | 公开 | 前台接口（文章/分类/标签/评论/简历/留言/统计） |
| `/admin/**` | 需 Token | 管理后台接口 |
| `/user/**` | 需 Token | 当前用户信息 |
| `/uploads/**` | 公开 | 上传文件静态访问 |

### 鉴权方式

除 `/auth` 与 `/portal` 外，其余接口需在请求头携带 JWT：

```
Authorization: Bearer <token>
```

### 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

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
  --jwt.secret=$JWT_SECRET
```
