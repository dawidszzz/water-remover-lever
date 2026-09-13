#!/usr/bin/env bash
set -e

echo "=============================================="
echo "  Water Remover Lever - automatyczny build"
echo "=============================================="
echo

# --- 1. Sprawdzenie Javy ---
if ! command -v java &> /dev/null; then
    echo "[BŁĄD] Nie znaleziono Javy (polecenie 'java')."
    echo "Zainstaluj JDK 21, np.:"
    echo "  - Linux (apt):   sudo apt install openjdk-21-jdk"
    echo "  - macOS (brew):  brew install openjdk@21"
    echo "  - albo pobierz z https://adoptium.net/temurin/releases/?version=21"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | sed -E 's/.*"([0-9]+)\..*/\1/')
if [ "$JAVA_VERSION" -lt 21 ] 2>/dev/null; then
    echo "[UWAGA] Wykryto Javę w wersji $JAVA_VERSION, a wymagana jest 21+."
    echo "Projekt może się nie zbudować. Zainstaluj JDK 21 (patrz wyżej)."
    echo
fi

echo "[OK] Znaleziono Javę:"
java -version
echo

# --- 2. Wygenerowanie Gradle Wrappera, jeśli brak ---
if [ ! -f "./gradlew" ]; then
    echo "[INFO] Brak Gradle Wrappera - generuję go (wymaga zainstalowanego Gradle)..."
    if ! command -v gradle &> /dev/null; then
        echo "[BŁĄD] Nie znaleziono polecenia 'gradle', a wrapper jeszcze nie istnieje."
        echo "Zainstaluj Gradle jednorazowo, np.:"
        echo "  - macOS (brew):  brew install gradle"
        echo "  - Linux (sdkman): sdk install gradle 8.8"
        echo "  - albo pobierz z https://gradle.org/install/"
        exit 1
    fi
    gradle wrapper --gradle-version 8.8
    echo "[OK] Wrapper wygenerowany."
    echo
fi

chmod +x ./gradlew

# --- 3. Budowanie moda ---
echo "[INFO] Budowanie moda (może potrwać kilka minut przy pierwszym uruchomieniu,"
echo "       bo Gradle pobiera Minecrafta, mapowania Yarn i Fabric API)..."
echo
./gradlew build

# --- 4. Podsumowanie ---
echo
echo "=============================================="
JAR_PATH=$(find build/libs -maxdepth 1 -name "*.jar" ! -name "*-sources.jar" | head -n 1)
if [ -n "$JAR_PATH" ]; then
    echo "[SUKCES] Gotowy plik moda:"
    echo "  $JAR_PATH"
    echo
    echo "Wrzuć go do folderu 'mods/' w katalogu gry (obok Fabric API)."
else
    echo "[BŁĄD] Nie znaleziono pliku .jar w build/libs/ - sprawdź logi powyżej."
    exit 1
fi
echo "=============================================="
