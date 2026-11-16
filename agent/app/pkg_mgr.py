import subprocess
import shutil
from typing import List, Dict, Any

SMART_ALIASES = {
    "whatsap": ["whatsapp", "whatsdesk", "zapzap"],
    "whatsapp": ["whatsapp", "whatsdesk", "zapzap"],
    "wa": ["whatsapp", "whatsdesk", "zapzap"],
    "vscode": ["code", "visual-studio-code"],
    "vs code": ["code", "visual-studio-code"],
    "chrome": ["google-chrome", "chromium"],
    "office": ["libreoffice", "onlyoffice"],
    "torrent": ["qbittorrent", "transmission"],
    "spotify": ["spotify"],
    "discord": ["discord"],
    "telegram": ["telegram-desktop"],
}

class PackageManager:
    def check_updates(self) -> Dict[str, Any]:
        updates = []
        if shutil.which("checkupdates"):
            try:
                res = subprocess.run(["checkupdates"], capture_output=True, text=True, timeout=15)
                if res.stdout:
                    for line in res.stdout.strip().split("\n"):
                        parts = line.split()
                        if len(parts) >= 4:
                            updates.append({
                                "package": parts[0],
                                "old_version": parts[1],
                                "new_version": parts[3]
                            })
            except Exception:
                pass
        return {
            "total_updates": len(updates),
            "updates": updates[:50]
        }

    def _query_binary(self, tool_bin: str, term: str) -> List[Dict[str, Any]]:
        results = []
        try:
            res = subprocess.run([tool_bin, "-Ss", term], capture_output=True, text=True, timeout=12)
            lines = res.stdout.strip().split("\n")
            i = 0
            while i < len(lines):
                line = lines[i].strip()
                if "/" in line and not line.startswith(" "):
                    parts = line.split()
                    repo_pkg = parts[0]
                    ver = parts[1] if len(parts) > 1 else ""
                    installed = "[installed]" in line
                    desc = lines[i+1].strip() if i+1 < len(lines) else ""
                    source = repo_pkg.split("/")[0]
                    results.append({
                        "source": source,
                        "name": repo_pkg,
                        "version": ver,
                        "installed": installed,
                        "description": desc
                    })
                    i += 2
                else:
                    i += 1
        except Exception:
            pass
        return results

    def search_packages(self, query: str) -> List[Dict[str, Any]]:
        clean_q = query.strip().lower()
        if not clean_q or len(clean_q) < 2:
            return []

        search_tool = shutil.which("yay") or shutil.which("pacman")
        if not search_tool:
            return []

        # Determine all search variations (original + smart aliases)
        keywords_to_try = [clean_q]
        for key, aliases in SMART_ALIASES.items():
            if key in clean_q or clean_q in key:
                for alias in aliases:
                    if alias not in keywords_to_try:
                        keywords_to_try.append(alias)

        # Collect results
        collected: Dict[str, Dict[str, Any]] = {}
        for term in keywords_to_try:
            items = self._query_binary(search_tool, term)
            for it in items:
                pkg_key = it["name"].lower()
                if pkg_key not in collected:
                    collected[pkg_key] = it
            if len(collected) >= 40:
                break

        # If yay failed or returned 0, try pacman as fallback
        if not collected and search_tool.endswith("yay") and shutil.which("pacman"):
            for term in keywords_to_try:
                items = self._query_binary(shutil.which("pacman"), term)
                for it in items:
                    pkg_key = it["name"].lower()
                    if pkg_key not in collected:
                        collected[pkg_key] = it

        # Smart Relevance Scoring & Ranking
        def score_item(it: Dict[str, Any]) -> int:
            score = 0
            name = it["name"].lower().split("/")[-1]
            desc = it["description"].lower()

            # Exact or prefix match in name gets highest priority
            if name == clean_q:
                score += 150
            elif name.startswith(clean_q):
                score += 90
            elif any(k in name for k in keywords_to_try):
                score += 60

            # Standalone apps & prebuilt binaries preferred for desktop users
            if "-bin" in name:
                score += 40
            if "desktop" in name or "client" in desc or "gui" in desc:
                score += 30
            if any(k in desc for k in keywords_to_try):
                score += 15

            # Deprioritize background libraries, headers, development tools
            if any(bad in name for bad in ["python-", "lib", "-git", "-debug", "-devel", "-headers"]):
                score -= 30
            if any(bad in desc for bad in ["gateway", "library", "bindings", "bridge"]):
                score -= 20

            return score

        ranked = sorted(collected.values(), key=score_item, reverse=True)
        return ranked[:25]

    def install_package(self, pkg_name: str) -> Dict[str, Any]:
        clean_name = pkg_name.strip()
        if "/" in clean_name:
            clean_name = clean_name.split("/")[-1]

        if not clean_name or not clean_name.replace("-", "").replace("_", "").replace(".", "").isalnum():
            return {"success": False, "message": f"Nama paket '{clean_name}' tidak valid"}

        installer_bin = shutil.which("yay") or shutil.which("pacman")
        is_yay = bool(installer_bin and "yay" in installer_bin)
        kitty_bin = shutil.which("kitty")

        if kitty_bin:
            try:
                import os
                env = os.environ.copy()
                if "WAYLAND_DISPLAY" not in env:
                    env["WAYLAND_DISPLAY"] = "wayland-0"
                if "DISPLAY" not in env:
                    env["DISPLAY"] = ":0"

                cmd = [kitty_bin, "--hold", "-e"]
                if is_yay:
                    cmd.extend(["yay", "-S", clean_name])
                else:
                    cmd.extend(["sudo", "pacman", "-S", clean_name])

                subprocess.Popen(cmd, env=env, start_new_session=True)
                return {"success": True, "message": f"Instalasi '{clean_name}' dibuka di jendela laptop Anda"}
            except Exception as e:
                return {"success": False, "message": f"Gagal membuka instalasi: {e}"}
        else:
            try:
                if is_yay:
                    subprocess.Popen(["yay", "-S", "--noconfirm", clean_name])
                else:
                    subprocess.Popen(["sudo", "pacman", "-S", "--noconfirm", clean_name])
                return {"success": True, "message": f"Memulai instalasi '{clean_name}'"}
            except Exception as e:
                return {"success": False, "message": str(e)}

package_manager = PackageManager()
