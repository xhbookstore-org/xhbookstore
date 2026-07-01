---
name: deploy-ui
description: Build and deploy xhbookstore-ui frontend with near-zero downtime. Usage: /deploy-ui -d <staging|prod>
arg-hint: -d <staging|prod>
---

# Deploy UI Module

## Parameters

- `-d <env>` — `staging` → 152.136.127.168, `prod` → production server.

## Prerequisites

- Node.js, SSH key at `~/.ssh/id_ed25519_deploy`

## Workflow (build first, replace last)

### 1. Load Config
Read `.claude/skills/servers.yml` → IP, ui_path.

### 2. Build & Upload (site still live)
```bash
cd xhbookstore-ui
npm run build:stage   # or build:prod
# Package (OS-aware): macOS/Linux → 'zip -r', Windows → Compress-Archive
scp dist.zip <user>@<ip>:/tmp/dist.zip
```

### 3. Atomic Swap (downtime: ~1s)
```bash
ssh <user>@<ip> "
  cd <ui_path>
  rm -rf * .[!.]*
  unzip -oq /tmp/dist.zip -d .
  rm -f /tmp/dist.zip
  curl -s -o /dev/null -w 'HTTP: %{http_code}' http://127.0.0.1/
"
```

Nginx serves static files directly — unzip overwrites are near-instant.

### 4. Report
Build → Upload → Replace → HTTP check.
