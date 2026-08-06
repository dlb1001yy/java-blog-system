# Checklist

## 部署操作手册.md 文档内容

- [x] 文档头部包含标题与目录
- [x] 包含「部署前准备」小节（确认 Docker 已安装、代码已克隆、目录结构检查）
- [x] 包含「Docker 镜像加速器配置」小节（daemon.json 配置、阿里云专属加速器获取方法）
- [x] 包含「基础镜像预拉取」小节（按依赖顺序列出 mysql、redis、eclipse-temurin、node、nginx 镜像，含大小参考与拉取命令）
- [x] 包含「一键启动全部服务」小节（docker compose up -d --build、MySQL→Redis→后端→前端的启动顺序说明、进度查看命令）
- [x] 包含「逐服务状态检查」小节（docker compose ps、健康检查说明、各服务日志查看命令）
- [x] 包含「访问测试」小节（前台门户 8082、管理后台 8081/admin/、后端 API 8080、接口文档 8080/api/doc.html 逐项验证步骤）
- [x] 包含「常见部署问题排查」小节（429 限流、apt-get 超时、Maven 下载慢、端口冲突、数据库连接失败、前端空白页）
- [x] 包含「停止与重新部署」小节（停止、清理缓存、更新代码重新构建的完整流程）

## README.md 链接

- [x] README.md Docker 部署章节添加了指向 `部署操作手册.md` 的超链接
