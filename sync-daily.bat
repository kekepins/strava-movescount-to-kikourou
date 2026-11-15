@echo off
setlocal

set "APP_DIR=%~dp0"
set "LOG_DIR=%LOCALAPPDATA%\KikStrava"
set "LOG_FILE=%LOG_DIR%\sync.log"

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
if errorlevel 1 exit /b 1

pushd "%APP_DIR%"
echo.>> "%LOG_FILE%"
echo ==== %date% %time% ====>> "%LOG_FILE%"
"%APP_DIR%KikStrava.exe" --sync %* >> "%LOG_FILE%" 2>&1
set "SYNC_EXIT_CODE=%ERRORLEVEL%"
popd

exit /b %SYNC_EXIT_CODE%
