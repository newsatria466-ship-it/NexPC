import socket
import json
import threading
import platform
from zeroconf import Zeroconf, ServiceInfo
from agent.app.config import config

def get_os_metadata():
    is_win = platform.system() == "Windows"
    os_family = "windows" if is_win else "linux"
    os_name = f"Windows {platform.release()}" if is_win else "Arch Linux"
    return os_family, os_name

class DiscoveryService:
    def __init__(self):
        self.zeroconf = None
        self.info = None
        self.udp_sock = None
        self.running = False

    def _register(self):
        try:
            self.zeroconf = Zeroconf()
            hostname = socket.gethostname().replace(" ", "-")
            service_type = "_nexpc._tcp.local."
            service_name = f"NexPC-{hostname}.{service_type}"

            # Get local IP
            s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            try:
                s.connect(('10.255.255.255', 1))
                ip_addr = s.getsockname()[0]
            except Exception:
                ip_addr = '127.0.0.1'
            finally:
                s.close()

            ip_bytes = socket.inet_aton(ip_addr)

            os_family, os_name = get_os_metadata()
            properties = {
                "device": config.device_name,
                "version": "1.0.0",
                "os": os_name,
                "os_family": os_family,
                "auth": "1" if config.enable_auth else "0"
            }

            self.info = ServiceInfo(
                type_=service_type,
                name=service_name,
                addresses=[ip_bytes],
                port=config.port,
                properties=properties,
                server=f"{hostname}.local."
            )

            self.zeroconf.register_service(self.info)
            print(f"[Discovery] Zeroconf mDNS service registered: {service_name} at {ip_addr}:{config.port}")
        except Exception as e:
            print(f"[Discovery] mDNS broadcast skipped: {e}")

    def _udp_responder(self):
        try:
            self.udp_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            self.udp_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            try:
                self.udp_sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
            except Exception:
                pass
            self.udp_sock.bind(('0.0.0.0', 8766))
            print("[Discovery] UDP broadcast discovery responder listening on port 8766")

            while self.running:
                try:
                    data, addr = self.udp_sock.recvfrom(1024)
                    msg = data.decode("utf-8", errors="ignore").strip()
                    if "DISCOVERY_PING" in msg or "NEXORA" in msg or "NEXPC" in msg:
                        os_family, os_name = get_os_metadata()
                        response_data = json.dumps({
                            "type": "discovery_pong",
                            "name": config.device_name,
                            "os": os_name,
                            "os_family": os_family,
                            "port": config.port,
                            "auth": 1 if config.enable_auth else 0
                        }).encode("utf-8")
                        self.udp_sock.sendto(response_data, addr)
                except Exception:
                    if not self.running:
                        break
        except Exception as e:
            print(f"[Discovery] UDP responder error: {e}")

    def start(self):
        self.running = True
        t_mdns = threading.Thread(target=self._register, daemon=True)
        t_mdns.start()

        t_udp = threading.Thread(target=self._udp_responder, daemon=True)
        t_udp.start()

    def stop(self):
        self.running = False
        if self.udp_sock:
            try:
                self.udp_sock.close()
            except Exception:
                pass
        if self.zeroconf and self.info:
            try:
                self.zeroconf.unregister_service(self.info)
                self.zeroconf.close()
            except Exception:
                pass

discovery_service = DiscoveryService()

