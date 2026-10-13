#!/data/data/com.termux/files/usr/bin/bash
#
# prueba-ui.sh — ciclo completo para mejorar la interfaz sin tocar el teléfono:
# espera a que GitHub Actions compile el commit actual, baja el APK, lo instala
# y graba una sesión de la app en la pantalla virtual.
#
#   scripts/prueba-ui.sh bajar              espera el run de HEAD y baja el APK
#   scripts/prueba-ui.sh instalar           instala el APK bajado
#   scripts/prueba-ui.sh probar NOMBRE [ACCIONES]
#   scripts/prueba-ui.sh todo NOMBRE [ACCIONES]
#
# ACCIONES es un fichero con líneas "<segundo> tap|swipe|text|key ..." (ver ~/vtest.sh).
# El vídeo y los fotogramas quedan en /sdcard/Movies/dsh, visibles en la galería.
#
# Variables: ADB_SERIAL (por defecto el primero conectado), UI_SECS, UI_DPIS.
set -uo pipefail

REPO="$(cd "$(dirname "$0")/.." && pwd)"
WORK="${UI_WORK:-$HOME/vtest}"
DEST="${UI_DEST:-/sdcard/Movies/dsh}"
VTEST="${UI_VTEST:-$HOME/vtest.sh}"
SECS="${UI_SECS:-30}"
SIZE="${UI_SIZE:-1080x2340}"
DPI="${UI_DPI:-440}"

say() { printf '%s\n' "$*" >&2; }
die() { printf 'error: %s\n' "$*" >&2; exit 1; }

serial() {
  if [ -n "${ADB_SERIAL:-}" ]; then printf '%s' "$ADB_SERIAL"; return; fi
  adb devices 2>/dev/null | awk 'NR>1 && $2 == "device" { print $1; exit }'
}

# --- bajar: espera el run de HEAD y trae el APK ------------------------------
cmd_bajar() {
  command -v gh >/dev/null || die "falta gh"
  local sha info id num estado
  # Sin APK viejo por medio: si el run falla, no queremos instalar el anterior.
  rm -rf "$WORK/apk"
  sha="$(git -C "$REPO" rev-parse HEAD)"
  info="$(gh run list --branch main --limit 20 \
    --json databaseId,number,headSha,status,conclusion \
    --jq "[.[] | select(.headSha == \"$sha\")] | first" 2>/dev/null)"
  [ -n "$info" ] && [ "$info" != "null" ] ||
    die "no hay ningún run de Actions para $sha. ¿Hiciste push?"
  id="$(printf '%s' "$info" | jq -r .databaseId)"
  num="$(printf '%s' "$info" | jq -r .number)"
  estado="$(printf '%s' "$info" | jq -r .status)"
  say "run $num ($sha) — estado: $estado"

  if [ "$estado" != "completed" ]; then
    say "esperando a que termine…"
    gh run watch "$id" --exit-status --interval 15 >/dev/null 2>&1
  fi
  info="$(gh run view "$id" --json status,conclusion)"
  [ "$(printf '%s' "$info" | jq -r .conclusion)" = "success" ] ||
    die "el run $num terminó en $(printf '%s' "$info" | jq -r .conclusion). Revisa: gh run view $id --log-failed"

  rm -rf "$WORK/apk"
  mkdir -p "$WORK/apk"
  gh run download "$id" -n "cartasdebuff-$num" -D "$WORK/apk" >/dev/null ||
    die "no pude bajar el artefacto cartasdebuff-$num"
  find "$WORK/apk" -name '*.apk'
}

cmd_instalar() {
  local apk s
  s="$(serial)"; [ -n "$s" ] || die "sin dispositivo adb"
  apk="${1:-}"
  [ -n "$apk" ] || apk="$(find "$WORK/apk" -name 'app-release.apk' | head -1)"
  [ -f "$apk" ] || die "no encontré el APK. Ejecuta antes: $0 bajar"
  say "instalando $apk en $s"
  # `-r` actualiza sin tocar los datos; `-d` permite bajar de versionCode.
  # Solo se desinstala si el fallo es de firma (build antiguo firmado por otro
  # runner): desinstalar por cualquier otro motivo deja el teléfono sin app.
  local salida
  if salida="$(adb -s "$s" install -r -d "$apk" 2>&1)"; then
    :
  elif printf '%s' "$salida" | grep -q 'INSTALL_FAILED_UPDATE_INCOMPATIBLE'; then
    say "la firma del APK instalado es de otro keystore: desinstalando y reinstalando"
    adb -s "$s" uninstall com.maximillionsnyder.cartasdebuff >/dev/null 2>&1
    adb -s "$s" install -r -d "$apk" >/dev/null || die "adb install falló"
  else
    printf '%s\n' "$salida" >&2
    case "$salida" in
    *INSTALL_FAILED_USER_RESTRICTED*)
      die "MIUI bloqueó la instalación: activa «Instalar vía USB» en Opciones de desarrollador" ;;
    esac
    die "adb install falló"
  fi
  adb -s "$s" shell dumpsys package com.maximillionsnyder.cartasdebuff |
    grep -m1 versionName | sed 's/^ */instalada: /' >&2
}

# --- probar: sesión grabada en la pantalla virtual ---------------------------
cmd_probar() {
  local nombre="${1:-prueba}" acciones="${2:-}"
  [ -x "$VTEST" ] || die "falta $VTEST (grabador de pantalla virtual)"
  pkill -x scrcpy 2>/dev/null
  local arg=()
  [ -n "$acciones" ] && arg+=(--actions "$acciones")
  "$VTEST" com.maximillionsnyder.cartasdebuff \
    --secs "$SECS" --size "$SIZE" --dpi "$DPI" --out "$nombre" "${arg[@]}" || return 1

  local s; s="$(serial)"
  mkdir -p "$DEST"
  local f base
  for f in "$WORK/$nombre".mp4 "$WORK/$nombre"-final.png "$WORK/$nombre"-seq*.png \
           "$WORK/$nombre"-timeline.txt; do
    [ -f "$f" ] || continue
    base="$(basename "$f")"
    cp -f "$f" "$DEST/$base"
    say "galería: $DEST/$base"
  done
  # MediaStore no ve lo que copia adb/termux hasta que se le avisa. Hay que
  # pedirle el escaneo a MediaProvider: `termux-media-scan` se queda colgado.
  if [ -n "$s" ]; then
    for f in "$DEST/$nombre".mp4 "$DEST/$nombre"-final.png "$DEST/$nombre"-seq*.png; do
      [ -f "$f" ] || continue
      timeout 20 adb -s "$s" shell content call --uri content://media/external/file \
        --method scan_file --arg "$f" >/dev/null 2>&1
    done
  fi
  say "listo: $DEST/$nombre.mp4"
}

case "${1:-}" in
  bajar)    shift; cmd_bajar "$@" ;;
  instalar) shift; cmd_instalar "$@" ;;
  probar)   shift; cmd_probar "$@" ;;
  todo)     shift; cmd_bajar && cmd_instalar && cmd_probar "$@" ;;
  *)        sed -n '3,16p' "$0" | sed 's/^# \{0,1\}//' ;;
esac
