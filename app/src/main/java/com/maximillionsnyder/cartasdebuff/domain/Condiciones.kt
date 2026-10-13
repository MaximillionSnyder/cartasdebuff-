package com.maximillionsnyder.cartasdebuff.domain

/* Las condiciones de las skills llegan de GameTora como una expresión con claves
   internas: "distance_rate>=50&distance_rate<=60&order_rate>50". Leídas así no
   hay quien las entienda, así que se parten en términos y se traducen las claves
   conocidas. Lo que no está en la tabla se deja tal cual y se marca como no
   traducido, para que la ficha pueda enseñar también la expresión original y no
   se pierda información. */

/* Claves que son un número (posición, metros, porcentaje…): siempre se enseña el
   valor. El resto se leen como sí/no cuando el valor es 0 o 1. */
private val NUMERICAS: Set<String> = setOf(
    "order", "order_rate", "distance_rate", "distance_rate_after_random",
    "remain_distance", "course_distance", "accumulatetime", "blocked_side_continuetime",
    "distance_diff_top", "distance_diff_rate", "bashin_diff_behind", "bashin_diff_infront",
    "slope", "base_power", "base_wiz", "base_speed", "base_stamina", "temptation_count",
    "popularity", "track_id", "near_count", "compete_fight_count", "infront_near_lane_time",
    "behind_near_lane_time", "overtake_target_time", "hp_per", "activate_count_all",
    "is_used_skill_id",
)

private val ETIQUETAS: Map<String, String> = mapOf(
    /* dónde y cuándo */
    "always" to "siempre",
    "phase" to "fase",
    "phase_random" to "fase (cualquiera)",
    "phase_firsthalf" to "primera mitad de la fase",
    "phase_firsthalf_random" to "primera mitad de la fase (cualquiera)",
    "phase_laterhalf" to "segunda mitad de la fase",
    "phase_laterhalf_random" to "segunda mitad de la fase (cualquiera)",
    "phase_firstquarter_random" to "primer cuarto de la fase (cualquiera)",
    "phase_straight_random" to "en recta, en cualquier fase",
    "phase_corner_random" to "en curva, en cualquier fase",
    "corner" to "en curva",
    "corner_random" to "en curva (cualquiera)",
    "all_corner_random" to "en cualquier curva",
    "straight_random" to "en recta (cualquiera)",
    "down_slope_random" to "en bajada (cualquiera)",
    "is_finalcorner" to "en la última curva",
    "is_finalcorner_laterhalf" to "en la segunda mitad de la última curva",
    "is_finalcorner_random" to "en la última curva (cualquiera)",
    "is_last_straight" to "en la recta final",
    "is_last_straight_onetime" to "en la recta final (una vez)",
    "is_lastspurt" to "en el último esprint",
    "lastspurt" to "último esprint",
    /* qué pasa en la carrera */
    "order" to "posición",
    "order_rate" to "puesto relativo (%)",
    "distance_rate" to "recorrido (%)",
    "distance_rate_after_random" to "recorrido después (%)",
    "remain_distance" to "metros restantes",
    "course_distance" to "distancia de la carrera",
    "distance_diff_top" to "diferencia con el primero",
    "distance_diff_rate" to "diferencia con el primero (%)",
    "bashin_diff_behind" to "diferencia con el de detrás",
    "bashin_diff_infront" to "diferencia con el de delante",
    "is_overtake" to "al adelantar",
    "overtake_target_time" to "tiempo de adelantamiento",
    "is_behind_in" to "por detrás",
    "is_move_lane" to "al cambiar de carril",
    "is_goodstart" to "salida buena",
    "is_badstart" to "salida mala",
    "change_order_onetime" to "cambio de posición (una vez)",
    "change_order_up_end_after" to "tras remontar al final",
    "change_order_up_middle" to "al remontar",
    "blocked_side_continuetime" to "tiempo bloqueado",
    "infront_near_lane_time" to "tiempo pegado al de delante",
    "behind_near_lane_time" to "tiempo con alguien detrás",
    "near_count" to "rivales cerca",
    "compete_fight_count" to "rivales en pugna",
    /* estado de la carrera y del corredor */
    "running_style" to "estilo de carrera",
    "distance_type" to "tipo de distancia",
    "ground_type" to "tipo de terreno",
    "ground_condition" to "estado del terreno",
    "weather" to "clima",
    "season" to "temporada",
    "rotation" to "sentido de la pista",
    "slope" to "pendiente",
    "popularity" to "popularidad",
    "motivation" to "motivación",
    "hp_per" to "aguante (%)",
    "base_speed" to "velocidad base",
    "base_stamina" to "aguante base",
    "base_power" to "fuerza base",
    "base_wiz" to "ingenio base",
    "temptation_count" to "tentaciones",
    "track_id" to "hipódromo",
    "is_basis_distance" to "distancia base",
    "accumulatetime" to "tiempo acumulado",
    /* skills */
    "is_activate_any_skill" to "con cualquier skill activada",
    "is_activate_other_skill_detail" to "con otra skill activada",
    "is_other_character_activate_advantage_skill" to "con la skill de ventaja de otro",
    "is_used_skill_id" to "skill concreta",
    "activate_count_all" to "skills activadas",
)

/* Valores con nombre: "phase==2" se lee "fase: final". */
private val VALORES: Map<String, Map<String, String>> = mapOf(
    "phase" to mapOf("0" to "inicio", "1" to "media", "2" to "final"),
    "running_style" to mapOf(
        "1" to "escapada",
        "2" to "líder",
        "3" to "intermedia",
        "4" to "perseguidora",
    ),
    "distance_type" to mapOf("1" to "corta", "2" to "media", "3" to "larga", "4" to "arena"),
    "ground_type" to mapOf("1" to "césped", "2" to "tierra"),
    "ground_condition" to mapOf(
        "1" to "firme",
        "2" to "algo blanda",
        "3" to "blanda",
        "4" to "pesada",
    ),
    "weather" to mapOf("1" to "sol", "2" to "lluvia", "3" to "nieve", "4" to "nublado"),
    "season" to mapOf("1" to "primavera", "2" to "verano", "3" to "otoño", "4" to "invierno"),
    "rotation" to mapOf("1" to "a la derecha", "2" to "a la izquierda"),
    "motivation" to mapOf(
        "1" to "mal humor",
        "2" to "normal",
        "3" to "buena",
        "4" to "perfecta",
    ),
)

private val OPERADORES = listOf("==", "!=", ">=", "<=", ">", "<")

/* Un término ya legible y si se pudo traducir o quedó en crudo. */
data class TerminoCondicion(val texto: String, val traducido: Boolean)

/* Parte la expresión en términos legibles. Devuelve la lista vacía si no hay
   nada que enseñar. */
fun leerCondiciones(expresion: String?): List<TerminoCondicion> {
    if (expresion.isNullOrBlank()) return emptyList()
    return expresion.split('&')
        .mapNotNull { leerTermino(it.trim()) }
        .filter { it.texto.isNotBlank() }
}

private fun leerTermino(termino: String): TerminoCondicion? {
    if (termino.isBlank()) return null
    val operador = OPERADORES.firstOrNull { termino.contains(it) }
        ?: return TerminoCondicion(termino, false)
    val clave = termino.substringBefore(operador).trim()
    val valor = termino.substringAfter(operador).trim()
    val etiqueta = ETIQUETAS[clave] ?: return TerminoCondicion(termino, false)

    val nombreValor = VALORES[clave]?.get(valor)
    val texto = when {
        nombreValor != null && operador == "==" -> "$etiqueta: $nombreValor"
        nombreValor != null && operador == "!=" -> "$etiqueta: distinto de $nombreValor"
        clave in NUMERICAS -> "$etiqueta ${simbolo(operador)} $valor"
        valor == "1" && operador == "==" -> etiqueta
        valor == "0" && operador == "==" -> "no $etiqueta"
        valor == "1" && operador == "!=" -> "no $etiqueta"
        valor == "0" && operador == "!=" -> etiqueta
        else -> "$etiqueta ${simbolo(operador)} $valor"
    }
    return TerminoCondicion(texto, true)
}

private fun simbolo(operador: String): String = when (operador) {
    "==" -> "="
    "!=" -> "≠"
    ">=" -> "≥"
    "<=" -> "≤"
    else -> operador
}
