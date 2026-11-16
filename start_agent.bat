@echo off
set "DIR=%~dp0"
cd /d "%DIR%"

echo ==================================================
echo Starting NexPC Agent on Windows...
echo ==================================================

if exist "C:\Users\Dwi\anaconda3\python.exe" (
    set "PY=C:\Users\Dwi\anaconda3\python.exe"
) else (
    set "PY=python"
)

%PY% -m uvicorn agent.app.main:app --host 0.0.0.0 --port 8765
if %ERRORLEVEL% neq 0 (
    pause
)
