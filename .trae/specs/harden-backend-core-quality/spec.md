# 后端核心安全与正确性加固 Spec

## Why
当前博客系统的评论/留言存在存储型 XSS 与参数覆盖风险，文章保存与浏览量计数存在事务/竞态正确性问题，文件上传校验薄弱；这些问题在生产环境会直接导致安全漏洞或数据不一致。

## What Changes
- **XSS 清洗接入**：评论、留言、文章富文本内容入口统一调用已有的 `JsoupXssUtil.clean()`（当前工具已写但全工程 0 调用）
- **DTO + 参数校验**：portal 评论/留言接口改用专用 DTO 替代直接绑定实体（防 `status`/`userId` 伪造），引入 `spring-boot-starter-validation`，`GlobalExceptionHandler` 补校验异常处理
- **文章保存事务化**：`AdminArticleController` 中「保存文章+保存标签」下沉到 Service 并加 `@Transactional`
- **浏览量/点赞计数原子化**：改为 `UPDATE ... SET view_count = view_count + 1` 原子自增，消除 Redis+DB 双写竞态；Redis 仅做防刷去重，新增 `@Scheduled` 定时任务把 Redis 累计增量回写数据库
- **文件上传加固**：增加文件大小上限校验、基于 Content-Type/magic bytes 的真实类型校验（白名单），图片重编码消除恶意载荷
- **写接口限流**：`/portal/comments`、`/portal/messages`、`/{id}/like` 补 `@RateLimit`（复用现有注解，IP 维度）

## Impact
- Affected specs: 无冲突；与 enhance-jwt-security-guard、add-auth-unit-tests 等已完成 spec 互不影响
- Affected code:
  - `blog-backend/.../controller/portal/PortalCommentController.java`
  - `blog-backend/.../controller/portal/PortalMessageController.java`
  - `blog-backend/.../controller/admin/AdminArticleController.java`
  - `blog-backend/.../service/impl/ArticleServiceImpl.java`
  - `blog-backend/.../utils/FileUtils.java`
  - `blog-backend/.../common/exception/`（全局异常处理）
  - `pom.xml`（新增 validation starter）

## ADDED Requirements
### Requirement: XSS 内容清洗
系统 SHALL 在评论、留言、文章内容持久化前调用 JsoupXssUtil 清洗，移除恶意脚本标签与属性。

#### Scenario: 提交含脚本的评论
- **WHEN** 用户提交内容为 `<script>alert(1)</script>hello` 的评论
- **THEN** 数据库中保存的内容不含 script 标签，前台渲染无脚本执行

### Requirement: 写接口参数校验与 DTO 隔离
评论/留言接口 SHALL 使用专用 DTO 接收请求，字段带 `@NotBlank/@Size` 等约束，客户端无法提交 `status`、`userId` 等服务端字段。

#### Scenario: 恶意提交审核状态
- **WHEN** 请求体携带 `"status": 1` 提交评论
- **THEN** 该字段被忽略，评论仍按默认待审核状态入库

### Requirement: 文章保存事务一致性
文章与其标签关联 SHALL 在同一事务内保存，任一失败整体回滚。

#### Scenario: 标签保存失败
- **WHEN** 文章保存成功后标签关联写入抛出异常
- **THEN** 文章记录一并回滚，不产生无标签的半成品数据

### Requirement: 计数原子更新
浏览量/点赞 SHALL 通过数据库原子自增（`SET view_count = view_count + 1`）更新，并发下不丢失更新；Redis 增量由定时任务批量回写。

#### Scenario: 并发访问同一文章
- **WHEN** 100 个并发请求查看同一文章
- **THEN** 数据库 view_count 精确 +100

### Requirement: 上传文件类型与大小校验
上传接口 SHALL 校验文件真实类型（Content-Type/magic bytes 白名单）与大小上限，非白名单类型拒绝。

#### Scenario: 上传伪装成 png 的脚本文件
- **WHEN** 上传扩展名为 .png 但实际内容为可执行脚本的文件
- **THEN** 请求被拒绝，返回明确错误

### Requirement: 公开写接口限流
评论、留言、点赞接口 SHALL 应用基于 IP 的 `@RateLimit` 限流。

#### Scenario: 高频刷评论
- **WHEN** 同一 IP 短时间内提交超过阈值的评论
- **THEN** 超出部分返回 429，不写入库

## 后续方向（本 spec 不实现，仅记录）
- 核心业务（文章/评论/留言）单元测试补齐，controller 逻辑下沉 service
- 文章列表 N+1 查询优化（JOIN 批量填充）+ hot/latest 接口 `@Cacheable`
- 补数据库组合索引（`blog_article(is_publish,create_time)`、`blog_comment(article_id,status)` 等）
- 评论接口分页、敏感词过滤
- 前台 SEO meta、图片懒加载、错误边界
- healthcheck 改用 actuator、MySQL 定时备份脚本、CI/CD 流水线
