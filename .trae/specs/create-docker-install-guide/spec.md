# Docker 环境安装指南 Spec

## Why

用户拥有 Ubuntu 虚拟机环境（用户称 22.5，实际应为 22.04 LTS），需要在上面安装 Docker 与 Docker Compose 以便部署博客系统。当前 README.md 中的安装说明仅为简短代码片段，缺少前置条件检查、多种安装方式、镜像加速配置、防火墙、日志轮转、卸载等完整内容，需要生成一份独立的、可直接照做的 Docker 环境安装文档。

## What Changes

- **新增** 根目录 `Docker环境安装指南.md`，包含以下完整内容：
  - 目标系统说明（Ubuntu 22.04 LTS，注：不存在 22.5 版本）
  - 前置条件检查（系统版本、架构、内核版本、磁盘空间）
  - 卸载旧版本 Docker（如存在）
  - 方式一：官方 APT 仓库安装（推荐，含 GPG 密钥与仓库添加）
  - 方式二：一键脚本安装（适合快速测试）
  - 方式三：Ubuntu 默认仓库安装（版本较旧，备选）
  - 安装 Docker Compose 插件
  - 安装后配置（将用户加入 docker 组免 sudo、配置国内镜像加速器、配置日志轮转限制、设置开机自启）
  - 验证安装（hello-world 测试、版本检查、compose 检查）
  - 防火墙配置（ufw 放行端口）
  - 常见问题排查（权限拒绝、镜像拉取超时、磁盘空间不足、DNS 解析失败、containerd 异常等）
  - 卸载 Docker（含清理数据）
  - 与博客项目对接的快速验证（拉取代码、一键启动）
- **修改** 根目录 `README.md`，在「Docker 部署（Ubuntu 22）」→「环境准备」小节中添加指向 `Docker环境安装指南.md` 的链接

## Impact

- Affected specs: `deploy-with-docker-compose`（该 spec 的「环境准备」部分由新文档提供更详细的指引）
- Affected code:
  - `Docker环境安装指南.md`（新建）
  - `README.md`（修改，添加链接）

## ADDED Requirements

### Requirement: Docker 环境安装指南文档

系统 SHALL 在项目根目录提供 `Docker环境安装指南.md`，针对 Ubuntu 22.04 LTS 虚拟机环境，提供从零开始安装 Docker Engine + Docker Compose 的完整步骤，包含前置检查、三种安装方式、安装后配置（用户组、镜像加速、日志轮转、开机自启）、验证测试、防火墙、常见问题排查与卸载方法，所有命令可直接复制执行。

#### Scenario: 用户在全新 Ubuntu 虚拟机上安装 Docker
- **WHEN** 用户在全新 Ubuntu 22.04 虚拟机上按照 `Docker环境安装指南.md` 逐步执行
- **THEN** 能够成功安装 Docker Engine 与 Docker Compose 插件，配置国内镜像加速，通过 `docker run hello-world` 验证，并以非 root 用户运行 docker 命令

#### Scenario: 用户配置镜像加速解决拉取超时
- **WHEN** 用户在国内网络环境遇到镜像拉取超时
- **THEN** 按照文档「配置国内镜像加速器」小节配置 `daemon.json` 后，重启 Docker 即可正常拉取镜像

#### Scenario: 用户排查权限问题
- **WHEN** 用户执行 docker 命令遇到 `permission denied` 错误
- **THEN** 按照文档「常见问题排查」小节的权限拒绝条目，将用户加入 docker 组并重新登录后解决

### Requirement: README.md 添加文档链接

系统 SHALL 在 README.md 的「Docker 部署（Ubuntu 22）」→「环境准备」小节中添加指向 `Docker环境安装指南.md` 的超链接，方便用户跳转到详细安装指南。

#### Scenario: 用户从 README 跳转到安装指南
- **WHEN** 用户阅读 README.md 的环境准备小节
- **THEN** 能够看到并点击指向 `Docker环境安装指南.md` 的链接，跳转到完整安装文档

## MODIFIED Requirements

### Requirement: 环境准备

README.md「Docker 部署（Ubuntu 22）」→「环境准备」小节保留原有简短安装命令，并在小节开头新增一行提示链接：「详细的 Docker 安装步骤请参考 [Docker环境安装指南.md](Docker环境安装指南.md)」。

## REMOVED Requirements

无
