#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVICE_DIR="$HOME/.config/systemd/user"

mkdir -p "$SERVICE_DIR"
cp "$DIR/nexpc.service" "$SERVICE_DIR/nexpc.service"

systemctl --user daemon-reload
systemctl --user enable --now nexpc.service

echo "============================================="
echo "✅ NexPC Service installed & started successfully!"
echo "Status check: systemctl --user status nexpc.service"
echo "Logs: journalctl --user -u nexpc.service -f"
echo "============================================="
