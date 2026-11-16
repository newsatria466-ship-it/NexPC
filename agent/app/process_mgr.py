import os
import signal
import psutil
from typing import List, Dict, Any, Optional

class ProcessManager:
    def list_processes(self, limit: int = 50, sort_by: str = "cpu", search: Optional[str] = None) -> List[Dict[str, Any]]:
        procs = []
        for p in psutil.process_iter(['pid', 'name', 'username', 'cpu_percent', 'memory_percent', 'memory_info', 'status', 'create_time']):
            try:
                info = p.info
                name = info['name'] or ""
                if search and search.lower() not in name.lower():
                    continue

                mem_info = info.get('memory_info')
                rss_mb = round(mem_info.rss / (1024**2), 1) if mem_info else 0.0

                procs.append({
                    "pid": info['pid'],
                    "name": name,
                    "username": info.get('username') or "",
                    "cpu_percent": round(info.get('cpu_percent') or 0.0, 1),
                    "memory_percent": round(info.get('memory_percent') or 0.0, 1),
                    "memory_mb": rss_mb,
                    "status": info.get('status') or "unknown"
                })
            except (psutil.NoSuchProcess, psutil.AccessDenied, psutil.ZombieProcess):
                continue

        # Sort
        if sort_by == "cpu":
            procs.sort(key=lambda x: x["cpu_percent"], reverse=True)
        elif sort_by == "memory":
            procs.sort(key=lambda x: x["memory_mb"], reverse=True)
        elif sort_by == "name":
            procs.sort(key=lambda x: x["name"].lower())

        return procs[:limit]

    def get_process_detail(self, pid: int) -> Optional[Dict[str, Any]]:
        try:
            p = psutil.Process(pid)
            with p.oneshot():
                mem_info = p.memory_info()
                return {
                    "pid": p.pid,
                    "name": p.name(),
                    "exe": p.exe() if hasattr(p, "exe") else "",
                    "cmdline": " ".join(p.cmdline()),
                    "username": p.username(),
                    "cpu_percent": round(p.cpu_percent(), 1),
                    "memory_percent": round(p.memory_percent(), 1),
                    "memory_mb": round(mem_info.rss / (1024**2), 1),
                    "status": p.status(),
                    "num_threads": p.num_threads(),
                    "create_time": p.create_time()
                }
        except (psutil.NoSuchProcess, psutil.AccessDenied):
            return None

    def kill_process(self, pid: int, force: bool = False) -> Dict[str, Any]:
        # Guard against killing pid 1 or current agent process
        if pid <= 1 or pid == os.getpid():
            return {"success": False, "message": "Cannot terminate critical system or agent process"}

        try:
            p = psutil.Process(pid)
            if force:
                p.kill()
            else:
                p.terminate()
            return {"success": True, "message": f"Signal sent to process {pid} ({p.name()})"}
        except psutil.NoSuchProcess:
            return {"success": True, "message": f"Process {pid} already exited"}
        except psutil.AccessDenied:
            return {"success": False, "message": f"Access denied terminating process {pid}"}
        except Exception as e:
            return {"success": False, "message": str(e)}

process_manager = ProcessManager()
