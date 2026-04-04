@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion
cd /d "%~dp0"

echo.
echo === EngTest: Gradle/빌드 캐시 초기화 ===
echo.
echo 1) Android Studio / Cursor에서 이 프로젝트를 닫고, Gradle Sync가 끝날 때까지 기다리세요.
echo 2) "연속 빌드(Continuous build)"를 쓰는 IDE 설정이 있으면 끄세요.
echo 3) OneDrive/백신이 E:\gitSrc\eng_test 를 실시간 검사하면 잠금이 남을 수 있습니다.
echo.
pause

echo Gradle 데몬 중지...
call gradlew.bat --stop 2>nul
echo 5초 대기(파일 핸들 해제)...
timeout /t 5 /nobreak >nul

echo.
echo 삭제 시도(최대 3회): app\build ...
set TRIES=0
:DEL_APP_BUILD
if not exist "app\build" goto DEL_DOT_GRADLE
rd /s /q "app\build" 2>nul
if exist "app\build" (
  set /a TRIES+=1
  if !TRIES! lss 3 (
    echo   재시도 !TRIES!/3 ...
    timeout /t 3 /nobreak >nul
    goto DEL_APP_BUILD
  )
  echo [실패] app\build — IDE/백신이 잠갔을 수 있습니다. PC 재시작 후 다시 실행하세요.
) else (
  echo [완료] app\build 삭제됨.
)

:DEL_DOT_GRADLE
echo.
echo 삭제 시도(최대 3회): .gradle ...
set TRIES=0
:DEL_DOT_GRADLE_LOOP
if not exist ".gradle" goto DONE
rd /s /q ".gradle" 2>nul
if exist ".gradle" (
  set /a TRIES+=1
  if !TRIES! lss 3 (
    echo   재시도 !TRIES!/3 ...
    timeout /t 3 /nobreak >nul
    goto DEL_DOT_GRADLE_LOOP
  )
  echo [실패] .gradle — 위와 동일. 관리자 권한 CMD에서 이 bat을 다시 실행해 보세요.
) else (
  echo [완료] .gradle 삭제됨.
)

:DONE
echo.
echo 다음: 새 CMD에서  gradlew.bat assembleDebug  (IDE는 열기 전에 한 번 성공하는지 확인)
echo.
pause
