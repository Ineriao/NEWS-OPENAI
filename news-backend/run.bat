@echo off
REM Run script - Start all services
cd /d "%~dp0"

REM Start DailyHotApi in a new window
echo Starting DailyHotApi...
start "DailyHotApi" cmd /c "cd /d D:\Learn\WEBdesign\DailyHotApi && npm start"

REM Wait a moment for DailyHotApi to start
timeout /t 3 /nobreak >nul

REM Start Spring Boot backend
set JAVA_HOME=D:\jdk17
set PATH=%JAVA_HOME%\bin;%PATH%

REM Set upload path
set UPLOAD_PATH=D:/Learn/WEBdesign/uploads/

echo Using Java: %JAVA_HOME%
java -version

echo.
echo Starting Spring Boot application...
call D:\maven\apache-maven-3.9.11\bin\mvn spring-boot:run %*
