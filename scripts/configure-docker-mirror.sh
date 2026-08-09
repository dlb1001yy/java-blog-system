#!/usr/bin/env bash
# ==========================================================
# Docker 镜像加速一键配置脚本（Ubuntu + Docker CE）
# 功能：备份 -> 合并写入加速源 -> 重启 Docker -> 验证
# 用法：sudo bash scripts/configure-docker-mirror.sh
# ==========================================================
set -euo pipefail

# ---------- 1. 前置校验 ----------
if [ "$(id -u)" -ne 0 ]; then
  echo "[错误] 请使用 sudo 运行本脚本：sudo bash $0"
  exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "[错误] 未检测到 docker 命令，请先安装 Docker Engine"
  exit 1
fi

if ! command -v python3 >/dev/null 2>&1; then
  echo "[错误] 未检测到 python3，请先安装：sudo apt install -y python3"
  exit 1
fi

# ---------- 2. 定位模板文件 ----------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEMPLATE_FILE="$SCRIPT_DIR/../docker/daemon.json"

if [ ! -f "$TEMPLATE_FILE" ]; then
  echo "[错误] 未找到模板文件: $TEMPLATE_FILE"
  exit 1
fi

# ---------- 3. 备份现有配置 ----------
DAEMON_FILE="/etc/docker/daemon.json"
mkdir -p /etc/docker

if [ -f "$DAEMON_FILE" ]; then
  BACKUP_FILE="$DAEMON_FILE.bak.$(date +%Y%m%d%H%M%S)"
  cp "$DAEMON_FILE" "$BACKUP_FILE"
  echo "[信息] 已备份原配置 -> $BACKUP_FILE"
fi

# ---------- 4. 合并写入（保留原 log-opts 等配置，仅更新 registry-mirrors） ----------
TEMPLATE_FILE="$TEMPLATE_FILE" DAEMON_FILE="$DAEMON_FILE" python3 - <<'PYEOF'
import json, os
template_path = os.environ["TEMPLATE_FILE"]
daemon_path = os.environ["DAEMON_FILE"]

with open(template_path, encoding="utf-8") as f:
    template = json.load(f)

data = {}
if os.path.exists(daemon_path):
    with open(daemon_path, encoding="utf-8") as f:
        try:
            data = json.load(f)
        except Exception:
            # 原文件损坏则从空配置开始（已有备份兜底）
            data = {}

# 仅更新 registry-mirrors，保留原有其他配置（如 log-opts 日志轮转）
data["registry-mirrors"] = template["registry-mirrors"]

with open(daemon_path, "w", encoding="utf-8") as f:
    json.dump(data, f, ensure_ascii=False, indent=2)
    f.write("\n")

print("[信息] 已写入加速源:")
for m in data["registry-mirrors"]:
    print("  - " + m)
PYEOF

# ---------- 5. 重启 Docker ----------
echo "[信息] 正在重启 Docker ..."
systemctl daemon-reload
systemctl restart docker
echo "[完成] Docker 已重启"

# ---------- 6. 验证 ----------
echo ""
echo "[验证] docker info Registry Mirrors:"
docker info 2>/dev/null | grep -A 20 "Registry Mirrors" || {
  echo "[提示] 未在 docker info 中看到 Registry Mirrors，请手动检查：cat $DAEMON_FILE"
  exit 1
}
echo ""
echo "[完成] 镜像加速配置成功！可重新拉取镜像：docker compose pull"
