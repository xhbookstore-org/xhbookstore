---
name: deploy-ui
description: Build and deploy the xhbookstore-ui frontend module. Usage: /deploy-ui -d <staging|prod>
arg-hint: -d <staging|prod>
---

# Deploy UI Module

## Parameters

- `-d <env>` — `staging` → 152.136.127.168, `prod` → production server.

## Prerequisites

- Node.js, SSH key at `~/.ssh/id_ed25519_deploy`

## Workflow

### 1. Load Config
Read `.claude/skills/servers.yml` → resolve IP, UI path.

### 2. Build
From `xhbookstore-ui/`:
```bash
npm run build:stage   # for staging
npm run build:prod    # for prod
```

### 3. Package (OS-aware)
- **macOS/Linux**: `cd dist && zip -r ../dist.zip .`
- **Windows**: `powershell Compress-Archive -Path .\dist\* -DestinationPath .\dist.zip -Force`

### 4. Upload
```bash
scp -i ~/.ssh/id_ed25519_deploy dist.zip <user>@<ip>:/tmp/dist.zip
```

### 5. Deploy
```bash
ssh -i ~/.ssh/id_ed25519_deploy <user>@<ip> "
  cd <ui_path>
  tar -czf /tmp/xhbookstore-ui-backup-\$(date +%Y%m%d_%H%M%S).tar.gz .
  rm -rf * .[!.]*
  unzip -oq /tmp/dist.zip -d .
  rm -f /tmp/dist.zip
  curl -s -o /dev/null -w 'UI HTTP: %{http_code}' http://127.0.0.1/
"
```

### 6. Report
Build → Package → Upload → Deploy → HTTP check.
