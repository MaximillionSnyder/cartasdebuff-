# AGENTS.md — cartasdebuff

## Build y releases (GitHub Actions)

No compilar localmente: lo hace `.github/workflows/android.yml`.

- Push a `main` / PR: corre los tests unitarios y compila los APKs (quedan como
  artefactos del run).
- Tag `v*`: además publica la release con `app-release.apk` adjunto.

```bash
git tag -a v0.5 -m "Versión 0.5" && git push origin v0.5
```

## Datos

`npm run fetch` descarga las tablas de GameTora a `data/` y las copia a
`app/src/main/assets/data/` (`npm run assets` solo copia).

## Ciclo para cambiar la interfaz (pantalla virtual)

Cada cambio visual se compila en CI, se instala y se prueba **sin tocar la
pantalla física del teléfono**:

```bash
git commit -am "feat(ui): …" && git push origin main
scripts/prueba-ui.sh bajar          # espera el run verde y baja el APK
scripts/prueba-ui.sh instalar       # adb install -r (actualiza en sitio)
scripts/prueba-ui.sh probar NOMBRE ~/vtest/acc.txt   # sesión grabada
```

- `~/vtest.sh` crea la pantalla virtual con scrcpy y graba `<NOMBRE>.mp4`.
- El fichero de acciones son líneas `<segundo> tap|swipe|text|key …`.
- `prueba-ui.sh probar` copia vídeo y fotogramas a `/sdcard/Movies/dsh` y avisa
  a MediaStore, así que se ven en la galería del teléfono.
- Las releases se firman con un keystore fijo guardado en los secretos del repo
  (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`): así
  `adb install -r` **actualiza** la app conservando sus ajustes. Si algún día
  falta el secreto, CI cae al keystore de depuración y toca desinstalar.
- **Si el teléfono entra en reposo, la pantalla virtual se apaga y los toques
  inyectados no llegan a la app**: la sesión se graba igual y parece que la
  interfaz está rota. `prueba-ui.sh` ya despierta el móvil y lo mantiene
  despierto durante la grabación; si una prueba sale "sin reacción", mira
  primero `dumpsys power | grep mWakefulness`.
- El input sobre una pantalla virtual (`input -d N`) a veces tarda: deja varios
  segundos entre acciones y mira `-timeline.txt` para saber cuándo ocurrió cada
  una de verdad.
- Una lista solo se desplaza si su contenido es más alto que el hueco: una ficha
  corta no se mueve y no muestra el botón de volver arriba, y eso es correcto.
- La depuración inalámbrica cambia de puerto al reactivarse: `adb-ui.sh connect`
  lo busca solo, y `prueba-ui.sh` le pasa el puerto conectado a `vtest.sh`.
  MIUI exige además «Instalar vía USB» activado para instalar APKs nuevos.

