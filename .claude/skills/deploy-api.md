---
name: deploy-api
description: Build and deploy the xhbookstore-api module. Usage: /deploy-api -d <staging|prod>
arg-hint: -d <staging|prod>
---

# Deploy API Module

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
mvn clean package -pl xhbookstore-api -am -Dmaven.test.skip=true -q
```
JAR is at: `xhbookstore-api/target/xhbookstore-api.jar`

### 3. Upload
```bash
scp -i ~/.ssh/id_ed25519_deploy xhbookstore-api/target/xhbookstore-api.jar <user>@<ip>:<jar_path>/xhbookstore-api.jar
```

### 4. Deploy
```bash
ssh -i ~/.ssh/id_ed25519_deploy <user>@<ip> "
  systemctl stop <service>
  cp <jar_path>/xhbookstore-api.jar <jar_path>/xhbookstore-api.jar.bak
  systemctl start <service>
  sleep 4
  systemctl is-active <service>
  curl -s -o /dev/null -w 'HTTP: %{http_code}' http://127.0.0.1:8091/
"
```

### 5. Report
Build → Upload → Deploy → Health check result.
