#!/usr/bin/env bash
# ==========================================================
# Docker 镜像导入脚本（在服务器上执行）
# 功能：遍历 images/*.tar 逐个 docker load
# 用法：sudo bash scripts/load-images.sh
# ==========================================================
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IN_DIR="$ROOT_DIR/images"

if [ ! -d "$IN_DIR" ]; then
  echo "[错误] 未找到 $IN_DIR 目录，请先从外网机器拷贝 images/ 目录到项目根目录"
  exit 1
fi

shopt -s nullglob
tars=("$IN_DIR"/*.tar)
shopt -u nullglob

if [ "${#tars[@]}" -eq 0 ]; then
  echo "[错误] $IN_DIR 目录为空，未找到任何 .tar 镜像文件"
  exit 1
fi

for f in "${tars[@]}"; do
  echo "==> docker load -i $f"
  docker load -i "$f"
done

echo ""
echo "[完成] 全部镜像加载成功！"
echo "可执行：docker compose up -d --build"
echo "验证：docker images"
