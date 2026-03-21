# eng_test

## Windows에서 `clean` / `app\build` 삭제가 안 될 때

Gradle 데몬·Android Studio·백신 등이 `app\build` 안의 KSP/Room 생성 파일을 잠그면  
`Unable to delete directory '...\app\build'` 가 납니다.

1. **Android Studio**를 종료하고, 이 프로젝트를 쓰는 **터미널**도 닫습니다.  
2. 프로젝트 루트에서 **`scripts\clean-windows.bat`** 을 더블클릭하거나 실행합니다.  
   - 또는 PowerShell: `.\scripts\clean-windows.ps1`
3. 그래도 안 되면 **작업 관리자**에서 `java.exe`(Gradle) 종료 후 다시 시도하거나 **PC 재부팅** 후 `scripts\clean-windows.bat` 실행.

`gradle.properties`에 `org.gradle.vfs.watch=false` 를 넣어 두어, 같은 증상 완화에 도움이 되도록 했습니다.
