# AGENTS.md — cartasdebuff

## Build local (Termux)

```bash
export JAVA_HOME=$HOME/buildtools/jdk-17.0.20.1+1
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :app:assembleDebug    # debug
VERSION_NAME=0.1 VERSION_CODE=1 ./gradlew :app:assembleRelease   # release (firma debug)
```

Al terminar cualquier build, dejar el APK en la carpeta Download del equipo:

```bash
cp app/build/outputs/apk/debug/app-debug.apk /sdcard/Download/cartasdebuff-debug.apk
cp app/build/outputs/apk/release/app-release.apk /sdcard/Download/cartasdebuff-<version>.apk
```

## Datos

`npm run fetch` descarga las tablas de GameTora a `data/` y las copia a
`app/src/main/assets/data/` (`npm run assets` solo copia).
