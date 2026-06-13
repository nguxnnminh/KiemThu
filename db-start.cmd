@echo off
echo Preparing Smart Dental database for XAMPP MySQL...

set MYSQL_EXE=C:\xampp\mysql\bin\mysql.exe
if not exist "%MYSQL_EXE%" (
    echo Cannot find %MYSQL_EXE%.
    echo Please start MySQL from XAMPP Control Panel, then create database Clinic manually if needed.
    exit /b 1
)

"%MYSQL_EXE%" -u root -e "CREATE DATABASE IF NOT EXISTS Clinic CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if %ERRORLEVEL% NEQ 0 (
    echo Could not connect to XAMPP MySQL as root without password.
    echo Please start MySQL in XAMPP and check the root password.
    exit /b %ERRORLEVEL%
)

echo Done. XAMPP MySQL is available at localhost:3306 (db: Clinic, user: root, pass: empty)
