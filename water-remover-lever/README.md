# Water Remover Lever

Mod do Minecraft 1.21.5 (Fabric) — zwykła dźwignia po przełączeniu w stan
`powered = true` usuwa wodę w promieniu 8 bloków wokół siebie (sześcian
17×17×17). Działanie moda można w każdej chwili włączyć/wyłączyć klawiszem
**R** (konfigurowalnym w Opcje → Sterowanie → "Water Remover Lever").

## Wymagania

- JDK 21
- Ten projekt **nie zawiera** Gradle Wrappera (`gradlew`) — pobierz go
  standardowo poleceniem poniżej, albo użyj lokalnie zainstalowanego Gradle
  w wersji zgodnej z Loom 1.10 (Gradle 8.8+).

## Budowanie (najprościej — gotowy skrypt)

W projekcie są dołączone skrypty, które same sprawdzają Javę, w razie
potrzeby generują Gradle Wrapper i budują mod jednym poleceniem:

**Linux / macOS:**
```bash
chmod +x build.sh
./build.sh
```

**Windows:**
```
build.bat
```
(albo dwuklik na plik w Eksploratorze)

Skrypt na końcu wypisze dokładną ścieżkę do gotowego `.jar`.

## Budowanie ręczne (alternatywa)

```bash
# Jeśli nie masz jeszcze wrappera:
gradle wrapper --gradle-version 8.8

# Budowanie moda:
./gradlew build      # Linux/macOS
gradlew.bat build    # Windows
```

Gotowy plik `.jar` pojawi się w `build/libs/water-remover-lever-1.1.0.jar`.
Wersję ze źródłami (`-sources.jar`) też znajdziesz w tym katalogu.

## Wymagania wstępne — jeśli nie masz jeszcze JDK 21

- **Windows/macOS/Linux:** pobierz instalator z
  https://adoptium.net/temurin/releases/?version=21 (Eclipse Temurin,
  darmowy, oficjalny build OpenJDK) i zainstaluj z opcją dodania do PATH.
- **macOS (Homebrew):** `brew install openjdk@21`
- **Linux (apt, Ubuntu/Debian):** `sudo apt install openjdk-21-jdk`
- **Linux (sdkman):** `sdk install java 21-tem`

Po instalacji sprawdź w terminalu: `java -version` — powinno pokazać `21`.
Skrypty `build.sh`/`build.bat` same to zweryfikują i podpowiedzą, czego
brakuje.

## Instalacja

1. Zainstaluj Fabric Loader (https://fabricmc.net/use/) dla Minecraft 1.21.5.
2. Wrzuć zbudowany `.jar` do folderu `mods/` w katalogu gry.
3. Doinstaluj **Fabric API** w tej samej wersji co w `gradle.properties`
   (`fabric_version`) — pobierz z Modrinth/CurseForge, to osobny plik,
   wymagany jako zależność.

## Użycie

1. Postaw dźwignię (`Lever`).
2. Kliknij ją prawym przyciskiem myszy — jeśli była wyłączona, po jej
   przełączeniu w okolicznej wodzie (promień 8 bloków) zniknie cała woda
   (źródła, płynąca oraz "waterlogged" bloki jak schody/płyty).
3. Klawisz **R** włącza/wyłącza tę funkcję globalnie na serwerze — w
   action barze pojawi się komunikat o aktualnym stanie.

## Konfiguracja promienia

Promień skanu ustawiony jest na stałe w kodzie:

```java
// src/main/java/com/example/waterremover/WaterRemoverMod.java
private static final int RADIUS = 8;
```

Zmień tę wartość i przebuduj mod, żeby zwiększyć/zmniejszyć zasięg.
Uwaga: koszt skanu rośnie sześciennie — (2*RADIUS+1)³ sprawdzanych bloków.

## Struktura projektu

```
water-remover-lever/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── build.sh                 (skrypt budujący - Linux/macOS)
├── build.bat                (skrypt budujący - Windows)
├── LICENSE
├── README.md
└── src/
    ├── main/                    (kod wspólny + logika serwerowa)
    │   ├── java/com/example/waterremover/
    │   │   ├── WaterRemoverMod.java
    │   │   └── network/ToggleModPayload.java
    │   └── resources/fabric.mod.json
    └── client/                  (kod tylko kliencki - keybind)
        ├── java/com/example/waterremover/client/
        │   └── WaterRemoverClient.java
        └── resources/assets/waterremover/lang/
            ├── en_us.json
            └── pl_pl.json
```

## Możliwe rozszerzenia (do dogadania)

- Flood fill zamiast sztywnego sześcianu (usuwa całe jezioro/rzekę
  niezależnie od odległości, ale nie rusza wody oddzielonej lądem).
- Rozłożenie usuwania w czasie (np. 200 bloków/tick) — przydatne przy
  bardzo dużych zasięgach, żeby nie zamrozić serwera na chwilę.
- Osobny toggle per gracz zamiast jednego globalnego przełącznika.
- Limit działania tylko w dół (żeby nie osuszać np. oceanu nad dźwignią).
