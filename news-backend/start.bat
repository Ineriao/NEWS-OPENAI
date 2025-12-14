@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo =============================================
echo   新闻发布系统 - 后端启动脚本
echo =============================================
echo.

REM 设置 Java 17 (spring-dotenv 需要 Java 17)
set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.17.10-hotspot"
set "PATH=%JAVA_HOME%\bin;D:\maven\apache-maven-3.9.11\bin;%PATH%"

echo 使用 Java 版本:
java -version 2>&1 | findstr "version"
echo.

REM 检查 .env 文件是否存在
if not exist ".env" (
    echo [错误] .env 文件不存在！
    echo 请复制 .env.example 为 .env 并填入实际配置值
    echo.
    echo   copy .env.example .env
    echo.
    pause
    exit /b 1
)

echo [OK] .env 文件已找到，spring-dotenv 将自动加载
echo.
echo 正在启动 Spring Boot 应用...
echo =============================================
echo.

mvn spring-boot:run

pause
