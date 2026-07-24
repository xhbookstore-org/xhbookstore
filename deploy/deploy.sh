#!/usr/bin/env bash

set -Eeuo pipefail

ACTION="${1:-}"
VERSION="${2:-}"

DEPLOY_ROOT="${DEPLOY_ROOT:-/www/server/java/projects}"
SOURCE_DIR="${DEPLOY_SOURCE_DIR:-/tmp/deploy}"
VERSIONS_DIR="${DEPLOY_ROOT}/versions"
UI_ROOT="${UI_ROOT:-/www/wwwroot/xhbookstore-ui}"
ADMIN_SERVICE="${ADMIN_SERVICE:-spring_xhbookstore-admin}"
API_SERVICE="${API_SERVICE:-spring_xhbookstore-api}"
ADMIN_HEALTH_URL="${ADMIN_HEALTH_URL:-http://127.0.0.1:8090/getInfo}"
API_HEALTH_URL="${API_HEALTH_URL:-http://127.0.0.1:8091/api/mp/v1/stores}"
HEALTH_RETRIES="${HEALTH_RETRIES:-30}"
DEPLOY_KEEP="${DEPLOY_KEEP:-8}"

ADMIN_ARTIFACT="${SOURCE_DIR}/xhbookstore-admin.jar"
API_ARTIFACT="${SOURCE_DIR}/xhbookstore-api.jar"
UI_ARTIFACT="${SOURCE_DIR}/dist.zip"
LOCK_FILE="${DEPLOY_ROOT}/.deploy.lock"

log() {
  printf '[%s] %s\n' "$(date '+%F %T')" "$*"
}

die() {
  log "ERROR: $*"
  exit 1
}

systemctl_cmd() {
  if [[ "$(id -u)" -eq 0 ]]; then
    systemctl "$@"
  else
    sudo -n systemctl "$@"
  fi
}

atomic_symlink() {
  local target="$1"
  local link="$2"
  local temporary="${link}.new.$$"

  ln -s "$target" "$temporary"
  mv -Tf "$temporary" "$link"
}

wait_for_url() {
  local name="$1"
  local url="$2"
  local attempt

  for ((attempt = 1; attempt <= HEALTH_RETRIES; attempt++)); do
    if curl --fail --silent --max-time 5 --output /dev/null "$url"; then
      log "${name} health check passed"
      return 0
    fi
    sleep 2
  done

  log "${name} health check failed: ${url}"
  return 1
}

validate_inputs() {
  [[ "$VERSION" =~ ^[A-Za-z0-9._-]+$ ]] || die "invalid version: ${VERSION}"
  [[ -f "$ADMIN_ARTIFACT" ]] || die "missing ${ADMIN_ARTIFACT}"
  [[ -f "$API_ARTIFACT" ]] || die "missing ${API_ARTIFACT}"
  [[ -f "$UI_ARTIFACT" ]] || die "missing ${UI_ARTIFACT}"

  unzip -tq "$ADMIN_ARTIFACT" >/dev/null || die "invalid admin JAR"
  unzip -tq "$API_ARTIFACT" >/dev/null || die "invalid api JAR"
  unzip -tq "$UI_ARTIFACT" >/dev/null || die "invalid UI ZIP"
}

install_release() {
  local release_dir="${VERSIONS_DIR}/${VERSION}"
  local extract_dir="${release_dir}/ui-extract"
  local ui_source
  local previous_admin=""
  local previous_api=""
  local ui_backup="${UI_ROOT}.before-${VERSION}"
  local ui_was_present=0
  local rollback_required=1

  validate_inputs
  mkdir -p "$VERSIONS_DIR"
  [[ ! -e "$release_dir" ]] || die "version already exists: ${release_dir}"
  mkdir -p "$release_dir" "$extract_dir" "$(dirname "$UI_ROOT")"

  cp "$ADMIN_ARTIFACT" "${release_dir}/admin.jar"
  cp "$API_ARTIFACT" "${release_dir}/api.jar"
  cp "$UI_ARTIFACT" "${release_dir}/ui.zip"
  unzip -q "${release_dir}/ui.zip" -d "$extract_dir"

  if [[ -f "${extract_dir}/index.html" ]]; then
    ui_source="$extract_dir"
  elif [[ -f "${extract_dir}/dist/index.html" ]]; then
    ui_source="${extract_dir}/dist"
  else
    die "UI ZIP does not contain index.html"
  fi

  mkdir -p "${release_dir}/ui"
  cp -a "${ui_source}/." "${release_dir}/ui/"
  rm -rf "$extract_dir"

  if [[ -L "${DEPLOY_ROOT}/xhbookstore-admin.jar" ]]; then
    previous_admin="$(readlink -f "${DEPLOY_ROOT}/xhbookstore-admin.jar")"
  fi
  if [[ -L "${DEPLOY_ROOT}/xhbookstore-api.jar" ]]; then
    previous_api="$(readlink -f "${DEPLOY_ROOT}/xhbookstore-api.jar")"
  fi

  rollback() {
    local exit_code=$?
    if [[ "$rollback_required" -eq 1 ]]; then
      log "deployment failed; rolling back ${VERSION}"
      set +e
      [[ -n "$previous_admin" ]] && atomic_symlink "$previous_admin" "${DEPLOY_ROOT}/xhbookstore-admin.jar"
      [[ -n "$previous_api" ]] && atomic_symlink "$previous_api" "${DEPLOY_ROOT}/xhbookstore-api.jar"
      if [[ -d "$ui_backup" ]]; then
        rm -rf "$UI_ROOT"
        mv "$ui_backup" "$UI_ROOT"
      elif [[ "$ui_was_present" -eq 0 ]]; then
        rm -rf "$UI_ROOT"
      fi
      systemctl_cmd restart "$ADMIN_SERVICE" "$API_SERVICE"
      log "rollback completed"
    fi
    exit "$exit_code"
  }
  trap rollback ERR INT TERM

  rm -rf "$ui_backup"
  if [[ -d "$UI_ROOT" ]]; then
    ui_was_present=1
    mv "$UI_ROOT" "$ui_backup"
  fi
  mv "${release_dir}/ui" "$UI_ROOT"

  atomic_symlink "${release_dir}/admin.jar" "${DEPLOY_ROOT}/xhbookstore-admin.jar"
  atomic_symlink "${release_dir}/api.jar" "${DEPLOY_ROOT}/xhbookstore-api.jar"

  systemctl_cmd restart "$ADMIN_SERVICE" "$API_SERVICE"
  systemctl_cmd is-active --quiet "$ADMIN_SERVICE"
  systemctl_cmd is-active --quiet "$API_SERVICE"
  wait_for_url "admin" "$ADMIN_HEALTH_URL"
  wait_for_url "api" "$API_HEALTH_URL"

  rollback_required=0
  trap - ERR INT TERM
  rm -rf "$ui_backup"

  cat >"${release_dir}/meta.json" <<EOF
{"version":"${VERSION}","environment":"${DEPLOY_ENV:-unknown}","deployedAt":"$(date --iso-8601=seconds)"}
EOF

  mapfile -t obsolete_releases < <(
    find "$VERSIONS_DIR" -mindepth 1 -maxdepth 1 -type d -printf '%T@ %p\n' \
      | sort -nr \
      | awk -v keep="$DEPLOY_KEEP" 'NR > keep {$1=""; sub(/^ /, ""); print}'
  )
  for obsolete_release in "${obsolete_releases[@]}"; do
    if [[ -n "$obsolete_release" ]] && ! rm -rf "$obsolete_release"; then
      log "warning: unable to remove obsolete release: ${obsolete_release}"
    fi
  done

  log "deployment completed: ${VERSION}"
  log "admin -> $(readlink -f "${DEPLOY_ROOT}/xhbookstore-admin.jar")"
  log "api   -> $(readlink -f "${DEPLOY_ROOT}/xhbookstore-api.jar")"
}

[[ "$ACTION" == "install" ]] || die "usage: $0 install <version>"
[[ -n "$VERSION" ]] || die "version is required"
command -v curl >/dev/null || die "curl is required"
command -v unzip >/dev/null || die "unzip is required"
command -v flock >/dev/null || die "flock is required"

mkdir -p "$DEPLOY_ROOT"
exec 9>"$LOCK_FILE"
flock -n 9 || die "another deployment is running"

install_release
