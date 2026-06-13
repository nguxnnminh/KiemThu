@echo off
echo Building Smart Dental application...
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo Build failed.
    exit /b %ERRORLEVEL%
)
echo Starting application...
java -jar target\smart-dental-0.0.1-SNAPSHOT.jar
