---
name: deploy-all
description: One-click deploy all modules (admin + api + ui) to server. Usage: /deploy-all -d <staging|prod>
arg-hint: -d <staging|prod>
---

# Deploy All Modules

Build and deploy all three modules (admin, api, ui) to the target server in one pass.

## Parameters

- `-d <env>` — `staging` → 152.136.127.168, `prod` → production server.
  - If `-d` is omitted, ask the user.

## Prerequisites (any OS — macOS / Windows / Linux)

- **Java 17** — `java --version`
- **Maven** — `mvn --version` (must be in PATH)
- **Node.js** — `node --version`
- **SSH key** — `~/.ssh/id_ed25519_deploy` must exist

## Workflow

### Step 1: Load Config

Read `.claude/skills/servers.yml`, resolve `<env>` → IP, paths, etc.

### Step 2: Detect OS and Set Commands

- **Project root**: use the current git repo root (`git rev-parse --show-toplevel`)
- **Maven**: use `mvn` from PATH (assume user has it configured)
- **Zip**: on Windows use PowerShell `Compress-Archive`, on macOS/Linux use `zip -r`
- All project-relative paths are the same on all platforms (e.g. `xhbookstore-admin/target/...`)

### Step 3: Build

From the project root:

```bash
# Build both JARs in one command (Maven handles dependency order)
mvn clean package -pl xhbookstore-admin,xhbookstore-api -am -Dmaven.test.skip=true -q

# Build UI (env-aware)
cd xhbookstore-ui
npm run build:stage   # for staging
npm run build:prod    # for prod
```

Zip the dist folder using the OS-appropriate command.

### Step 4: Upload

```bash
scp -i ~/.ssh/id_ed25519_deploy xhbookstore-admin/target/xhbookstore-admin.jar <user>@<ip>:<jar_path>/xhbookstore-admin.jar
scp -i ~/.ssh/id_ed25519_deploy xhbookstore-api/target/xhbookstore-api.jar <user>@<ip>:<jar_path>/xhbookstore-api.jar
scp -i ~/.ssh/id_ed25519_deploy xhbookstore-ui/dist.zip <user>@<ip>:/tmp/dist.zip
```

### Step 5: Deploy on Server

```bash
ssh -i ~/.ssh/id_ed25519_deploy <user>@<ip> "
  systemctl stop xhbookstore-admin xhbookstore-api
  cp <jar_path>/xhbookstore-admin.jar <jar_path>/xhbookstore-admin.jar.bak
  cp <jar_path>/xhbookstore-api.jar <jar_path>/xhbookstore-api.jar.bak
  cd <ui_path>
  tar -czf /tmp/xhbookstore-ui-backup-\$(date +%Y%m%d_%H%M%S).tar.gz .
  rm -rf * .[!.]*
  unzip -oq /tmp/dist.zip -d .
  rm -f /tmp/dist.zip
  systemctl start xhbookstore-admin xhbookstore-api
  sleep 5
  echo '--- Health ---'
  systemctl is-active xhbookstore-admin xhbookstore-api
  curl -s -o /dev/null -w 'UI: %{http_code}\n' http://127.0.0.1/
  curl -s -o /dev/null -w 'Admin: %{http_code}\n' http://127.0.0.1:8090/
  curl -s -o /dev/null -w 'API: %{http_code}\n' http://127.0.0.1:8091/
"
```

### Step 6: Report

| Module | Build | Upload | Deploy | Health |
|--------|-------|--------|--------|--------|
| Admin  | ✅/❌ | ✅/❌ | ✅/❌ | HTTP xxx |
| API    | ✅/❌ | ✅/❌ | ✅/❌ | HTTP xxx |
| UI     | ✅/❌ | ✅/❌ | ✅/❌ | HTTP xxx |
