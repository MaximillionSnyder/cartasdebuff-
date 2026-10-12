# Pantalla virtual en Android — recopilación oficial

Fuentes: referencia de `developer.android.com`, documentación de `source.android.com`,
javadoc y código de AOSP (`platform_frameworks_base`). Verificado contra el dispositivo
(POCO X6 Pro 5G, Android 15 / API 35) donde se indica.

---

## 0. Resumen: qué se puede y qué no

| Escenario | ¿Posible? | Por qué |
|---|---|---|
| App normal crea un display y dibuja **su** contenido | Sí | API pública; display privado y no confiable, con `Surface` propia |
| App normal abre **otra** app en ese display | No | `setLaunchDisplayId` solo permite displays públicos o donde la app ya tiene actividades; en no confiables exige `allowEmbedded` + `ACTIVITY_EMBEDDING` |
| App normal hace mirroring de la pantalla física | No | `AUTO_MIRROR` exige `CAPTURE_VIDEO_OUTPUT` (signature, solo sistema) |
| Proceso con identidad de **shell** (adb, Shizuku, scrcpy) crea `PUBLIC\|OWN_CONTENT_ONLY\|TRUSTED` y lanza apps ahí | Sí | El uid shell tiene `ADD_TRUSTED_DISPLAY`, `INTERNAL_SYSTEM_WINDOW`, `INJECT_EVENTS` |
| Inyectar toques solo en ese display sin tocar el físico | Sí | `input -d N` / `injectInputEvent` con `displayId` |
| Ver esa pantalla sin mirarla | Solo el creador | `ImageReader` sobre la `Surface`, o el propio creador grabando (scrcpy `--record`). **`screencap -d N` desde otro proceso falla** con `Status: -2` (NAME_NOT_FOUND): el display es privado de quien lo creó. Ver §9 |
| Pantalla simulada por `overlay_display_devices` invisible | No | "Creates a secondary display as an **overlay on the device**" |
| App de terceros hace streaming de otra app a un display virtual | Solo con rol | `COMPANION_DEVICE_APP_STREAMING` + `CREATE_VIRTUAL_DEVICE`; **solo priv-apps preinstaladas** |
| Un agente automatiza apps ajenas "de forma oficial" | Solo con rol | ComputerControl (Android 15): `ACCESS_COMPUTER_CONTROL` + rol de asistente + consentimiento por app |

## 1. API pública para crear una pantalla virtual

`DisplayManager.createVirtualDisplay(String name, int width, int height, int densityDpi, Surface surface, int flags)`
devuelve un `VirtualDisplay`.

Javadoc de `VirtualDisplay`:

> The content of a virtual display is rendered to a `Surface` that you must provide to
> `createVirtualDisplay()`. Because a virtual display renders to a surface provided by the
> application, it will be **released automatically when the process terminates and all
> remaining windows on it will be forcibly removed**. However, you should also explicitly
> call `release()` when you're done.

Otros métodos: `resize()`, `setSurface()`, `release()`, `getDisplay()`, `VirtualDisplay.Callback`.
Selección: `DisplayManager.getDisplays()`, `getDisplays(DISPLAY_CATEGORY_PRESENTATION)`,
`getDisplay(int)`.

Para contenido propio en un display secundario, la API pública es **`Presentation`**
("a special kind of dialog whose purpose is to present content on a secondary display"),
que se cancela sola cuando el display desaparece y usa el contexto del display para
inflar recursos con la densidad correcta.

## 2. Banderas (`DisplayManager.VIRTUAL_DISPLAY_FLAG_*`)

| Bandera | Qué hace (javadoc oficial) | Requisito oficial |
|---|---|---|
| `PUBLIC` | "A public virtual display behaves just like most any other display... Applications can open windows on the display and the system may mirror the contents of other displays onto it." Sin ella, privado (`Display#FLAG_PRIVATE`). | — (ver AUTO_MIRROR) |
| `OWN_CONTENT_ONLY` | "the virtual display will only ever show its own content and will be blanked instead if it has no windows". Mutuamente excluyente con `AUTO_MIRROR`; si se ponen ambas, gana own-content. | — |
| `AUTO_MIRROR` | Implícita al pedir `PUBLIC` sin `OWN_CONTENT_ONLY`. | "requires the **CAPTURE_VIDEO_OUTPUT** or **CAPTURE_SECURE_VIDEO_OUTPUT** permission. These permissions are reserved for use by system components and are **not available to third-party applications**." |
| `SECURE` | Display seguro (`Display#FLAG_SECURE`); el llamante promete cifrado. | "requires the **CAPTURE_SECURE_VIDEO_OUTPUT** permission" (sistema) |
| `PRESENTATION` | Se registra en `DISPLAY_CATEGORY_PRESENTATION`; las apps pueden proyectar automáticamente. | — |
| `SUPPORTS_TOUCH` | Asociable a un dispositivo táctil que case con su `uniqueId`. | `@TestApi` |
| `TRUSTED` | "Indicates that the display is trusted to show system decorations and receive inputs without users' touch." | `@hide @SystemApi`; el servicio exige **ADD_TRUSTED_DISPLAY** |
| `OWN_DISPLAY_GROUP` | Fuera del `DisplayGroup` por defecto. | `@hide` |
| `ALWAYS_UNLOCKED` | Siempre desbloqueado, sin keyguard. "Only valid for virtual displays that aren't in the default display group." | `@hide` |
| `TOUCH_FEEDBACK_DISABLED` | Sin sonido ni vibración al tocar. | `@hide` |
| `OWN_FOCUS` | Foco y touch mode propios. "The display must be trusted in order to have its own focus." | `@hide @TestApi` |
| `DEVICE_DISPLAY_GROUP` | `DisplayGroup` asociado a su dispositivo virtual. | `@hide` |

En `DisplayManagerService.createVirtualDisplayInternal`:

- `PUBLIC` fuerza `AUTO_MIRROR`; `OWN_CONTENT_ONLY` lo desactiva después.
- Un display `PUBLIC` **no** puede marcarse `CAN_SHOW_WITH_INSECURE_KEYGUARD` (lanza
  `IllegalArgumentException`).
- Si `callingUid != SYSTEM_UID` y se pide `TRUSTED` → exige `ADD_TRUSTED_DISPLAY`
  o lanza `SecurityException`.

## 3. Lanzar aplicaciones en esa pantalla

`ActivityOptions.setLaunchDisplayId(int)` — javadoc oficial:

> An app can launch activities on **public displays or displays where the app already has
> activities**. Otherwise, trying to launch on a private display or providing an invalid
> display id will result in an exception.
> Setting launch display id will be **ignored on devices that don't have
> `PackageManager.FEATURE_ACTIVITIES_ON_SECONDARY_DISPLAYS`**.

Documentación oficial de política de lanzamiento (`multi_display/activity-launch`):

- Por defecto la actividad se lanza en el display del llamante. Si el lanzamiento viene de
  algo no asociado a un display (**como un shell** o un contexto de aplicación), se coloca en
  el display superior donde el usuario interactuó por última vez, salvo que se fije el display.
- **Restricciones de seguridad**:
  - "apps can launch their own activities only on a virtual display of their creation"
    (desde Android 10, para que una app no lea información sensible de la superficie de un
    display que creó).
  - "System components with the **INTERNAL_SYSTEM_WINDOW** permission can launch on any display."
  - "Callers with the **ACTIVITY_EMBEDDING** permission can launch activities from other apps
    that have the flag `ActivityInfo.FLAG_ALLOW_EMBEDDED`."
  - "Activity launches on **private displays are allowed only for the owner or activities
    present on that display**."
  - Existe `ActivityManager#isActivityStartAllowedOnDisplay(Context, int, Intent)` para
    comprobar las restricciones antes de intentarlo.
  - "Most security restrictions are applied in the
    `ActivityStackSupervisor#isCallerAllowedToLaunchOnDisplay`" (hoy `ActivityTaskSupervisor`).

## 4. Vías **oficiales sancionadas** para ejecutar apps en un display virtual

### 4.1 Companion app streaming (Android 13+)

De `source.android.com/docs/core/permissions/app-streaming`:

- El rol **`COMPANION_DEVICE_APP_STREAMING`** permite crear y gestionar proxies de
  dispositivos remotos para transmitir apps del dispositivo local al remoto.
- "The app holding the `COMPANION_DEVICE_APP_STREAMING` role can obtain permissions to
  **create a virtual display, launch an app on it**, and then stream a video of the launched
  app to another device."
- "When the role is granted, the app receives the **`CREATE_VIRTUAL_DEVICE`** permission".
- "The Virtual Device Manager creates a virtual display that's **separate from the visible
  primary display**"; la app "is launched on, or transferred to, the virtual display".
- El rol asume capacidades como "Create and manage **trusted virtual displays**, including
  unlocked displays... **Start activities on the virtual display**".
- **Límite clave**: "Only **preinstalled priv-apps** are allowed to hold the
  `COMPANION_DEVICE_APP_STREAMING` role."

### 4.2 VirtualDeviceManager y ComputerControl (Android 15)

De la referencia de `android.companion.virtual.VirtualDeviceManager`:

- "System level service for creation and management of virtual devices... enables interactive
  sharing of capabilities between the host Android device and a remote device."
- El dispositivo solo lo soporta si habilita `config_enableVirtualDeviceManager` y
  `android.software.activities_on_secondary_displays`; si no,
  `Context#getSystemService(VirtualDeviceManager.class)` devuelve `null`.
- Da soporte a la función **ComputerControl**, que es el marco oficial para que un agente
  automatice aplicaciones:
  - "ComputerControl may only be used by agent applications holding the
    **`ACCESS_COMPUTER_CONTROL`** permission and meeting the relevant policy requirements.
    **Only the current ASSISTANT role holder** can use ComputerControl."
  - El dispositivo debe respetar una **allowlist/denylist** de apps automatizables y
    "MUST support the per-app consent dialog UI", es decir, **consentimiento explícito del
    usuario por aplicación**.
- `VirtualDevice` expone `getDisplayIds()` y, en la implementación del rol, permite crear y
  gestionar **displays virtuales e inputs virtuales**.

Conclusión práctica: Android tiene un camino *sancionado* para "una app corre en un display
virtual y algo la controla", pero pasa por **rol + permiso de sistema + consentimiento**, no
por una API abierta a cualquier app.

## 5. Control por ADB / shell (superficie de AOSP)

```bash
# Uso real del binario: input [<source>] [-d DISPLAY_ID] <command> [<arg>...]
adb shell input -d 2 tap 300 500
adb shell input -d 2 swipe 300 900 300 300 300
adb shell input -d 2 text "hola"
adb shell input -d 2 keyevent KEYCODE_BACK
# DISPLAY_ID admite también DEFAULT_DISPLAY e INVALID_DISPLAY

adb shell am start-activity --display 2 -n com.paquete/.Actividad
adb shell screencap -d 2 -p > pantalla2.png   # uso: screencap [-ahp] [-d display-id] [FILENAME]

adb shell cmd display get-displays            # listar (no crea pantallas)
adb shell cmd display enable-display|disable-display|dock|undock|power-off|power-reset
adb shell dumpsys display                     # DisplayDeviceInfo y address por display
adb shell dumpsys input                       # Event Hub State / asociación de inputs

# Pantalla simulada (ajuste de prueba, NO API):
adb shell settings put global overlay_display_devices "1920x1080/320"
adb shell settings put global overlay_display_devices ""
```

- `Settings.Global.OVERLAY_DISPLAY_DEVICES` está marcado `@TestApi` y
  `@UnsupportedAppUsage(maxTargetSdk = R)`.
- `cmd display` **no** tiene subcomando para crear pantallas.
- Enrutado de entrada en displays físicos: se asocia por puerto en
  `/vendor/etc/input-port-associations.xml` (documentado en `multi_display/input-routing`).

## 6. Permisos: quién puede hacer qué

`protectionLevel` según `core/res/AndroidManifest.xml` de AOSP:

| Permiso | protectionLevel | Para qué |
|---|---|---|
| `ADD_TRUSTED_DISPLAY` | `signature\|role` | Crear un display virtual **confiable** |
| `CAPTURE_VIDEO_OUTPUT` | `signature` | Displays con `AUTO_MIRROR` |
| `CAPTURE_SECURE_VIDEO_OUTPUT` | `signature\|role` | Displays `SECURE` / mirror seguro |
| `INTERNAL_SYSTEM_WINDOW` | `signature\|module\|recents` | Lanzar actividades en cualquier display |
| `ACTIVITY_EMBEDDING` | `signature\|privileged` | Incrustar actividades ajenas (`allowEmbedded`) |
| `INJECT_EVENTS` | `signature` | Inyectar toques y teclas (`input`) |
| `WRITE_SECURE_SETTINGS` | `signature\|privileged\|development\|role\|installer` | `settings put global ...` |
| `CREATE_VIRTUAL_DEVICE` | (vía rol) | Crear un dispositivo virtual (app streaming) |
| `ACCESS_COMPUTER_CONTROL` | (vía rol de asistente) | Automatizar apps con ComputerControl |

**Verificado en el dispositivo** leyendo el manifiesto real de `/system/priv-app/Shell/Shell.apk`
(uid 2000, el mismo que dan adb y Shizuku): declara `ADD_TRUSTED_DISPLAY`,
`INTERNAL_SYSTEM_WINDOW`, `INJECT_EVENTS`, `ACTIVITY_EMBEDDING` y `CAPTURE_VIDEO_OUTPUT`.
Por eso un proceso con identidad de shell sí puede crear un display confiable y hospedar apps
de terceros, y una app normal no.

## 7. Documentación oficial

**Multi-Display (source.android.com)** — la ruta correcta usa guion bajo:
- Visión general: https://source.android.com/docs/core/display/multi_display
- Displays: https://source.android.com/docs/core/display/multi_display/displays
- **Política de lanzamiento de actividades**: https://source.android.com/docs/core/display/multi_display/activity-launch
- **Enrutado de entrada**: https://source.android.com/docs/core/display/multi_display/input-routing
- Pantalla de bloqueo: https://source.android.com/docs/core/display/multi_display/lock-screen
- Multi-resume: https://source.android.com/docs/core/display/multi_display/multi-resume
- Decoraciones del sistema: https://source.android.com/docs/core/display/multi_display/system-decorations
- Soporte de IME: https://source.android.com/docs/core/display/multi_display/ime-support
- Audio multi-zona: https://source.android.com/docs/core/display/multi_display/multi-zone-audio
- Buenas prácticas: https://source.android.com/docs/core/display/multi_display/recommended-practices
- **Entorno de pruebas y desarrollo**: https://source.android.com/docs/core/display/multi_display/testing-dev-environment
- FAQ: https://source.android.com/docs/core/display/multi_display/faq
- Companion app streaming: https://source.android.com/docs/core/permissions/app-streaming
- Índice de Display: https://source.android.com/docs/core/display
- Desktop windowing: https://source.android.com/docs/core/display/desktop-windowing

**Referencia (developer.android.com)**
- DisplayManager: https://developer.android.com/reference/android/hardware/display/DisplayManager
- VirtualDisplay: https://developer.android.com/reference/android/hardware/display/VirtualDisplay
- Display: https://developer.android.com/reference/android/view/Display
- ActivityOptions: https://developer.android.com/reference/android/app/ActivityOptions
- Presentation: https://developer.android.com/reference/android/app/Presentation
- Guía de presentaciones: https://developer.android.com/develop/ui/views/layout/presentations
- VirtualDeviceManager: https://developer.android.com/reference/android/companion/virtual/VirtualDeviceManager
- VirtualDevice: https://developer.android.com/reference/android/companion/virtual/VirtualDevice
- MediaProjection: https://developer.android.com/media/grow/media-projection
- Opciones de desarrollador (pantallas simuladas): https://developer.android.com/studio/debug/dev-options
- adb: https://developer.android.com/tools/adb

**AOSP (código)**
- `services/core/java/com/android/server/display/DisplayManagerService.java` — `createVirtualDisplayInternal`, `ADD_TRUSTED_DISPLAY`
- `services/core/java/com/android/server/display/DisplayManagerShellCommand.java` — subcomandos de `cmd display`
- `services/core/java/com/android/server/wm/ActivityTaskSupervisor.java` — `isCallerAllowedToLaunchOnDisplay`
- `services/core/java/com/android/server/input/InputShellCommand.java` — `-d` de `input`
- `services/core/java/com/android/server/am/ActivityManagerShellCommand.java` — `--display`
- `cmds/screencap/screencap.cpp` — `-d display-id`
- `core/java/android/provider/Settings.java` — `OVERLAY_DISPLAY_DEVICES`
- `core/java/android/companion/virtual/VirtualDeviceManager.java`
- `core/res/AndroidManifest.xml` — `protectionLevel`
- `packages/Shell/AndroidManifest.xml` — permisos de la app Shell

## 8. Notas del dispositivo y avisos

- Android 15 (API 35), POCO X6 Pro 5G: `cmd display get-displays` devuelve una sola pantalla
  (`Display id 0`, INTERNAL, 1220x2712, densidad 480, `state ON`, 120 Hz).
- `input` desde Termux (uid de app) falla con
  `SecurityException: ... requires the INJECT_EVENTS permission`; por `adb shell` funciona.
- ~~**Aviso**: hay un informe en el tracker de Google...~~ **Resuelto, ver §9.** El informe
  (issue 384752747) tiene causa raíz documentada: AOSP eliminó `SurfaceControl.createDisplay`
  en diciembre de 2023. **Este dispositivo no está afectado** — la pantalla virtual funciona.

---

## 9. Verificado: pantalla virtual funcional (2026-10-12)

La pregunta que quedó abierta en §0 y §8 — si un proceso con identidad de **shell** puede
crear un display virtual y hospedar apps de terceros en él — **tiene respuesta afirmativa en
este dispositivo**. Probado de punta a punta.

### 9.1 Requisito previo: identidad de shell

`adb` emparejado por depuración inalámbrica (`adb pair` + `adb connect`), que da
`uid=2000(shell)`, `context=u:r:shell:s0` — el uid que §6 identificó como necesario.
Desde Termux (uid de app) sigue sin ser posible.

### 9.2 El experimento

```bash
pkg install scrcpy                       # 5.0.1, repo termux-x11 (~108 MB)
scrcpy --new-display=1920x1080/320 \
       --start-app=+com.android.settings \
       --no-audio --no-window --record=/ruta/salida.mp4
```

Resultado, en orden:

| Comprobación | Salida observada |
|---|---|
| Creación | `[server] INFO: New display: 1920x1072/320 (id=2)` |
| `cmd display get-displays` | `Display id 0` + `Display id 2` |
| Lanzamiento | `Starting app "Configuración" on display 3...` |
| `dumpsys activity activities` | `Display #3 ... topResumedActivity=com.android.settings/.MainSettings` |
| Grabación | h264, 1920x1072, 93 fotogramas, 362 KB |
| Frame extraído | Configuración renderizada correctamente |
| Al morir scrcpy | El display desaparece solo |

La última fila confirma el javadoc de `VirtualDisplay`: *"released automatically when the
process terminates and all remaining windows on it will be forcibly removed"*.

Nota: el id del display **no es estable** entre ejecuciones (fue 2 y luego 3), así que hay que
leerlo del log o de `cmd display get-displays`, nunca darlo por supuesto.

### 9.3 Tres correcciones a este documento

1. **`am start-activity --display N` por sí solo NO basta.** Se probó con
   `am start-activity --display 2 -n com.android.settings/.Settings`: no devolvió error, pero
   la actividad acabó en el **display 0** (`dumpsys` mostró `Display #2` vacío). Android
   reutilizó la instancia ya existente. Hace falta parada previa — que es justo lo que hace
   `--start-app=+paquete` de scrcpy — o banderas que fuercen instancia nueva.

2. **`screencap -d N` desde un proceso distinto del creador falla.** Devuelve
   `Failed to take screenshot. Status: -2` (= `NAME_NOT_FOUND`): el display es **privado de
   quien lo creó** y no aparece en la lista que ve `screencap`. La captura debe hacerla el
   creador (`--record`, o `ImageReader` sobre la propia `Surface`). Esto corrige la fila
   correspondiente de §0.

3. **`--no-window` a secas es incompatible con `--new-display`.** Sin ningún sink de vídeo,
   scrcpy desactiva el vídeo y aborta con
   `ERROR: --new-display is incompatible with --no-video`. Hay que darle un sink:
   `--record=fichero.mp4`.

### 9.4 Sobre el issue 384752747

Su causa raíz está documentada en el
[PR #5008 de scrcpy](https://github.com/Genymobile/scrcpy/pull/5008): AOSP **eliminó
`SurfaceControl.createDisplay`** el 21-dic-2023
([CL: Delete Unsupported APIs](https://cs.android.com/android/_/android/platform/frameworks/base/+/e35fd6654f3ff6572b52fe125110af4d716a4974)),
y las ROMs posteriores a marzo de 2024 incorporan el cambio. El reemplazo es
`DisplayManager.createVirtualDisplay`.

El fallo asociado ([scrcpy #6445](https://github.com/Genymobile/scrcpy/issues/6445), abierto)
describe que en **Android 16 QPR2** el display se crea pero las apps no se lanzan en él.
**Este dispositivo (Android 15, parche 2025-11-01, HyperOS OS2.0.209.0.VNLMIXM) no está
afectado**: las apps se lanzan y renderizan correctamente.

### 9.5 Inyección de input: verificada (2026-10-12)

`input -d N` funciona sobre el display virtual y **no afecta al display físico**. Probado con
`com.example.morphdemo` (demo de Compose cuyo contenido muta al tocarlo), grabando con
`--time-limit=100` para que scrcpy cerrara solo y finalizara el mp4.

| Instante | Acción | Resultado observado |
|---|---|---|
| t=45 s | — | Card "3 - Transmutacion por voxeles" en estado **card**: "Presupuesto mensual S/ 1.240" y cuatro barras finas |
| t=49 s | `input -d 5 tap 960 536` | La misma card pasa a estado **gráfica**: barras con Comida 520, Transp. 310, Ocio 190, Otros 220 |
| t=61 s | `input -d 5 swipe 960 800 960 250 400` | La lista se desplaza |
| t=74 s | `input -d 5 tap 700 700` | — |
| t=84 s | `input -d 5 tap 1200 400` | — |

`input` no devolvió ningún error. La `SecurityException` de `INJECT_EVENTS` solo aparece al
invocarlo desde Termux (uid de app) — ver §8; por `adb shell` (uid 2000) funciona.

**Dos trampas al grabar:**

- Matar scrcpy con `SIGKILL` deja el mp4 corrupto (`moov atom not found`). Hay que dejar que
  cierre solo (`--time-limit=N`) o enviarle una señal que le permita finalizar.
- `SIGTERM` no siempre lo detiene en modo `--no-window`; conviene usar `--time-limit`.
- Cuidado con `pkill -f "scrcpy -s"`: la cadena coincide con la línea de comandos del propio
  shell que lo invoca y **se mata a sí mismo**. Usar `pkill -x scrcpy`.

### 9.6 App pesada: juego 3D renderizando (2026-10-12)

Prueba de estrés con **Honor of Kings** (`com.levelinfinite.sgameGlobal`), la app más pesada
instalada: 540 MB de APK y **1.2 GB de RAM residente** en ejecución.

```bash
scrcpy -s 127.0.0.1:37267 --new-display=1920x1080/320 --no-window --no-audio \
       --record=juego.mp4 --start-app=+com.levelinfinite.sgameGlobal --time-limit=150
```

| Comprobación | Resultado |
|---|---|
| Display creado | `New display: 1920x1072/320 (id=7)` |
| Actividad | `Display #7 ... com.levelinfinite.sgameGlobal visible=true` |
| RAM del proceso | **1.2 GB** (`RES`), más un subproceso `weg_web` de 152 MB |
| CPU | picos de 160 % y 128 % |
| Memoria del sistema | 386 MB libres durante la carga (de 11 GB) |
| Renderizado | **Correcto**: UI completa del juego, escena estable entre t=80 s y t=145 s |
| Al liberar el display | El proceso principal muere con él |

Conclusión: la pantalla virtual soporta sin problema una app 3D exigente. El juego se renderiza
a 1920x1072 sin corrupción, cuelgue ni caída.

Dos observaciones:

- Durante el arranque apareció un diálogo de permisos
  (`com.google.android.permissioncontroller`) en el display virtual; el juego continuó igualmente.
- El juego estaba **logueado en una cuenta real** y cargó la pantalla de inicio con normalidad.
  No dio señales de detectar la pantalla no estándar.
- Tras cerrar, conviene `am force-stop <paquete>`: queda un subproceso residual y el
  dispositivo va muy justo de memoria (≈100 MB libres en reposo, no por el test).

### 9.7 Pendiente

- Abrir una segunda app en el mismo display.
- `termux-x11` para ver la pantalla en vivo, en vez de grabar a fichero.

### 9.8 Evidencia
- `/sdcard/Movies/dsh/pantalla-virtual-2026-10-12.mp4` — grabación del display virtual con Settings.
- `/sdcard/Movies/dsh/pantalla-virtual-frame.png` — fotograma con la app renderizada.
- `/sdcard/Movies/dsh/morph-tap-test-2026-10-12.mp4` — grabación con la inyección de toques.
- `/sdcard/Movies/dsh/morph-t45s.png` — estado **card**, antes del toque.
- `/sdcard/Movies/dsh/morph-t55s.png` — estado **gráfica**, tras el toque.
- `/sdcard/Movies/dsh/morph-t90s.png` — tras el swipe, lista desplazada.
- `/sdcard/Movies/dsh/juego-honor-of-kings-2026-10-12.mp4` — el juego 3D en la pantalla virtual.
- `/sdcard/Movies/dsh/juego-t80s.png` — fotograma del juego (t=80 s).
- `/sdcard/Movies/dsh/juego-t145s.png` — fotograma del juego (t=145 s).

---

## 10. Flujo de trabajo: probar apps sin tocar la pantalla física

Caso de uso real: **automatizar una app en una pantalla virtual mientras el usuario sigue
usando el teléfono con normalidad**. Verificado con `com.maximillionsnyder.cartasdebuff`
en el display 8, con Chrome activo en el display 0 al mismo tiempo.

### 10.1 La herramienta

`~/vtest.sh` (en el home de Termux) automatiza el ciclo completo:

```bash
# fichero de acciones: <segundo> <accion> <args>
printf '8 tap 550 330\n16 swipe 536 1400 536 700 400\n24 tap 400 2245\n' > acc.txt

~/vtest.sh com.maximillionsnyder.cartasdebuff --secs 34 --actions acc.txt --samples 8
```

Opciones: `--secs`, `--size WxH`, `--dpi`, `--actions`, `--out`, `--samples`, `--keep`.
Acciones soportadas: `tap`, `swipe`, `text`, `key`. Salida: `<prefijo>.mp4`,
`<prefijo>-final.png` (estado final), `<prefijo>-seqNN.png`, `<prefijo>-timeline.txt`.

### 10.2 Dos trampas descubiertas al construirlo

**1. scrcpy solo codifica cuando el contenido CAMBIA.** No graba a ritmo constante. Una
sesión de 25 s con una pantalla estática produjo **85 fotogramas y un vídeo de 2,3 s**. Con
interacción real, la misma duración dio 418 fotogramas y 26 s.

Consecuencia: **no se puede extraer por marca de tiempo** — el segundo 20 del vídeo no es el
segundo 20 de la sesión. Hay que extraer **por índice de fotograma**. El último fotograma es
siempre el estado final y es el artefacto más útil.

**2. `adb shell` se come el `stdin` de un bucle `while read`.** Al recorrer el fichero de
acciones, solo se ejecutaba la primera: `adb` consumía el resto. Hay que añadir
`</dev/null` a cada invocación.

### 10.3 Resultado sobre cartasdebuff

Tres acciones inyectadas (un `tap` en un chip de filtro, un `swipe` y un `tap` en la barra
de navegación inferior) produjeron la navegación completa de *Skills* a *Cartas de personaje*,
con sus filtros (Todas / 3★ / 2★ / 1★), el contador "270 de 270 cartas" y la lista cargada.

Todo ello **sin que la pantalla física del dispositivo se viera afectada** en ningún momento.

### 10.4 Cuándo NO usar esto

Si solo quieres automatizar y ver lo que pasa, **la pantalla física es más simple**: `adb`
directo, sin scrcpy ni displays virtuales. La pantalla virtual se justifica cuando necesitas
**no secuestrar el teléfono**, probar **otra resolución/densidad** (la virtual se creó a
1072x2336 @440 dpi frente a la física 1220x2712 @480), o tener **varias apps en primer plano
a la vez**.
