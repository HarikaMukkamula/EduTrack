@echo off
setlocal
cd /d "%~dp0"
echo =======================================================
echo   Launching EduTrack: Console Menu Application (MySQL)
echo =======================================================

:: Locate Java runtime
if exist "C:\Users\harik\.jdk\jdk-17.0.20.1+1\bin\java.exe" (
    set JAVA="C:\Users\harik\.jdk\jdk-17.0.20.1+1\bin\java.exe"
) else (
    set JAVA=java
)

if not exist bin\com\edutrack\EduTrackApp.class (
    echo Compiled binaries not found. Running compile first...
    call compile.bat
)

%JAVA% "-Dfile.encoding=UTF-8" -cp "bin;lib/*" com.edutrack.EduTrackApp data

pause
