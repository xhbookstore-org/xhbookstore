---
name: deploy-api
description: Build and deploy xhbookstore-api JAR with minimal downtime (~5s). Usage: /deploy-api -d <staging|prod>
arg-hint: -d <staging|prod>
---

# Deploy API Module

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
mvn clean package -pl xhbookstore-api -am -Dmaven.test.skip=true -q
scp xhbookstore-api/target/xhbookstore-api.jar <user>@<ip>:<jar_path>/xhbookstore-api.jar.tmp
```

### 3. Atomic Swap (~5s downtime)
```bash
ssh <user>@<ip> "
  systemctl stop <service>
  mv <jar_path>/xhbookstore-api.jar.tmp <jar_path>/xhbookstore-api.jar
  systemctl start <service>
  sleep 4
  systemctl is-active <service>
  curl -s -o /dev/null -w 'HTTP: %{http_code}' http://127.0.0.1:8091/
"
```

### 4. Report
Build → Upload → Stop+Swap+Start → Health.
