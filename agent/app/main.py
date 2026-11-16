import asyncio
from contextlib import asynccontextmanager
from fastapi import FastAPI, Depends, WebSocket, WebSocketDisconnect, Query, HTTPException, File, UploadFile, Form
from fastapi.middleware.cors import CORSMiddleware
from pathlib import Path
from fastapi.responses import FileResponse
from pydantic import BaseModel
from typing import Optional

from agent.app.config import config, get_live_token, is_auth_enabled
from agent.app.monitor import monitor
from agent.app.process_mgr import process_manager
from agent.app.control import system_controller
from agent.app.app_mgr import app_manager
from agent.app.pkg_mgr import package_manager
from agent.app.discovery import discovery_service
from agent.app.security import verify_token
from agent.app.websocket_handler import ws_manager
from agent.app.file_mgr import file_manager
from agent.app.laptop_mgr import laptop_manager
from agent.app.clipboard_mgr import clipboard_manager


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    print(f"==================================================")
    print(f"🚀 NexPC Linux Agent v1.1.0 Started")
    print(f"🔑 Auth Token: {get_live_token()}")
    print(f"🌐 Listening on http://{config.host}:{config.port}")
    print(f"📁 Safe Uploads folder: ~/Downloads/NexPC")
    print(f"==================================================")
    discovery_service.start()
    ws_manager.start_broadcaster()
    yield
    # Shutdown
    discovery_service.stop()
    ws_manager.stop_broadcaster()
    print("🛑 NexPC Linux Agent Stopped.")

app = FastAPI(
    title="NexPC Linux Agent",
    description="Arch Linux Remote Control, Monitoring, File Transfer and Laptop Management API",
    version="1.1.0",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# === MVP & Status Endpoints ===

@app.get("/status")
@app.get("/api/status")
async def get_status():
    """Returns general PC hardware, network, and system status"""
    return monitor.get_system_status()

@app.get("/api/auth/verify")
async def verify_auth(authorized: bool = Depends(verify_token)):
    """Verifies that the client token is valid"""
    return {
        "valid": True,
        "device_name": config.device_name,
        "message": "Authenticated successfully"
    }

# === Process Management Endpoints ===

@app.get("/api/processes")
async def list_processes(
    limit: int = Query(60, ge=1, le=200),
    sort_by: str = Query("cpu", enum=["cpu", "memory", "name"]),
    search: Optional[str] = Query(None),
    authorized: bool = Depends(verify_token)
):
    return process_manager.list_processes(limit=limit, sort_by=sort_by, search=search)

@app.get("/api/processes/{pid}")
async def get_process(pid: int, authorized: bool = Depends(verify_token)):
    proc = process_manager.get_process_detail(pid)
    if not proc:
        raise HTTPException(status_code=404, detail="Process not found or already exited")
    return proc

class KillRequest(BaseModel):
    force: bool = False

@app.post("/api/processes/{pid}/kill")
async def kill_process(pid: int, req: KillRequest = KillRequest(), authorized: bool = Depends(verify_token)):
    return process_manager.kill_process(pid, force=req.force)

# === Desktop App Management Endpoints ===

@app.get("/api/apps")
async def list_apps(authorized: bool = Depends(verify_token)):
    return app_manager.get_installed_apps()

@app.post("/api/apps/{desktop_id}/launch")
async def launch_app(desktop_id: str, authorized: bool = Depends(verify_token)):
    return app_manager.launch_app(desktop_id)

@app.post("/api/apps/{desktop_id}/stop")
async def stop_app(desktop_id: str, authorized: bool = Depends(verify_token)):
    return app_manager.stop_app(desktop_id)

# === PC Control Endpoints ===

@app.post("/api/control/lock")
async def lock_pc(authorized: bool = Depends(verify_token)):
    return system_controller.lock_screen()

@app.post("/api/control/suspend")
async def suspend_pc(authorized: bool = Depends(verify_token)):
    return system_controller.suspend()

@app.post("/api/control/reboot")
async def reboot_pc(authorized: bool = Depends(verify_token)):
    return system_controller.reboot()

@app.post("/api/control/shutdown")
async def shutdown_pc(authorized: bool = Depends(verify_token)):
    return system_controller.shutdown()

class VolumeRequest(BaseModel):
    action: str # "up", "down", "mute"
    value: int = 5

@app.post("/api/control/volume")
async def control_volume(req: VolumeRequest, authorized: bool = Depends(verify_token)):
    return system_controller.set_volume(req.action, req.value)

# === Package Manager Endpoints ===

@app.get("/api/packages/updates")
async def check_updates(authorized: bool = Depends(verify_token)):
    return package_manager.check_updates()

@app.get("/api/packages/search")
async def search_packages(query: str = Query(..., min_length=2), authorized: bool = Depends(verify_token)):
    return package_manager.search_packages(query)

class PackageInstallRequest(BaseModel):
    package_name: str

@app.post("/api/packages/install")
async def install_package(req: PackageInstallRequest, authorized: bool = Depends(verify_token)):
    return package_manager.install_package(req.package_name)

# === File Transfer & Management Endpoints ===

class FileDeleteRequest(BaseModel):
    path: str

class FileRenameRequest(BaseModel):
    path: str
    new_name: str

class FileMoveRequest(BaseModel):
    source_path: str
    target_dir: str

class CreateFolderRequest(BaseModel):
    parent_path: str
    name: str

@app.post("/api/files/upload")
async def upload_file(
    file: UploadFile = File(...),
    target_dir: Optional[str] = Form(None),
    authorized: bool = Depends(verify_token)
):
    return await file_manager.save_uploaded_file(file, target_dir)

@app.get("/api/files/browse")
async def browse_files(
    path: Optional[str] = Query(None),
    authorized: bool = Depends(verify_token)
):
    return file_manager.browse(path)

@app.get("/api/files/download")
async def download_file(
    path: str = Query(...),
    authorized: bool = Depends(verify_token)
):
    return file_manager.get_file_response(path)

@app.post("/api/files/delete")
async def delete_file_item(
    req: FileDeleteRequest,
    authorized: bool = Depends(verify_token)
):
    return file_manager.delete_item(req.path)

@app.post("/api/files/rename")
async def rename_file_item(
    req: FileRenameRequest,
    authorized: bool = Depends(verify_token)
):
    return file_manager.rename_item(req.path, req.new_name)

@app.post("/api/files/move")
async def move_file_item(
    req: FileMoveRequest,
    authorized: bool = Depends(verify_token)
):
    return file_manager.move_item(req.source_path, req.target_dir)

@app.post("/api/files/mkdir")
async def create_folder_item(
    req: CreateFolderRequest,
    authorized: bool = Depends(verify_token)
):
    return file_manager.create_folder(req.parent_path, req.name)

@app.get("/api/files/history")
async def get_transfer_history(authorized: bool = Depends(verify_token)):
    return file_manager.get_history()

# === Laptop Mode Endpoints ===

@app.get("/api/laptop/status")
async def get_laptop_status(authorized: bool = Depends(verify_token)):
    return laptop_manager.get_laptop_status()

class PowerProfileRequest(BaseModel):
    profile: str

@app.post("/api/laptop/power-profile")
async def set_power_profile(
    req: PowerProfileRequest,
    authorized: bool = Depends(verify_token)
):
    return laptop_manager.set_power_profile(req.profile)

# === Clipboard Endpoints ===

class ClipboardSetRequest(BaseModel):
    text: str

@app.get("/api/clipboard")
async def get_clipboard(authorized: bool = Depends(verify_token)):
    text = clipboard_manager.get_text()
    return {
        "text": text,
        "status": "ok",
        "length": len(text)
    }

@app.post("/api/clipboard")
async def set_clipboard(
    req: ClipboardSetRequest,
    authorized: bool = Depends(verify_token)
):
    success = clipboard_manager.set_text(req.text)
    return {
        "success": success,
        "status": "ok" if success else "error",
        "message": "Copied to PC clipboard" if success else "Failed to copy to PC clipboard",
        "length": len(req.text)
    }


# === App OTA Auto-Update Endpoints ===

@app.get("/api/app/version")
async def get_app_version():
    root_dir = Path(__file__).resolve().parent.parent.parent
    apk_path = root_dir / "Nexora.apk"
    if not apk_path.exists():
        apk_path = root_dir / "ControlPC.apk"
    if not apk_path.exists():
        apk_path = root_dir / "NexPC.apk"
    apk_exists = apk_path.exists()
    apk_size = apk_path.stat().st_size if apk_exists else 0
    return {
        "version_code": 19,
        "version_name": "2.6.0",
        "changelog": "Dual-OS & Neo-Brutalism Update 🚀 Auto-detect Windows vs Arch Linux! Tema baru Neo-Brutalism khusus Windows (High-Contrast, Tactile Dock & Hard Shadows) dengan fitur File Sharing, 2-Way Clipboard Sync, Laptop Battery & OTA Update.",
        "apk_available": apk_exists,
        "apk_size": apk_size
    }



@app.get("/api/app/download")
async def download_latest_apk():
    root_dir = Path(__file__).resolve().parent.parent.parent
    apk_path = root_dir / "Nexora.apk"
    if not apk_path.exists():
        apk_path = root_dir / "ControlPC.apk"
    if not apk_path.exists():
        apk_path = root_dir / "NexPC.apk"
    if not apk_path.exists():
        raise HTTPException(status_code=404, detail="Nexora.apk not found on PC")
    return FileResponse(
        path=str(apk_path),
        filename="Nexora.apk",
        media_type="application/vnd.android.package-archive"
    )

# === WebSocket Realtime Stream ===

@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket, token: Optional[str] = Query(None)):
    clean_token = (token or "").strip().upper()
    clean_expected = (get_live_token() or "").strip().upper()
    if is_auth_enabled() and clean_token != clean_expected:
        await websocket.close(code=4001, reason="Unauthorized token")
        return

    await ws_manager.connect(websocket)
    try:
        while True:
            data = await websocket.receive_text()
            if data == "ping":
                await websocket.send_text('{"type":"pong"}')
    except WebSocketDisconnect:
        ws_manager.disconnect(websocket)
    except Exception:
        ws_manager.disconnect(websocket)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("agent.app.main:app", host=config.host, port=config.port, reload=False)
