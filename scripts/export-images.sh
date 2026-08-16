#!/usr/bin/env bash
# ==========================================================
# Docker 镜像导出脚本（在有外网的 Linux/macOS 机器上执行）
# 功能：docker pull + docker save 全部基镜像到 images/ 目录
# 用法：bash scripts/export-images.sh
# 然后：将 images/ 目录拷贝到服务器，执行 load-images.sh
# ==========================================================
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT_DIR="$ROOT_DIR/images"
mkdir -p "$OUT_DIR"

# 所需基镜像（与各服务 Dockerfile 的 FROM 保持一致）
IMAGES=(
  "mysql:8.0"
  "redis:7-alpine"
  "node:18-alpine"
  "nginx:1.25-alpine"
  "eclipse-temurin:17-jdk-jammy"
  "eclipse-temurin:17-jre-jammy"
)
# 可选：如需在服务器离线启用 ES 全文检索，取消下面一行的注释
# IMAGES+=("docker.elastic.co/elasticsearch/elasticsearch:8.11.1")
# 可选：如需离线启用 Prometheus + Grafana 监控（monitor 分组），取消下面两行的注释
# IMAGES+=("prom/prometheus:v2.51.0")
# IMAGES+=("grafana/grafana:10.4.2")

for img in "${IMAGES[@]}"; do
  # 文件名安全化：/ 与 : 替换为 _
  fname="${img//\//_}"
  fname="${fname//:/_}"

  echo "==> [1/2] docker pull $img"
  docker pull "$img"

  echo "==> [2/2] docker save $img -> $OUT_DIR/$fname.tar"
  docker save -o "$OUT_DIR/$fname.tar" "$img"
done

echo ""
echo "[完成] 镜像已导出到 $OUT_DIR"
echo "将 images/ 目录拷贝到服务器（scp/U盘）后，在服务器项目根目录执行："
echo "  sudo bash scripts/load-images.sh"
