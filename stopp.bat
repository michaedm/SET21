@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo Stopper Østfold husflid ...
docker compose down
echo Stoppet.
pause
