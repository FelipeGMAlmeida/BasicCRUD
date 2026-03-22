#!/usr/bin/env bash
# =============================================================================
# BasicCRUD — DigitalOcean Droplet Setup Script
# Run once as root on a fresh Ubuntu 22.04+ Droplet
# =============================================================================
set -e

echo "=== BasicCRUD — Droplet Setup ==="
echo ""

# 1. Update system packages
echo "[1/4] Updating system packages..."
# Remove PPAs quebrados que possam causar falha no update (ex: certbot no Ubuntu 24.04)
find /etc/apt/sources.list.d/ -name "*.list" \
  -exec grep -l "launchpadcontent.net/certbot" {} \; \
  | xargs rm -f 2>/dev/null || true
apt-get update -y
apt-get upgrade -y

# 2. Install Docker (official script)
echo "[2/4] Installing Docker..."
curl -fsSL https://get.docker.com | sh
systemctl enable docker
systemctl start docker

# Add current user to docker group if not root
if [ "$(id -u)" -ne 0 ]; then
  usermod -aG docker "$USER"
  echo "      Added $USER to the docker group. Re-login for it to take effect."
fi

# 3. Create application directory
echo "[3/4] Creating app directory at /opt/basiccrud..."
mkdir -p /opt/basiccrud

# 4. Print next steps
echo "[4/4] Setup complete!"
echo ""
echo "======================================================="
echo "  NEXT STEPS"
echo "======================================================="
echo ""
echo "1. Add these GitHub Secrets to your repository"
echo "   (Settings → Secrets and variables → Actions):"
echo ""
echo "   Secret Name         | Value"
echo "   --------------------|---------------------------------------"
echo "   DO_SSH_HOST         | $(curl -s ifconfig.me 2>/dev/null || echo '<DROPLET_IP>')"
echo "   DO_SSH_USER         | root"
echo "   DO_SSH_PRIVATE_KEY  | <your SSH private key (~/.ssh/id_rsa)>"
echo "   GHCR_TOKEN          | <GitHub PAT with read:packages scope>"
echo "   DB_PASSWORD         | <strong password for PostgreSQL>"
echo "   JWT_SECRET          | <strong secret for JWT signing>"
echo "   VITE_API_URL        | http://<DROPLET_IP>:8080"
echo ""
echo "2. Generate a GitHub Personal Access Token (PAT):"
echo "   https://github.com/settings/tokens/new"
echo "   Scopes required: read:packages"
echo ""
echo "3. Push to the 'main' branch to trigger the first deploy:"
echo "   git push origin main"
echo ""
echo "4. After the first deploy, your app will be available at:"
echo "   Frontend → http://<DROPLET_IP>:3000"
echo "   Backend  → http://<DROPLET_IP>:8080"
echo "======================================================="
