# Tasks

- [x] Task 1: 创建 `部署操作手册.md` 文档主体
  - [x] SubTask 1.1: 编写文档头部与目录
  - [x] SubTask 1.2: 编写「部署前准备」小节（确认 Docker、代码克隆、目录结构检查）
  - [x] SubTask 1.3: 编写「Docker 镜像加速器配置」小节（daemon.json 配置、阿里云专属加速器获取）
  - [x] SubTask 1.4: 编写「基础镜像预拉取」小节（按依赖顺序列出所有镜像，含大小参考与拉取命令）
  - [x] SubTask 1.5: 编写「一键启动全部服务」小节（docker compose up -d --build、启动顺序说明、进度查看）
  - [x] SubTask 1.6: 编写「逐服务状态检查」小节（docker compose ps、健康检查、日志查看）
  - [x] SubTask 1.7: 编写「访问测试」小节（前台门户、管理后台、后端 API、接口文档逐项验证）
  - [x] SubTask 1.8: 编写「常见部署问题排查」小节（429 限流、apt-get 超时、Maven 下载慢、端口冲突、数据库连接失败、前端空白页）
  - [x] SubTask 1.9: 编写「停止与重新部署」小节（停止、清理缓存、更新代码重新构建）

- [x] Task 2: 修改 README.md 添加文档链接
  - [x] SubTask 2.1: 在 README.md Docker 部署章节添加指向 `部署操作手册.md` 的超链接

# Task Dependencies

- Task 2 依赖 Task 1
