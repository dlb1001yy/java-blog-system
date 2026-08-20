# 修复生产环境（Docker 部署 192.168.244.130）MinIO 封面图 403

## 问题定位（已验证）

生产环境浏览器访问 `http://192.168.244.130:8081/uploads/2026/08/20/1787211409454_92.png` → **403 Forbidden**。

链路与结论（非开发环境问题，vite 代理无关）：

```
浏览器 → 192.168.244.130:8081（blog-admin nginx 容器）
       → location /uploads/ 反代 → http://minio:9000/blog/<key>   ← 已生效（否则 404/502）
       → MinIO 返回 403 AccessDenied                                ← 根因在此
```

- **nginx 不会自己返回 403**：该 location 无 deny 规则，403 是 MinIO 上游的桶访问拒绝
- **根因——桶的匿名只读策略缺失**：`blog` 桶很可能是此前在 MinIO 控制台（9001）手动创建（默认**私有**）。服务器上运行的旧版后端（统一链路版，commit c99e9d3）只在 `if (!exists)` **新建桶**分支设置策略——桶已存在则永远跳过 → 浏览器匿名 GET → 403。上传不受影响（后端持管理员密钥），故"上传成功、预览 403"
- **本地最新代码已根治但未部署**：commit 75ada1a（幂等策略：桶已存在也补设 + 启动预建桶）已在本地提交，服务器后端镜像未重建
- **mc 可直接使用**：docker-compose.yml:82-83 healthcheck 即 `mc ready local`，容器内 mc 与 `local` alias 就绪

## 改动清单

### 1. 代码改动：无

幂等策略 + 启动预建桶修复已完成（[MinioStorageServiceImpl.java#L146-L172](file:///d:/my-project/java-blog-system/blog-backend/src/main/java/com/dlbyy/blog/storage/impl/MinioStorageServiceImpl.java)），单测断言已同步，无需再改。

### 2. 服务器操作（用户在 192.168.244.130 上执行）

```bash
cd <项目部署目录>   # docker-compose.yml 所在目录

# ①【诊断】确认根因：查看桶当前匿名策略（预期输出 Access permission for 'local/blog' is set to 'none'）
docker exec blog-minio mc anonymous get local/blog

# ②【立即生效】补设匿名只读策略（无需重启任何服务，图片立即可见）
docker exec blog-minio mc anonymous set download local/blog

# ③【根治】拉取最新代码（含幂等策略修复）并重建部署后端
git pull
docker compose build blog-backend && docker compose up -d blog-backend

# ④【验证】
curl -I http://192.168.244.130:8081/uploads/2026/08/20/1787211409454_92.png   # 预期 200
docker logs blog-backend 2>&1 | grep "匿名只读下载策略已设置"                    # 新代码启动即补设
docker exec blog-minio mc anonymous get local/blog                            # 预期 ... to 'download'
```

说明：
- ② 是 `setBucketPolicy` 的 mc 等价命令（canned policy "download" = 匿名 s3:GetObject），与后端 SDK 设置的策略语义一致
- ③ 部署后，未来无论桶是手动建、旧数据卷带过来的，还是新建的，启动时都会自动补设策略，此问题不再复发
- 若服务器代码是本地推送而非 git pull，则替换为：本地 `git push` → 服务器 `git pull`，或直接 scp 更新后重建

### 3. 文档补充：部署操作手册新增 403 排障小节

[部署操作手册.md](file:///d:/my-project/java-blog-system/部署操作手册.md) 在 1.5 节"重要说明"末尾追加一条（与既有 JWT_SECRET/MYSQL 排障条目同格式）：

> - **MinIO 上传文件预览 403**：浏览器访问 `/uploads/**` 返回 403 表示桶匿名只读策略缺失（多因桶在控制台手动创建）。执行 `docker exec blog-minio mc anonymous set download local/blog` 立即修复；新版后端（≥75ada1a）启动时会自动补设策略。注意与 404（反代未生效/对象不存在）区分。

## 假设与决策

1. 服务器部署目录有 git 仓库可 `git pull`；若无可用 scp/rsync 同步代码后重建
2. 不修改 vite.config.js（上轮已指向 `192.168.244.130:8081`，与生产同源，恰好正确）
3. 不做桶内对象迁移/刷库（存量 `/uploads/` 相对 URL 部署新配置后即可访问）
4. 9000 端口无需对公网开放（mc 命令在容器内执行，反代走容器内网）

## 验证步骤

1. 执行 ②后：管理后台（192.168.244.130:8081）刷新文章编辑页 → 封面图显示
2. 执行 ④后：curl 返回 `HTTP/1.1 200 OK` 且 `Content-Type: image/png`
3. 前台门户（8082）与 blog-app（封面 URL `http://gz.aeert.com:19612/uploads/...`，19612 若映射到同一 nginx）同步可见
4. 重启一次 `docker compose restart blog-backend`，日志仍出现策略设置行（幂等验证）
