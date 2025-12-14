@echo off
REM Stop all services script
cd /d "%~dp0"

echo Stopping services...

REM Stop Spring Boot (port 8080)
echo Checking port 8080 (Spring Boot)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
    echo Found Spring Boot PID: %%a
    taskkill /PID %%a /F
    echo Spring Boot stopped
)

REM Stop DailyHotApi (port 6688)
echo Checking port 6688 (DailyHotApi)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :6688 ^| findstr LISTENING') do (
    echo Found DailyHotApi PID: %%a
    taskkill /PID %%a /F
    echo DailyHotApi stopped
)

echo.
echo All services stopped.
pause
