@echo off
REM Build script - using JDK 17
cd /d "%~dp0"
set JAVA_HOME=D:\jdk17
set PATH=%JAVA_HOME%\bin;%PATH%

echo Using Java: %JAVA_HOME%
java -version

echo.
echo Building project...
call D:\maven\apache-maven-3.9.11\bin\mvn clean package -DskipTests %*
