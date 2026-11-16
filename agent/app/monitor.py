import os
import time
import socket
import platform
import psutil
from typing import Dict, Any, List, Optional

class SystemMonitor:
    def __init__(self):
        self.last_net_time = time.time()
        self.last_net_io = psutil.net_io_counters()
        self.boot_time = psutil.boot_time()

    def get_temperatures(self) -> Dict[str, float]:
        temps = {}
        try:
            raw_temps = psutil.sensors_temperatures()
            if raw_temps:
                for name, entries in raw_temps.items():
                    for entry in entries:
                        label = entry.label or name
                        if entry.current is not None and entry.current > 0:
                            temps[f"{name} ({label})"] = round(entry.current, 1)
        except Exception:
            pass

        # Fallback to /sys/class/thermal if psutil is empty
        if not temps:
            try:
                thermal_dir = "/sys/class/thermal"
                if os.path.exists(thermal_dir):
                    for zone in os.listdir(thermal_dir):
                        if zone.startswith("thermal_zone"):
                            temp_file = os.path.join(thermal_dir, zone, "temp")
                            type_file = os.path.join(thermal_dir, zone, "type")
                            if os.path.exists(temp_file):
                                with open(temp_file, "r") as f:
                                    t = float(f.read().strip()) / 1000.0
                                z_type = zone
                                if os.path.exists(type_file):
                                    with open(type_file, "r") as f:
                                        z_type = f.read().strip()
                                temps[z_type] = round(t, 1)
            except Exception:
                pass

        return temps

    def get_primary_temperature(self, temps: Dict[str, float]) -> float:
        # Search for cpu/tctl/package/coretemp
        for key, val in temps.items():
            key_lower = key.lower()
            if any(term in key_lower for term in ["tctl", "tdie", "package", "cpu", "core", "edge"]):
                return val
        if temps:
            return next(iter(temps.values()))
        return 0.0

    def get_network_speed(self) -> Dict[str, Any]:
        curr_time = time.time()
        curr_io = psutil.net_io_counters()
        time_diff = max(curr_time - self.last_net_time, 0.1)

        bytes_recv = curr_io.bytes_recv - self.last_net_io.bytes_recv
        bytes_sent = curr_io.bytes_sent - self.last_net_io.bytes_sent

        self.last_net_time = curr_time
        self.last_net_io = curr_io

        download_speed_kbps = round((bytes_recv / 1024.0) / time_diff, 1)
        upload_speed_kbps = round((bytes_sent / 1024.0) / time_diff, 1)

        # Get local IP addresses
        ip_addresses = []
        try:
            for iface, addrs in psutil.net_if_addrs().items():
                for addr in addrs:
                    if addr.family == socket.AF_INET and not addr.address.startswith("127."):
                        ip_addresses.append({"interface": iface, "ip": addr.address})
        except Exception:
            pass

        return {
            "download_kbps": download_speed_kbps,
            "upload_kbps": upload_speed_kbps,
            "bytes_recv_total": curr_io.bytes_recv,
            "bytes_sent_total": curr_io.bytes_sent,
            "interfaces": ip_addresses
        }

    def get_disks_info(self) -> List[Dict[str, Any]]:
        disks = []
        try:
            partitions = psutil.disk_partitions(all=False)
            seen_mounts = set()
            for part in partitions:
                if part.mountpoint in seen_mounts or part.fstype in ["squashfs", "tmpfs", "devtmpfs"]:
                    continue
                seen_mounts.add(part.mountpoint)
                try:
                    usage = psutil.disk_usage(part.mountpoint)
                    disks.append({
                        "device": part.device,
                        "mountpoint": part.mountpoint,
                        "fstype": part.fstype,
                        "total_gb": round(usage.total / (1024**3), 1),
                        "used_gb": round(usage.used / (1024**3), 1),
                        "free_gb": round(usage.free / (1024**3), 1),
                        "percent": usage.percent
                    })
                except (PermissionError, OSError):
                    continue
        except Exception:
            pass
        return disks

    def get_battery_info(self) -> Optional[Dict[str, Any]]:
        try:
            battery = psutil.sensors_battery()
            if battery:
                return {
                    "percent": round(battery.percent, 1),
                    "power_plugged": battery.power_plugged,
                    "secsleft": battery.secsleft if battery.secsleft != psutil.POWER_TIME_UNLIMITED else -1
                }
        except Exception:
            pass
        return None

    def get_system_status(self) -> Dict[str, Any]:
        # CPU
        cpu_percent = psutil.cpu_percent(interval=None)
        cpu_cores_percent = psutil.cpu_percent(interval=None, percpu=True)
        cpu_freq = psutil.cpu_freq()
        cpu_freq_current = round(cpu_freq.current, 0) if cpu_freq else 0

        # Memory
        mem = psutil.virtual_memory()
        swap = psutil.swap_memory()

        # Temps
        temps = self.get_temperatures()
        cpu_temp = self.get_primary_temperature(temps)

        # Network
        net = self.get_network_speed()

        # Disks
        disks = self.get_disks_info()
        primary_disk_percent = disks[0]["percent"] if disks else 0.0

        # Uptime
        uptime_seconds = int(time.time() - self.boot_time)
        hours, remainder = divmod(uptime_seconds, 3600)
        minutes, seconds = divmod(remainder, 60)
        uptime_str = f"{hours}h {minutes}m"

        # OS Detection
        is_win = platform.system() == "Windows"
        os_family = "windows" if is_win else "linux"
        os_label = f"Windows {platform.release()}" if is_win else f"Arch Linux ({platform.release()})"
        session_type = "Windows Desktop" if is_win else os.environ.get("XDG_SESSION_TYPE", "unknown")
        desktop_env = "Explorer / DWM" if is_win else os.environ.get("XDG_CURRENT_DESKTOP", "unknown")

        return {
            "status": "online",
            "timestamp": time.time(),
            "hostname": socket.gethostname(),
            "os_family": os_family,
            "os": os_label,
            "uptime_seconds": uptime_seconds,
            "uptime_formatted": uptime_str,
            "session_type": session_type,
            "desktop_env": desktop_env,
            "cpu": {
                "usage_percent": cpu_percent,
                "cores_usage": cpu_cores_percent,
                "cores_count": psutil.cpu_count(logical=True),
                "frequency_mhz": cpu_freq_current,
                "temperature": cpu_temp,
                "all_temperatures": temps
            },
            "ram": {
                "total_mb": round(mem.total / (1024**2), 0),
                "used_mb": round(mem.used / (1024**2), 0),
                "free_mb": round(mem.available / (1024**2), 0),
                "percent": mem.percent
            },
            "swap": {
                "total_mb": round(swap.total / (1024**2), 0),
                "used_mb": round(swap.used / (1024**2), 0),
                "percent": swap.percent
            },
            "disk": {
                "primary_percent": primary_disk_percent,
                "partitions": disks
            },
            "network": net,
            "battery": self.get_battery_info()
        }

monitor = SystemMonitor()
