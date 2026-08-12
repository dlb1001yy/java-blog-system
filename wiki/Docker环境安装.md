# Docker 环境安装指南（Ubuntu 22.04 LTS）

> 本文档指导在 Ubuntu 22.04 LTS 虚拟机环境中从零安装 Docker Engine 与 Docker Compose 插件，并配置国内镜像加速、日志轮转等优化项。
>
> **关于版本说明**：Ubuntu 官方不存在 22.5 版本，22.x 系列的 LTS 版本为 **22.04 LTS（Jammy Jellyfish）**。本文档适用于 22.04 LTS 及其所有点版本（22.04.1 ~ 22.04.4 等）。

---

## 目录

1. [前置条件检查](#1-前置条件检查)
2. [卸载旧版本](#2-卸载旧版本)
3. [方式一：官方 APT 仓库安装（推荐）](#3-方式一官方-apt-仓库安装推荐)
4. [方式二：一键脚本安装](#4-方式二一键脚本安装)
5. [方式三：Ubuntu 默认仓库安装](#5-方式三ubuntu-默认仓库安装)
6. [安装后配置](#6-安装后配置)
7. [验证安装](#7-验证安装)
8. [防火墙配置](#8-防火墙配置)
9. [常见问题排查](#9-常见问题排查)
10. [卸载 Docker](#10-卸载-docker)
11. [与博客项目对接](#11-与博客项目对接)

---

## 1. 前置条件检查

在安装 Docker 之前，请先确认系统环境满足要求。

### 1.1 检查系统版本

```bash
# 确认是 Ubuntu 22.04 LTS
cat /etc/os-release
```

预期输出包含 `Ubuntu 22.04.x LTS` 与 `VERSION_CODENAME=jammy`。

### 1.2 检查 CPU 架构

```bash
# 查看 CPU 架构（Docker 官方支持 amd64 与 arm64）
dpkg --print-architecture
```

预期输出 `amd64`（x86_64 虚拟机）或 `arm64`（ARM 虚拟机）。

### 1.3 检查内核版本

```bash
# Docker 要求内核版本 >= 3.10，Ubuntu 22.04 默认为 5.15+
uname -r
```

### 1.4 检查磁盘空间

```bash
# 查看根分区可用空间（建议至少 10GB）
df -h /
```

### 1.5 确保有 root 或 sudo 权限

```bash
# 确认当前用户有 sudo 权限
sudo whoami
```

预期输出 `root`。

---

## 2. 卸载旧版本

如果系统之前安装过 Docker（如 `docker.io`、`docker-engine`、`containerd`、`runc`），需先卸载：

```bash
sudo apt remove -y docker docker.io docker-engine docker-doc docker-compose podman-docker containerd runc
sudo apt autoremove -y
```

> 卸载不会删除 `/var/lib/docker/` 下的镜像与容器数据，如需彻底清理见第 10 节。

---

## 3. 方式一：官方 APT 仓库安装（推荐）

这是 Docker 官方推荐的安装方式，可获得最新版本与持续更新。

### 3.1 更新包索引并安装依赖

```bash
sudo apt update
sudo apt install -y ca-certificates curl gnupg lsb-release
```

### 3.2 添加 Docker 官方 GPG 密钥

```bash
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg
```

### 3.3 添加 Docker APT 仓库

```bash
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
```

### 3.4 安装 Docker Engine 与相关组件

```bash
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

| 包名 | 说明 |
|------|------|
| docker-ce | Docker Engine（守护进程） |
| docker-ce-cli | Docker CLI 命令行工具 |
| containerd.io | 容器运行时 |
| docker-buildx-plugin | BuildKit 构建增强插件 |
| docker-compose-plugin | Docker Compose v2 插件 |

### 3.5 启动 Docker 并设置开机自启

```bash
sudo systemctl enable docker
sudo systemctl start docker
```

---

## 4. 方式二：一键脚本安装

适合快速测试环境，通过 Docker 官方提供的脚本自动安装：

```bash
# 下载并执行安装脚本
curl -fsSL https://get.docker.com | sudo sh
```

安装完成后同样设置开机自启：

```bash
sudo systemctl enable docker
```

> **注意**：此脚本会自动添加 Docker 官方仓库并安装最新版本，适合快速部署但不适合需要精细控制版本的生产环境。

---

## 5. 方式三：Ubuntu 默认仓库安装

Ubuntu 22.04 官方仓库自带 `docker.io` 包，但版本较旧（通常落后数个大版本），仅作为备选：

```bash
sudo apt update
sudo apt install -y docker.io docker-compose-v2
```

```bash
sudo systemctl enable docker
sudo systemctl start docker
```

> **注意**：此方式安装的 Docker 版本较旧，且不含 `docker-buildx-plugin`。如需使用多阶段构建等新特性，建议使用方式一或方式二。

---

## 6. 安装后配置

### 6.1 将当前用户加入 docker 组（免 sudo）

默认情况下只有 root 用户能运行 docker 命令。将当前用户加入 `docker` 组后即可免 sudo：

```bash
sudo usermod -aG docker $USER
```

使组变更立即生效（二选一）：

```bash
# 方式一：切换到当前用户重新加载组（推荐）
newgrp docker

# 方式二：注销并重新登录
exit
# 然后重新 SSH 登录
```

验证免 sudo 是否生效：

```bash
docker ps
```

> **安全提示**：docker 组成员等同于 root 权限。生产环境请仅将受信任的用户加入此组。

### 6.2 配置国内镜像加速器

国内网络环境拉取 Docker Hub 镜像可能超时，建议配置镜像加速：

```bash
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<'EOF'
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://dockerproxy.com",
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  }
}
EOF
```

重启 Docker 使配置生效：

```bash
sudo systemctl daemon-reload
sudo systemctl restart docker
```

### 6.3 配置日志轮转（已在上方 daemon.json 中包含）

上方 `daemon.json` 中的 `log-opts` 配置已开启日志轮转：
- `max-size: 100m` — 单个日志文件最大 100MB
- `max-file: 3` — 每个容器最多保留 3 个日志文件

这可以防止容器日志无限增长占满磁盘。

### 6.4 设置 Docker 开机自启

```bash
# 设置 Docker 守护进程开机自启
sudo systemctl enable docker

# 设置 containerd 开机自启（Docker 依赖）
sudo systemctl enable containerd
```

---

## 7. 验证安装

### 7.1 检查 Docker 版本

```bash
docker version
```

预期显示 `Client` 与 `Server` 两个部分的版本信息。

### 7.2 检查 Docker Compose 版本

```bash
docker compose version
```

预期输出类似 `Docker Compose version v2.x.x`。

### 7.3 运行 hello-world 测试

```bash
docker run --rm hello-world
```

预期输出 `Hello from Docker!` 信息，表示 Docker Engine 工作正常。

### 7.4 检查镜像加速是否生效

```bash
docker info | grep -A 5 "Registry Mirrors"
```

预期输出配置的镜像加速地址列表。

### 7.5 检查 Docker 服务状态

```bash
sudo systemctl status docker
```

预期显示 `active (running)` 状态。

---

## 8. 防火墙配置

如果 Ubuntu 启用了 UFW 防火墙，需放行博客系统所需端口：

```bash
# 查看防火墙状态
sudo ufw status

# 如果防火墙未启用，可跳过此节
# 如已启用，放行以下端口：
sudo ufw allow 8082/tcp   # 前台门户
sudo ufw allow 8081/tcp   # 管理后台
sudo ufw allow 8080/tcp   # 后端 API（可选，通常不需要外部直接访问）
sudo ufw allow 3306/tcp   # MySQL（可选，仅远程调试时开放）
sudo ufw allow 6379/tcp   # Redis（可选，仅远程调试时开放）

# 如果使用 SSH 连接服务器，确保放行 SSH
sudo ufw allow 22/tcp

# 重新加载防火墙
sudo ufw reload
```

> **安全提示**：生产环境不建议对外暴露 3306（MySQL）与 6379（Redis）端口，它们仅用于容器间通信。

---

## 9. 常见问题排查

### 9.1 权限拒绝：permission denied while trying to connect to the Docker daemon

**现象**：执行 `docker` 命令报错 `Got permission denied while trying to connect to the Docker daemon socket`

**原因**：当前用户不在 `docker` 组中

**解决**：

```bash
# 将当前用户加入 docker 组
sudo usermod -aG docker $USER

# 重新加载组（或重新登录）
newgrp docker

# 验证
docker ps
```

### 9.2 镜像拉取超时：timeout / connection refused

**现象**：`docker pull` 时报错 `net/http: TLS handshake timeout` 或 `i/o timeout`

**原因**：国内网络无法直接访问 Docker Hub

**解决**：

```bash
# 检查镜像加速是否已配置
cat /etc/docker/daemon.json

# 如未配置，参见第 6.2 节配置镜像加速器
# 如已配置仍超时，尝试更换镜像源
# 编辑 /etc/docker/daemon.json 更换 registry-mirrors 地址

# 重启 Docker
sudo systemctl restart docker

# 测试拉取
docker pull hello-world
```

### 9.3 磁盘空间不足：no space left on device

**现象**：构建或运行容器时报错 `no space left on device`

**解决**：

```bash
# 查看 Docker 磁盘占用
docker system df

# 清理无用的镜像、容器、网络与构建缓存
docker system prune -a -f

# 清理无用的数据卷（⚠️ 确认无重要数据）
docker volume prune -f

# 查看磁盘空间
df -h
```

### 9.4 DNS 解析失败：lookup ... on ...: server misbehaving

**现象**：容器内无法解析域名，报 DNS 相关错误

**解决**：

```bash
# 检查容器 DNS 配置
docker run --rm alpine nslookup google.com

# 如失败，在 daemon.json 中添加 DNS 配置
sudo tee /etc/docker/daemon.json <<'EOF'
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://dockerproxy.com"
  ],
  "dns": ["8.8.8.8", "114.114.114.114"],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  }
}
EOF

sudo systemctl restart docker
```

### 9.5 containerd 异常：failed to start containerd

**现象**：Docker 无法启动，日志显示 `failed to start containerd`

**解决**：

```bash
# 查看 containerd 日志
sudo journalctl -u containerd --no-pager | tail -50

# 重启 containerd
sudo systemctl restart containerd

# 重启 Docker
sudo systemctl restart docker

# 如仍失败，清理 containerd 运行时状态
sudo systemctl stop docker
sudo systemctl stop containerd
sudo rm -rf /var/run/containerd
sudo systemctl start containerd
sudo systemctl start docker
```

### 9.6 端口冲突：bind: address already in use

**现象**：启动容器时报错 `bind: address already in use`

**解决**：

```bash
# 查看占用端口的进程
sudo lsof -i :8080

# 或使用 ss 命令
sudo ss -tlnp | grep 8080

# 停止占用端口的进程
sudo kill -9 <PID>

# 或修改 docker-compose.yml 中的端口映射
```

### 9.7 Docker 服务无法启动

**现象**：`sudo systemctl start docker` 失败

**解决**：

```bash
# 查看详细错误日志
sudo journalctl -u docker --no-pager | tail -50

# 检查 daemon.json 是否有语法错误
cat /etc/docker/daemon.json | python3 -m json.tool

# 如 JSON 格式错误，修正后重启
sudo systemctl restart docker
```

---

## 10. 卸载 Docker

如需彻底卸载 Docker：

### 10.1 停止 Docker 服务

```bash
sudo systemctl stop docker
sudo systemctl stop docker.socket
sudo systemctl stop containerd
```

### 10.2 卸载 Docker 包

```bash
sudo apt purge -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin docker-ce-rootless-extras
sudo apt autoremove -y
```

### 10.3 删除 Docker 数据（⚠️ 不可恢复）

```bash
# 删除镜像、容器、数据卷等全部数据
sudo rm -rf /var/lib/docker
sudo rm -rf /var/lib/containerd
sudo rm -rf /etc/docker
sudo rm -rf /var/run/docker
```

### 10.4 移除 Docker 仓库

```bash
sudo rm /etc/apt/sources.list.d/docker.list
sudo rm /etc/apt/keyrings/docker.gpg
sudo apt update
```

---

## 11. 与博客项目对接

Docker 环境安装完成后，按以下步骤部署博客系统。

### 11.1 安装 Git

```bash
sudo apt install -y git
git --version
```

### 11.2 从 Gitee 克隆代码

```bash
# 替换为你的 Gitee 仓库地址
cd ~
git clone https://gitee.com/<你的用户名>/<仓库名>.git java-blog-system
cd java-blog-system
```

### 11.3 一键启动全部服务

```bash
# 构建镜像并后台启动（首次构建约 5-15 分钟）
docker compose up -d --build
```

### 11.4 查看启动状态

```bash
# 查看全部服务状态
docker compose ps

# 查看后端日志（等待出现 "Started JavaBlogApplication"）
docker compose logs -f blog-backend
```

### 11.5 访问验证

将 `<服务器IP>` 替换为虚拟机 IP（使用 `ip addr` 查看）：

| 服务 | 地址 |
|------|------|
| 前台门户 | http://\<服务器IP\>:8082/blog/ |
| 管理后台 | http://\<服务器IP\>:8081/admin/ |
| 后端 API | http://\<服务器IP\>:8080/api |
| 接口文档 | http://\<服务器IP\>:8080/api/doc.html |

如配置了外部 Nginx（80 端口）统一入口，也可通过以下地址访问：

| 服务 | 地址 |
|------|------|
| 前台门户 | http://\<服务器IP\>/blog/ |
| 管理后台 | http://\<服务器IP\>/admin/ |

管理后台默认账号：`admin` / `admin123`

### 11.6 常用运维命令

```bash
# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f blog-backend

# 重启服务
docker compose restart blog-backend

# 停止全部服务（保留数据）
docker compose down

# 重新构建并启动
docker compose up -d --build

# 进入容器
docker exec -it blog-backend bash
```

---

## 附录：快速安装命令汇总

如需快速安装（跳过详细说明），直接执行以下命令：

```bash
# 1. 卸载旧版本
sudo apt remove -y docker docker.io docker-engine containerd runc 2>/dev/null

# 2. 安装依赖
sudo apt update
sudo apt install -y ca-certificates curl gnupg

# 3. 添加 GPG 密钥
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

# 4. 添加仓库
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 5. 安装 Docker
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 6. 配置镜像加速与日志轮转
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<'EOF'
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://dockerproxy.com"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  }
}
EOF

# 7. 启动并设置开机自启
sudo systemctl daemon-reload
sudo systemctl restart docker
sudo systemctl enable docker

# 8. 当前用户加入 docker 组
sudo usermod -aG docker $USER

# 9. 验证
docker version
docker compose version
sudo docker run --rm hello-world

echo "✅ Docker 安装完成！请重新登录以使 docker 组生效，或执行 newgrp docker"
```
