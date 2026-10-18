#!/usr/bin/env bash
#
# One-time bootstrap for the Shipment Tracker VM (Oracle Cloud Free Tier).
# Turns a fresh VM into a running deployment:
#   Docker + Compose -> clone the repo -> generate .env -> firewall -> compose up
#
# Once this file is pushed to GitHub, the whole setup is one command:
#   curl -fsSL https://raw.githubusercontent.com/masadaslam1/shipment-tracker/main/deploy/setup-vm.sh | sudo bash
# (Before that: clone the repo manually and run `sudo bash deploy/setup-vm.sh`.)
#
# Safe to re-run: an existing .env is kept, an existing clone is pulled.
set -euo pipefail

# ── 1. Root check ───────────────────────────────────────────────
if [ "$(id -u)" -ne 0 ]; then
  echo "Not root, re-running with sudo..."
  exec sudo bash "$0"
fi

# The human account on the VM (ubuntu on Ubuntu, opc on Oracle Linux).
# The CI deploy job connects as this user and runs `cd ~/shipment-tracker`,
# so everything in APP_DIR must be owned by it.
TARGET_USER="${SUDO_USER:-}"
if [ -z "$TARGET_USER" ]; then
  echo "Run this via: sudo bash deploy/setup-vm.sh (from your normal user)" >&2
  exit 1
fi

APP_DIR="/home/${TARGET_USER}/shipment-tracker"
REPO_URL="https://github.com/masadaslam1/shipment-tracker.git"

# ── 2. Detect the distro ────────────────────────────────────────
. /etc/os-release   # sets $ID (ubuntu | ol) and $PRETTY_NAME
echo "Detected: ${PRETTY_NAME} (${ID})"
if [ "$ID" != "ubuntu" ] && [ "$ID" != "ol" ]; then
  echo "Unsupported distro: ${ID}. Expected ubuntu or ol (Oracle Linux)." >&2
  exit 1
fi

# ── 3. Install Docker ───────────────────────────────────────────
install_docker_ubuntu() {
  apt-get update -y
  apt-get install -y ca-certificates curl git
  # Docker's official script: adds the apt repo, installs docker-ce
  # plus the Compose v2 plugin, starts and enables the daemon.
  curl -fsSL https://get.docker.com | sh
}

install_docker_ol() {
  dnf install -y dnf-plugins-core git curl
  # Docker's official CentOS repo also serves Oracle Linux 8/9.
  dnf config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
  dnf install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
}

if [ "$ID" = "ubuntu" ]; then
  install_docker_ubuntu
else
  install_docker_ol
fi
systemctl enable --now docker   # idempotent; safe on both distros

# ── 4. OS firewall: open port 80 ────────────────────────────────
# This is only HALF of it. The OCI security list (configured in the
# console, not on the VM) must also allow ingress TCP 80. See the
# notes printed at the end of the run.
if [ "$ID" = "ol" ]; then
  # Oracle Linux ships firewalld active, blocking everything but SSH.
  systemctl enable --now firewalld
  firewall-cmd --permanent --add-port=80/tcp
  firewall-cmd --reload
else
  # OCI Ubuntu images: iptables ACCEPT, ufw inactive. Handle ufw only
  # if someone turned it on.
  if command -v ufw >/dev/null 2>&1 && ufw status | grep -q "Status: active"; then
    ufw allow 80/tcp
  fi
fi
echo "OS firewall: port 80 open."

# ── 5. Give the human user docker rights ────────────────────────
# The CI deploy SSHes in as this user and runs `docker compose`.
# New logins pick up the group, so the next CI run works.
usermod -aG docker "$TARGET_USER"

# ── 6. Get the code ─────────────────────────────────────────────
if [ -d "$APP_DIR/.git" ]; then
  echo "Existing clone found, pulling latest..."
  git -C "$APP_DIR" pull
else
  echo "Cloning ${REPO_URL} ..."
  git clone "$REPO_URL" "$APP_DIR"
fi
chown -R "$TARGET_USER":"$TARGET_USER" "$APP_DIR"

# ── 7. Create .env (kept if it already exists) ──────────────────
if [ ! -f "$APP_DIR/.env" ]; then
  cat > "$APP_DIR/.env" <<EOF
POSTGRES_DB=shipmenttracker
POSTGRES_USER=shipment
POSTGRES_PASSWORD=$(openssl rand -hex 24)
EOF
  chmod 600 "$APP_DIR/.env"
  echo ".env created with a generated Postgres password."
else
  echo ".env already exists, leaving it alone."
fi
chown "$TARGET_USER":"$TARGET_USER" "$APP_DIR/.env"

# ── 8. Start the stack ──────────────────────────────────────────
cd "$APP_DIR"
docker compose up -d --build
docker compose ps

# ── 9. Done ─────────────────────────────────────────────────────
PUBLIC_IP=$(curl -fsS --max-time 5 ifconfig.me 2>/dev/null || echo "unknown")
echo
echo "Stack is up. Open http://${PUBLIC_IP}/ in a browser."
echo
echo "Still to do manually:"
echo "  1. OCI console: open ingress TCP 80 in the VCN security list"
echo "  2. GitHub secrets: SSH_HOST=${PUBLIC_IP}, SSH_USER=${TARGET_USER}, SSH_KEY=<the VM's private key>"
