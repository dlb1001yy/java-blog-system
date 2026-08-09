# Checklist

- [x] `docker/daemon.json` 为合法 JSON（`python3 -m json.tool` 校验通过），`registry-mirrors` 含 ≥4 个当前可用的 https 加速源
- [x] `scripts/configure-docker-mirror.sh` 以 root 校验开头，非 root 时中文提示且不修改任何文件
- [x] 脚本正确备份原 daemon.json（`daemon.json.bak.<时间戳>`）
- [x] 脚本合并写入时保留原有 `log-opts` 等配置项，仅更新 `registry-mirrors`
- [x] 脚本执行 `systemctl daemon-reload` 与 `systemctl restart docker`，并打印 `docker info` 验证段
- [x] README.md 中的镜像加速源已替换为当前可用列表，并指引一键脚本
- [x] Docker环境安装指南.md 中所有失效镜像源（USTC/163）已替换
- [x] 部署操作手册.md 第 2 节镜像加速器配置已替换失效源并推荐一键脚本
