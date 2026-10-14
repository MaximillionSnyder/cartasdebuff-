package com.maximillionsnyder.cartasdebuff.domain

/* Tablas de etiquetas de las fichas: los códigos que trae GameTora (tipos de
   skill, de dónde sale cada skill, tipo y obtención de una carta de apoyo) se
   enseñan en español. Viven en domain y no en la UI porque son texto puro y así
   se pueden probar: EtiquetasDatosTest recorre los assets y falla si aparece un
   código nuevo sin traducir, igual que hace CondicionesDatosTest con las
   condiciones.

   Ojo con los falsos amigos: "dir" es DIRT (tierra), no arena; las
   descripciones oficiales dicen "if it's a dirt race" / "in Dirt races". Y el
   mismo 1 corta / 2 milla / 3 media / 4 larga de las condiciones. */

val ETIQUETAS_TIPO: Map<String, String> = mapOf(
    "nac" to "Sin condición",
    "run" to "Escapada",
    "ldr" to "Líder",
    "btw" to "Intermedia",
    "cha" to "Perseguidora",
    "sho" to "Corta",
    "mil" to "Milla",
    "med" to "Media",
    "lng" to "Larga",
    "dir" to "Tierra",
    "tur" to "Césped",
    "l_0" to "Tramo inicial",
    "l_1" to "Tramo medio",
    "l_2" to "Tramo final",
    "l_3" to "Último esprint",
    "cor" to "Curva",
    "str" to "Recta",
    "f_c" to "Última curva",
    "f_s" to "Última recta",
    "slo" to "Subida/bajada",
    "dbf" to "Debuff",
)

fun etiquetaTipo(tipo: String): String = ETIQUETAS_TIPO[tipo] ?: tipo

fun etiquetaRareza(rarity: Int?): String = when (rarity) {
    1 -> "Normal"
    2 -> "Rara"
    3, 5 -> "Única"
    4, 6 -> "Evolución"
    else -> "?"
}

fun etiquetaKind(kind: String): String = when (kind) {
    "unique" -> "Única"
    "innate" -> "Innata"
    "awakening" -> "Despertar"
    "evolution" -> "Evolución"
    else -> kind
}

fun etiquetaTipoApoyo(type: String?): String = when (type) {
    "speed" -> "Velocidad"
    "stamina" -> "Aguante"
    "power" -> "Fuerza"
    "guts" -> "Coraje"
    "intelligence" -> "Ingenio"
    "friend" -> "Amistad"
    "group" -> "Grupo"
    else -> type ?: "?"
}

val TIPOS_APOYO: List<String> =
    listOf("speed", "stamina", "power", "guts", "intelligence", "friend", "group")

fun etiquetaObtencion(obtained: String?): String = when (obtained) {
    "gacha" -> "Gacha"
    "main_story" -> "Historia principal"
    "story_event" -> "Evento de historia"
    "promo" -> "Promoción"
    "limited_quest" -> "Misión limitada"
    "shop" -> "Tienda"
    else -> obtained ?: "?"
}
