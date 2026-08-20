# 修复 MinIO 封面图预览 404（nginx 反代方案 + 桶策略幂等）

## 问题诊断（根因）

上传/导入文章后返回的封面 URL `http://localhost:9000/blog/2026/08/20/xxx.png` 无法展示：

1. **主因——URL 前缀指向访问者本机**：`MINIO_URL_PREFIX` 用了默认值 `http://localhost:9000/blog/`（[docker-compose.yml:139](file:///d:/my-project/java-blog-system/docker-compose.yml)）。`<img>` 中的 localhost 由**浏览器**解析：管理员在开发机访问后台（vite localhost:3001），localhost = 开发机，本机 9000 无 MinIO → 裂图。而上传成功是因为后端走 `MINIO_ENDPOINT`（容器内 `http://minio:9000`）——上传与下载是两条独立链路，"上传成功但预览失败"由此而来。MinIO 实际部署在远程服务器 gz.aeert.com。
2. **次因——桶策略仅新建时设置**：[MinioStorageServiceImpl.java](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/storage/impl/MinioStorageServiceImpl.java) `ensureBucketExists` 中 `setBucketPolicy` 在 `if (!exists)` 分支内；桶已存在则匿名只读策略缺失，地址可达也 403。

用户决策：**nginx 反代**（不暴露 9000）、**不刷库**、**顺带修复桶策略幂等**。

## 方案设计

两个前端容器（blog-admin/blog-frontend 的 nginx）均在 `blog-net` 内网，可直接以容器名 `minio` 访问 MinIO。在两份 nginx.conf 增加 `location /uploads/` 反代到 `http://minio:9000/blog/`；`MINIO_URL_PREFIX` 改为**相对前缀 `/uploads/`**，三类消费端均可解析：

| 消费端 | URL 解析 | 可行性 |
|---|---|---|
| blog-admin 生产 | 同源（nginx 托管 admin 静态资源），`/uploads/...` 相对请求命中本容器 nginx → minio | ✅ |
| blog-frontend 生产 | 同上（nginx 托管 /blog） | ✅ |
| blog-admin 开发（vite:3001） | vite.config.js 新增 `/uploads` 代理 → 服务器 nginx | ✅（本任务补充） |
| blog-app | [resolveFileUrl](file:///d:/my-project/java-blog-system/blog-app/common/config.js#L32-L37) 对 `/` 开头路径拼 `SERVER_ORIGIN`（`http://gz.aeert.com:19612`） | ✅ 既有约定 |

与 local 模式的 `/api/uploads/`（后端静态映射）路径不冲突：nginx `location /uploads/` 与 `location /api` 互不干扰。

## 改动清单

### 1. blog-admin/nginx.conf — 新增 /uploads/ 反代
[blog-admin/nginx.conf](file:///d:/my-project/java-blog-system/blog-admin/nginx.conf) `/api` location 之后新增：
```nginx
# MinIO 对象存储反代（封面图等上传文件的浏览器访问，/uploads/<key> → minio <bucket>/<key>）
location /uploads/ {
    resolver 127.0.0.11 valid=30s ipv6=off;
    set $minio_upstream http://minio:9000;
    proxy_pass $minio_upstream/blog/;
    proxy_set_header Host $host;
    client_max_body_size 50m;
}
```
（`proxy_pass .../blog/` 尾部斜杠：`/uploads/2026/...` → `/blog/2026/...`，即桶名映射；与 /api location 同款 resolver+变量写法，避免容器重建 IP 变化）

### 2. blog-frontend/nginx.conf — 同样新增
[blog-frontend/nginx.conf](file:///d:/my-project/java-blog-system/blog-frontend/nginx.conf) `/api` location 之后新增相同片段（前台门户文章封面同样走此链路）。

### 3. blog-admin/vite.config.js — 开发环境代理
[vite.config.js:13-20](file:///d:/my-project/java-blog-system/blog-admin/vite.config.js) 的 proxy 增加：
```js
'/uploads': {
    target: 'http://gz.aeert.com:8081',  // 服务器 blog-admin nginx（已含 /uploads 反代）
    changeOrigin: true
}
```
（若 8081 未对公网开放，备选 `target: 'http://gz.aeert.com:9000'` + `rewrite: p => p.replace(/^\/uploads/, '/blog')` 直连 MinIO，仅开发用）

### 4. MinioStorageServiceImpl — 桶策略幂等
`ensureBucketExists()` 中把 `setBucketPolicy` 移出 `if (!exists)`：桶存在时也补设一次匿名只读策略（策略是覆盖式 set，幂等安全），消除"桶已存在但策略缺失 → 403"。日志相应调整（新建时"已创建并设置策略"，已存在时"已存在，补设匿名只读策略"）。

### 5. 单测同步
[MinioStorageServiceImplTest.java](file:///d:/my-project/java-blog-system/blog-backend/src/test/java/com/dlbyy/blog/storage/impl/MinioStorageServiceImplTest.java)：
- 用例 2（bucket 已存在）：期望从 `never().setBucketPolicy` 改为 `times(1).setBucketPolicy`（幂等补设）
- 用例 3（幂等）：第二次上传后 `setBucketPolicy` 仍为 1 次（bucketReady 短路，不重复 set）
- saveBytes 用例 6/7 不受影响（bucket 不存在场景）

### 6. 配置默认值与模板
- [docker-compose.yml:139](file:///d:/my-project/java-blog-system/docker-compose.yml)：`MINIO_URL_PREFIX` 默认值改 `/uploads/`，注释说明走 nginx 反代
- [application.yaml:117](file:///d:/my-project/java-blog-system/blog-backend/src/main/resources/application.yaml) 与 [application-docker.yaml:75](file:///d:/my-project/java-blog-system/blog-backend/src/main/resources/application-docker.yaml)：`${MINIO_URL_PREFIX:/uploads/}` 同步
- [.env.example](file:///d:/my-project/java-blog-system/.env.example)：MINIO_URL_PREFIX 值与注释更新（说明相对前缀 /uploads/ 由 nginx 反代到 MinIO，无需暴露 9000 端口）

### 7. 文档
[部署操作手册.md](file:///d:/my-project/java-blog-system/部署操作手册.md) 1.5 节 `MINIO_URL_PREFIX` 行：更新为"默认 `/uploads/`（nginx 反代到 MinIO，无需暴露 9000）；如需直链可改为 `http://<服务器IP>:9000/blog/`"。

## 服务器操作（用户手动，部署时执行）

```bash
# .env 中设置（或删除该行用新默认值）
MINIO_URL_PREFIX=/uploads/

# 重建含 nginx 配置的前端镜像并重启
docker compose build blog-admin blog-frontend
docker compose up -d blog-backend blog-admin blog-frontend
```

存量数据不刷库：已入库的 localhost 封面重新导入/上传即可覆盖。

## 假设与决策

1. 不暴露 9000 端口公网（服务器防火墙可保持关闭 9000 对外）
2. 已入库的 localhost URL 不做 SQL 刷新（用户决策）
3. blog-app H5 开发调试（localhost:8080 直连后端）下 `/uploads/` 相对路径不可达属既有模式限制，不在本任务范围（生产 app 走 gz.aeert.com 无此问题）
4. vite 代理目标默认 `gz.aeert.com:8081`；若防火墙未放行则用备选直连方案（注释中给出）

## 验证步骤

1. **编译**：javac（JDK 17.0.8 + 本地仓库 jar）编译 MinioStorageServiceImpl 及测试类，EXIT=0
2. **单测**：`mvn test -Dtest=MinioStorageServiceImplTest`（用户本机执行；策略幂等断言更新后 7 用例全绿）
3. **部署后端到端**（用户服务器）：
   - `curl -I http://gz.aeert.com:8081/uploads/2026/08/20/1787210044996_2987.png` → 200（nginx→minio 反代链路通）
   - 管理后台重新导入 Markdown → 封面 URL 应为 `/uploads/2026/...`，预览正常显示
   - 前台门户文章封面、blog-app（重新编译的 APK/H5）封面正常
4. **桶策略幂等**：MinIO 已存在桶的场景，重启后端日志应出现"补设匿名只读策略"；`curl -I` 未签名直链 200（非 403）
