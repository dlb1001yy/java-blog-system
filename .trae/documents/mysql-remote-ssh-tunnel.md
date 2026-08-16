# 计划：MySQL 远程连接采用 SSH 隧道方案（方式 A）并固化到项目文档

## Summary

将"远程连接服务器 Docker MySQL"的标准做法固化为 **SSH 隧道方式（服务器零改动，3306 不暴露公网）**，写入部署文档与环境变量模板注释，替代当前文档中"改为 3306 直接暴露"的单一指引。纯文档变更，不改任何代码与服务器配置。

## Current State Analysis

- [docker-compose.yml L27-28](d:/my-project/java-blog-system/docker-compose.yml)：MySQL 端口映射 `${MYSQL_PORT:-127.0.0.1:3306:3306}`，默认仅绑定服务器本机回环 —— SSH 隧道方案下**保持不变**
- [.env.example L21-22](d:/my-project/java-blog-system/.env.example)：注释仅给出方式 B（"需外部连接时改为 3306"），未提 SSH 隧道
- [部署操作手册.md L92](d:/my-project/java-blog-system/部署操作手册.md)：变量表 MYSQL_PORT 行同样只写"需外部连接时改为 3306"
- [部署操作手册.md L914-934](d:/my-project/java-blog-system/部署操作手册.md)（9.7 进入容器排查）：是最接近"数据库运维"语境的小节，其后是第 10 节监控
- 手册目录（L9-21）为编号列表，大节编号不宜打乱

## Proposed Changes

### 1. 部署操作手册.md — 新增 9.8 节「远程连接数据库（SSH 隧道，推荐）」

位置：9.7 节代码块结束后（L934 ` ``` ` 之后、`---` 与第 10 节之前）。

内容（保持手册既有 markdown 风格：三级标题 + bash 代码块 + 预期输出注释）：
- **原理说明**：MySQL 默认仅绑定 `127.0.0.1:3306`，SSH 隧道经 22 端口加密转发，无需修改 `MYSQL_PORT`、无需在安全组放行 3306
- **方式一：GUI 工具内置 SSH 通道**（Navicat / DataGrip / DBeaver）：
  - 常规页：主机 `127.0.0.1`、端口 `3306`、用户 `root`、密码 = 服务器 `MYSQL_ROOT_PASSWORD`、库 `dlbyy_zp_blog`
  - SSH 页：服务器 IP、端口 22、SSH 账号密码或私钥
- **方式二：命令行建立隧道**：
  ```bash
  ssh -L 3306:127.0.0.1:3306 root@<服务器IP> -N
  ```
  保持窗口开启，本地工具连 `localhost:3306`；本机 3306 被占用时改用 `-L 13306:127.0.0.1:3306` 并连接 13306
- **常见问题**：
  - MySQL 8 `caching_sha2_password` 认证：工具需 MySQL 8 驱动；报 `Public Key Retrieval is not allowed` 时在驱动高级选项勾选允许
  - 若 root 仅允许 localhost 登录（老数据卷账号）：`docker exec blog-mysql mysql -uroot -p*** -e "CREATE USER ... / GRANT"` 或继续用隧道内 127.0.0.1 即可
- **不推荐**：把 `MYSQL_PORT` 改为 `3306` 直接暴露公网（方式 B），仅内网环境且无法 SSH 时考虑，并须在安全组限制来源 IP

### 2. 部署操作手册.md — 更新 L92 变量表 MYSQL_PORT 行

说明末尾追加：`远程连接推荐使用 SSH 隧道（见 9.8），无需修改本项`。

### 3. .env.example — 更新 L21-22 MYSQL_PORT 注释

改为两行注释：首推 SSH 隧道（参考部署手册 9.8）保持默认回环绑定；仅在无法 SSH 的内网环境才改为 `3306` 并配合安全组来源限制。变量默认值 `127.0.0.1:3306:3306` 不变。

### 4. README.md — 检查是否需要同步（仅当存在"远程连接/数据库访问"相关指引时补一句指向手册 9.8）

Phase 1 已确认 README L608 端口表中 mysql 行为"3306 | 3306 | 数据库（可选关闭）"，在第 11 节上线前安全检查或端口表附近补一行"远程访问走 SSH 隧道（手册 9.8）"；若语境不合适则跳过，不强加。

## Assumptions & Decisions

- **服务器侧零改动**：不改 docker-compose.yml、不改 .env 默认值、不动安全组 —— 这正是方式 A 的定义
- 文档语言与现有手册一致（中文、bash 代码块）
- 不创建新文档，全部为既有文件的小节追加/注释更新
- 手册大节编号（1-11）保持不变，新内容作为 9.8 小节，避免目录大改；目录列表（L9-21）仅列大节，无需改动

## Verification steps

1. 通读手册 9.7 → 9.8 → 第 10 节衔接，确认格式（标题层级、代码块闭合）正确
2. 核对 9.8 中连接参数与 [docker-compose.yml](d:/my-project/java-blog-system/docker-compose.yml) 实际值一致：容器名 blog-mysql、root 密码取 `MYSQL_ROOT_PASSWORD`、库名（首次部署 `dlbyy_z`，老部署 `dlbyy_zp_blog`，手册现有 L89 已有此区分，9.8 沿用）
3. `.env.example` 修改后确认 YAML/ENV 语法未被破坏（纯注释行）
4. 按 9.8 命令行示例在本机模拟：`ssh -L 13306:127.0.0.1:3306 <user>@<host> -N` 建立隧道后 `mysql -h127.0.0.1 -P13306 -uroot -p` 能连通（用户有服务器时自测；无服务器时以文档评审为准）
