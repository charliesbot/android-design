@echo off
setlocal enabledelayedexpansion

set "SCRIPT_PATH=%~f0"
set "BIN_DIR=%~dp0"
for %%I in ("%BIN_DIR%..") do set "PLUGIN_DIR=%%~fI"
for %%I in ("%PLUGIN_DIR%\..") do set "REPO_DIR=%%~fI"

if defined ANDROID_DESIGN_CACHE_DIR (
  set "CACHE_ROOT=%ANDROID_DESIGN_CACHE_DIR%"
) else if defined ANDROID_DESIGN_PREVIEW_CACHE_DIR (
  set "CACHE_ROOT=%ANDROID_DESIGN_PREVIEW_CACHE_DIR%"
) else (
  set "CACHE_ROOT=%USERPROFILE%\.cache\android-design-preview"
)

set "VERSION=0.4.0"
if exist "%PLUGIN_DIR%\plugin.json" (
  for /f "usebackq delims=" %%V in (`powershell -NoProfile -Command "(Get-Content '%PLUGIN_DIR%\plugin.json' -Raw | ConvertFrom-Json).version" 2^>nul`) do (
    if not "%%V"=="" set "VERSION=%%V"
  )
)

call :find_java
if errorlevel 1 exit /b 3

call :resolve_jar
if errorlevel 1 exit /b 3

set "ANDROID_DESIGN_PREVIEW_SCRIPT=%SCRIPT_PATH%"
set "ANDROID_DESIGN_PREVIEW_JAR=%JAR_PATH%"

set "CMD=%~1"
if /I "%CMD%"=="server" goto :run_full
if /I "%CMD%"=="worker" goto :run_full
if /I "%CMD%"=="render" goto :run_full

"%JAVA_BIN%" -Djava.awt.headless=true -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -jar "%JAR_PATH%" %*
exit /b %errorlevel%

:run_full
"%JAVA_BIN%" -Djava.awt.headless=true -jar "%JAR_PATH%" %*
exit /b %errorlevel%

:check_java
set "CANDIDATE=%~1"
if not exist "%CANDIDATE%" exit /b 1
for /f "tokens=3" %%V in ('"%CANDIDATE%" -version 2^>^&1 ^| findstr /i "version"') do (
  set "RAW=%%~V"
  for /f "tokens=1,2 delims=." %%A in ("!RAW!") do (
    if "%%A"=="1" (set "MAJ=%%B") else (set "MAJ=%%A")
  )
  if !MAJ! GEQ 17 (
    set "JAVA_BIN=%CANDIDATE%"
    exit /b 0
  )
)
exit /b 1

:find_java
set "JAVA_BIN="
if defined JAVA_HOME (
  call :check_java "%JAVA_HOME%\bin\java.exe" && exit /b 0
)
for /f "delims=" %%J in ('where java 2^>nul') do (
  call :check_java "%%J" && exit /b 0
)
for %%P in (
  "%LOCALAPPDATA%\Programs\Android Studio\jbr\bin\java.exe"
  "%ProgramFiles%\Android\Android Studio\jbr\bin\java.exe"
  "%ProgramFiles%\Android\Android Studio Preview\jbr\bin\java.exe"
) do (
  call :check_java %%P && exit /b 0
)
echo {"ok":false,"error":"java_not_found","message":"Java 17+ is required. Install JDK 17+ or Android Studio and retry."}
exit /b 3

:resolve_jar
if defined ANDROID_DESIGN_PREVIEW_JAR (
  if not exist "%ANDROID_DESIGN_PREVIEW_JAR%" (
    echo {"ok":false,"error":"jar_not_found","message":"ANDROID_DESIGN_PREVIEW_JAR points to a missing file"}
    exit /b 3
  )
  set "JAR_PATH=%ANDROID_DESIGN_PREVIEW_JAR%"
  exit /b 0
)

set "CACHED_JAR=%CACHE_ROOT%\%VERSION%\android-design-preview.jar"
if exist "%CACHED_JAR%" (
  set "JAR_PATH=%CACHED_JAR%"
  exit /b 0
)

if exist "%REPO_DIR%\app\app\build\libs\android-design-preview.jar" (
  set "JAR_PATH=%REPO_DIR%\app\app\build\libs\android-design-preview.jar"
  exit /b 0
)

if defined ANDROID_DESIGN_PREVIEW_JAR_URL (
  set "JAR_URL=%ANDROID_DESIGN_PREVIEW_JAR_URL%"
) else (
  set "JAR_URL=https://github.com/charliesbot/android-design/releases/download/v%VERSION%/android-design-preview.jar"
)
if defined ANDROID_DESIGN_PREVIEW_SHA_URL (
  set "SHA_URL=%ANDROID_DESIGN_PREVIEW_SHA_URL%"
) else (
  set "SHA_URL=%JAR_URL%.sha256"
)

if not exist "%CACHE_ROOT%\%VERSION%" mkdir "%CACHE_ROOT%\%VERSION%"
set "TMP_JAR=%CACHED_JAR%.tmp.%RANDOM%"
set "TMP_SHA=%CACHED_JAR%.sha.%RANDOM%"
powershell -NoProfile -Command "$ErrorActionPreference='Stop'; Invoke-WebRequest -UseBasicParsing -Uri '%SHA_URL%' -OutFile '%TMP_SHA%'; $shaText=Get-Content -Raw -Encoding UTF8 '%TMP_SHA%'; if ($shaText -match '([A-Fa-f0-9]{64})') { $exp=$Matches[1].ToLower() } else { throw 'Invalid checksum' }; Invoke-WebRequest -UseBasicParsing -Uri '%JAR_URL%' -OutFile '%TMP_JAR%'; $act=(Get-FileHash -Algorithm SHA256 -Path '%TMP_JAR%').Hash.ToLower(); if ($act -ne $exp) { Remove-Item -Force '%TMP_JAR%'; throw 'Checksum mismatch' }"
if errorlevel 1 (
  if exist "%TMP_JAR%" del /f /q "%TMP_JAR%"
  if exist "%TMP_SHA%" del /f /q "%TMP_SHA%"
  echo {"ok":false,"error":"jar_download_failed","message":"Failed downloading or verifying android-design-preview.jar"}
  exit /b 3
)
if exist "%TMP_SHA%" del /f /q "%TMP_SHA%"
move /Y "%TMP_JAR%" "%CACHED_JAR%" >nul
set "JAR_PATH=%CACHED_JAR%"
exit /b 0
