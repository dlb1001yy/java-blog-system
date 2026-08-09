# Tasks

- [x] Task 1: 创建镜像导出脚本 `scripts/export-images.sh`
  - [x] 1.1 创建 `scripts/` 目录（若不存在）
  - [x] 1.2 脚本声明 `#!/usr/bin/env bash` + `set -euo pipefail`，定位项目根目录并 `mkdir -p images/`
  - [x] 1.3 定义 6 个 Docker Hub 基镜像数组（mysql:8.0、redis:7-alpine、node:18-alpine、nginx:1.25-alpine、eclipse-temurin:17-jdk-jammy、eclipse-temurin:17-jre-jammy），并含被注释的 ES 可选开关
  - [x] 1.4 逐个 `docker pull` + `docker save`（文件名将 `/`、`:` 替换为 `_`），结尾输出完成提示
- [x] Task 2: 创建镜像导入脚本 `scripts/load-images.sh`
  - [x] 2.1 脚本声明 `#!/usr/bin/env bash` + `set -euo pipefail`
  - [x] 2.2 校验 `images/` 目录存在且非空，否则中文错误提示并以非 0 退出
  - [x] 2.3 遍历 `images/*.tar` 逐个 `docker load -i`
- [x] Task 3: `.gitignore` 添加 `/images/`
- [x] Task 4: 文档补充离线部署步骤
  - [x] 4.1 README.md「Docker 部署」章节新增「离线镜像部署」小节
  - [x] 4.2 部署操作手册.md 第 3 节「基础镜像预拉取」补充离线方式

# Task Dependencies

- [Task 1]/[Task 2]/[Task 3]/[Task 4] 相互独立，可并行
