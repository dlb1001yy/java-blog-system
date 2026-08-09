# Checklist

- [x] `scripts/export-images.sh` 存在，含 6 个 Docker Hub 基镜像数组与 ES 可选开关，逐个 pull+save 到 `images/`
- [x] `scripts/load-images.sh` 存在，`images/` 目录缺失/为空时中文报错并非 0 退出，遍历 `*.tar` 执行 `docker load`
- [x] `.gitignore` 已包含 `/images/`
- [x] README.md 已新增「离线镜像部署」小节（导出→拷贝→加载→启动 4 步）
- [x] 部署操作手册.md 第 3 节已补充离线方式说明
