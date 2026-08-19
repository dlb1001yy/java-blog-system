# Tasks
- [x] Task 1: 后端新增 MinIO 存储策略
  - [x] 1.1 pom.xml 引入 `io.minio:minio`（8.5.10）依赖
  - [x] 1.2 `StorageProperties` 新增 `MinioConfig`（endpoint/accessKey/secretKey/bucketName/urlPrefix）
  - [x] 1.3 新增 `storage/impl/MinioStorageServiceImpl`：实现 `FileStorageService`，懒初始化 Bucket（不存在则创建 + 设置匿名只读策略），putObject 上传，异常包装为 `BusinessException`
  - [x] 1.4 `StorageAutoConfiguration` 新增 `storage.type=minio` 分支，装配 MinioClient 与 MinIO 策略 Bean
  - [x] 1.5 `application.yaml`、`application-docker.yaml` 增加 `storage.minio.*` 环境变量占位配置，`storage.type` 支持 `STORAGE_TYPE` 覆盖
- [x] Task 2: Docker 编排接入 MinIO
  - [x] 2.1 `docker-compose.yml` 新增 minio 服务（minio/minio 镜像、9000/9001 端口、`blog_minio_data` 卷、healthcheck、blog-net 网络）
  - [x] 2.2 `blog-backend` 服务注入 `STORAGE_TYPE`、`MINIO_ENDPOINT`、`MINIO_ACCESS_KEY`、`MINIO_SECRET_KEY`、`MINIO_BUCKET_NAME`、`MINIO_URL_PREFIX` 环境变量，并 depends_on minio（service_healthy）
  - [x] 2.3 `.env.example` 新增 MinIO 环境变量模板段
- [x] Task 3: 双策略兼容性保证（硬性要求）
  - [x] 3.1 确认不改动 `WebMvcConfig` 的 `/uploads/**` 静态资源映射、不移除 docker-compose 的 `uploads_data` 卷挂载（历史本地文件持续可访问）
  - [x] 3.2 确认 MinioClient 仅在 `storage.type=minio` 时按条件创建，local/oss 模式不初始化任何 MinIO 组件
  - [x] 3.3 验证 minio↔local 双向切换：仅改 `STORAGE_TYPE` 重启即生效，新上传走对应策略，旧文件（/uploads/** 与 MinIO URL）均正常展示
- [x] Task 4: 单元测试与验证
  - [x] 4.1 新增 `MinioStorageServiceImplTest`（Mockito 模拟 MinioClient）验证 objectKey 生成、URL 拼接、空文件/上传异常场景
  - [x] 4.2 执行 `mvn test` 确认全部通过（Tests run: 61, Failures: 0, Errors: 0）

# Task Dependencies
- Task 2、Task 3、Task 4 依赖 Task 1 完成后端策略代码；Task 2（compose）、Task 3（兼容性）、Task 4（测试）可并行

# 实施备注
- minio 镜像固定为 `RELEASE.2025-04-22T22-12-26Z`：2025-05-24 起社区版移除了 Web 控制台，latest 会导致 9001 无管理界面
