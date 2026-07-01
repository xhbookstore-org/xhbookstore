---
name: deploy-all
description: One-click deploy all modules (admin + api + ui) to server with minimal downtime. Usage: /deploy-all -d <staging|prod>
arg-hint: -d <staging|prod>
---

# Deploy All Modules (Minimal Downtime)

Build and deploy all three modules. **Services stay running during build/upload — only stopped for the ~10s replace+restart.**

## Parameters

- `-d <env>` — `staging` → 152.136.127.168, `prod` → production server.

## Prerequisites

- Java 17, Maven (in PATH), Node.js, SSH key at `~/.ssh/id_ed25519_deploy`

## Workflow

### Step 1: Load Config

Read `.claude/skills/servers.yml` → resolve IP, paths, etc.

### Step 2: Build (services still running!)

From project root:
```bash
# Build both JARs
mvn clean package -pl xhbookstore-admin,xhbookstore-api -am -Dmaven.test.skip=true -q

# Build UI
cd xhbookstore-ui
npm run build:stage   # or build:prod
# Package dist (OS-aware: macOS/Linux use 'zip -r', Windows use Compress-Archive)
```

### Step 3: Upload (services still running!)

Upload to tmp locations so live files are untouched:
```bash
scp xhbookstore-admin/target/xhbookstore-admin.jar <user>@<ip>:<jar_path>/xhbookstore-admin.jar.tmp
scp xhbookstore-api/target/xhbookstore-api.jar <user>@<ip>:<jar_path>/xhbookstore-api.jar.tmp
scp xhbookstore-ui/dist.zip <user>@<ip>:/tmp/dist.zip
```

### Step 4: Atomic Swap (downtime: ~10s)

One SSH call, minimal gap between stop and start:
```bash
ssh <user>@<ip> "
  # Stop (downtime starts)
  systemctl stop xhbookstore-admin xhbookstore-api

  # Swap JARs atomically (mv is instant)
  mv <jar_path>/xhbookstore-admin.jar.tmp <jar_path>/xhbookstore-admin.jar
  mv <jar_path>/xhbookstore-api.jar.tmp <jar_path>/xhbookstore-api.jar

  # Swap UI
  cd <ui_path>
  rm -rf * .[!.]*
  unzip -oq /tmp/dist.zip -d .
  rm -f /tmp/dist.zip

  # Start (downtime ends)
  systemctl start xhbookstore-admin xhbookstore-api
  sleep 5

  # Health check
  systemctl is-active xhbookstore-admin xhbookstore-api
  curl -s -o /dev/null -w 'UI: %{http_code}\n' http://127.0.0.1/
  curl -s -o /dev/null -w 'Admin: %{http_code}\n' http://127.0.0.1:8090/
  curl -s -o /dev/null -w 'API: %{http_code}\n' http://127.0.0.1:8091/
"
```

### Step 5: Report

| Module | Build | Upload | Deploy | Health |
|--------|-------|--------|--------|--------|
| Admin  | ✅/❌ | ✅/❌ | ✅/❌ | HTTP xxx |
| API    | ✅/❌ | ✅/❌ | ✅/❌ | HTTP xxx |
| UI     | ✅/❌ | ✅/❌ | ✅/❌ | HTTP xxx |
