---
name: deploy-all
description: One-click deploy all modules (admin + api + ui) to server. Usage: /deploy-all -d <staging|prod>
arg-hint: -d <staging|prod>
---

# Deploy All Modules

Build and deploy all three modules (admin, api, ui) to the target server in one pass.

## Parameters

- `-d <env>` — Deployment environment. `staging` → 152.136.127.168, `prod` → production server.
  - If `-d` is omitted, **ask the user** which environment to deploy to.

## Workflow

### Step 1: Load Server Config

Read `.claude/skills/servers.yml` to resolve the target server config for the given `<env>`.

### Step 2: Build All Modules

**Build order**: admin → api → ui (parallel where possible)

1. **Admin JAR** (port 8090):
   ```
   cd D:/Code/study/xhbookstorev2
   C:/ProgramData/maven/apache-maven-3.9.9/bin/mvn clean package -pl xhbookstore-admin -am -Dmaven.test.skip=true -q
   ```
   If Maven is not available, try `mvn` from PATH.

2. **API JAR** (port 8091):
   ```
   cd D:/Code/study/xhbookstorev2
   C:/ProgramData/maven/apache-maven-3.9.9/bin/mvn clean package -pl xhbookstore-api -am -Dmaven.test.skip=true -q
   ```

3. **UI dist**:
   ```
   cd D:/Code/study/xhbookstorev2/xhbookstore-ui
   npm run build:stage   # for staging
   npm run build:prod    # for prod
   ```
   Then zip the dist:
   ```
   powershell Compress-Archive -Path .\dist\* -DestinationPath .\dist.zip -Force
   ```

### Step 3: Upload All Files

```bash
# Upload admin JAR
scp -i <ssh_key> xhbookstore-admin/target/xhbookstore-admin.jar <ssh_user>@<ip>:<jar_path>/xhbookstore-admin.jar

# Upload API JAR
scp -i <ssh_key> xhbookstore-api/target/xhbookstore-api.jar <ssh_user>@<ip>:<jar_path>/xhbookstore-api.jar

# Upload UI
scp -i <ssh_key> xhbookstore-ui/dist.zip <ssh_user>@<ip>:/tmp/dist.zip
```

### Step 4: Deploy on Server

SSH into the server:

1. **Stop all services:**
   ```
   systemctl stop xhbookstore-admin xhbookstore-api
   ```

2. **Backup JARs:**
   ```
   cp <jar_path>/xhbookstore-admin.jar <jar_path>/xhbookstore-admin.jar.bak
   cp <jar_path>/xhbookstore-api.jar <jar_path>/xhbookstore-api.jar.bak
   ```

3. **Deploy UI:**
   ```
   cd <ui_path>
   tar -czf /tmp/xhbookstore-ui-backup-$(date +%Y%m%d_%H%M%S).tar.gz .
   rm -rf * .[!.]*
   unzip -o /tmp/dist.zip -d .
   rm -f /tmp/dist.zip
   ```

4. **Restart services:**
   ```
   systemctl start xhbookstore-admin xhbookstore-api
   ```

5. **Verify health:**
   ```
   systemctl status xhbookstore-admin --no-pager --lines=3
   systemctl status xhbookstore-api --no-pager --lines=3
   curl -s -o /dev/null -w "UI: %{http_code}" http://<ip>/
   curl -s -o /dev/null -w "Admin: %{http_code}" http://<ip>:8090/
   curl -s -o /dev/null -w "API: %{http_code}" http://<ip>:8091/
   ```

### Step 5: Report

Summarize all deployment results in a table format:

| Module | Build | Upload | Deploy | Health |
|--------|-------|--------|--------|--------|
| Admin  | ✅/❌   | ✅/❌    | ✅/❌    | HTTP xxx |
| API    | ✅/❌   | ✅/❌    | ✅/❌    | HTTP xxx |
| UI     | ✅/❌   | ✅/❌    | ✅/❌    | HTTP xxx |
