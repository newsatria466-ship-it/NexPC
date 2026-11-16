import os
import shutil
import time
import json
import uuid
from pathlib import Path
from typing import List, Dict, Any, Optional
from fastapi import UploadFile, HTTPException, status
from fastapi.responses import FileResponse

HOME_DIR = Path.home().resolve()
UPLOAD_DIR = HOME_DIR / "Downloads" / "NexPC"
TRANSFERS_LOG = HOME_DIR / ".config" / "nexpc" / "transfers.json"

QUICK_ROOTS = [
    {"name": "Home (~)", "path": str(HOME_DIR)},
    {"name": "Desktop", "path": str(HOME_DIR / "Desktop")},
    {"name": "Documents", "path": str(HOME_DIR / "Documents")},
    {"name": "Downloads", "path": str(HOME_DIR / "Downloads")},
    {"name": "Pictures", "path": str(HOME_DIR / "Pictures")},
    {"name": "Videos", "path": str(HOME_DIR / "Videos")},
    {"name": "NexPC Transfer", "path": str(UPLOAD_DIR)},
]

def format_size(bytes_val: int) -> str:
    if bytes_val < 1024:
        return f"{bytes_val} B"
    elif bytes_val < 1024 * 1024:
        return f"{bytes_val / 1024:.1f} KB"
    elif bytes_val < 1024 * 1024 * 1024:
        return f"{bytes_val / (1024 * 1024):.1f} MB"
    else:
        return f"{bytes_val / (1024 * 1024 * 1024):.2f} GB"

class FileManager:
    def __init__(self):
        UPLOAD_DIR.mkdir(parents=True, exist_ok=True)
        TRANSFERS_LOG.parent.mkdir(parents=True, exist_ok=True)

    def _is_safe_path(self, target_path: Path) -> bool:
        try:
            resolved = target_path.resolve()
            # Allow any directory or file inside the user's home directory (/home/wdwi)
            return resolved == HOME_DIR or HOME_DIR in resolved.parents
        except Exception:
            return False

    def get_history(self) -> List[Dict[str, Any]]:
        if TRANSFERS_LOG.exists():
            try:
                with open(TRANSFERS_LOG, "r") as f:
                    return json.load(f)
            except Exception:
                return []
        return []

    def record_transfer(self, filename: str, size: int, direction: str, status_str: str = "completed"):
        history = self.get_history()
        entry = {
            "id": str(uuid.uuid4())[:8],
            "filename": filename,
            "size_bytes": size,
            "size_formatted": format_size(size),
            "timestamp": time.time(),
            "time_formatted": time.strftime("%d %b %H:%M"),
            "direction": direction, # "upload" or "download"
            "status": status_str
        }
        history.insert(0, entry)
        history = history[:50] # keep last 50
        try:
            with open(TRANSFERS_LOG, "w") as f:
                json.dump(history, f, indent=2)
        except Exception as e:
            print(f"[FileManager] Failed to save transfer history: {e}")

    async def save_uploaded_file(self, upload_file: UploadFile, target_dir_str: Optional[str] = None) -> Dict[str, Any]:
        dest_dir = UPLOAD_DIR
        if target_dir_str and target_dir_str.strip():
            candidate = Path(target_dir_str).resolve()
            if self._is_safe_path(candidate) and candidate.is_dir():
                dest_dir = candidate

        filename = os.path.basename(upload_file.filename or "uploaded_file")
        safe_name = "".join([c for c in filename if c.isalnum() or c in "._- "]).strip()
        if not safe_name:
            safe_name = f"file_{int(time.time())}"

        target_file = dest_dir / safe_name
        if target_file.exists():
            stem = target_file.stem
            suffix = target_file.suffix
            target_file = dest_dir / f"{stem}_{int(time.time())}{suffix}"

        total_bytes = 0
        try:
            with open(target_file, "wb") as f:
                while chunk := await upload_file.read(1024 * 1024):
                    f.write(chunk)
                    total_bytes += len(chunk)

            self.record_transfer(target_file.name, total_bytes, "upload", "completed")
            display_dir = str(dest_dir).replace(str(HOME_DIR), "~")
            return {
                "success": True,
                "message": f"File saved to {display_dir}/{target_file.name}",
                "filename": target_file.name,
                "path": str(target_file),
                "size_bytes": total_bytes,
                "size_formatted": format_size(total_bytes)
            }
        except Exception as e:
            self.record_transfer(safe_name, total_bytes, "upload", "failed")
            if target_file.exists():
                try:
                    target_file.unlink()
                except Exception:
                    pass
            raise HTTPException(status_code=500, detail=f"Upload failed: {str(e)}")

    def browse(self, requested_path: Optional[str] = None) -> Dict[str, Any]:
        if not requested_path or requested_path.strip() == "":
            target_dir = HOME_DIR
        else:
            target_dir = Path(requested_path).resolve()

        if not self._is_safe_path(target_dir):
            target_dir = HOME_DIR

        if not target_dir.exists() or not target_dir.is_dir():
            target_dir = HOME_DIR

        items = []
        try:
            for entry in sorted(os.scandir(target_dir), key=lambda e: (not e.is_dir(), e.name.lower())):
                try:
                    stat = entry.stat()
                    is_dir = entry.is_dir()
                    size = 0 if is_dir else stat.st_size
                    items.append({
                        "name": entry.name,
                        "path": entry.path,
                        "is_dir": is_dir,
                        "size_bytes": size,
                        "size_formatted": "" if is_dir else format_size(size),
                        "modified": int(stat.st_mtime)
                    })
                except (PermissionError, OSError):
                    continue
        except Exception as e:
            print(f"[FileManager] Error reading directory: {e}")

        parent = target_dir.parent
        parent_str = str(parent) if self._is_safe_path(parent) and parent != target_dir else None

        roots_info = [r for r in QUICK_ROOTS if Path(r["path"]).exists()]

        return {
            "current_path": str(target_dir),
            "parent_path": parent_str,
            "allowed_roots": roots_info,
            "items": items
        }

    def delete_item(self, path_str: str) -> Dict[str, Any]:
        target = Path(path_str).resolve()
        if not self._is_safe_path(target):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied")
        if target == HOME_DIR:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Cannot delete root home folder")
        if not target.exists():
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Item not found")

        try:
            if target.is_dir():
                shutil.rmtree(target)
            else:
                target.unlink()
            return {"success": True, "message": f"Successfully deleted {target.name}"}
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"Delete failed: {str(e)}")

    def rename_item(self, path_str: str, new_name: str) -> Dict[str, Any]:
        target = Path(path_str).resolve()
        if not self._is_safe_path(target):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied")
        if target == HOME_DIR:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Cannot rename root home folder")
        if not target.exists():
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Item not found")

        clean_name = os.path.basename(new_name.strip())
        if not clean_name or "/" in clean_name or "\\" in clean_name:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid new name")

        new_target = target.parent / clean_name
        if new_target.exists():
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Destination already exists")

        try:
            target.rename(new_target)
            return {"success": True, "message": f"Renamed to {clean_name}", "new_path": str(new_target)}
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"Rename failed: {str(e)}")

    def move_item(self, source_str: str, target_dir_str: str) -> Dict[str, Any]:
        source = Path(source_str).resolve()
        dest_dir = Path(target_dir_str).resolve()
        if not self._is_safe_path(source) or not self._is_safe_path(dest_dir):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied")
        if source == HOME_DIR:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Cannot move root home folder")
        if not source.exists():
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Source item not found")
        if not dest_dir.exists() or not dest_dir.is_dir():
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Destination must be an existing directory")

        try:
            shutil.move(str(source), str(dest_dir))
            return {"success": True, "message": f"Moved {source.name} to {dest_dir.name}"}
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"Move failed: {str(e)}")

    def create_folder(self, parent_str: str, folder_name: str) -> Dict[str, Any]:
        parent = Path(parent_str).resolve()
        if not self._is_safe_path(parent):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied")
        if not parent.exists() or not parent.is_dir():
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Parent directory does not exist")

        clean_name = os.path.basename(folder_name.strip())
        if not clean_name:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Folder name cannot be empty")

        new_dir = parent / clean_name
        if new_dir.exists():
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Folder already exists")

        try:
            new_dir.mkdir(parents=True, exist_ok=True)
            return {"success": True, "message": f"Folder {clean_name} created", "path": str(new_dir)}
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"Failed to create folder: {str(e)}")

    def get_file_response(self, file_path_str: str) -> FileResponse:
        target_path = Path(file_path_str).resolve()
        if not self._is_safe_path(target_path):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied to requested file")

        if not target_path.exists() or not target_path.is_file():
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="File not found")

        self.record_transfer(target_path.name, target_path.stat().st_size, "download", "completed")
        return FileResponse(
            path=str(target_path),
            filename=target_path.name,
            media_type="application/octet-stream"
        )

file_manager = FileManager()
