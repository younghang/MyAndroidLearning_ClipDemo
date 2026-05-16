@echo off
cd /d "%~dp0"
set ELECTRON_MIRROR=https://npmmirror.com/mirrors/electron/
npm run dist
pause
