@echo off
setlocal
cd /d "%~dp0"
echo =======================================================
echo   Launching EduTrack: Java Swing Panel System (MySQL)
echo =======================================================

:: Locate Java runtime
if exist "C:\Users\harik\.jdk\jdk-17.0.20.1+1\bin\java.exe" (
    set JAVA="C:\Users\harik\.jdk\jdk-17.0.20.1+1\bin\java.exe"
) else (
    set JAVA=java
)

if not exist bin\com\edutrack\gui\EduTrackGUI.class (
    echo Compiled binaries not found. Running compile first...
    call compile.bat
)

echo Starting EduTrack Swing GUI Panel...
%JAVA% "-Dfile.encoding=UTF-8" -cp "bin;lib/*" com.edutrack.gui.EduTrackGUI data

pause
