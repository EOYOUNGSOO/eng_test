# Gradle 데몬이 app/build 를 잡고 있으면 :clean 이 실패할 수 있습니다.
# Android Studio 를 닫은 뒤 프로젝트 루트에서 실행:  .\scripts\clean-windows.ps1

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

Write-Host "[1/3] Stopping Gradle daemons..."
& "$root\gradlew.bat" --stop
Start-Sleep -Seconds 2

$buildDir = Join-Path $root "app\build"
if (Test-Path -LiteralPath $buildDir) {
    Write-Host "[2/3] Removing app\build ..."
    Remove-Item -LiteralPath $buildDir -Recurse -Force
    Write-Host "Done."
} else {
    Write-Host "app\build not found (already clean)."
}

Write-Host "[3/3] Next: .\gradlew.bat assembleRelease"
