#!/bin/bash
# ============================================================
#  Mac Mini SSH 环境一键配置脚本
#  配置后可免密连接测试服务器 + 生产服务器
#
#  用法:
#    chmod +x setup-mac-ssh.sh
#    ./setup-mac-ssh.sh
# ============================================================
set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

echo ""
echo -e "${CYAN}╔══════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║   Mac Mini SSH 环境一键配置              ║${NC}"
echo -e "${CYAN}║   测试服务器 + 生产服务器                ║${NC}"
echo -e "${CYAN}╚══════════════════════════════════════════╝${NC}"
echo ""

# ---- Step 1: 创建私钥 ----
echo -e "${YELLOW}[1/4]${NC} 创建 SSH 私钥..."
mkdir -p ~/.ssh

cat > ~/.ssh/id_ed25519_deploy << 'KEYEOF'
-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW
QyNTUxOQAAACBkGZUMysEW7OOC0HPDqDeBpdez6izmSsBLm9IgQHGRyQAAAJgxrbcIMa23
CAAAAAtzc2gtZWQyNTUxOQAAACBkGZUMysEW7OOC0HPDqDeBpdez6izmSsBLm9IgQHGRyQ
AAAEDe7EP4h9xYqLx7s76Vny0jn8EnJs56ahEQ09pcdpOIbGQZlQzKwRbs44LQc8OoN4Gl
17PqLOZKwEub0iBAcZHJAAAAEnhoYm9va3N0b3JlLWRlcGxveQECAw==
-----END OPENSSH PRIVATE KEY-----
KEYEOF

chmod 600 ~/.ssh/id_ed25519_deploy
echo -e "  ${GREEN}✅ 私钥已创建: ~/.ssh/id_ed25519_deploy${NC}"

# ---- Step 2: 配置 SSH config ----
echo -e "${YELLOW}[2/4]${NC} 配置 SSH 别名..."

# 备份现有 config
if [ -f ~/.ssh/config ]; then
    cp ~/.ssh/config ~/.ssh/config.bak.$(date +%Y%m%d_%H%M%S)
    echo -e "  ${CYAN}📦 已备份原有 config${NC}"
fi

cat >> ~/.ssh/config << 'SSHEOF'

# ═══════════════════════════════════════════
#  XhBookstore 项目服务器
# ═══════════════════════════════════════════

# 测试服务器 (腾讯云 上海)
Host staging
    HostName 152.136.127.168
    User root
    IdentityFile ~/.ssh/id_ed25519_deploy
    StrictHostKeyChecking accept-new

# 生产服务器 (腾讯云 上海)
Host prod
    HostName 43.138.104.168
    User ubuntu
    IdentityFile ~/.ssh/id_ed25519_deploy
    StrictHostKeyChecking accept-new
SSHEOF

chmod 600 ~/.ssh/config
echo -e "  ${GREEN}✅ SSH config 已配置${NC}"

# ---- Step 3: 测试连接 ----
echo -e "${YELLOW}[3/4]${NC} 测试服务器连接..."

# 测试 staging
echo -n "  测试服务器 (staging)... "
if ssh -o ConnectTimeout=5 -o StrictHostKeyChecking=accept-new staging "echo OK" &>/dev/null; then
    echo -e "${GREEN}✅ 连接成功${NC}"
    STAGING_OK=true
else
    echo -e "${RED}❌ 连接失败${NC}"
    STAGING_OK=false
fi

# 测试 prod
echo -n "  生产服务器 (prod)... "
if ssh -o ConnectTimeout=5 -o StrictHostKeyChecking=accept-new prod "echo OK" &>/dev/null; then
    echo -e "${GREEN}✅ 连接成功${NC}"
    PROD_OK=true
else
    echo -e "${RED}❌ 连接失败${NC}"
    PROD_OK=false
fi

# ---- Step 4: 输出摘要 ----
echo -e "${YELLOW}[4/4]${NC} 配置完成！"
echo ""
echo -e "${CYAN}╔══════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║          配置摘要                        ║${NC}"
echo -e "${CYAN}╠══════════════════════════════════════════╣${NC}"

if [ "$STAGING_OK" = true ]; then
    echo -e "${CYAN}║${NC}  🟢 staging → ssh staging             ${CYAN}║${NC}"
    echo -e "${CYAN}║${NC}     152.136.127.168 (root)            ${CYAN}║${NC}"
else
    echo -e "${CYAN}║${NC}  🔴 staging → 连接失败，请检查网络     ${CYAN}║${NC}"
fi

if [ "$PROD_OK" = true ]; then
    echo -e "${CYAN}║${NC}  🟢 prod    → ssh prod                ${CYAN}║${NC}"
    echo -e "${CYAN}║${NC}     43.138.104.168 (ubuntu)           ${CYAN}║${NC}"
else
    echo -e "${CYAN}║${NC}  🔴 prod    → 连接失败，请检查网络     ${CYAN}║${NC}"
fi

echo -e "${CYAN}╚══════════════════════════════════════════╝${NC}"
echo ""
echo -e "  使用方式："
echo -e "    ${GREEN}ssh staging${NC}    # 测试服务器"
echo -e "    ${GREEN}ssh prod${NC}       # 生产服务器"
echo -e "    ${GREEN}scp file staging:/path/${NC}  # 传文件"
echo ""
