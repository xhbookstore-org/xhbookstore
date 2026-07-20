#!/usr/bin/env bash

set -Eeuo pipefail

ENVIRONMENT="${1:-}"
VERSION="${2:-}"
COMMIT_SHA="${3:-}"

DEPLOY_ROOT="${DEPLOY_ROOT:-/www/server/java/projects}"
SOURCE_ROOT="${SOURCE_ROOT:-/www/server/java/source}"
REPO_DIR="${REPO_DIR:-${SOURCE_ROOT}/xhbookstore}"
PACKAGE_DIR="/tmp/deploy/${VERSION}"
REPOSITORY="${REPOSITORY:-https://github.com/xhbookstore-org/xhbookstore.git}"

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
command -v git >/dev/null || die "git is required"
command -v java >/dev/null || die "Java is required"
command -v mvn >/dev/null || die "Maven is required"
command -v node >/dev/null || die "Node.js is required"
command -v npm >/dev/null || die "npm is required"
command -v zip >/dev/null || die "zip is required"

mkdir -p "$DEPLOY_ROOT" "$SOURCE_ROOT"
exec 8>"${DEPLOY_ROOT}/.source-deploy.lock"
flock -n 8 || die "another source build or deployment is running"

if [[ ! -d "${REPO_DIR}/.git" ]]; then
  log "cloning repository"
  git clone --depth=1 --no-checkout "$REPOSITORY" "$REPO_DIR"
fi

cd "$REPO_DIR"
git remote set-url origin "$REPOSITORY"
log "fetching commit ${COMMIT_SHA}"
git fetch --depth=1 origin "$COMMIT_SHA"
git checkout --detach --force FETCH_HEAD
git reset --hard FETCH_HEAD

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
