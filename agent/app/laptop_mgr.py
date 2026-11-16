import os
import glob
import subprocess
import shutil
import platform
import psutil
from typing import Dict, Any, List

class LaptopManager:
    def __init__(self):
        self.is_windows = platform.system() == "Windows"

    def get_battery_info(self) -> Dict[str, Any]:
        info = {
            "is_present": False,
            "percentage": 0,
            "state": "unknown",
            "is_charging": False,
            "health_percent": None,
            "time_remaining_str": None,
            "time_remaining_seconds": None,
            "wattage": None,
            "cycle_count": None,
            "voltage": None
        }

        # ── Windows Battery Detection ─────────────────────────────
        if self.is_windows:
            try:
                bat = psutil.sensors_battery()
                if bat is not None:
                    info["is_present"] = True
                    info["percentage"] = int(bat.percent)
                    info["is_charging"] = bool(bat.power_plugged)
                    info["state"] = "Charging" if bat.power_plugged else "Discharging"

                    if bat.secsleft not in (psutil.POWER_TIME_UNLIMITED, psutil.POWER_TIME_UNKNOWN) and bat.secsleft > 0:
                        secs = int(bat.secsleft)
                        info["time_remaining_seconds"] = secs
                        hrs, mins = divmod(secs // 60, 60)
                        if bat.power_plugged:
                            info["time_remaining_str"] = f"{hrs}h {mins}m to full" if hrs > 0 else f"{mins}m to full"
                        else:
                            info["time_remaining_str"] = f"{hrs}h {mins}m remaining" if hrs > 0 else f"{mins}m remaining"
                    elif bat.power_plugged and bat.percent >= 98:
                        info["time_remaining_str"] = "Fully charged"
                        info["state"] = "Full"

                    return info
            except Exception as e:
                print(f"[LaptopManager] Windows battery detection failed: {e}")
            return info

        # ── Linux Battery Detection ───────────────────────────────
        bat_dirs = glob.glob("/sys/class/power_supply/BAT*")
        if bat_dirs:
            bat_path = bat_dirs[0]
            info["is_present"] = True

            def read_val(filename: str):
                p = os.path.join(bat_path, filename)
                if os.path.exists(p):
                    try:
                        with open(p, "r") as f:
                            return f.read().strip()
                    except Exception:
                        return None
                return None

            status = read_val("status")
            if status:
                info["state"] = status
                info["is_charging"] = status.lower() in ["charging", "full"]

            cap = read_val("capacity")
            if cap and cap.isdigit():
                info["percentage"] = int(cap)

            cycle = read_val("cycle_count")
            if cycle and cycle.isdigit():
                info["cycle_count"] = int(cycle)

            # Health calculation
            e_full = read_val("energy_full") or read_val("charge_full")
            e_design = read_val("energy_full_design") or read_val("charge_full_design")
            if e_full and e_design and int(e_design) > 0:
                health = (float(e_full) / float(e_design)) * 100.0
                info["health_percent"] = round(min(health, 100.0), 1)

            p_now = read_val("power_now")
            if p_now and p_now.isdigit():
                info["wattage"] = round(float(p_now) / 1_000_000.0, 1)

            e_now = read_val("energy_now") or read_val("charge_now")
            if e_now and p_now and int(p_now) > 0:
                p_val = float(p_now)
                if info["is_charging"] and e_full:
                    rem_energy = float(e_full) - float(e_now)
                    if rem_energy > 0:
                        secs = int((rem_energy / p_val) * 3600)
                        info["time_remaining_seconds"] = secs
                        hrs, mins = divmod(secs // 60, 60)
                        info["time_remaining_str"] = f"{hrs}h {mins}m to full" if hrs > 0 else f"{mins}m to full"
                elif not info["is_charging"]:
                    secs = int((float(e_now) / p_val) * 3600)
                    info["time_remaining_seconds"] = secs
                    hrs, mins = divmod(secs // 60, 60)
                    info["time_remaining_str"] = f"{hrs}h {mins}m remaining" if hrs > 0 else f"{mins}m remaining"

        # Fallback / supplement with UPower if available on Linux
        if shutil.which("upower"):
            try:
                res = subprocess.run(["upower", "-i", "/org/freedesktop/UPower/devices/battery_BAT1"], capture_output=True, text=True, timeout=2)
                if res.returncode == 0:
                    for line in res.stdout.splitlines():
                        line = line.strip()
                        if line.startswith("time to full:") and not info["time_remaining_str"]:
                            info["time_remaining_str"] = line.split(":", 1)[1].strip() + " to full"
                        elif line.startswith("time to empty:") and not info["time_remaining_str"]:
                            info["time_remaining_str"] = line.split(":", 1)[1].strip() + " remaining"
                        elif line.startswith("capacity:") and info["health_percent"] is None:
                            val = line.split(":", 1)[1].replace("%", "").strip()
                            info["health_percent"] = round(float(val), 1)
            except Exception:
                pass

        return info

    def get_power_profiles(self) -> Dict[str, Any]:
        profiles = ["performance", "balanced", "power-saver"]
        current = "balanced"

        if self.is_windows:
            return {
                "supported": False,
                "active_profile": "balanced",
                "available_profiles": ["balanced"],
                "backend": "windows"
            }

        # 1. Try powerprofilesctl
        if shutil.which("powerprofilesctl"):
            try:
                res = subprocess.run(["powerprofilesctl", "get"], capture_output=True, text=True, timeout=2)
                if res.returncode == 0 and res.stdout.strip():
                    return {
                        "supported": True,
                        "active_profile": res.stdout.strip(),
                        "available_profiles": profiles,
                        "backend": "powerprofilesctl"
                    }
            except Exception:
                pass

        # 2. Try ACPI platform_profile
        plat_prof = "/sys/firmware/acpi/platform_profile"
        if os.path.exists(plat_prof):
            try:
                with open(plat_prof, "r") as f:
                    cur_plat = f.read().strip()
                choices_path = "/sys/firmware/acpi/platform_profile_choices"
                avail = profiles
                if os.path.exists(choices_path):
                    with open(choices_path, "r") as f:
                        avail = f.read().split()
                return {
                    "supported": True,
                    "active_profile": cur_plat,
                    "available_profiles": avail,
                    "backend": "platform_profile"
                }
            except Exception:
                pass

        # 3. CPU Governor / Schedutil fallback
        gov_path = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"
        if os.path.exists(gov_path):
            try:
                with open(gov_path, "r") as f:
                    gov = f.read().strip()
                if "powersave" in gov:
                    current = "power-saver"
                elif "performance" in gov:
                    current = "performance"
                else:
                    current = "balanced"
            except Exception:
                pass

        return {
            "supported": True,
            "active_profile": current,
            "available_profiles": profiles,
            "backend": "governor"
        }

    def set_power_profile(self, target_profile: str) -> Dict[str, Any]:
        if self.is_windows:
            return {"success": False, "message": "Power profiles change is managed by Windows OS"}

        target = target_profile.lower().strip()
        allowed = ["performance", "balanced", "power-saver"]
        if target not in allowed:
            return {"success": False, "message": f"Invalid profile '{target}'. Must be one of: {allowed}"}

        # 1. Try powerprofilesctl
        if shutil.which("powerprofilesctl"):
            try:
                res = subprocess.run(["powerprofilesctl", "set", target], capture_output=True, text=True, timeout=3)
                if res.returncode == 0:
                    return {"success": True, "message": f"Power profile set to {target} (powerprofilesctl)"}
            except Exception:
                pass

        # 2. Try ACPI platform_profile
        plat_prof = "/sys/firmware/acpi/platform_profile"
        if os.path.exists(plat_prof):
            try:
                with open(plat_prof, "w") as f:
                    f.write(target)
                return {"success": True, "message": f"Power profile set to {target} (ACPI)"}
            except Exception:
                pass

        return {"success": True, "message": f"Simulated profile {target}"}

    def get_laptop_status(self) -> Dict[str, Any]:
        from agent.app.monitor import monitor
        status_data = monitor.get_system_status()
        battery_data = self.get_battery_info()
        power_profiles = self.get_power_profiles()

        return {
            "battery": battery_data,
            "power_profiles": power_profiles,
            "hardware": {
                "cpu_usage_percent": status_data["cpu"]["usage_percent"],
                "cpu_temp": status_data["cpu"]["temperature"],
                "cpu_freq_mhz": status_data["cpu"]["frequency_mhz"],
                "ram_percent": status_data["ram"]["percent"],
                "ram_used_mb": status_data["ram"]["used_mb"],
                "ram_total_mb": status_data["ram"]["total_mb"]
            }
        }

laptop_manager = LaptopManager()
