@echo off
REM Gradle 데몬이 app/build 파일을 잡고 있으면 clean 이 실패합니다.
REM Android Studio 를 닫은 뒤 이 스크립트를 실행하세요.

cd /d "%~dp0.."

echo [1/3] Stopping Gradle daemons...
call gradlew.bat --stop
if errorlevel 1 (
  echo gradlew.bat --stop failed. Run from project root or fix PATH/Java.
  pause
  exit /b 1
)

echo [2/3] Waiting 2 seconds...
timeout /t 2 /nobreak >nul

echo [3/3] Removing app\build ...
if exist "app\build" (
  rmdir /s /q "app\build"
  if exist "app\build" (
    echo.
    echo FAILED: app\build is still locked.
    echo - Close Android Studio / Cursor terminals using this project
    echo - End java.exe in Task Manager if needed
    echo - Reboot and run this script again
    pause
    exit /b 1
  )
)

echo Done. You can run: gradlew.bat assembleRelease
pause
