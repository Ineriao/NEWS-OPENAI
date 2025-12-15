@echo off
REM Stop all services script
cd /d "%~dp0"

echo ========================================
echo    News System - Stop All Services
echo ========================================
echo.

REM Stop Frontend (port 5173)
echo Stopping Frontend (port 5173)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :5173 ^| findstr LISTENING') do (
    taskkill /PID %%a /F > nul 2>&1
    echo   Frontend stopped
)

REM Stop Spring Boot (port 8080)
echo Stopping Spring Boot (port 8080)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
    taskkill /PID %%a /F > nul 2>&1
    echo   Spring Boot stopped
)

REM Stop DailyHotApi (port 6688)
echo Stopping DailyHotApi (port 6688)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :6688 ^| findstr LISTENING') do (
    taskkill /PID %%a /F > nul 2>&1
    echo   DailyHotApi stopped
)

REM Stop Elasticsearch (port 9200)
echo Stopping Elasticsearch (port 9200)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :9200 ^| findstr LISTENING') do (
    taskkill /PID %%a /F > nul 2>&1
    echo   Elasticsearch stopped
)

REM Stop Redis (port 6379)
echo Stopping Redis (port 6379)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :6379 ^| findstr LISTENING') do (
    taskkill /PID %%a /F > nul 2>&1
    echo   Redis stopped
)

echo.
echo ========================================
echo    All services stopped.
echo ========================================
pause
