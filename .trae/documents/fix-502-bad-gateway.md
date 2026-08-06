# 修复部署后 API 502 — 前端容器到后端 8080 连接被拒

## Summary
博客系统部署 Ubuntu 后，blog-admin / blog-frontend 的 API 请求全部 502。用户提供的 **nginx 错误日志是铁证**：

```
[error] connect() failed (111: Connection refused) while connecting to upstream
upstream: "http://172.18.0.5:8080/api/auth/login"
```

**说明**：
- Docker 内部 DNS 把 `blog-backend` 解析到 `172.18.0.5` ✅ 正确
- `172.18.0.5:8080` 端口 **拒绝连接**（111 Connection refused）

**真因**：前端 nginx 容器启动时，blog-backend 容器还没就绪（8080 未监听），nginx 把 DNS 解析结果**缓存了失效的 IP**；或者 blog-backend 容器在中间重启/崩了，IP 变了但前端 nginx 不重读 DNS。本质是**容器启动顺序竞争 + nginx DNS 缓存**。

## Current State Analysis
- docker-compose.yml 编排正确：`depends_on: blog-backend: condition: service_healthy`
- nginx.conf 正确：`proxy_pass http://blog-backend:8080`（Docker 内部服务名）
- 8080 端口能从浏览器直连 → 后端本身正常，但前端容器请求的 `172.18.0.5:8080` 拒绝连接 → 两种可能：
  1. 后端容器在启动前端容器之后重启了，新 IP 变了
  2. 后端容器虽健康但其 8080 端口未暴露给 `blog-net` 网络（可能性低，但需验证）

## Proposed Changes

### 改动 1：nginx 加 resolver + proxy_pass 用变量（根治 DNS 缓存，最关键）
**文件**：`blog-admin/nginx.conf`、`blog-frontend/nginx.conf`

**问题根因**：nginx 启动时一次性把 `blog-backend` 解析为 IP，之后**永久缓存**不重读。后端容器一旦重启/IP 变更 → 前端 nginx 打到旧 IP → 502。

**修复**：在 `http` 或 `server` 块加 Docker DNS resolver，且 `proxy_pass` 改用**变量**写法（强制 nginx 每次请求都走 resolver 解析）：

```nginx
server {
    listen       80;
    server_name  _;

    # Docker 内网 DNS（强制 nginx 每次请求重读 DNS，避免缓存失效 IP）
    resolver 127.0.0.11 valid=10s ipv6=off;

    root   /usr/share/nginx/html;
    index  index.html;
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript image/svg+xml;
    gzip_min_length 1024;

    # /api 反代：proxy_pass 用变量写法，配合 resolver 动态解析
    location /api {
        set $backend http://blog-backend:8080;
        proxy_pass $backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        client_max_body_size 50m;
    }
    # ... 其余 location 不变
}
```

**关键点**：
- `resolver 127.0.0.11` 是 Docker 内置 DNS 服务地址（固定）
- `valid=10s` TTL 10 秒，避免缓存过期 IP
- `set $backend http://blog-backend:8080;` + `proxy_pass $backend;` 变量写法强制每次解析
- **不用变量写法的话，resolver 不生效**

### 改动 2：nginx 加 upstream 健康检查 + 失败重试（容错双保险）
**文件**：同上两个 nginx.conf

在 server 块外（http 上下文）定义 upstream，加 `max_fails` / `fail_timeout` 自动摘除不可用的后端：

```nginx
# blog-admin / blog-frontend 各自的 nginx.conf 顶部（server 块外）
upstream blog_backend {
    server blog-backend:8080 max_fails=3 fail_timeout=10s;
    # 单节点时 keepalive 可提升性能
    keepalive 16;
}
```

然后把 `location /api` 改为：
```nginx
location /api {
    set $backend http://blog_backend;
    proxy_pass $backend;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_set_header Connection "";
    proxy_http_version 1.1;
    client_max_body_size 50m;

    # 后端响应失败时重试一次
    proxy_next_upstream error timeout http_502 http_503;
    proxy_next_upstream_tries 2;
}
```

**Why**：
- `max_fails=3 fail_timeout=10s`：连续 3 次失败 → 摘除 10 秒 → 再尝试，避免持续打到挂掉的后端
- `keepalive 16` + `proxy_http_version 1.1` + `Connection ""`：复用 TCP 连接，减少建连开销
- `proxy_next_upstream`：后端返回 502/503 时自动重试

### 改动 3：docker-compose.yml 加容器重启策略强化
**文件**：`docker-compose.yml`

给 blog-backend 加 `deploy.restart_policy` 确保后端挂了立即重启（前端 DNS 缓存最多 10s，后端快速重启可降低 502 窗口）：

```yaml
blog-backend:
  # ... 现有配置不变
  restart: unless-stopped  # 已有，保持
  # 无需额外加，restart: unless-stopped 已覆盖
```

> 注：`restart: unless-stopped` 已在原 compose 中配置，无需改动。本项仅验证确认。

### 改动 4（可选但推荐）：nginx 加健康检查 location
**文件**：两个 nginx.conf 的 server 块内

```nginx
# 健康检查端点（前端容器自身是否 OK，用于排查）
location /healthz {
    access_log off;
    return 200 'ok';
    add_header Content-Type text/plain;
}
```

**Why**：在服务器上 `curl http://localhost:8081/healthz` 确认前端 nginx 本身 OK，排除 nginx 自身故障。

## Assumptions & Decisions
- **假设**：用户用 `docker compose up -d --build` 启动，端口 8081:80 / 8082:80 / 8080:8080
- **假设**：`172.18.0.5` 是 blog-backend 容器在启动早期的 IP，后端之后可能重启/换 IP → nginx 缓存旧 IP → 502
- **决定**：不预设后端已死，而是给出自查步骤 + 根治 DNS 缓存。
- **决定**：不引入新工具/新依赖，纯 nginx 配置 + compose 验证
- **不处理**：不改前端业务代码、不改后端代码、不修改 nginx 静态资源 location

## Verification
1. 用户在服务器上执行：
```bash
# 看当前 blog-backend 的实际 IP
docker inspect blog-gateway... # 取 Networks.blog-net.IPAddress
# 确认前端容器能否解析+连通
docker exec blog-admin bash -c "getent hosts blog-backend && curl -s -o /dev/null -w '%{http_code}' http://blog-backend:8080/api/portal/stats"
```

2. 用户改完 nginx.conf 后：
```bash
docker compose down && docker compose up -d --build
# 等 30 秒让后端就绪
curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/api/portal/stats
# 期望 200
```

3. 浏览器访问 http://192.168.244.130:8081/admin/ 登录成功
4. 浏览器访问 http://192.168.244.130:8082/ 文章列表加载成功

5. 模拟后端重启验证 DNS 缓存已修复：
```bash
docker restart blog-gateway
sleep 3
curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/api/portal/stats
# 修复前：502（旧 IP）；修复后：200（自动解析新 IP）
```
