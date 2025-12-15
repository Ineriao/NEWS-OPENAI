@echo off
REM Start all services script
cd /d "%~dp0"

echo ========================================
echo    News System - Start All Services
echo ========================================
echo.

REM 1. Start Elasticsearch
echo [1/5] Starting Elasticsearch...
start "Elasticsearch" cmd /c "cd /d D:\elasticsearch-7.17.16 && bin\elasticsearch.bat"
echo Elasticsearch starting on port 9200...
timeout /t 5 /nobreak > nul

REM 2. Start Redis
echo [2/5] Starting Redis...
start "Redis" cmd /c "D:\Redis\redis-server.exe D:\Redis\redis.windows.conf"
echo Redis starting on port 6379...
timeout /t 2 /nobreak > nul

REM 3. Start Spring Boot Backend
echo [3/5] Starting Spring Boot Backend...
cd /d "%~dp0news-backend"
set JAVA_HOME=D:\jdk17
set PATH=%JAVA_HOME%\bin;%PATH%
set UPLOAD_PATH=D:/Learn/WEBdesign/uploads/
start "Spring Boot" cmd /c "D:\maven\apache-maven-3.9.11\bin\mvn spring-boot:run"
echo Backend starting on port 8080...
timeout /t 3 /nobreak > nul

REM 4. Start DailyHotApi
echo [4/5] Starting DailyHotApi...
cd /d "%~dp0DailyHotApi"
start "DailyHotApi" cmd /c "npm run start"
echo DailyHotApi starting on port 6688...
timeout /t 2 /nobreak > nul

REM 5. Start Frontend
echo [5/5] Starting Frontend...
cd /d "%~dp0news-frontend"
start "Frontend" cmd /c "npm run dev"
echo Frontend starting on port 5173...

echo.
echo ========================================
echo    All services are starting...
echo ========================================
echo.
echo Services:
echo   - Elasticsearch: http://localhost:9200
echo   - Redis:         localhost:6379
echo   - Backend:       http://localhost:8080
echo   - DailyHotApi:   http://localhost:6688
echo   - Frontend:      http://localhost:5173
echo.
echo Press any key to exit this window...
pause > nul
