@echo off
title Stop NexPC Windows Agent
echo ==================================================
echo   🛑 Menghentikan NexPC Agent...
echo ==================================================

set "KILLED=0"
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8765 ^| findstr LISTENING') do (
    taskkill /F /PID %%a >nul 2>nul
    set "KILLED=1"
)

if "%KILLED%"=="1" (
    echo   [OK] NexPC Agent berhasil dimatikan.
    echo   RAM dan CPU laptop Anda sudah bersih kembali.
) else (
    echo   [INFO] NexPC Agent tidak sedang berjalan.
)

echo ==================================================
timeout /t 2 >nul
