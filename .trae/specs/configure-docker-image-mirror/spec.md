# Docker 镜像加速一键配置 Spec

## Why

用户部署博客系统时，docker-compose.yml 所需的中间件镜像（mysql:8.0、redis:7-alpine、nginx、eclipse-temurin、node 等 Docker Hub 镜像）在国内网络环境下下载极慢（经常 <100KB/s、超时、429 限流）。项目现有文档中推荐的加速源（USTC `docker.mirrors.ustc.edu.cn`、网易 `hub-mirror.c.163.com`）已于 2024-2026 年间先后停止同步 Docker Hub，属于**失效源**，配置后不但无加速效果，反而因逐个尝试超时拖慢拉取。用户已确认采用 **daemon.json 一键脚本**方案（系统级 registry-mirrors，对所有镜像生效，含 Dockerfile 构建时拉取的基础镜像）。

## What Changes

- **新增** `docker/daemon.json`：当前（2026-08）实测可用的国内镜像加速源列表模板（纯合法 JSON，无注释）：
  `docker.xuanyuan.me`、`docker.1ms.run`、`docker.m.daocloud.io`、`docker.1panel.live`、`dockerproxy.link`
  （多源 failover：Docker 按顺序尝试，失败自动切换下一个）
- **新增** `scripts/configure-docker-mirror.sh`：Ubuntu 一键配置脚本，执行流程：
  1. 校验 root 权限（无则提示 `sudo`）
  2. 备份现有 `/etc/docker/daemon.json` 为 `daemon.json.bak.<时间戳>`
  3. 读取仓库内 `docker/daemon.json` 模板，用 python3 **合并**写入 `/etc/docker/daemon.json`（保留原文件中的 `log-opts` 等其他已有配置项，避免破坏日志轮转设置）
  4. `systemctl daemon-reload` + `systemctl restart docker`
  5. 输出 `docker info` 中 `Registry Mirrors` 段验证生效
- **修改** 现有文档中已失效的镜像源地址，替换为当前可用列表并指引一键脚本：
  - `README.md`「Docker 部署」章节的镜像加速器配置片段
  - `Docker环境安装指南.md` 第 6.2/6.3 节及常见问题中的 daemon.json 片段
  - `部署操作手册.md` 第 2 节「Docker 镜像加速器配置」

## Impact

- Affected specs: 无（本次为新增部署工具，非文档类 spec 的继续）
- Affected code:
  - `docker/daemon.json`（新建）
  - `scripts/configure-docker-mirror.sh`（新建）
  - `README.md`（修改，替换失效镜像源）
  - `Docker环境安装指南.md`（修改，替换失效镜像源）
  - `部署操作手册.md`（修改，替换失效镜像源）
- 已知限制（不改动）：registry-mirrors 仅作用于 Docker Hub（docker.io）；elasticsearch 镜像来自 `docker.elastic.co`，不受本方案加速，且该服务处于可选 `search` profile，不在本次范围。

## ADDED Requirements

### Requirement: 镜像加速源模板

系统 SHALL 在 `docker/daemon.json` 提供合法的 JSON 模板，`registry-mirrors` 数组包含 ≥4 个当前可用的国内镜像加速源，Docker 会按序尝试并在单个源失败时自动切换，用于加速 docker-compose.yml 与 Dockerfile 所需的全部 Docker Hub 镜像拉取。

#### Scenario: 检查模板有效性
- **WHEN** 执行 `python3 -m json.tool docker/daemon.json`
- **THEN** 输出 JSON 合法且 `registry-mirrors` 含 ≥4 个 https 加速源地址

#### Scenario: 手动配置
- **WHEN** 用户将模板内容复制到 `/etc/docker/daemon.json` 并 `systemctl restart docker`
- **THEN** `docker info` 的 `Registry Mirrors` 段显示已配置的加速源列表

### Requirement: 一键配置脚本

系统 SHALL 提供 `scripts/configure-docker-mirror.sh`，以 root 权限在 Ubuntu（systemd + docker ce）环境下一条命令完成：备份原 daemon.json → 合并写入新加速源（保留原有 `log-opts` 等配置）→ 重启 Docker → 打印验证结果。脚本须支持非交互式执行，任一前置缺失（非 root、无 docker、无 python3）时给出明确中文提示并安全退出（不破坏原配置）。

#### Scenario: 首次配置
- **WHEN** 服务器尚无 `/etc/docker/daemon.json`，执行 `sudo bash scripts/configure-docker-mirror.sh`
- **THEN** 生成新的 daemon.json（含加速源 + 无其他多余配置），Docker 重启后 `docker info` 显示加速源生效

#### Scenario: 已有配置时执行（幂等合并）
- **WHEN** 服务器已有 daemon.json（含 `log-opts` 日志轮转配置），再次执行脚本
- **THEN** 原文件先备份为 `daemon.json.bak.<时间戳>`，新文件保留 `log-opts` 等原配置项、仅更新 `registry-mirrors` 列表，Docker 重启成功

#### Scenario: 权限不足
- **WHEN** 以非 root 用户直接执行脚本
- **THEN** 输出中文提示「请使用 sudo 运行本脚本」并以非 0 退出码结束，不改动任何文件

### Requirement: 文档镜像源一致性

系统 SHALL 将 README.md、Docker环境安装指南.md、部署操作手册.md 中已失效的镜像源地址（`docker.mirrors.ustc.edu.cn`、`hub-mirror.c.163.com` 等）替换为当前可用列表，并在脚本相关位置补充指向一键脚本的使用说明，避免用户按旧文档配置后仍然拉取缓慢。

#### Scenario: 用户按文档配置
- **WHEN** 用户阅读 README.md 的镜像加速器章节
- **THEN** 看到的是当前可用的加速源列表，且推荐优先使用 `scripts/configure-docker-mirror.sh` 一键配置

## MODIFIED Requirements

无

## REMOVED Requirements

无
