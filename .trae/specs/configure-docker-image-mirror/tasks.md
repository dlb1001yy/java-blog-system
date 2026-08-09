# Tasks

- [x] Task 1: 创建镜像加速源模板 `docker/daemon.json`
  - [x] 1.1 创建 `docker/` 目录
  - [x] 1.2 写入合法 JSON，`registry-mirrors` 含 5 个当前可用加速源（docker.xuanyuan.me、docker.1ms.run、docker.m.daocloud.io、docker.1panel.live、dockerproxy.link），无注释、无尾逗号
- [x] Task 2: 编写一键配置脚本 `scripts/configure-docker-mirror.sh`
  - [x] 2.1 创建 `scripts/` 目录，脚本声明 `#!/usr/bin/env bash` 并 `set -euo pipefail`
  - [x] 2.2 root 权限校验：非 root 输出中文提示并以非 0 退出
  - [x] 2.3 备份现有 `/etc/docker/daemon.json` 为 `daemon.json.bak.<时间戳>`
  - [x] 2.4 用 python3 读取仓库模板并合并写入（保留原 `log-opts` 等配置项，仅更新 `registry-mirrors`）
  - [x] 2.5 执行 `systemctl daemon-reload`、`systemctl restart docker`
  - [x] 2.6 打印 `docker info` 的 `Registry Mirrors` 段作为验证输出
- [x] Task 3: 更新文档中的失效镜像源地址
  - [x] 3.1 README.md「Docker 部署」章节：替换失效源为当前列表，并指引一键脚本
  - [x] 3.2 Docker环境安装指南.md 第 6.2/6.3 节及常见问题/完整配置片段：替换失效源
  - [x] 3.3 部署操作手册.md 第 2 节：替换失效源并推荐一键脚本

# Task Dependencies

- [Task 2] 依赖 [Task 1]（脚本读取模板文件）
- [Task 3] 与 [Task 1]/[Task 2] 无强依赖，可并行
