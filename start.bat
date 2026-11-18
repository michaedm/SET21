@echo off
chcp 65001 >nul
cd /d "%~dp0"
title Østfold husflid

echo Starter Østfold husflid med chatbot ...
echo.

docker info >nul 2>&1
if errorlevel 1 (
    echo Docker Desktop kjører ikke. Start Docker Desktop og prøv igjen.
    pause
    exit /b 1
)

if not exist ".env" (
    echo Fant ikke .env i denne mappa. Se KJOR_MEG.txt.
    pause
    exit /b 1
)

echo Første gang lastes og bygges alt. Det kan ta 5-10 minutter.
docker compose up -d --build
if errorlevel 1 (
    echo Noe gikk galt under oppstart. Se meldingene over.
    pause
    exit /b 1
)

echo.
echo Venter på at nettsiden er klar ...
:vent
curl -s -o nul http://localhost:7070 && goto klar
timeout /t 2 /nobreak >nul
goto vent

:klar
echo Klar! Åpner http://localhost:7070
start "" http://localhost:7070
echo.
echo Kjør stopp.bat når du er ferdig.
pause
