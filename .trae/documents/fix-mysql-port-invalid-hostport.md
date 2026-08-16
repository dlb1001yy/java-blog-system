# 修复 docker compose `invalid hostPort: 127.0.0.1` 计划

## Summary
上一轮「上线前安全加固」将 mysql 端口映射写为 `"${MYSQL_PORT:-127.0.0.1:3306}"`，导致服务器上 `docker compose up -d --build` 直接报错 `invalid hostPort: 127.0.0.1`，无法部署。根因是 Compose 端口映射语法为 `[host_ip:]host_port:container_port`，两段式值 `127.0.0.1:3306` 被解析为 host_port=`127.0.0.1`（非法端口）。绑定回环必须用完整三段式 `127.0.0.1:3306:3306`。

## Current State Analysis
- [docker-compose.yml:28](d:/my-project/java-blog-system/docker-compose.yml#L28)：`- "${MYSQL_PORT:-127.0.0.1:3306}"` —— 错误写法（报错点）
- [.env.example:22](d:/my-project/java-blog-system/.env.example#L22)：`MYSQL_PORT=127.0.0.1:3306` —— 同样是错误的两段式示例，用户若照抄 .env 复制部署会复现同样报错
- [部署操作手册.md:92/1062](d:/my-project/java-blog-system/部署操作手册.md)、[wiki/部署操作手册.md:87/733](d:/my-project/java-blog-system/wiki/部署操作手册.md)：文档中示例值均为 `127.0.0.1:3306`，需同步修正为三段式并说明格式含义
- 其余服务（redis 6379、backend 8080 等）端口映射是标准两段式 `"host:container"`，无问题，不动

## Proposed Changes

### 1. docker-compose.yml（第 28 行）
```yaml
    ports:
      - "${MYSQL_PORT:-127.0.0.1:3306:3306}"
```
- 默认值改为三段式：host_ip=127.0.0.1、host_port=3306、container_port=3306
- 保留端口收敛语义（仅本机回环可达），修复后 `docker compose up` 可正常启动

### 2. .env.example（第 21-22 行）
```env
# MySQL 宿主机端口映射（格式 host_ip:host_port:container_port）：默认仅绑定本机回环（安全）；需外部连接时改为 3306
MYSQL_PORT=127.0.0.1:3306:3306
```
- 示例值改为三段式，注释补格式说明，避免用户改错

### 3. 部署操作手册.md（2 处）
- 1.5 节变量表（92 行）：默认值列 `127.0.0.1:3306` → `127.0.0.1:3306:3306`
- 第 11 章安全检查表（1062 行）：`MYSQL_PORT 保持 127.0.0.1:3306` → `127.0.0.1:3306:3306`

### 4. wiki/部署操作手册.md（2 处，保持与主手册同步）
- 87 行与 733 行做同样替换

## Assumptions & Decisions
- 不回退端口收敛功能本身，仅修正语法（安全目标不变）
- spec 文档（.trae/specs/harden-security-for-production/*）为历史记录，不修改
- redis 等其它端口映射不动

## Verification
1. 全文 Read docker-compose.yml 确认第 28 行为三段式且缩进正确
2. Grep `127.0.0.1:3306` 确认仓库内所有该模式均已带三段式（`:3306:3306`）
3. 提示用户在服务器重跑 `docker compose up -d --build` 验证（本机无 docker，无法实测）
