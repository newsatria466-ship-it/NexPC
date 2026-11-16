import subprocess
import os
import shutil
import platform
import ctypes

class ClipboardManager:
    def __init__(self):
        self.is_windows = platform.system() == "Windows"

    def _get_linux_env(self):
        env = os.environ.copy()
        if hasattr(os, "getuid"):
            uid = os.getuid()
            if "XDG_RUNTIME_DIR" not in env:
                env["XDG_RUNTIME_DIR"] = f"/run/user/{uid}"
        if "WAYLAND_DISPLAY" not in env:
            env["WAYLAND_DISPLAY"] = "wayland-0"
        if "DISPLAY" not in env:
            env["DISPLAY"] = ":0"
        return env

    def get_text(self) -> str:
        if self.is_windows:
            # 1. Windows ctypes User32 API (no external processes, instant)
            try:
                u32 = ctypes.windll.user32
                k32 = ctypes.windll.kernel32
                if u32.OpenClipboard(None):
                    try:
                        CF_UNICODETEXT = 13
                        handle = u32.GetClipboardData(CF_UNICODETEXT)
                        if handle:
                            p_text = k32.GlobalLock(handle)
                            if p_text:
                                try:
                                    text = ctypes.c_wchar_p(p_text).value or ""
                                    return text
                                finally:
                                    k32.GlobalUnlock(handle)
                    finally:
                        u32.CloseClipboard()
            except Exception as e:
                print(f"[ClipboardManager] Windows ctypes read failed: {e}")

            # 2. Windows PowerShell fallback
            try:
                res = subprocess.run(
                    ["powershell", "-NoProfile", "-Command", "Get-Clipboard"],
                    capture_output=True,
                    text=True,
                    timeout=2
                )
                if res.returncode == 0:
                    return res.stdout.rstrip("\r\n")
            except Exception:
                pass
            return ""

        # Linux Implementation
        env = self._get_linux_env()
        # 1. Try wl-paste
        if shutil.which("wl-paste"):
            try:
                res = subprocess.run(
                    ["wl-paste", "--no-newline"],
                    capture_output=True,
                    text=True,
                    env=env,
                    timeout=2
                )
                if res.returncode == 0:
                    return res.stdout
            except Exception as e:
                print(f"[ClipboardManager] wl-paste failed: {e}")

        # 2. Try xclip fallback
        if shutil.which("xclip"):
            try:
                res = subprocess.run(
                    ["xclip", "-selection", "clipboard", "-o"],
                    capture_output=True,
                    text=True,
                    env=env,
                    timeout=2
                )
                if res.returncode == 0:
                    return res.stdout
            except Exception as e:
                print(f"[ClipboardManager] xclip paste failed: {e}")

        # 3. Try xsel fallback
        if shutil.which("xsel"):
            try:
                res = subprocess.run(
                    ["xsel", "--clipboard", "--output"],
                    capture_output=True,
                    text=True,
                    env=env,
                    timeout=2
                )
                if res.returncode == 0:
                    return res.stdout
            except Exception as e:
                print(f"[ClipboardManager] xsel paste failed: {e}")

        return ""

    def set_text(self, text: str) -> bool:
        if self.is_windows:
            # 1. Windows ctypes User32 API
            try:
                u32 = ctypes.windll.user32
                k32 = ctypes.windll.kernel32
                if u32.OpenClipboard(None):
                    try:
                        u32.EmptyClipboard()
                        CF_UNICODETEXT = 13
                        GMEM_MOVEABLE = 0x0002
                        encoded = (text + "\0").encode("utf-16le")
                        h_mem = k32.GlobalAlloc(GMEM_MOVEABLE, len(encoded))
                        if h_mem:
                            p_mem = k32.GlobalLock(h_mem)
                            if p_mem:
                                ctypes.memmove(p_mem, encoded, len(encoded))
                                k32.GlobalUnlock(h_mem)
                                u32.SetClipboardData(CF_UNICODETEXT, h_mem)
                                return True
                    finally:
                        u32.CloseClipboard()
            except Exception as e:
                print(f"[ClipboardManager] Windows ctypes write failed: {e}")

            # 2. Windows clip.exe fallback
            try:
                p = subprocess.Popen(["clip"], stdin=subprocess.PIPE, text=True)
                p.communicate(input=text, timeout=2)
                return p.returncode == 0
            except Exception:
                return False

        # Linux Implementation
        env = self._get_linux_env()
        # 1. Try wl-copy
        if shutil.which("wl-copy"):
            try:
                res = subprocess.run(
                    ["wl-copy"],
                    input=text,
                    text=True,
                    env=env,
                    timeout=2
                )
                if res.returncode == 0:
                    return True
            except Exception as e:
                print(f"[ClipboardManager] wl-copy failed: {e}")

        # 2. Try xclip fallback
        if shutil.which("xclip"):
            try:
                res = subprocess.run(
                    ["xclip", "-selection", "clipboard"],
                    input=text,
                    text=True,
                    env=env,
                    timeout=2
                )
                if res.returncode == 0:
                    return True
            except Exception as e:
                print(f"[ClipboardManager] xclip copy failed: {e}")

        # 3. Try xsel fallback
        if shutil.which("xsel"):
            try:
                res = subprocess.run(
                    ["xsel", "--clipboard", "--input"],
                    input=text,
                    text=True,
                    env=env,
                    timeout=2
                )
                if res.returncode == 0:
                    return True
            except Exception as e:
                print(f"[ClipboardManager] xsel copy failed: {e}")

        return False

clipboard_manager = ClipboardManager()
