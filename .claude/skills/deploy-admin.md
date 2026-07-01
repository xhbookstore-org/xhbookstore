---
name: deploy-admin
description: Build and deploy xhbookstore-admin JAR with minimal downtime (~5s). Usage: /deploy-admin -d <staging|prod>
arg-hint: -d <staging|prod>
---

# Deploy Admin Module

## Parameters

- `-d <env>` — `staging` → 152.136.127.168, `prod` → production server.

## Prerequisites

- Java 17, Maven (in PATH), SSH key at `~/.ssh/id_ed25519_deploy`

## Workflow (build first, stop last)

### 1. Load Config
Read `.claude/skills/servers.yml` → IP, jar_path, service name, profile.

### 2. Build & Upload (service still running)
```bash
cd $(git rev-parse --show-toplevel)
mvn clean package -pl xhbookstore-admin -am -Dmaven.test.skip=true -q
scp xhbookstore-admin/target/xhbookstore-admin.jar <user>@<ip>:<jar_path>/xhbookstore-admin.jar.tmp
```

### 3. Atomic Swap (~5s downtime)
```bash
ssh <user>@<ip> "
  systemctl stop <service>
  mv <jar_path>/xhbookstore-admin.jar.tmp <jar_path>/xhbookstore-admin.jar
  systemctl start <service>
  sleep 4
  systemctl is-active <service>
  curl -s -o /dev/null -w 'HTTP: %{http_code}' http://127.0.0.1:8090/
"
```

### 4. Report
Build → Upload → Stop+Swap+Start → Health.
