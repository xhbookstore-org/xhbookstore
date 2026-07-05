#!/bin/bash
# ============================================================
#  XhBookstore 服务器端版本管理 + 部署脚本
#  用法:
#    ./deploy.sh install <version>  安装指定版本
#    ./deploy.sh list               列出所有版本
#    ./deploy.sh rollback <version> 回滚到指定版本
#    ./deploy.sh current            查看当前版本
#    ./deploy.sh clean              清理旧版本(保留最近10个)
#    ./deploy.sh init               初始化目录结构
# ============================================================
set -euo pipefail

PROJECT_DIR="/www/server/java/projects"
VERSIONS_DIR="$PROJECT_DIR/versions"
UI_DIR="/www/wwwroot/xhbookstore-ui"

ADMIN_PROJECT="xhbookstore-admin"
API_PROJECT="xhbookstore-api"
JAVA_SERVICE_BIN="/usr/bin/java-service"

restart_backend_services() {
  if [ ! -x "$JAVA_SERVICE_BIN" ]; then
    echo "[deploy] java-service not found: $JAVA_SERVICE_BIN"
    exit 1
  fi

  "$JAVA_SERVICE_BIN" "$ADMIN_PROJECT" restart
  "$JAVA_SERVICE_BIN" "$API_PROJECT" restart
  sleep 4

  "$JAVA_SERVICE_BIN" "$ADMIN_PROJECT" start
  "$JAVA_SERVICE_BIN" "$API_PROJECT" start
}

# ---- 初始化 ----
init() {
  mkdir -p "$VERSIONS_DIR"
  echo "[deploy] 版本目录: $VERSIONS_DIR"
}

# ---- 安装 ----
install() {
  local version="${1:-}"
  if [ -z "$version" ]; then
    echo "用法: $0 install <version>"
    exit 1
  fi

  local vdir="$VERSIONS_DIR/$version"
  if [ -d "$vdir" ]; then
    echo "[deploy] 版本 $version 已存在，覆盖安装"
    rm -rf "$vdir"
  fi
  mkdir -p "$vdir"

  local tmp="/tmp/deploy"
  cd "$tmp"

  # 元数据
  cat > "$vdir/meta.json" << MEOF
{
  "version": "$version",
  "time": "$(date -Iseconds)",
  "env": "${DEPLOY_ENV:-unknown}"
}
MEOF

  # 移入版本目录
  if [ -f "xhbookstore-admin.jar" ]; then
    mv xhbookstore-admin.jar "$vdir/admin.jar"
    ln -sfn "$vdir/admin.jar" "$PROJECT_DIR/xhbookstore-admin.jar"
    echo "[deploy] admin.jar OK"
  fi
  if [ -f "xhbookstore-api.jar" ]; then
    mv xhbookstore-api.jar "$vdir/api.jar"
    ln -sfn "$vdir/api.jar" "$PROJECT_DIR/xhbookstore-api.jar"
    echo "[deploy] api.jar OK"
  fi
  if [ -f "dist.zip" ]; then
    cp dist.zip "$vdir/ui.zip"
    unzip -oq dist.zip -d "$UI_DIR/"
    echo "[deploy] ui OK"
  fi

  # 清理上传临时文件
  rm -f "$tmp"/*.jar "$tmp"/dist.zip

  # 重启服务
  restart_backend_services

  echo ""
  echo "========================================"
  echo "  ✅ 已部署版本: $version"
  echo "========================================"
  systemctl is-active "spring_$ADMIN_PROJECT" "spring_$API_PROJECT"
}

# ---- 列出所有版本 ----
list() {
  local current=$(readlink "$PROJECT_DIR/xhbookstore-admin.jar" 2>/dev/null | grep -oP 'v\d+_\d+' || echo "未知")
  echo ""
  echo "当前版本: $current"
  echo "----------------------------------------"
  if [ -d "$VERSIONS_DIR" ]; then
    for v in $(ls -1dr "$VERSIONS_DIR"/*/ 2>/dev/null); do
      local name=$(basename "$v")
      local marker="  "
      [ "$name" = "$current" ] && marker="✦ "
      local time=""
      [ -f "$v/meta.json" ] && time=$(grep -oP '"time":\s*"\K[^"]*' "$v/meta.json" | cut -c1-19 | tr T ' ')
      echo "  $marker$name  $time"
    done
  fi
  echo "----------------------------------------"
  echo "共 $(ls -1d "$VERSIONS_DIR"/*/ 2>/dev/null | wc -l) 个版本"
}

# ---- 回滚 ----
rollback() {
  local target="$1"
  local vdir="$VERSIONS_DIR/$target"
  if [ ! -d "$vdir" ]; then
    echo "❌ 版本 $target 不存在"
    echo "可用版本:"
    list
    exit 1
  fi

  echo "回滚到: $target"

  if [ -f "$vdir/admin.jar" ]; then
    ln -sfn "$vdir/admin.jar" "$PROJECT_DIR/xhbookstore-admin.jar"
  fi
  if [ -f "$vdir/api.jar" ]; then
    ln -sfn "$vdir/api.jar" "$PROJECT_DIR/xhbookstore-api.jar"
  fi
  if [ -f "$vdir/ui.zip" ]; then
    unzip -oq "$vdir/ui.zip" -d "$UI_DIR/"
  fi

  restart_backend_services

  echo ""
  echo "========================================"
  echo "  ✅ 已回滚到: $target"
  echo "========================================"
  systemctl is-active "spring_$ADMIN_PROJECT" "spring_$API_PROJECT"
}

# ---- 当前版本 ----
current() {
  readlink "$PROJECT_DIR/xhbookstore-admin.jar" 2>/dev/null | grep -oP 'v\d+_\d+' || echo "未知"
}

# ---- 清理 ----
clean() {
  local keep="${1:-10}"
  local count=$(ls -1d "$VERSIONS_DIR"/*/ 2>/dev/null | wc -l)
  if [ "$count" -gt "$keep" ]; then
    ls -1dt "$VERSIONS_DIR"/*/ | tail -n +$((keep + 1)) | xargs rm -rf
    echo "已清理，保留最近 $keep 个版本"
  else
    echo "版本数 $count ≤ $keep，无需清理"
  fi
}

# ---- Main ----
case "${1:-}" in
  install)  init && install "${2:-}" ;;
  list)     list ;;
  rollback) rollback "${2:-}" ;;
  current)  current ;;
  clean)    clean "${2:-10}" ;;
  init)     init ;;
  *)
    echo "用法: $0 {install|list|rollback|current|clean} [version]"
    echo ""
    echo "  install <version>  安装新版本"
    echo "  list               列出所有版本"
    echo "  rollback <version> 回滚到指定版本"
    echo "  current            查看当前版本"
    echo "  clean [n]          保留最近 n 个版本(默认10)"
    echo "  init               初始化目录结构"
    ;;
esac
