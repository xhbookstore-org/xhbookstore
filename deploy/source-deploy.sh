#!/usr/bin/env bash

set -Eeuo pipefail

ENVIRONMENT="${1:-}"
VERSION="${2:-}"
COMMIT_SHA="${3:-}"

DEPLOY_ROOT="${DEPLOY_ROOT:-/www/server/java/projects}"
SOURCE_ROOT="${SOURCE_ROOT:-/www/server/java/source}"
REPO_DIR="${REPO_DIR:-${SOURCE_ROOT}/xhbookstore}"
SOURCE_ARCHIVES="${SOURCE_ARCHIVES:-${SOURCE_ROOT}/archives}"
PACKAGE_DIR="/tmp/deploy/${VERSION}"
STAGING_MIRROR_KEY="${STAGING_MIRROR_KEY:-${HOME}/.ssh/xhbookstore_staging_git}"

log() {
  printf '[%s] %s\n' "$(date '+%F %T')" "$*"
}

die() {
  log "ERROR: $*"
  exit 1
}

[[ "$ENVIRONMENT" == "staging" || "$ENVIRONMENT" == "prod" ]] || die "environment must be staging or prod"
[[ "$VERSION" =~ ^[A-Za-z0-9._-]+$ ]] || die "invalid version: ${VERSION}"
[[ "$COMMIT_SHA" =~ ^[0-9a-f]{40}$ ]] || die "invalid commit SHA: ${COMMIT_SHA}"

if [[ "$ENVIRONMENT" == "prod" ]]; then
  [[ -f "$STAGING_MIRROR_KEY" ]] || die "missing staging mirror key: ${STAGING_MIRROR_KEY}"
fi

command -v curl >/dev/null || die "curl is required"
command -v rsync >/dev/null || die "rsync is required"
command -v tar >/dev/null || die "tar is required"
command -v java >/dev/null || die "Java is required"
command -v mvn >/dev/null || die "Maven is required"
command -v node >/dev/null || die "Node.js is required"
command -v npm >/dev/null || die "npm is required"
command -v zip >/dev/null || die "zip is required"

mkdir -p "$DEPLOY_ROOT" "$SOURCE_ROOT" "$SOURCE_ARCHIVES"
exec 8>"${DEPLOY_ROOT}/.source-deploy.lock"
flock -n 8 || die "another source build or deployment is running"

archive_file="${SOURCE_ARCHIVES}/${COMMIT_SHA}.tar.gz"
archive_partial="${archive_file}.part"

if [[ ! -f "$archive_file" ]]; then
  if [[ "$ENVIRONMENT" == "staging" ]]; then
    log "downloading source archive ${COMMIT_SHA} from GitHub"
    for attempt in 1 2 3 4 5; do
      if curl --fail --location --silent --show-error \
        --connect-timeout 15 --max-time 600 --retry 3 --retry-all-errors \
        --continue-at - \
        "https://codeload.github.com/xhbookstore-org/xhbookstore/tar.gz/${COMMIT_SHA}" \
        --output "$archive_partial"; then
        break
      fi
      log "source download attempt ${attempt} failed; resuming"
    done
  else
    log "downloading validated source archive ${COMMIT_SHA} from staging"
    ssh -i "$STAGING_MIRROR_KEY" \
      -o IdentitiesOnly=yes -o StrictHostKeyChecking=yes \
      root@152.136.127.168 "get ${COMMIT_SHA}" >"$archive_partial"
  fi

  tar -tzf "$archive_partial" >/dev/null || die "invalid or unavailable source archive for ${COMMIT_SHA}"
  mv "$archive_partial" "$archive_file"
fi

extract_dir="${SOURCE_ROOT}/extract-${COMMIT_SHA}"
rm -rf "$extract_dir"
mkdir -p "$extract_dir" "$REPO_DIR"
tar -xzf "$archive_file" --strip-components=1 -C "$extract_dir"
rsync -a --delete --exclude 'xhbookstore-ui/node_modules/' "${extract_dir}/" "${REPO_DIR}/"
rm -rf "$extract_dir"
printf '%s\n' "$COMMIT_SHA" >"${REPO_DIR}/.deployment-commit"

cd "$REPO_DIR"

log "testing and building Java modules"
MAVEN_OPTS="${MAVEN_OPTS:--Xms128m -Xmx768m}" \
  mvn clean package -pl xhbookstore-admin,xhbookstore-api -am --batch-mode --no-transfer-progress

log "installing UI dependencies"
(
  cd xhbookstore-ui
  npm install --no-audit --no-fund
  rm -rf dist dist.zip
  if [[ "$ENVIRONMENT" == "prod" ]]; then
    NODE_OPTIONS="${NODE_OPTIONS:---max-old-space-size=768}" npm run build:prod
  else
    NODE_OPTIONS="${NODE_OPTIONS:---max-old-space-size=768}" npm run build:stage
  fi
  cd dist
  zip -qr ../dist.zip .
)

rm -rf "$PACKAGE_DIR"
mkdir -p "$PACKAGE_DIR"
cp xhbookstore-admin/target/xhbookstore-admin.jar "$PACKAGE_DIR/"
cp xhbookstore-api/target/xhbookstore-api.jar "$PACKAGE_DIR/"
cp xhbookstore-ui/dist.zip "$PACKAGE_DIR/"
cp deploy/deploy.sh "$PACKAGE_DIR/"
chmod 0755 "$PACKAGE_DIR/deploy.sh"
sha256sum "$PACKAGE_DIR"/* >"$PACKAGE_DIR/SHA256SUMS"
(
  cd "$PACKAGE_DIR"
  sha256sum --check SHA256SUMS
)

install -m 0755 deploy/deploy.sh "${DEPLOY_ROOT}/deploy.sh"
if [[ -f deploy/source-deploy.sh ]]; then
  install -m 0755 deploy/source-deploy.sh "${DEPLOY_ROOT}/source-deploy.sh"
fi

log "installing version ${VERSION}"
DEPLOY_ENV="$ENVIRONMENT" \
DEPLOY_SOURCE_DIR="$PACKAGE_DIR" \
bash "${DEPLOY_ROOT}/deploy.sh" install "$VERSION"

log "source deployment completed for ${COMMIT_SHA}"
