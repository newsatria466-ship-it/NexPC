#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PARENT_DIR="$(cd "$DIR/.." && pwd)"

if [ ! -d "$DIR/venv" ]; then
    echo "Creating virtual environment..."
    python3 -m venv "$DIR/venv"
    "$DIR/venv/bin/pip" install -r "$DIR/requirements.txt"
fi

cd "$PARENT_DIR"
echo "Starting NexPC Agent..."
exec "$DIR/venv/bin/python" -m uvicorn agent.app.main:app --host 0.0.0.0 --port 8765
