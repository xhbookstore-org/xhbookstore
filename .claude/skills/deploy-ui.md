---
name: deploy-ui
description: Build, upload, and deploy the xhbookstore-ui (前端UI) module to the server. Usage: /deploy-ui -d <staging|prod>
arg-hint: -d <staging|prod>
---

# Deploy UI Module

Build the `xhbookstore-ui` Vue.js frontend, upload to the server, and replace the existing Nginx-served site files.

## Parameters

- `-d <env>` — Deployment environment. `staging` → 152.136.127.168, `prod` → production server.
  - If `-d` is omitted, **ask the user** which environment to deploy to.

## Workflow

### Step 1: Load Server Config

Read `.claude/skills/servers.yml` to resolve the target server IP, SSH key, and UI site path for the given `<env>`.

If the IP is not configured (e.g., prod shows `<PROD_IP_NOT_SET>`), ask the user for the production server IP and update `servers.yml`.

### Step 2: Build the UI

Run from the UI module directory (`D:\Code\study\xhbookstorev2\xhbookstore-ui`):

- **staging**: `npm run build:stage` (uses `.env.staging` → VUE_APP_BASE_API = `/stage-api`)
- **prod**: `npm run build:prod` (uses `.env.production` → VUE_APP_BASE_API = `/prod-api`)

The output is in: `xhbookstore-ui/dist/`

### Step 3: Package and Upload

1. **Zip the dist folder:**
   ```powershell
   Compress-Archive -Path .\dist\* -DestinationPath .\dist.zip -Force
   ```

2. **Upload to server:**
   ```bash
   scp -i <ssh_key> dist.zip <ssh_user>@<ip>:/tmp/dist.zip
   ```

### Step 4: Deploy on Server

SSH into the server and:

1. **Backup existing site:**
   ```
   cd <ui_path> && tar -czf /tmp/xhbookstore-ui-backup-$(date +%Y%m%d_%H%M%S).tar.gz .
   ```

2. **Clean and extract:**
   ```
   rm -rf <ui_path>/* <ui_path>/.[!.]*
   unzip -o /tmp/dist.zip -d <ui_path>/
   ```

3. **Verify files are in place:**
   ```
   ls -la <ui_path>/
   ```

4. **Clean up temp file:**
   ```
   rm -f /tmp/dist.zip
   ```

5. **Verify the site is accessible:**
   ```
   curl -s -o /dev/null -w "%{http_code}" http://<ip>/
   ```
   Should return HTTP 200.

### Step 5: Report

Summarize the deployment: build status, file size, upload result, backup location, and HTTP health check.
