#!/data/data/com.termux/files/usr/bin/bash
# (en Termux la ruta del intérprete es la de arriba; fuera de Termux: bash scripts/adb-ui.sh)
# scripts/adb-ui.sh — manejar el móvil por ADB inalámbrico desde Termux:
# saltar entre apps, tocar, escribir y leer la UI.
#
# Requiere "Depuración inalámbrica" activada (Ajustes → Opciones de desarrollador).
# El cliente adb de Termux se conecta al adbd del propio teléfono por 127.0.0.1.
#
# Flujo típico:
#   scripts/adb-ui.sh connect              # localiza el puerto y conecta
#   scripts/adb-ui.sh pair 41234 123456    # solo si pide vincular
#   scripts/adb-ui.sh find "Ajustes"       # vuelca la UI y lista coincidencias
#   scripts/adb-ui.sh tap-node "Ajustes"   # toca el centro del nodo que coincida
#
# La clave para tocar bien: `find` da coordenadas reales sacadas del árbol de UI,
# así no hay que adivinar píxeles a ojo.

set -euo pipefail

LO=${ADB_PORT_LO:-30000}
HI=${ADB_PORT_HI:-49999}
DUMP_LOCAL=${ADB_UI_DUMP:-${TMPDIR:-/tmp}/adb-ui-dump.xml}
DUMP_REMOTE=/data/local/tmp/adb-ui-dump.xml

say() { printf '%s\n' "$*" >&2; }
die() { printf 'error: %s\n' "$*" >&2; exit 1; }

adbd_state() { getprop init.svc.adbd 2>/dev/null || echo '?'; }

need_adbd() {
  local st
  st=$(adbd_state)
  [ "$st" = running ] ||
    die "adbd está en '$st'. Activa Depuración inalámbrica y reintenta."
}

dev() { adb devices 2>/dev/null | awk 'NR>1 && $2 == "device" { print $1; exit }'; }

require_dev() {
  local d
  d=$(dev)
  [ -n "$d" ] || die "sin dispositivo conectado. Ejecuta: $0 connect"
  printf '%s' "$d"
}

# Puertos TCP abiertos en loopback dentro del rango donde adbd abre el suyo.
scan_ports() {
  python3 - "$LO" "$HI" <<'PY'
import asyncio, sys
lo, hi = int(sys.argv[1]), int(sys.argv[2])
sem = asyncio.Semaphore(400)

async def probe(port):
    async with sem:
        try:
            _, w = await asyncio.open_connection('127.0.0.1', port)
        except OSError:
            return
        w.close()
        try:
            await w.wait_closed()
        except Exception:
            pass
        print(port)

async def main():
    await asyncio.gather(*(probe(p) for p in range(lo, hi + 1)))

asyncio.run(main())
PY
}

# Consulta el último volcado: modo "first" (x y) o "list" (tabla legible).
ui_query() {
  python3 - "$1" "$2" "$3" <<'PY'
import re, sys, xml.etree.ElementTree as ET

path, needle, mode = sys.argv[1], sys.argv[2], sys.argv[3]
try:
    root = ET.parse(path).getroot()
except Exception as exc:
    sys.exit(f'no pude leer {path}: {exc}')

regex = needle.startswith('~')
pat = needle[1:] if regex else needle
rows = []
for node in root.iter('node'):
    text = node.get('text') or ''
    rid = node.get('resource-id') or ''
    desc = node.get('content-desc') or ''
    hay = ' | '.join(x for x in (text, rid, desc) if x)
    hit = re.search(pat, hay) if regex else (pat.lower() in hay.lower())
    if not hit:
        continue
    m = re.match(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]', node.get('bounds') or '')
    if not m:
        continue
    x1, y1, x2, y2 = map(int, m.groups())
    rows.append(((x2 - x1) * (y2 - y1), (x1 + x2) // 2, (y1 + y2) // 2,
                 node.get('class') or '', text, rid, desc, node.get('clickable')))

rows.sort()
if not rows:
    sys.exit(f'sin coincidencias para: {needle}')

if mode == 'first':
    print(f'{rows[0][1]} {rows[0][2]}')
else:
    for area, x, y, cls, text, rid, desc, clickable in rows[:25]:
        label = text or desc or rid or cls
        flag = 'clic' if clickable == 'true' else '    '
        print(f'({x:>5},{y:>5})  {area:>8}px  {flag}  {cls.split(".")[-1]:<14} {label}')
PY
}

cmd_connect() {
  local port=${1:-} out p open d
  need_adbd
  if [ -n "$port" ]; then
    out=$(adb connect "127.0.0.1:$port" 2>&1 || true)
    say "$out"
  else
    open=$(scan_ports || true)
    [ -n "$open" ] ||
      die "adbd corre pero no hay puertos abiertos en $LO-$HI (¿rango distinto? usa: $0 connect <PUERTO>)"
    say "puertos abiertos: $(printf '%s ' $open)"
    for p in $open; do
      out=$(adb connect "127.0.0.1:$p" 2>&1 || true)
      say "  $out"
      d=$(dev)
      [ -n "$d" ] && break
    done
  fi

  # Los puertos abiertos que no son adbd quedan como entradas "offline": se limpian.
  adb devices 2>/dev/null | awk 'NR>1 && $2 == "offline" { print $1 }' |
    while read -r ghost; do adb disconnect "$ghost" >/dev/null 2>&1 || true; done

  d=$(dev)
  if [ -n "$d" ]; then
    say "conectado: $d"
    return 0
  fi
  if adb devices 2>/dev/null | awk 'NR>1 && $2 == "unauthorized"' | grep -q .; then
    die "el teléfono pide autorización: $0 pair <PUERTO_VINCULACION> <CODIGO_6_DIGITOS>"
  fi
  die "no se pudo conectar. Prueba con el puerto a mano: $0 connect <PUERTO>"
}

cmd_pair() {
  [ $# -eq 2 ] || die "uso: $0 pair <PUERTO_VINCULACION> <CODIGO>"
  adb pair "127.0.0.1:$1" "$2"
}

cmd_status() {
  local st d
  st=$(adbd_state)
  printf 'adbd: %s\n' "$st"
  printf 'dispositivo: %s\n' "$(dev || true)"
  adb devices -l 2>/dev/null | tail -n +2
  [ -f "$DUMP_LOCAL" ] && printf 'último volcado: %s (%s bytes)\n' "$DUMP_LOCAL" "$(wc -c <"$DUMP_LOCAL")"
  [ "$st" = running ] || say 'pista: activa Depuración inalámbrica para poder conectar.'
}

cmd_dump() {
  local out=${1:-$DUMP_LOCAL} d
  d=$(require_dev) || exit 1
  adb -s "$d" shell uiautomator dump "$DUMP_REMOTE" >/dev/null 2>&1 ||
    die "uiautomator dump falló (¿pantalla bloqueada?)"
  adb -s "$d" exec-out cat "$DUMP_REMOTE" >"$out"
  say "volcado en $out ($(wc -c <"$out") bytes)"
}

cmd_find() {
  [ $# -ge 1 ] || die "uso: $0 find <texto|~regex> [archivo.xml]"
  local f=${2:-$DUMP_LOCAL}
  [ -f "$f" ] || cmd_dump "$f"
  ui_query "$f" "$1" list
}

cmd_where() {
  [ $# -ge 1 ] || die "uso: $0 where <texto|~regex> [archivo.xml]"
  local f=${2:-$DUMP_LOCAL}
  [ -f "$f" ] || cmd_dump "$f"
  ui_query "$f" "$1" first
}

cmd_tap_node() {
  [ $# -ge 1 ] || die "uso: $0 tap-node <texto|~regex>"
  local xy x y
  xy=$(cmd_where "$@")
  x=${xy% *}
  y=${xy#* }
  say "tocando ($x,$y) por «$1»"
  cmd_tap "$x" "$y"
}

cmd_tap() {
  [ $# -eq 2 ] || die "uso: $0 tap <x> <y>"
  local d
  d=$(require_dev) || exit 1
  adb -s "$d" shell input tap "$1" "$2"
}

cmd_swipe() {
  [ $# -ge 4 ] || die "uso: $0 swipe <x1> <y1> <x2> <y2> [ms]"
  local d
  d=$(require_dev) || exit 1
  adb -s "$d" shell input swipe "$1" "$2" "$3" "$4" "${5:-300}"
}

cmd_longpress() {
  [ $# -eq 2 ] || die "uso: $0 longpress <x> <y>"
  local d
  d=$(require_dev) || exit 1
  adb -s "$d" shell input swipe "$1" "$2" "$1" "$2" 800
}

cmd_text() {
  [ $# -ge 1 ] || die "uso: $0 text <texto>"
  local s="$*" d
  d=$(require_dev) || exit 1
  printf '%s' "$s" | LC_ALL=C grep -q '[^ -~]' &&
    say 'aviso: input text no traga acentos ni emoji; usa un teclado tipo ADBKeyboard.'
  s=${s// /%s}
  s=${s//\'/}
  adb -s "$d" shell "input text '$s'"
}

cmd_key() {
  [ $# -eq 1 ] || die "uso: $0 key <KEYCODE|alias>"
  local k=$1 d
  d=$(require_dev) || exit 1
  case "$k" in
  home | HOME) k=KEYCODE_HOME ;;
  back | BACK) k=KEYCODE_BACK ;;
  recents | RECENTS) k=KEYCODE_APP_SWITCH ;;
  enter | ENTER) k=KEYCODE_ENTER ;;
  wake | WAKE) k=KEYCODE_WAKEUP ;;
  sleep | SLEEP) k=KEYCODE_SLEEP ;;
  esac
  adb -s "$d" shell input keyevent "$k"
}

cmd_open() {
  [ $# -eq 1 ] || die "uso: $0 open <paquete[/Actividad]>"
  local d act
  d=$(require_dev) || exit 1
  case "$1" in
  */*) adb -s "$d" shell am start -n "$1" ;;
  *)
    act=$(adb -s "$d" shell cmd package resolve-activity --brief "$1" 2>/dev/null | tail -1 | tr -d '\r') || true
    [ -n "$act" ] || die "no encontré actividad lanzable para «$1»"
    adb -s "$d" shell am start -n "$act"
    ;;
  esac
}

cmd_apps() {
  local d
  d=$(require_dev) || exit 1
  adb -s "$d" shell pm list packages 2>/dev/null | sed 's/^package://' | LC_ALL=C sort | grep -i -- "${1:-.}" || true
}

cmd_shot() {
  local out=${1:-shot.png} d
  d=$(require_dev) || exit 1
  adb -s "$d" exec-out screencap -p >"$out"
  say "captura en $out"
}

# ---------- pantallas (físicas y virtuales) ----------
# Las operaciones sobre una pantalla concreta van con `-d DISPLAY_ID`:
# `input -d N`, `am start-activity --display N`, `screencap -d N`.
displays_raw() {
  if [ -n "${1:-}" ] && [ -f "$1" ]; then
    cat "$1"
  else
    local d
    d=$(require_dev) || exit 1
    adb -s "$d" shell cmd display get-displays 2>/dev/null
  fi
}

display_list() {
  local tmp
  tmp=$(mktemp)
  displays_raw "${1:-}" >"$tmp"
  python3 - "$tmp" <<'PYPARSE'
import re, sys
rows = []
for line in open(sys.argv[1], encoding='utf-8', errors='replace'):
    m = re.match(r'\s*Display id (\d+): DisplayInfo\{(.*)\}\s*$', line.strip())
    if not m:
        continue
    did, body = int(m.group(1)), m.group(2)
    def grab(pattern):
        hit = re.search(pattern, body)
        return hit.group(1) if hit else '?'
    rows.append((did, grab(r'type (\w+)'),
                 grab(r'real (\d+)') + 'x' + grab(r'real \d+ x (\d+)'),
                 grab(r'density (\d+)'), grab(r'state (\w+)'),
                 grab(r'^"([^"]*)"')))
if not rows:
    sys.exit('no pude interpretar la salida de get-displays')
print('%3s  %-9s %-11s %-5s %-5s %s' % ('id', 'tipo', 'tamano', 'dpi', 'estado', 'nombre'))
for did, typ, size, dpi, state, name in sorted(rows):
    print('%3d  %-9s %-11s %-5s %-5s %s' % (did, typ, size, dpi, state, name))
PYPARSE
  rm -f "$tmp"
}

display_overlay() {
  local spec=${1:-1280x720/320} d
  d=$(require_dev) || exit 1
  adb -s "$d" shell "settings put global overlay_display_devices '$spec'"
  if [ -z "$spec" ]; then say 'pantallas simuladas limpiadas'; else say "pantalla simulada pedida: $spec"; fi
  sleep 2
  display_list
}

display_launch() {
  [ $# -ge 2 ] || die "uso: $0 display launch <id> <paquete[/Actividad]>"
  local id=$1 target=$2 d act
  d=$(require_dev) || exit 1
  case "$target" in
  */*) adb -s "$d" shell am start-activity --display "$id" -n "$target" ;;
  *)
    act=$(adb -s "$d" shell cmd package resolve-activity --brief "$target" 2>/dev/null | tail -1 | tr -d '\r') || true
    [ -n "$act" ] || die "no encontré actividad lanzable para «$target»"
    adb -s "$d" shell am start-activity --display "$id" -n "$act"
    ;;
  esac
}

display_tap() {
  [ $# -eq 3 ] || die "uso: $0 display tap <id> <x> <y>"
  local d
  d=$(require_dev) || exit 1
  adb -s "$d" shell input -d "$1" tap "$2" "$3"
}

display_swipe() {
  [ $# -ge 5 ] || die "uso: $0 display swipe <id> <x1> <y1> <x2> <y2> [ms]"
  local d
  d=$(require_dev) || exit 1
  adb -s "$d" shell input -d "$1" swipe "$2" "$3" "$4" "$5" "${6:-300}"
}

display_text() {
  [ $# -ge 2 ] || die "uso: $0 display text <id> <texto>"
  local id=$1 s d
  shift
  s="$*"
  d=$(require_dev) || exit 1
  s=${s// /%s}
  adb -s "$d" shell "input -d $id text '$s'"
}

display_key() {
  [ $# -eq 2 ] || die "uso: $0 display key <id> <KEYCODE>"
  local d
  d=$(require_dev) || exit 1
  adb -s "$d" shell input -d "$1" keyevent "$2"
}

display_shot() {
  [ $# -ge 1 ] || die "uso: $0 display shot <id> [archivo.png]"
  local id=$1 out=${2:-pantalla-$1.png} d
  d=$(require_dev) || exit 1
  adb -s "$d" exec-out screencap -d "$id" -p >"$out"
  say "captura de la pantalla $id en $out"
}

cmd_display() {
  local sub=${1:-list}
  [ $# -gt 0 ] && shift || true
  case "$sub" in
  list | ls) display_list "$@" ;;
  overlay | simular) display_overlay "$@" ;;
  clear | limpiar) display_overlay "" ;;
  launch | abrir) display_launch "$@" ;;
  tap) display_tap "$@" ;;
  swipe) display_swipe "$@" ;;
  text) display_text "$@" ;;
  key) display_key "$@" ;;
  shot) display_shot "$@" ;;
  *) die "uso: $0 display <list|overlay [WxH/DPI]|clear|launch|tap|swipe|text|key|shot>" ;;
  esac
}

usage() {
  cat <<'EOF'
scripts/adb-ui.sh — control del móvil por ADB inalámbrico

  status                     estado de adbd, conexión y último volcado
  connect [puerto]           conecta (sin puerto: busca el de adbd y lo prueba)
  pair <puerto> <codigo>     vincula (solo si connect pide autorización)

  dump [archivo.xml]         vuelca el árbol de UI
  find <texto|~regex>        lista nodos que coinciden, con coordenadas
  where <texto|~regex>       imprime "x y" del nodo más pequeño que coincide
  tap-node <texto|~regex>    vuelca, localiza y toca ese nodo
  shot [archivo.png]         captura de pantalla

  tap <x> <y>                toque
  longpress <x> <y>          pulsación larga
  swipe <x1> <y1> <x2> <y2> [ms]
  text <texto>               escribe en el campo con foco
  key <KEYCODE|alias>        home|back|recents|enter|wake|sleep
  open <paquete[/Actividad]> salta a una app

  display list               pantallas con id, tipo, tamano, dpi y estado
  display overlay [WxH/DPI]  crea una pantalla simulada (por defecto 1280x720/320)
  display clear              elimina las pantallas simuladas
  display launch <id> <pkg>  abre una app en esa pantalla
  display tap|swipe|text|key <id> ...   interaccion en esa pantalla
  display shot <id> [f.png]  captura de esa pantalla
  apps [filtro]              lista paquetes instalados

Variables: ADB_PORT_LO, ADB_PORT_HI (rango del escaneo), ADB_UI_DUMP (ruta local).
EOF
}

main() {
  local cmd=${1:-}
  [ $# -gt 0 ] && shift || true
  case "$cmd" in
  status) cmd_status "$@" ;;
  connect) cmd_connect "$@" ;;
  pair) cmd_pair "$@" ;;
  dump) cmd_dump "$@" ;;
  find) cmd_find "$@" ;;
  where) cmd_where "$@" ;;
  tap-node) cmd_tap_node "$@" ;;
  tap) cmd_tap "$@" ;;
  longpress) cmd_longpress "$@" ;;
  swipe) cmd_swipe "$@" ;;
  text) cmd_text "$@" ;;
  key) cmd_key "$@" ;;
  open) cmd_open "$@" ;;
  display) cmd_display "$@" ;;
  apps) cmd_apps "$@" ;;
  shot) cmd_shot "$@" ;;
  '' | -h | --help | help) usage ;;
  *) usage; die "comando desconocido: $cmd" ;;
  esac
}

main "$@"
