# Tasks

- [x] Task 1: 创建 `Docker环境安装指南.md` 文档主体
  - [x] SubTask 1.1: 编写文档头部（标题、适用环境说明、Ubuntu 22.04 LTS 注释）
  - [x] SubTask 1.2: 编写「前置条件检查」小节（系统版本、架构、内核、磁盘空间检查命令）
  - [x] SubTask 1.3: 编写「卸载旧版本」小节（卸载 docker.io、docker-engine 等旧包的命令）
  - [x] SubTask 1.4: 编写「方式一：官方 APT 仓库安装（推荐）」小节（安装依赖、添加 GPG 密钥、添加仓库、安装 docker-ce 等组件）
  - [x] SubTask 1.5: 编写「方式二：一键脚本安装」小节（get.docker.com 脚本）
  - [x] SubTask 1.6: 编写「方式三：Ubuntu 默认仓库安装」小节（apt install docker.io，注明版本较旧）
  - [x] SubTask 1.7: 编写「安装后配置」小节（用户加入 docker 组、配置国内镜像加速器 daemon.json、日志轮转、开机自启）
  - [x] SubTask 1.8: 编写「验证安装」小节（docker version、docker compose version、docker run hello-world、docker info 检查镜像加速）
  - [x] SubTask 1.9: 编写「防火墙配置」小节（ufw 放行 8080/8081/8082/3306/6379 端口）
  - [x] SubTask 1.10: 编写「常见问题排查」小节（权限拒绝、镜像拉取超时、磁盘空间不足、DNS 解析失败、containerd 异常、端口冲突）
  - [x] SubTask 1.11: 编写「卸载 Docker」小节（停止服务、卸载包、删除数据目录）
  - [x] SubTask 1.12: 编写「与博客项目对接」小节（安装 Git、克隆代码、一键 docker compose up -d --build、验证访问）

- [x] Task 2: 修改 README.md 添加文档链接
  - [x] SubTask 2.1: 在 README.md「Docker 部署（Ubuntu 22）」→「### 1. 环境准备」小节开头添加指向 `Docker环境安装指南.md` 的超链接提示

# Task Dependencies

- Task 2 依赖 Task 1（链接指向的文件需先存在）
