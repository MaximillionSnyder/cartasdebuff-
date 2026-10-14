# AGENTS.md — cartasdebuff

## Reglas fijas (preferencias del dueño del repo)

Estas dos no se negocian; el resto del documento solo explica cómo cumplirlas.

1. **La app se prueba SIEMPRE en una pantalla virtual en background.** Nunca se
   abre en la pantalla física del teléfono, nunca se le inyectan toques
   (`input` sin `-d N`) y el móvil se queda como está: el usuario sigue usando
   su pantalla mientras la prueba corre. Lo hace `~/vtest.sh` con scrcpy
   (`--new-display --no-window`), que crea un display privado que no se ve.
   - **No usar la pantalla simulada** (`adb-ui.sh display overlay`, que toca
     `settings put global overlay_display_devices`): en esta ROM puede acabar
     dibujándose encima de la pantalla física (ya pasó una vez) y además
     `screencap -d N` no funciona con ella. Si alguna vez se crea una, se quita
     con `adb-ui.sh display clear` y se comprueba que solo queda el display 0.
   - De la pantalla virtual **no se pueden sacar capturas** (`screencap -d N`
     da Status -2: el display es privado de scrcpy). Los fotogramas se sacan
     del vídeo de la sesión: `<nombre>-final.png` es el estado final y
     `<nombre>-seqNN.png` son muestras. Para encontrar el fotograma bueno entre
     decenas, primero OCR sobre todos (barato, sin gastar contexto en imágenes)
     y luego se mira ese con la vista.
2. **No compilar en local.** Ni gradle ni kotlinc: compila GitHub Actions. El
   ciclo es commit → push a `main` → esperar el run → bajar el APK → instalar.
   Si el run falla, `gh run view <id> --log-failed` (no hace falta reproducirlo
   en local). El `versionName` del APK instalado lleva el SHA, así se sabe
   siempre qué build se está probando.

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
- Para tocar una fila concreta de una lista larga: haz primero una sesión
  **sonda** que solo baje (sin tocar), mide en `<nombre>-final.png` dónde quedó
  la fila que buscas y calcula el toque; repitiendo el mismo gesto, la lista se
  queda donde la mediste. Y ojo: un toque de 0 ms a veces no dispara los
  `clickable` de Compose, así que usa una pulsación de 120 ms
  (`swipe x y x y 120`).
- Las listas muy largas (1921 skills) no se recorren a swipes hasta el final:
  elige un objetivo cercano o un filtro que acorte la lista antes de bajar.
- Una lista solo se desplaza si su contenido es más alto que el hueco: una ficha
  corta no se mueve y no muestra el botón de volver arriba, y eso es correcto.
- Con la inyección de toques, los controles de dentro de las tarjetas de
  Ajustes (segmentados de Tema y Listas) no siempre responden, aunque en el
  móvil funcionen: para ver el tema claro, cambia el modo noche del sistema
  (`adb shell cmd uimode night no` y luego `yes`) y la app lo sigue.
- **El buscador no sirve para las pruebas con toques inyectados**: en cuanto el
  campo «Buscar por nombre» se queda con el foco (basta con que la pantalla lo
  enfoque sola), ni los toques ni `BACK` llegan como se espera —el campo sigue
  enfocado, la lista no se mueve y el BACK se lleva por delante la app en vez
  del teclado, porque el IME no vive en el display virtual—. Para probar una
  ficha concreta, ve por la tarjeta del personaje: pestaña Personajes → tarjeta
  → fila de la skill. Esa ruta sí responde.
- La depuración inalámbrica cambia de puerto al reactivarse: `adb-ui.sh connect`
  lo busca solo, y `prueba-ui.sh` le pasa el puerto conectado a `vtest.sh`.
  MIUI exige además «Instalar vía USB» activado para instalar APKs nuevos.

