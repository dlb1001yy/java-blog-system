# MinIO 对象存储接入 Spec

## Why
当前后端文件上传默认落在本地磁盘（`storage.type=local`），容器化部署时依赖宿主机数据卷，不便于扩展与备份。用户希望在 Docker 上部署 MinIO（S3 兼容对象存储），后台服务上传文件统一写入 MinIO。

## What Changes
- 新增 MinIO 存储策略：`storage.type=minio` 时装配 `MinioStorageServiceImpl`，上传走 MinIO S3 API
- `StorageProperties` 新增 `MinioConfig` 配置段（endpoint/accessKey/secretKey/bucketName/urlPrefix）
- `docker-compose.yml` 新增 `minio` 服务（API 9000 / 控制台 9001，数据卷持久化，健康检查），`blog-backend` 注入 MinIO 相关环境变量
- `application.yaml` / `application-docker.yaml` 新增 `storage.minio` 配置占位（环境变量注入）
- `.env.example` 新增 MinIO 相关环境变量模板
- 启动时自动创建 Bucket 并设置只读（public download）访问策略，保证图片 URL 可直接访问
- pom.xml 引入 `io.minio:minio` SDK
- **兼容性保证（硬性要求）**：不改动 `WebMvcConfig` 的 `/uploads/**` 静态资源映射，不移除 docker-compose 中 `uploads_data` 卷挂载，local 与 minio 两策略互不影响、可随时双向回切

## Impact
- Affected specs: 无（首次引入 MinIO 能力）
- Affected code:
  - `blog-backend/pom.xml`（新增依赖）
  - `storage/StorageProperties.java`（新增 MinioConfig）
  - `storage/StorageAutoConfiguration.java`（新增 minio 分支）
  - `storage/impl/MinioStorageServiceImpl.java`（新增）
  - `src/main/resources/application.yaml`、`application-docker.yaml`
  - `docker-compose.yml`、`.env.example`
- 兼容性：默认 `storage.type=local` 不变，不切换配置时行为完全不变（无 **BREAKING**）；已有本地上传文件不迁移，切到 minio 后旧 `/uploads/**` URL 仍由现有静态资源映射继续服务

## ADDED Requirements

### Requirement: MinIO 存储策略
系统 SHALL 在 `storage.type=minio` 时使用 MinIO S3 API 上传文件，返回可直接访问的对象 URL。

#### Scenario: 管理后台上传图片到 MinIO
- **WHEN** `storage.type=minio` 且管理员在后台上传图片
- **THEN** 文件以 `yyyy/MM/dd` 日期目录 + 唯一文件名写入 MinIO Bucket，返回 `{url-prefix}/{objectKey}` 形式 URL

#### Scenario: Bucket 自动初始化
- **WHEN** 后端以 minio 策略启动且 Bucket 不存在
- **THEN** 自动创建 Bucket 并设置匿名只读（download）策略，无需手工在控制台操作

#### Scenario: 上传失败降级为业务异常
- **WHEN** MinIO 不可达或凭据错误
- **THEN** 上传接口抛出 `BusinessException`，返回友好错误信息，不影响服务存活

### Requirement: Docker 编排 MinIO 服务
docker-compose SHALL 提供 MinIO 服务供后端容器内网访问，并暴露控制台供宿主机管理。

#### Scenario: docker compose 启动 MinIO
- **WHEN** 执行 `docker compose up -d`
- **THEN** MinIO 容器启动（API 9000、控制台 9001），数据持久化到 `blog_minio_data` 卷，健康检查通过后端才依赖启动

#### Scenario: 后端容器连接 MinIO
- **WHEN** `.env` 中设置 `STORAGE_TYPE=minio` 及 MinIO 凭据
- **THEN** blog-backend 容器通过内网地址 `http://minio:9000` 上传文件，浏览器通过 `MINIO_URL_PREFIX`（默认 `http://<服务器IP>:9000/blog/`）直接访问上传文件

## MODIFIED Requirements

### Requirement: 双存储策略共存与回切兼容（硬性要求）
local 与 minio 两种存储策略 SHALL 完全隔离、互不影响：`storage.type` 仅决定**新上传文件**的落盘位置，不影响任何**已有文件**的访问；两策略间可随时双向切换且无需数据迁移。

#### Scenario: minio 策略下历史本地文件正常展示
- **WHEN** `storage.type=minio` 且文章中存在旧的本地上传图片（URL 形如 `/uploads/2026/08/01/xxx.png`）
- **THEN** 前端图片正常加载（`/uploads/**` 静态资源映射与 uploads 数据卷保持不变，MinIO 不接管该路径）

#### Scenario: minio 回切 local 后本地上传正常
- **WHEN** 曾使用 minio 上传后，将 `storage.type` 改回 `local` 并重启
- **THEN** 本地上传功能行为与接入 MinIO 前完全一致（写入 upload-path、返回 `/uploads/` 前缀 URL），不因 MinIO 相关代码/依赖存在而受影响

#### Scenario: 已写入 MinIO 的文件在回切后仍可访问
- **WHEN** `storage.type` 从 minio 切回 local
- **THEN** 之前上传到 MinIO 的文件 URL（`MINIO_URL_PREFIX` 前缀）仍指向 MinIO 服务，只要 MinIO 容器在运行即可继续访问（MinIO 服务与 storage.type 无关，独立常驻）

#### Scenario: MinIO 配置缺失不影响 local 模式
- **WHEN** `storage.type=local` 且未配置任何 `MINIO_*` 环境变量
- **THEN** 后端正常启动，不创建 MinioClient（按条件装配），local 上传功能不受影响

### Requirement: 文件存储策略配置
`storage.type` 可选值由 `local | oss` 扩展为 `local | oss | minio`（默认仍为 `local`），`storage.minio.*` 配置段结构：

```yaml
storage:
  type: ${STORAGE_TYPE:local}
  minio:
    endpoint: ${MINIO_ENDPOINT:http://localhost:9000}      # 后端访问地址（容器内为 http://minio:9000）
    access-key: ${MINIO_ACCESS_KEY:minioadmin}
    secret-key: ${MINIO_SECRET_KEY:minioadmin}
    bucket-name: ${MINIO_BUCKET_NAME:blog}
    url-prefix: ${MINIO_URL_PREFIX:http://localhost:9000/blog/}  # 浏览器访问前缀
```

## REMOVED Requirements
无
