---
name: deploy-api
description: Build, upload, and deploy the xhbookstore-api (API模块) module to the server. Usage: /deploy-api -d <staging|prod>
arg-hint: -d <staging|prod>
---

# Deploy API Module

Build the `xhbookstore-api` Spring Boot module, upload the JAR to the server, update the systemd service, and restart it.

## Parameters

- `-d <env>` — Deployment environment. `staging` → 152.136.127.168, `prod` → production server.
  - If `-d` is omitted, **ask the user** which environment to deploy to.

## Workflow

### Step 1: Load Server Config

Read `.claude/skills/servers.yml` to resolve the target server IP, SSH key, JAR path, service name, JVM options, and Spring profile for the given `<env>`.

If the IP is not configured (e.g., prod shows `<PROD_IP_NOT_SET>`), ask the user for the production server IP and update `servers.yml`.

### Step 2: Build the JAR

Run from the project root (`D:\Code\study\xhbookstorev2`):

```
mvn package -pl xhbookstore-api -am -Dmaven.test.skip=true -q
```

If Maven is not in PATH, use the installed Maven at `C:\ProgramData\maven\apache-maven-3.9.9\bin\mvn` with `JAVA_HOME=C:\Program Files\Java\jdk-17`.

The output JAR is at: `xhbookstore-api/target/xhbookstore-api.jar`

### Step 3: Upload JAR to Server

```bash
scp -i <ssh_key> xhbookstore-api/target/xhbookstore-api.jar <ssh_user>@<ip>:<jar_path>/xhbookstore-api.jar.tmp
```

### Step 4: Update Systemd Service and Deploy

SSH into the server and:

1. **Stop the service:**
   ```
   systemctl stop <api_service>
   ```

2. **Backup old JAR:**
   ```
   cp <jar_path>/xhbookstore-api.jar <jar_path>/xhbookstore-api.jar.bak
   ```

3. **Replace with new JAR:**
   ```
   mv <jar_path>/xhbookstore-api.jar.tmp <jar_path>/xhbookstore-api.jar
   ```

4. **Update systemd service file** to ensure the correct Spring profile and JVM options for this environment.

   Read the current service file with `systemctl cat <api_service>`, then update it at `/etc/systemd/system/<api_service>.service`:
   ```
   [Unit]
   Description=XhBookstore API Service
   After=network.target

   [Service]
   Type=simple
   User=root
   WorkingDirectory=<jar_path>
   ExecStart=<java_home> <jvm_opts> -jar <jar_path>/xhbookstore-api.jar --server.port=8091 --spring.profiles.active=<profile>
   Restart=on-failure
   RestartSec=10

   [Install]
   WantedBy=multi-user.target
   ```

5. **Reload systemd and restart:**
   ```
   systemctl daemon-reload
   systemctl start <api_service>
   ```

6. **Check status:**
   ```
   systemctl status <api_service> --no-pager
   ```

7. **Verify health** (wait a few seconds for startup):
   ```
   curl -s http://<ip>:8091/ | head -5
   ```

### Step 5: Report

Summarize the deployment result: build status, upload status, service status, and health check result.
