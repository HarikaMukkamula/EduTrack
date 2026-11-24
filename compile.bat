@echo off
setlocal
cd /d "%~dp0"
echo =======================================================
echo   EduTrack: Compiling Java Course Management System
echo =======================================================

:: Locate Java compiler
if exist "C:\Users\harik\.jdk\jdk-17.0.20.1+1\bin\javac.exe" (
    set JAVAC="C:\Users\harik\.jdk\jdk-17.0.20.1+1\bin\javac.exe"
) else (
    set JAVAC=javac
)

if not exist bin mkdir bin

echo Compiling source files with MySQL JDBC library...
%JAVAC% -encoding UTF-8 -cp "lib/*;bin" -d bin -sourcepath src src\com\edutrack\model\*.java src\com\edutrack\exception\*.java src\com\edutrack\data\*.java src\com\edutrack\service\*.java src\com\edutrack\util\*.java src\com\edutrack\gui\*.java src\com\edutrack\*.java

if %ERRORLEVEL% equ 0 (
    echo [OK] Compilation successful! Output located in bin/
) else (
    echo [ERROR] Compilation failed.
)

pause
