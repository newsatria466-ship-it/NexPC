@echo off
set "DIR=%~dp0"
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "ANDROID_HOME=C:\Users\Dwi\AppData\Local\Android\Sdk"

echo =============================================
echo Building NexPC Android APK on Windows...
echo =============================================

cd /d "%DIR%android"
call gradlew.bat assembleDebug
if %ERRORLEVEL% neq 0 (
    echo Build failed!
    exit /b %ERRORLEVEL%
)

copy /Y "%DIR%android\app\build\outputs\apk\debug\app-debug.apk" "%DIR%Nexora.apk"
copy /Y "%DIR%android\app\build\outputs\apk\debug\app-debug.apk" "%DIR%ControlPC.apk"
copy /Y "%DIR%android\app\build\outputs\apk\debug\app-debug.apk" "%DIR%NexPC.apk"

echo =============================================
echo Build Complete!
echo =============================================
