@echo off
setlocal enabledelayedexpansion

echo ==============================================
echo   Water Remover Lever - automatyczny build
echo ==============================================
echo.

REM --- 1. Sprawdzenie Javy ---
where java >nul 2>nul
if errorlevel 1 (
    echo [BLAD] Nie znaleziono Javy w PATH.
    echo Zainstaluj JDK 21, np. z https://adoptium.net/temurin/releases/?version=21
    echo Podczas instalacji zaznacz opcje "Add to PATH" / "Set JAVA_HOME".
    pause
    exit /b 1
)

echo [OK] Znaleziono Jave:
java -version
echo.

REM --- 2. Wygenerowanie Gradle Wrappera, jesli brak ---
if not exist "gradlew.bat" (
    echo [INFO] Brak Gradle Wrappera - generuje go ^(wymaga zainstalowanego Gradle^)...
    where gradle >nul 2>nul
    if errorlevel 1 (
        echo [BLAD] Nie znaleziono polecenia 'gradle', a wrapper jeszcze nie istnieje.
        echo Zainstaluj Gradle jednorazowo z https://gradle.org/install/
        echo albo przez Scoop:  scoop install gradle
        pause
        exit /b 1
    )
    call gradle wrapper --gradle-version 8.8
    echo [OK] Wrapper wygenerowany.
    echo.
)

REM --- 3. Budowanie moda ---
echo [INFO] Budowanie moda ^(moze potrwac kilka minut przy pierwszym uruchomieniu,
echo        bo Gradle pobiera Minecrafta, mapowania Yarn i Fabric API^)...
echo.
call gradlew.bat build
if errorlevel 1 (
    echo.
    echo [BLAD] Budowanie nie powiodlo sie - sprawdz logi powyzej.
    pause
    exit /b 1
)

REM --- 4. Podsumowanie ---
echo.
echo ==============================================
set "JARFILE="
for %%f in (build\libs\*.jar) do (
    echo %%~nf | findstr /v /c:"-sources" >nul
    if not errorlevel 1 (
        set "JARFILE=%%f"
    )
)

if defined JARFILE (
    echo [SUKCES] Gotowy plik moda:
    echo   !JARFILE!
    echo.
    echo Wrzuc go do folderu "mods\" w katalogu gry ^(obok Fabric API^).
) else (
    echo [BLAD] Nie znaleziono pliku .jar w build\libs\ - sprawdz logi powyzej.
)
echo ==============================================
pause
