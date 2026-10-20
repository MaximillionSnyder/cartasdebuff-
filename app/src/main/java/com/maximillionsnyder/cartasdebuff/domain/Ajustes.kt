package com.maximillionsnyder.cartasdebuff.domain

import java.util.Locale

/* Preferencias de la app: valores válidos y etiquetas que usa la pantalla de
   Ajustes. Las claves son códigos internos (no se enseñan) y el texto sale en
   el idioma elegido, como el resto de la app. */

const val TEMA_SISTEMA = "sistema"
const val TEMA_CLARO = "claro"
const val TEMA_OSCURO = "oscuro"

val TEMAS = listOf(TEMA_SISTEMA, TEMA_CLARO, TEMA_OSCURO)

/* Cómo de apretadas se ven las listas: cómoda (por defecto) o compacta, para
   ver más filas de un vistazo. */
const val VISTA_COMODA = "comoda"
const val VISTA_COMPACTA = "compacta"

val VISTAS = listOf(VISTA_COMODA, VISTA_COMPACTA)

const val INICIO_SKILLS = "skills"
const val INICIO_PERSONAJES = "personajes"
const val INICIO_APOYOS = "apoyos"
const val INICIO_AJUSTES = "ajustes"

val PANTALLAS_INICIO = listOf(INICIO_SKILLS, INICIO_PERSONAJES, INICIO_APOYOS, INICIO_AJUSTES)

/* Los cuatro idiomas de una fila, en el mismo orden que IDIOMAS (en, ja, ko, tw). */
private fun textosAjuste(en: String, ja: String, ko: String, tw: String): Map<String, String> =
    mapOf("en" to en, "ja" to ja, "ko" to ko, "tw" to tw)

private val TABLA_AJUSTES: Map<String, Map<String, String>> = mapOf(
    TEMA_SISTEMA to textosAjuste("System", "システム", "시스템", "系統"),
    TEMA_CLARO to textosAjuste("Light", "ライト", "라이트", "淺色"),
    TEMA_OSCURO to textosAjuste("Dark", "ダーク", "다크", "深色"),
    VISTA_COMODA to textosAjuste("Comfortable", "標準", "여유", "舒適"),
    VISTA_COMPACTA to textosAjuste("Compact", "コンパクト", "컴팩트", "緊湊"),
    INICIO_SKILLS to textosAjuste("Skills", "スキル", "스킬", "技能"),
    INICIO_PERSONAJES to textosAjuste("Characters", "キャラクター", "캐릭터", "角色"),
    INICIO_APOYOS to textosAjuste("Support Cards", "サポートカード", "서포트 카드", "支援卡"),
    INICIO_AJUSTES to textosAjuste("Settings", "設定", "설정", "設定"),
)

private val AJUSTES = Textos(TABLA_AJUSTES)

fun etiquetaTema(tema: String, idioma: String): String = when (tema) {
    TEMA_CLARO -> AJUSTES[TEMA_CLARO, idioma]
    TEMA_OSCURO -> AJUSTES[TEMA_OSCURO, idioma]
    else -> AJUSTES[TEMA_SISTEMA, idioma]
}

fun etiquetaVista(vista: String, idioma: String): String = when (vista) {
    VISTA_COMPACTA -> AJUSTES[VISTA_COMPACTA, idioma]
    else -> AJUSTES[VISTA_COMODA, idioma]
}

fun etiquetaPantallaInicio(clave: String, idioma: String): String = when (clave) {
    INICIO_PERSONAJES, INICIO_APOYOS, INICIO_AJUSTES -> AJUSTES[clave, idioma]
    else -> AJUSTES[INICIO_SKILLS, idioma]
}

/* El base_time de las condiciones viene en milisegundos desde GameTora; en la app
   se muestra en segundos (con un decimal cuando no es exacto).

   Decimales con punto en los cuatro idiomas: es el separador que usan los datos
   de GameTora y el que se lee igual en en/ja/ko/tw (ninguno de los cuatro exige
   la coma, que sí pide el español). Se formatea con Locale.US justo para que el
   móvil no lo cambie a coma. */
fun duracionEnSegundos(milisegundos: Long, idioma: String): String {
    val texto = if (milisegundos % 1000L == 0L) {
        (milisegundos / 1000L).toString()
    } else {
        String.format(Locale.US, "%.1f", milisegundos / 1000.0)
    }
    return when (idioma) {
        "ja", "tw" -> "${texto}秒"
        "ko" -> "${texto}초"
        else -> "$texto s"
    }
}

/* Todas las filas de las etiquetas, para el test que exige los cuatro idiomas. */
val FILAS_AJUSTES: List<Pair<String, Map<String, String>>> =
    TABLA_AJUSTES.entries.map { it.key to it.value }
