import os
import glob
import subprocess
import shutil
import psutil
from pathlib import Path
from typing import List, Dict, Any, Optional

class ApplicationManager:
    def __init__(self):
        self.cached_apps: List[Dict[str, Any]] = []

    def _parse_desktop_file(self, file_path: str) -> Optional[Dict[str, Any]]:
        try:
            name, exec_cmd, icon, categories, nodisplay = None, None, None, [], False
            with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
                in_desktop_entry = False
                for line in f:
                    line = line.strip()
                    if line == "[Desktop Entry]":
                        in_desktop_entry = True
                        continue
                    elif line.startswith("[") and in_desktop_entry:
                        break # End of [Desktop Entry] section

                    if not in_desktop_entry:
                        continue

                    if line.startswith("Name=") and not name:
                        name = line[5:].strip()
                    elif line.startswith("Exec=") and not exec_cmd:
                        exec_cmd = line[5:].strip().split("%")[0].strip()
                    elif line.startswith("Icon=") and not icon:
                        icon = line[5:].strip()
                    elif line.startswith("Categories="):
                        categories = [c.strip() for c in line[11:].split(";") if c.strip()]
                    elif line.startswith("NoDisplay="):
                        nodisplay = line[10:].strip().lower() == "true"

            if name and exec_cmd and not nodisplay:
                desktop_id = Path(file_path).stem
                bin_name = exec_cmd.split()[0] if exec_cmd else ""
                return {
                    "id": desktop_id,
                    "name": name,
                    "exec": exec_cmd,
                    "binary": os.path.basename(bin_name),
                    "icon": icon or "application-x-executable",
                    "categories": categories,
                    "file_path": file_path
                }
        except Exception:
            pass
        return None

    def get_installed_apps(self) -> List[Dict[str, Any]]:
        apps_dict = {}
        paths = [
            os.path.expanduser("~/.local/share/applications/*.desktop"),
            "/usr/share/applications/*.desktop"
        ]

        for p in paths:
            for file_path in glob.glob(p):
                app = self._parse_desktop_file(file_path)
                if app and app["id"] not in apps_dict:
                    apps_dict[app["id"]] = app

        # Get list of running processes to flag active apps
        running_names = set()
        for p in psutil.process_iter(['name']):
            try:
                if p.info['name']:
                    running_names.add(p.info['name'].lower())
            except Exception:
                continue

        app_list = []
        for app in apps_dict.values():
            bin_name = app["binary"].lower()
            is_running = (bin_name in running_names) or (app["name"].lower() in running_names)
            app_copy = dict(app)
            app_copy["is_running"] = is_running
            app_list.append(app_copy)

        # Sort: running first, then alphabetical
        app_list.sort(key=lambda x: (not x["is_running"], x["name"].lower()))
        return app_list

    def launch_app(self, desktop_id: str) -> Dict[str, Any]:
        # Try gtk-launch first
        if shutil.which("gtk-launch"):
            try:
                subprocess.Popen(["gtk-launch", desktop_id], start_new_session=True)
                return {"success": True, "message": f"Launched {desktop_id}"}
            except Exception as e:
                pass

        # Fallback: search desktop file and run exec
        apps = self.get_installed_apps()
        for app in apps:
            if app["id"] == desktop_id:
                try:
                    subprocess.Popen(app["exec"].split(), start_new_session=True)
                    return {"success": True, "message": f"Launched {app['name']}"}
                except Exception as e:
                    return {"success": False, "message": str(e)}

        return {"success": False, "message": f"Application {desktop_id} not found"}

    def stop_app(self, desktop_id: str) -> Dict[str, Any]:
        apps = self.get_installed_apps()
        target_app = next((a for a in apps if a["id"] == desktop_id), None)
        if not target_app:
            return {"success": False, "message": f"Application {desktop_id} not found"}

        bin_name = target_app["binary"].lower()
        killed_count = 0
        for p in psutil.process_iter(['pid', 'name']):
            try:
                if p.info['name'] and p.info['name'].lower() == bin_name:
                    p.terminate()
                    killed_count += 1
            except Exception:
                continue

        return {"success": True, "message": f"Terminated {killed_count} instances of {target_app['name']}"}

app_manager = ApplicationManager()
