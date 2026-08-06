# 修复 blog-admin 登录接口 502（Connection refused）

## 一、问题摘要

请求 `POST /api/auth/login` 时，blog-admin 容器内的 nginx 返回 502：

```
connect() failed (111: Connection refused) while connecting to upstream
upstream: "http://172.18.0.5:8080/api/auth/login"
```

即 nginx 无法连上后端 `blog-backend:8080`。这属于 **运行时环境问题**（Docker 网络/容器状态），不是接口逻辑问题 —— nginx 配置与后端 context-path 的路径拼接（`/api` 前缀透传 + `server.servlet.context-path: /api`）均已核实正确。

Docker 运行在 Ubuntu 服务器 `192.168.244.130`（本机 Windows 无 docker，客户端 IP `192.168.244.1` 即本机）。**诊断命令需在服务器上执行**。

## 二、现状分析（已核实的事实）

| 文件                                                                                                                              | 关键内容                                                                          |
| ------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------- |
| [nginx.conf](file:///d:/my-project/java-blog-system/blog-admin/nginx.conf#L14-L22)                                              | `/api` 代理到 `proxy_pass http://blog-backend:8080;`，**无** **`resolver`** **指令** |
| [blog-frontend/nginx.conf](file:///d:/my-project/java-blog-system/blog-frontend/nginx.conf#L14-L22)                             | 同样代理 `/api`，同样无 `resolver`                                                    |
| [docker-compose.yml](file:///d:/my-project/java-blog-system/docker-compose.yml#L82-L124)                                        | blog-backend 监听 8080 并暴露；blog-admin `depends_on` blog-backend healthy         |
| [application-docker.yaml](file:///d:/my-project/java-blog-system/blog-backend/src/main/resources/application-docker.yaml#L3-L6) | 后端端口 8080，context-path `/api`                                                 |

**核心机制问题**：nginx 对 `proxy_pass http://blog-backend:8080` 这种**静态主机名**只在**启动时解析一次并缓存 IP**（无 `resolver` 指令时不会重新解析）。而 Docker Compose 重建容器（`docker compose up -d --build` 时仅重建变更服务，或容器被 recreate）后，blog-backend 会获得**新 IP**。此时仍运行的 blog-admin nginx 继续连接旧 IP → `Connection refused`。

错误日志中的 `172.18.0.5` 很可能是**过期的旧容器 IP**（今天的多次 rebuild 会不断变更容器 IP）。

### 关键证据（用户确认）

`http://192.168.244.130:8080/api/portal/stats` 直连（宿主机发布端口 8080 → blog-backend）**返回正常** → 后端应用存活且在监听 8080。

**结论：排除"后端崩溃/未监听"根因。** 问题仅出在 blog-admin 内 nginx 到后端的连通：nginx 日志里的 `172.18.0.5` 是**过期 IP**（blog-backend 容器在今天多次 `--build` 重建中已更换 IP，而 blog-admin 的 nginx 启动时只解析一次并缓存）。两种触发场景：

1. **（主因）nginx 缓存过期上游 IP**：blog-backend 重建后 IP 变化，blog-admin 未随同重建/重启，仍连接旧 IP `172.18.0.5` → Connection refused。
2. **（次要）13:51 时后端正处于重建窗口**：当时容器短暂不可用；之后已恢复（所以现在直连正常），但 nginx 报错后用户才看到日志。

## 三、诊断步骤（在服务器 192.168.244.130 上执行，确认即修）

后端已被直连验证存活，诊断只需一步确认过期 IP：

```bash
# 对比 blog-backend 当前实际 IP 与 nginx 日志里的 172.18.0.5
docker inspect blog-backend -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}'
```

* 若当前 IP **≠ 172.18.0.5** → 坐实"nginx 缓存过期 IP"，执行第四节修复。

* 若当前 IP **== 172.18.0.5** → 说明当时后端短暂不可用（重建窗口），现在已恢复，仅需 `docker compose restart blog-admin` 重新建立连接缓存即可。

## 四、修复方案

### 1. 临时恢复（立即可用）

在服务器上重启 blog-admin，让 nginx 重新解析后端 IP：

```bash
docker compose restart blog-admin
```

### 2. 永久修复（推荐，防止再次发生）

让 nginx 通过 **Docker 内置 DNS（127.0.0.11）每次请求时动态解析**，不再缓存过期 IP。修改两个文件：

**[blog-admin/nginx.conf](file:///d:/my-project/java-blog-system/blog-admin/nginx.conf)** **与** **[blog-frontend/nginx.conf](file:///d:/my-project/java-blog-system/blog-frontend/nginx.conf)** —— 将 `/api` location 改为：

```nginx
location /api {
    # Docker 内置 DNS，让 nginx 按需重新解析 blog-backend，避免容器重建后 IP 变化导致 502
    resolver 127.0.0.11 valid=30s ipv6=off;
    set $backend_upstream http://blog-backend:8080;
    proxy_pass $backend_upstream;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    client_max_body_size 50m;
}
```

**原理**：`proxy_pass` 使用变量时，nginx 会为每次请求执行 DNS 解析（走 `resolver` 指定的 Docker DNS），从而始终拿到 blog-backend 的当前 IP。这是 Docker + nginx 场景的标准做法。

### 3. 重新构建生效

```bash
git pull
docker compose up -d --build blog-admin blog-frontend
```

（若根因是崩溃重启，则还需先解决 blog-backend 应用问题再重建；本方案不修改 blog-backend。）

## 五、验证

1. 重新执行 `POST /api/auth/login`，预期返回 200 及 token（不再 502）。
2. 观察 nginx 错误日志不再新增 `connect() failed ... blog-backend` 记录：

   ```bash
   docker logs --tail 20 blog-admin
   ```
3. 验证动态解析生效：

   ```bash
   docker compose restart blog-backend   # 模拟容器重建（IP 会变化）
   # 再次登录，应仍正常
   ```

## 六、假设与决策

* **已确认**：blog-backend 应用运行正常（直连 `:8080` 可访问），问题仅在 nginx→后端连通性；根因是 nginx 启动时缓存的 `blog-backend` 容器 IP 已过期，或 13:51 时后端处于重建窗口。

* **决策**：采用 `resolver + 变量 proxy_pass` 动态解析方案，从根上避免"容器重建后 IP 变化导致 502"再次发生；临时先用 `docker compose restart blog-admin` 恢复。

* **不改动**：docker-compose.yml、blog-backend 代码、后端端口/context-path 配置（均已核实正确）。

