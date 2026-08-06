# Checklist

## Docker环境安装指南.md 文档内容

- [x] 文档头部包含标题与适用环境说明（Ubuntu 22.04 LTS，注明不存在 22.5 版本）
- [x] 包含「前置条件检查」小节，有检查系统版本、CPU 架构、内核版本、磁盘空间的命令
- [x] 包含「卸载旧版本」小节，有卸载 docker.io、docker-engine、podman 等旧包的命令
- [x] 包含「方式一：官方 APT 仓库安装（推荐）」小节，含安装依赖、添加 GPG 密钥、添加仓库、apt install docker-ce 等完整步骤
- [x] 包含「方式二：一键脚本安装」小节，使用 get.docker.com 脚本
- [x] 包含「方式三：Ubuntu 默认仓库安装」小节，注明版本较旧作为备选
- [x] 包含「安装后配置」小节，含：将用户加入 docker 组、配置国内镜像加速器（daemon.json）、日志轮转限制、开机自启
- [x] 包含「验证安装」小节，含 docker version、docker compose version、docker run hello-world、docker info 检查镜像加速
- [x] 包含「防火墙配置」小节，ufw 放行 8080/8081/8082/3306/6379 端口
- [x] 包含「常见问题排查」小节，覆盖权限拒绝、镜像拉取超时、磁盘空间不足、DNS 解析失败、containerd 异常、端口冲突
- [x] 包含「卸载 Docker」小节，含停止服务、卸载包、删除 /var/lib/docker 等数据目录
- [x] 包含「与博客项目对接」小节，含安装 Git、克隆 Gitee 代码、docker compose up -d --build、访问地址验证

## README.md 链接

- [x] README.md「### 1. 环境准备」小节开头添加了指向 `Docker环境安装指南.md` 的超链接
- [x] 链接使用相对路径格式 `[Docker环境安装指南.md](Docker环境安装指南.md)`
- [x] 原有安装命令保留未被删除
