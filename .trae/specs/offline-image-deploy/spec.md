# 离线镜像部署（提前导出镜像）Spec

## Why

用户在服务器上通过 docker compose 拉取中间件镜像仍然很慢（即便已配置镜像加速器）。用户计划在**另一台有外网且装有 Docker 的 Linux/macOS 机器**上提前 `docker pull` 并用 `docker save` 导出全部所需镜像，将 `images/` 目录拷贝到服务器后 `docker load` 直接使用，服务器无需再联网下载镜像。

## What Changes

- **新增** `scripts/export-images.sh`：外网机器导出脚本（逐个 `docker pull` + `docker save` 到项目根目录 `images/*.tar`），文件名安全化（`/`、`:` 替换为 `_`），幂等可重复执行
- **新增** `scripts/load-images.sh`：服务器导入脚本（遍历 `images/*.tar` 逐个 `docker load`），目录缺失/为空时给出中文错误提示并以非 0 退出
- **修改** 根目录 `.gitignore`：添加 `/images/`（tar 体积大，不入版本库）
- **修改** `README.md`：在「Docker 部署」章节补充「离线镜像部署」小节（导出 → 拷贝 → 加载 → 启动）
- **修改** `部署操作手册.md`：第 3 节「基础镜像预拉取」补充离线方式

## Impact

- Affected specs: 无（本次为新增离线部署能力）
- Affected code:
  - `scripts/export-images.sh`（新建）
  - `scripts/load-images.sh`（新建）
  - `.gitignore`（修改）
  - `README.md`（修改）
  - `部署操作手册.md`（修改）
- 已知限制（不改动）：docker save/load 只能解决**镜像层**下载。服务器构建阶段仍有少量 URL 下载需外网，但已有国内镜像源加速且体积小：
  - 后端 Maven 依赖 → 阿里云 maven（`blog-backend/Dockerfile`）
  - 前端 npm 依赖 → npmmirror（`blog-admin/Dockerfile`、`blog-frontend/Dockerfile`）
  - ES IK 插件 zip → `release.infinilabs.com`（`elasticsearch/Dockerfile`）
  - 其中 **ES 基镜像来自 `docker.elastic.co`，镜像加速器不覆盖**，建议一并导出（脚本中提供可选开关）

## ADDED Requirements

### Requirement: 镜像导出脚本

系统 SHALL 提供 `scripts/export-images.sh`（bash，适用 Linux/macOS 且已装 Docker 的外网机器），包含全部 6 个 Docker Hub 基镜像：`mysql:8.0`、`redis:7-alpine`、`node:18-alpine`、`nginx:1.25-alpine`、`eclipse-temurin:17-jdk-jammy`、`eclipse-temurin:17-jre-jammy`；逐个执行 pull 与 save 到项目根目录 `images/`；提供被注释的可选开关用于导出 ES 基镜像 `docker.elastic.co/elasticsearch/elasticsearch:8.11.1`；任一镜像 pull 失败即中止（`set -euo pipefail`）。

#### Scenario: 外网机器导出
- **WHEN** 在外网 Linux/macOS 机器上执行 `bash scripts/export-images.sh`
- **THEN** 项目根目录 `images/` 下生成 6 个 `.tar` 文件，`docker images` 中存在对应镜像与 tag

#### Scenario: 重复执行（幂等）
- **WHEN** 再次执行导出脚本
- **THEN** 重新 pull/save，覆盖同名 tar，无报错

### Requirement: 镜像导入脚本

系统 SHALL 提供 `scripts/load-images.sh`（bash，适用 Ubuntu 服务器），遍历 `images/*.tar` 逐个执行 `docker load -i`；当 `images/` 目录不存在或为空时，输出中文错误提示并以非 0 退出码结束，不执行任何加载。

#### Scenario: 服务器导入
- **WHEN** 将 `images/` 目录拷贝到服务器项目根目录后，执行 `sudo bash scripts/load-images.sh`
- **THEN** 全部镜像加载成功，`docker images` 出现对应 tag，随后 `docker compose up -d --build` 不再从 Docker Hub 拉取这些镜像

#### Scenario: 目录缺失
- **WHEN** 服务器上尚无 `images/` 目录时执行导入脚本
- **THEN** 输出「请先从外网机器拷贝 images/ 目录」的中文提示并以非 0 退出，不改动系统

### Requirement: 离线镜像目录忽略

系统 SHALL 在根目录 `.gitignore` 中添加 `/images/`，使导出的镜像 tar 文件不被提交到版本库。

### Requirement: 文档补充离线部署步骤

系统 SHALL 在 README.md「Docker 部署」章节新增「离线镜像部署」小节，并在 部署操作手册.md 第 3 节补充离线方式，内容包含：① 外网机器执行 `scripts/export-images.sh` 导出；② 拷贝 `images/` 目录到服务器（scp/U盘）；③ 服务器执行 `scripts/load-images.sh` 加载；④ `docker compose up -d --build` 启动；并注明该方法仅覆盖镜像层下载。

#### Scenario: 用户按文档离线部署
- **WHEN** 用户在无外网（或下载极慢）的服务器上按文档步骤操作
- **THEN** 无需等待镜像从 Docker Hub 下载，直接基于本地已加载镜像完成构建与启动

## MODIFIED Requirements

无

## REMOVED Requirements

无
