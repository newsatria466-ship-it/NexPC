import os
import json
import secrets
import socket
from pathlib import Path
from pydantic import BaseModel

CONFIG_DIR = Path.home() / ".config" / "nexpc"
CONFIG_FILE = CONFIG_DIR / "config.json"

class AppConfig(BaseModel):
    host: str = "0.0.0.0"
    port: int = 8765
    auth_token: str = "0909"
    enable_auth: bool = True
    device_name: str = socket.gethostname()
    refresh_rate_ms: int = 1000

def load_or_create_config() -> AppConfig:
    CONFIG_DIR.mkdir(parents=True, exist_ok=True)
    if CONFIG_FILE.exists():
        try:
            with open(CONFIG_FILE, "r") as f:
                data = json.load(f)
                return AppConfig(**data)
        except Exception as e:
            print(f"[Config] Error loading config: {e}, recreating default config...")

    config = AppConfig(auth_token="0909")
    save_config(config)
    return config

def save_config(config: AppConfig):
    CONFIG_DIR.mkdir(parents=True, exist_ok=True)
    with open(CONFIG_FILE, "w") as f:
        json.dump(config.model_dump(), f, indent=2)

def get_live_token() -> str:
    try:
        if CONFIG_FILE.exists():
            with open(CONFIG_FILE, "r") as f:
                data = json.load(f)
                return str(data.get("auth_token", "0909"))
    except Exception:
        pass
    return config.auth_token

def is_auth_enabled() -> bool:
    try:
        if CONFIG_FILE.exists():
            with open(CONFIG_FILE, "r") as f:
                data = json.load(f)
                return bool(data.get("enable_auth", True))
    except Exception:
        pass
    return config.enable_auth

config = load_or_create_config()
