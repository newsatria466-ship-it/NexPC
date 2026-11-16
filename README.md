# 🖥️ NexPC — Arch Linux Remote Control & Monitoring Center

**NexPC** adalah aplikasi Android dan service Linux untuk **remote monitoring, manajemen proses, kontrol daya, aplikasi desktop, dan paket Arch Linux melalui jaringan lokal (Wi-Fi/LAN)**.

Aplikasi Android didistribusikan dalam format **APK standalone** yang dapat langsung di-install di HP tanpa Google Play Store.

---

## 🌟 Fitur Utama (Phase 1 – Phase 10)

1. **Hardware & System Monitoring (Phase 1–3)**:
   * Realtime telemetry via **WebSocket** (1 detik refresh rate).
   * Visual gauge & live Sparkline trends: CPU (overall & per-core), RAM, Swap, Disk mounts, Suhu (k10temp/coretemp/nvme/amdgpu), Uptime, Baterai.
   * Indikator kecepatan bandwidth Network (Download & Upload KB/s).
2. **Process Manager (Phase 4)**:
   * Daftar proses aktif dengan sorting (CPU, RAM, Name).
   * Pencarian proses real-time.
   * Fitur **Kill Process** (SIGTERM) dan **Force Kill** (SIGKILL) dengan konfirmasi keamanan.
3. **PC Power & Audio Control (Phase 5)**:
   * **Lock Screen** (`loginctl lock-session` / KDE screenlocker).
   * **Suspend**, **Restart**, dan **Shutdown** dengan dialog konfirmasi.
   * Quick volume control (Mute, Volume Down, Volume Up via `wpctl`/`pactl`).
4. **Desktop Application Manager (Phase 6)**:
   * Deteksi otomatis file `.desktop` aplikasi terinstall (Firefox, VS Code, Discord, Spotify, Steam, dll.).
   * Indikator status aplikasi aktif/berjalan (Running indicator).
   * One-tap **Launch** dan **Stop** aplikasi.
5. **Arch Linux Package Manager (Phase 7)**:
   * Cek pembaruan paket pending (`checkupdates` / `pacman -Qu`).
   * Pencarian paket pacman (`pacman -Ss`).
6. **Auto-Discovery LAN (Phase 8)**:
   * mDNS Zeroconf service discovery (`_nexpc._tcp.local.`), otomatis menemukan PC di jaringan Wi-Fi tanpa perlu mengetik IP manual.
7. **Security & Authentication (Phase 9)**:
   * PIN / Token authentication (`X-NexPC-Token`).
   * LAN-only access mode.
8. **APK Distribution (Phase 10)**:
   * File `.apk` mandiri yang siap dikirim via USB, Bluetooth, Telegram, WhatsApp, atau Cloud Storage.

---

## 🚀 Cara Menjalankan Linux Agent

### 1. Jalankan Langsung (Testing / Development)
```bash
./start_agent.sh
```
Agent akan berjalan di `http://0.0.0.0:8765`. 

PIN / Token autentikasi otomatis digenerate dan tersimpan di `~/.config/nexpc/config.json`. Anda juga dapat melihat token di output terminal saat agent pertama kali dijalankan.

### 2. Jalankan Otomatis sebagai Background Service (systemd)
```bash
cd agent
./install_service.sh
```
Cek status service:
```bash
systemctl --user status nexpc.service
```
Melihat logs:
```bash
journalctl --user -u nexpc.service -f
```

---

## 📱 Cara Build & Install Aplikasi Android

### 1. Build APK
Jalankan script:
```bash
./build_apk.sh
```
Output file APK akan berada di:
```text
android/app/build/outputs/apk/debug/app-debug.apk
```

### 2. Pasang ke HP Android
Kirim file `app-debug.apk` ke HP Anda melalui salah satu cara berikut:
* **Kabel USB / ADB**:
  ```bash
  adb install -r android/app/build/outputs/apk/debug/app-debug.apk
  ```
* **Kirim via WhatsApp / Telegram / Google Drive / Bluetooth** lalu buka file `.apk` di HP untuk melakukan instalasi (izinkan *Install unknown apps*).

---

## 🔌 Dokumentasi REST API & WebSocket

| Endpoint | Method | Keterangan | Header Auth |
| :--- | :--- | :--- | :--- |
| `/status` | `GET` | Telemetry dasar (MVP) | Opsional |
| `/api/status` | `GET` | Telemetry lengkap (CPU, RAM, Disk, Temp, Net, Battery) | Opsional |
| `/api/auth/verify` | `GET` | Verifikasi token autentikasi | `X-NexPC-Token` |
| `/api/processes` | `GET` | List proses (`?limit=50&sort_by=cpu&search=name`) | `X-NexPC-Token` |
| `/api/processes/{pid}/kill` | `POST` | Hentikan proses (`{"force": false}`) | `X-NexPC-Token` |
| `/api/apps` | `GET` | List desktop applications & status | `X-NexPC-Token` |
| `/api/apps/{id}/launch` | `POST` | Jalankan aplikasi desktop | `X-NexPC-Token` |
| `/api/apps/{id}/stop` | `POST` | Hentikan aplikasi desktop | `X-NexPC-Token` |
| `/api/control/lock` | `POST` | Kunci layar desktop | `X-NexPC-Token` |
| `/api/control/suspend` | `POST` | Masuk ke mode Sleep / Suspend | `X-NexPC-Token` |
| `/api/control/reboot` | `POST` | Restart sistem | `X-NexPC-Token` |
| `/api/control/shutdown` | `POST` | Matikan sistem | `X-NexPC-Token` |
| `/api/control/volume` | `POST` | Atur volume (`{"action": "up"/"down"/"mute"}`) | `X-NexPC-Token` |
| `/api/packages/updates` | `GET` | Cek pembaruan paket sistem | `X-NexPC-Token` |
| `/api/packages/search` | `GET` | Cari paket pacman (`?query=pkgname`) | `X-NexPC-Token` |
| `/ws` | `WebSocket` | Realtime streaming telemetry (1 frame/detik) | `?token=TOKEN` |
