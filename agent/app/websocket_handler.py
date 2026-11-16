import asyncio
import json
from typing import Set
from fastapi import WebSocket, WebSocketDisconnect
from agent.app.monitor import monitor
from agent.app.config import config

class WebSocketManager:
    def __init__(self):
        self.active_connections: Set[WebSocket] = set()
        self.broadcast_task: asyncio.Task = None

    async def connect(self, websocket: WebSocket):
        await websocket.accept()
        self.active_connections.add(websocket)
        print(f"[WebSocket] Client connected. Total active: {len(self.active_connections)}")

    def disconnect(self, websocket: WebSocket):
        if websocket in self.active_connections:
            self.active_connections.remove(websocket)
            print(f"[WebSocket] Client disconnected. Total active: {len(self.active_connections)}")

    async def broadcast_loop(self):
        while True:
            try:
                if self.active_connections:
                    status_data = monitor.get_system_status()
                    msg = json.dumps({"type": "telemetry", "data": status_data})
                    to_remove = set()
                    for connection in list(self.active_connections):
                        try:
                            await connection.send_text(msg)
                        except Exception:
                            to_remove.add(connection)
                    for dead_conn in to_remove:
                        self.disconnect(dead_conn)
                await asyncio.sleep(1.0)
            except asyncio.CancelledError:
                break
            except Exception as e:
                print(f"[WebSocket] Broadcast error: {e}")
                await asyncio.sleep(1.0)

    def start_broadcaster(self):
        if self.broadcast_task is None or self.broadcast_task.done():
            self.broadcast_task = asyncio.create_task(self.broadcast_loop())

    def stop_broadcaster(self):
        if self.broadcast_task and not self.broadcast_task.done():
            self.broadcast_task.cancel()

ws_manager = WebSocketManager()
