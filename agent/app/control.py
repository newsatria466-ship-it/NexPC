import subprocess
import shutil
import platform
import ctypes
from typing import Dict, Any

class SystemController:
    def __init__(self):
        self.is_windows = platform.system() == "Windows"

    def lock_screen(self) -> Dict[str, Any]:
        if self.is_windows:
            try:
                ctypes.windll.user32.LockWorkStation()
                return {"success": True, "message": "Windows screen locked"}
            except Exception as e:
                return {"success": False, "message": str(e)}

        cmds = [
            ["loginctl", "lock-session"],
            ["qdbus", "org.freedesktop.ScreenSaver", "/ScreenSaver", "Lock"],
            ["xdg-screensaver", "lock"],
            ["hyprlock"],
            ["swaylock"]
        ]
        for cmd in cmds:
            if shutil.which(cmd[0]):
                try:
                    res = subprocess.run(cmd, capture_output=True, text=True, timeout=3)
                    if res.returncode == 0:
                        return {"success": True, "message": f"Screen locked using {cmd[0]}"}
                except Exception:
                    continue
        return {"success": False, "message": "No lock screen command succeeded"}

    def suspend(self) -> Dict[str, Any]:
        if self.is_windows:
            try:
                subprocess.Popen(["rundll32.exe", "powrprof.dll,SetSuspendState", "0,1,0"])
                return {"success": True, "message": "Windows suspending..."}
            except Exception as e:
                return {"success": False, "message": str(e)}
        try:
            subprocess.Popen(["systemctl", "suspend"])
            return {"success": True, "message": "System suspending..."}
        except Exception as e:
            return {"success": False, "message": str(e)}

    def reboot(self) -> Dict[str, Any]:
        if self.is_windows:
            try:
                subprocess.Popen(["shutdown.exe", "/r", "/t", "0"])
                return {"success": True, "message": "Windows rebooting..."}
            except Exception as e:
                return {"success": False, "message": str(e)}
        try:
            subprocess.Popen(["systemctl", "reboot"])
            return {"success": True, "message": "System rebooting..."}
        except Exception as e:
            return {"success": False, "message": str(e)}

    def shutdown(self) -> Dict[str, Any]:
        if self.is_windows:
            try:
                subprocess.Popen(["shutdown.exe", "/s", "/t", "0"])
                return {"success": True, "message": "Windows shutting down..."}
            except Exception as e:
                return {"success": False, "message": str(e)}
        try:
            subprocess.Popen(["systemctl", "poweroff"])
            return {"success": True, "message": "System shutting down..."}
        except Exception as e:
            return {"success": False, "message": str(e)}

    def set_volume(self, action: str, value: int = 5) -> Dict[str, Any]:
        if self.is_windows:
            # Volume via PowerShell Sound key emulator
            try:
                key_code = 0xAF if action == "up" else (0xAE if action == "down" else 0xAD)
                ctypes.windll.user32.keybd_event(key_code, 0, 0, 0)
                ctypes.windll.user32.keybd_event(key_code, 0, 2, 0)
                return {"success": True, "message": f"Windows volume {action} executed"}
            except Exception as e:
                return {"success": False, "message": str(e)}

        if shutil.which("wpctl"):
            try:
                if action == "up":
                    subprocess.run(["wpctl", "set-volume", "@DEFAULT_AUDIO_SINK@", f"{value}%+"], timeout=2)
                elif action == "down":
                    subprocess.run(["wpctl", "set-volume", "@DEFAULT_AUDIO_SINK@", f"{value}%-"], timeout=2)
                elif action == "mute":
                    subprocess.run(["wpctl", "set-mute", "@DEFAULT_AUDIO_SINK@", "toggle"], timeout=2)
                return {"success": True, "message": f"Volume {action} executed (wpctl)"}
            except Exception as e:
                return {"success": False, "message": str(e)}
        elif shutil.which("pactl"):
            try:
                if action == "up":
                    subprocess.run(["pactl", "set-sink-volume", "@DEFAULT_SINK@", f"+{value}%"], timeout=2)
                elif action == "down":
                    subprocess.run(["pactl", "set-sink-volume", "@DEFAULT_SINK@", f"-{value}%"], timeout=2)
                elif action == "mute":
                    subprocess.run(["pactl", "set-sink-mute", "@DEFAULT_SINK@", "toggle"], timeout=2)
                return {"success": True, "message": f"Volume {action} executed (pactl)"}
            except Exception as e:
                return {"success": False, "message": str(e)}
        return {"success": False, "message": "No supported audio controller found"}

system_controller = SystemController()
