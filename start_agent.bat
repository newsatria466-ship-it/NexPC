@echo off
title NexPC Windows Agent
set "DIR=%~dp0"
cd /d "%DIR%"

echo ==================================================
echo   🚀 NexPC Windows Agent v2.6.0
echo ==================================================
echo   Status           : AKTIF (Menghubungkan Laptop ke HP)
echo   Port             : 8765
echo   Untuk Mematikan  : Cukup tutup jendela ini [X] atau Ctrl+C
echo ==================================================
echo.

if exist "C:\Users\Dwi\anaconda3\python.exe" (
    set "PY=C:\Users\Dwi\anaconda3\python.exe"
) else (
    set "PY=python"
)

%PY% -m uvicorn agent.app.main:app --host 0.0.0.0 --port 8765
if %ERRORLEVEL% neq 0 (
    echo.
    echo Ada kendala saat menjalankan agent. Tekan sembarang tombol untuk keluar.
    pause >nul
)
