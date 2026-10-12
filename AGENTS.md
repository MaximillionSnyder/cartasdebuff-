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
scripts/prueba-ui.sh instalar       # adb install (desinstala si cambia la firma)
scripts/prueba-ui.sh probar NOMBRE ~/vtest/acc.txt   # sesión grabada
```

- `~/vtest.sh` crea la pantalla virtual con scrcpy y graba `<NOMBRE>.mp4`.
- El fichero de acciones son líneas `<segundo> tap|swipe|text|key …`.
- `prueba-ui.sh probar` copia vídeo y fotogramas a `/sdcard/Movies/dsh` y avisa
  a MediaStore, así que se ven en la galería del teléfono.
- Cada runner de Actions firma con su propio keystore de depuración: la primera
  instalación tras un push suele exigir desinstalar antes (el script lo hace).
- El input sobre una pantalla virtual (`input -d N`) a veces tarda: deja varios
  segundos entre acciones y mira `-timeline.txt` para saber cuándo ocurrió cada
  una de verdad.

