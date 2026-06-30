---
name: deploy-admin
description: Build, upload, and deploy the xhbookstore-admin (管理后台) module to the server. Usage: /deploy-admin -d <staging|prod>
arg-hint: -d <staging|prod>
---

# Deploy Admin Module

Build the `xhbookstore-admin` Spring Boot module, upload the JAR to the server, update the systemd service, and restart it.

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
mvn package -pl xhbookstore-admin -am -Dmaven.test.skip=true -q
```

If Maven is not in PATH, use the installed Maven at `C:\ProgramData\maven\apache-maven-3.9.9\bin\mvn` with `JAVA_HOME=C:\Program Files\Java\jdk-17`.

The output JAR is at: `xhbookstore-admin/target/xhbookstore-admin.jar`

### Step 3: Upload JAR to Server

```bash
scp -i <ssh_key> xhbookstore-admin/target/xhbookstore-admin.jar <ssh_user>@<ip>:<jar_path>/xhbookstore-admin.jar.tmp
```

### Step 4: Update Systemd Service and Deploy

SSH into the server and:

1. **Stop the service:**
   ```
   systemctl stop <admin_service>
   ```

2. **Backup old JAR:**
   ```
   cp <jar_path>/xhbookstore-admin.jar <jar_path>/xhbookstore-admin.jar.bak
   ```

3. **Replace with new JAR:**
   ```
   mv <jar_path>/xhbookstore-admin.jar.tmp <jar_path>/xhbookstore-admin.jar
   ```

4. **Update systemd service file** to ensure the correct Spring profile and JVM options for this environment.

   Read the current service file with `systemctl cat <admin_service>`, then update it at `/etc/systemd/system/<admin_service>.service`:
   ```
   [Unit]
   Description=XhBookstore Admin Service
   After=network.target

   [Service]
   Type=simple
   User=root
   WorkingDirectory=<jar_path>
   ExecStart=<java_home> <jvm_opts> -jar <jar_path>/xhbookstore-admin.jar --server.port=8090 --spring.profiles.active=<profile>
   Restart=on-failure
   RestartSec=10

   [Install]
   WantedBy=multi-user.target
   ```

5. **Reload systemd and restart:**
   ```
   systemctl daemon-reload
   systemctl start <admin_service>
   ```

6. **Check status:**
   ```
   systemctl status <admin_service> --no-pager
   ```

7. **Verify health** (wait a few seconds for startup):
   ```
   curl -s http://<ip>:8090/ | head -5
   ```

### Step 5: Report

Summarize the deployment result: build status, upload status, service status, and health check result.
