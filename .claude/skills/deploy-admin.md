---
name: deploy-admin
description: Build and deploy the xhbookstore-admin module. Usage: /deploy-admin -d <staging|prod>
arg-hint: -d <staging|prod>
---

# Deploy Admin Module

## Parameters

- `-d <env>` — `staging` → 152.136.127.168, `prod` → production server.

## Prerequisites

- Java 17, Maven (in PATH), SSH key at `~/.ssh/id_ed25519_deploy`

## Workflow

### 1. Load Config
Read `.claude/skills/servers.yml` → resolve IP, jar path, service name, JVM opts, profile.

### 2. Build
From the project root (`git rev-parse --show-toplevel`):
```bash
mvn clean package -pl xhbookstore-admin -am -Dmaven.test.skip=true -q
```
JAR is at: `xhbookstore-admin/target/xhbookstore-admin.jar`

### 3. Upload
```bash
scp -i ~/.ssh/id_ed25519_deploy xhbookstore-admin/target/xhbookstore-admin.jar <user>@<ip>:<jar_path>/xhbookstore-admin.jar
```

### 4. Deploy
```bash
ssh -i ~/.ssh/id_ed25519_deploy <user>@<ip> "
  systemctl stop <service>
  cp <jar_path>/xhbookstore-admin.jar <jar_path>/xhbookstore-admin.jar.bak
  systemctl start <service>
  sleep 4
  systemctl is-active <service>
  curl -s -o /dev/null -w 'HTTP: %{http_code}' http://127.0.0.1:8090/
"
```

### 5. Report
Build → Upload → Deploy → Health check result.
