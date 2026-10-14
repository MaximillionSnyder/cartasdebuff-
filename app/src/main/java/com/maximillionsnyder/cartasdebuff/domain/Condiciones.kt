package com.maximillionsnyder.cartasdebuff.domain

/* Las condiciones de las skills llegan de GameTora como una expresión con claves
   internas: "distance_rate>=50&distance_rate<=60&order_rate>50". Leídas así no
   hay quien las entienda, así que se parten en términos y se traducen las claves
   conocidas. Lo que no está en la tabla se deja tal cual y se marca como no
   traducido, para que la ficha pueda enseñar también la expresión original y no
   se pierda información.

   Ojo con los valores: son los del juego y no significan lo mismo de una clave a
   otra. En `distance_type` el 2 es milla y el 3 media; en `weather` el 2 es
   nublado y el 3 lluvia; y `motivation` va de 1 (fatal) a 5 (perfecta). Cada
   tabla de abajo está contrastada con la descripción oficial de las skills que
   usan ese valor, que es la única fuente fiable: el número por sí solo engaña. */

/* Claves que son un número (posición, metros, porcentaje…): siempre se enseña el
   valor. El resto se leen como sí/no cuando el valor es 0 o 1. */
private val NUMERICAS: Set<String> = setOf(
    "order", "order_rate", "distance_rate", "distance_rate_after_random",
    "remain_distance", "course_distance", "accumulatetime", "blocked_side_continuetime",
    "blocked_front_continuetime", "corner_count", "near_infront_count", "fan_count",
    "visiblehorse",
    /* Contadores de skills activadas y de rivales. */
    "activate_count_all_team", "activate_count_end_after", "activate_count_heal",
    "activate_count_later_half", "activate_count_middle", "activate_count_param_skill",
    "activate_count_start", "same_skill_horse_count", "succession_skill_count",
    "temptation_opponent_count_behind", "temptation_opponent_count_infront",
    "running_style_count_nige_otherself", "running_style_count_senko_otherself",
    "running_style_count_sashi_otherself", "running_style_count_oikomi_otherself",
    "running_style_count_same", "running_style_count_same_rate",
    "running_style_temptation_opponent_count_nige", "running_style_temptation_opponent_count_senko",
    "running_style_temptation_opponent_count_sashi", "running_style_temptation_opponent_count_oikomi",
    /* Números sueltos. */
    "base_guts", "overtake_target_no_order_up_time", "post_number", "random_lot",
    "random_lot_shared", "remain_distance_viewer_id", "behind_near_lane_time_set1",
    "change_order_up_finalcorner_after", "distance_diff_top_float",
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
    "corner" to "en curva",
    "straight_random" to "en recta (cualquiera)",
    "down_slope_random" to "en bajada (cualquiera)",
    "up_slope_random" to "en subida (cualquiera)",
    "up_slope_random_later_half" to "en subida de la segunda mitad (cualquiera)",
    "last_straight_random" to "en la recta final (cualquiera)",
    "run_at_full_speed_random" to "a plena velocidad (cualquiera)",
    /* El carril 0 es el rail interior: las skills dicen "near the inner rail". */
    "lane_type" to "carril",
    "straight_front_type" to "en recta",
    "is_finalcorner" to "en la última curva",
    "is_finalcorner_laterhalf" to "en la segunda mitad de la última curva",
    "is_finalcorner_random" to "en la última curva (cualquiera)",
    "is_last_straight" to "en la recta final",
    "is_last_straight_onetime" to "en la recta final (una vez)",
    "is_lastspurt" to "en el último esprint",
    "lastspurt" to "último esprint",
    "all_corner_random" to "en cualquier curva",
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
    /* El paso tapado por delante: "when the way ahead is jammed". */
    "blocked_front" to "con el paso bloqueado delante",
    "blocked_front_continuetime" to "tiempo con el paso bloqueado delante",
    "is_surrounded" to "rodeado",
    /* 掛かり (rushed): la app ya llama "tentaciones" a temptation_count. */
    "is_temptation" to "tentado",
    "near_infront_count" to "rivales justo delante",
    /* Pistas sin curvas: "tracks with no corners". */
    "corner_count" to "curvas de la pista",
    "infront_near_lane_time" to "tiempo pegado al de delante",
    "behind_near_lane_time" to "tiempo con alguien detrás",
    "near_count" to "rivales cerca",
    "compete_fight_count" to "rivales en pugna",
    /* "Mantenerse" en un puesto: las skills dicen "for the entire first half",
       "stayed well-positioned from the start up until the second half". El
       número va en la propia clave, así que la frase ya lo lleva. */
    "order_rate_in20_continue" to "te mantienes en el top 20%",
    "order_rate_in40_continue" to "te mantienes en el top 40%",
    "order_rate_in50_continue" to "te mantienes en el top 50%",
    "order_rate_in80_continue" to "te mantienes en el top 80%",
    "order_rate_out20_continue" to "te mantienes fuera del top 20%",
    "order_rate_out40_continue" to "te mantienes fuera del top 40%",
    "order_rate_out50_continue" to "te mantienes fuera del top 50%",
    "order_rate_out70_continue" to "te mantienes fuera del top 70%",
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
    "temptation_opponent_count_behind" to "rivales tentados detrás",
    "temptation_opponent_count_infront" to "rivales tentados delante",
    /* Rivales por estilo: nige escapada, senko líder, sashi intermedia y
       oikomi perseguidora, igual que la tabla running_style de arriba. */
    "running_style_count_nige_otherself" to "rivales de escapada",
    "running_style_count_senko_otherself" to "rivales líder",
    "running_style_count_sashi_otherself" to "rivales intermedios",
    "running_style_count_oikomi_otherself" to "rivales perseguidores",
    "running_style_count_same" to "rivales con tu mismo estilo",
    "running_style_count_same_rate" to "rivales con tu mismo estilo (%)",
    "running_style_temptation_opponent_count_nige" to "rivales de escapada tentados",
    "running_style_temptation_opponent_count_senko" to "rivales líder tentados",
    "running_style_temptation_opponent_count_sashi" to "rivales intermedios tentados",
    "running_style_temptation_opponent_count_oikomi" to "rivales perseguidores tentados",
    "running_style_equal_popularity_one" to "el favorito usa tu mismo estilo",
    /* Contadores de skills. "param" son las verdes: la skill dice "green skills". */
    "activate_count_start" to "skills activadas al principio",
    "activate_count_middle" to "skills activadas en la fase media",
    "activate_count_later_half" to "skills activadas en la segunda mitad",
    "activate_count_end_after" to "skills activadas al final",
    "activate_count_heal" to "skills de aguante activadas",
    "activate_count_param_skill" to "skills verdes activadas",
    "activate_count_all_team" to "skills activadas por el equipo",
    "same_skill_horse_count" to "rivales con la misma skill",
    "succession_skill_count" to "skills heredadas",
    /* Números sueltos. */
    "base_guts" to "coraje base",
    "post_number" to "bracket de salida",
    "furlong" to "furlong",
    "grade" to "categoría de la carrera",
    "time" to "franja horaria",
    "random_lot" to "probabilidad (%)",
    "random_lot_shared" to "probabilidad compartida (%)",
    "remain_distance_viewer_id" to "metros restantes del personaje vigilado",
    "overtake_target_no_order_up_time" to "tiempo pegado a un rival sin adelantarlo",
    "change_order_up_finalcorner_after" to "adelantamientos tras la última curva",
    "distance_diff_top_float" to "diferencia con el primero",
    /* El sufijo _set1 no aparece documentado en el dato: se deja la misma frase
       que la clave base en vez de inventarse un matiz. */
    "behind_near_lane_time_set1" to "tiempo con alguien detrás",
    "track_id" to "hipódromo",
    "is_basis_distance" to "distancia base",
    "accumulatetime" to "tiempo acumulado",
    /* Carreras de fuera y de tierra de grado (Kawasaki, Funabashi, Morioka, Ooi). */
    "is_abroad" to "en el extranjero",
    "is_dirtgrade" to "en carrera de grado de tierra",
    /* Hipódromos de curvas cerradas y aforo. */
    "is_tight_track" to "en pista de curvas cerradas",
    "fan_count" to "aficionados",
    "visiblehorse" to "rivales visibles",
    /* skills */
    "is_activate_any_skill" to "con cualquier skill activada",
    "is_activate_other_skill_detail" to "con otra skill activada",
    "is_other_character_activate_advantage_skill" to "con la skill de ventaja de otro",
    "is_used_skill_id" to "skill concreta",
    "is_used_skill_id_with_detail_one" to "skill concreta",
    "is_exist_skill_id" to "skill en la carrera",
    "is_activate_heal_skill" to "con una skill de aguante usada",
    "is_popularity_top_character_activate_advantage_skill" to "el favorito activa una skill de ventaja",
    /* Qué personaje tiene que estar en la carrera (1002 es Silence Suzuka). */
    "is_exist_chara_id" to "personaje en la carrera",
    "activate_count_all" to "skills activadas",
)

/* Valores con nombre. Se leen "etiqueta: valor" cuando el operador es ==. */
private val VALORES: Map<String, Map<String, String>> = mapOf(
    /* 0 apertura, 1 mitad, 2 recta final, 3 último esprint. */
    "phase" to mapOf("0" to "inicial", "1" to "media", "2" to "final", "3" to "esprint final"),
    "running_style" to mapOf(
        "1" to "escapada",
        "2" to "líder",
        "3" to "intermedia",
        "4" to "perseguidora",
    ),
    /* Ojo: 1 corta, 2 milla, 3 media y 4 larga. "Arena" no es una distancia. */
    "distance_type" to mapOf("1" to "corta", "2" to "milla", "3" to "media", "4" to "larga"),
    "ground_type" to mapOf("1" to "césped", "2" to "tierra"),
    "ground_condition" to mapOf(
        "1" to "firme",
        "2" to "algo blanda",
        "3" to "blanda",
        "4" to "pesada",
    ),
    /* Ojo: 1 sol, 2 nublado, 3 lluvia y 4 nieve. */
    "weather" to mapOf("1" to "sol", "2" to "nublado", "3" to "lluvia", "4" to "nieve"),
    /* El 5 es la temporada de sakura, que para la skill cuenta como primavera. */
    "season" to mapOf(
        "1" to "primavera",
        "2" to "verano",
        "3" to "otoño",
        "4" to "invierno",
        "5" to "primavera (sakura)",
    ),
    "rotation" to mapOf("1" to "a la derecha", "2" to "a la izquierda"),
    /* El 0 es el rail de dentro ("near the inner rail"); el 2, ir en cabeza. */
    "lane_type" to mapOf("0" to "interior"),
    "straight_front_type" to mapOf("2" to "en cabeza"),
    /* `furlong` viene con base 0: el 1 es el segundo furlong (No Second
       Thoughts pide "at the second furlong" con furlong==1). Se enseña 1-based. */
    "furlong" to mapOf("0" to "1.º", "1" to "2.º", "2" to "3.º", "3" to "4.º"),
    /* grade==100 es una carrera de G1 ("if it's a G1 or similarly major race"). */
    "grade" to mapOf("100" to "G1"),
    /* time==4 es de noche (Night Races ◎: "Increase performance at night"). */
    "time" to mapOf("4" to "noche"),
    /* El -1 es el "sí" de esta bandera ("when the most popular girl activates a skill"). */
    "is_popularity_top_character_activate_advantage_skill" to mapOf("-1" to "sí"),
    /* Escala de cinco pasos, del 1 (fatal) al 5 (perfecta). */
    "motivation" to mapOf(
        "1" to "fatal",
        "2" to "mala",
        "3" to "normal",
        "4" to "buena",
        "5" to "perfecta",
    ),
)

/* Claves de fase: el valor es el número de fase y su nombre entra en la frase.
   Sin esto, "phase_random==1" se leía "fase (cualquiera)" —que es justo lo
   contrario de lo que dice, porque la fase media sí importa— y
   "phase_laterhalf_random==0" acababa en "no segunda mitad de la fase". */
private val ETIQUETAS_FASE: Map<String, String> = mapOf(
    "phase_random" to "en la fase %s (cualquiera)",
    "phase_firsthalf" to "primera mitad de la fase %s",
    "phase_firsthalf_random" to "primera mitad de la fase %s (cualquiera)",
    "phase_laterhalf" to "segunda mitad de la fase %s",
    "phase_laterhalf_random" to "segunda mitad de la fase %s (cualquiera)",
    "phase_firstquarter_random" to "primer cuarto de la fase %s (cualquiera)",
    /* El dato usa las dos grafías: "firsthalf/laterhalf" y "first_half/latter_half". */
    "phase_firstquarter" to "primer cuarto de la fase %s",
    "phase_first_half_straight_random" to "en recta de la primera mitad de la fase %s (cualquiera)",
    "phase_latter_half_straight_random" to "en recta de la segunda mitad de la fase %s (cualquiera)",
    "phase_straight_random" to "en recta de la fase %s (cualquiera)",
    "phase_corner_random" to "en curva de la fase %s (cualquiera)",
)

private val FASES: Map<String, String> = mapOf(
    "0" to "inicial",
    "1" to "media",
    "2" to "final",
    "3" to "esprint final",
)

private val OPERADORES = listOf("==", "!=", ">=", "<=", ">", "<")

/* Un término ya legible, si se pudo traducir y si es sólo el separador "o" que
   une dos alternativas (esos no llevan punto). */
data class TerminoCondicion(
    val texto: String,
    val traducido: Boolean,
    val separador: Boolean = false,
)

/* Parte la expresión en términos legibles. Devuelve la lista vacía si no hay
   nada que enseñar.

   La expresión puede traer alternativas separadas por "@": la skill se activa
   si se cumple CUALQUIERA de ellas. Se enseñan seguidas, con un "o" en medio,
   en vez de mezclarlas con los "&", que sí son todos obligatorios. */
fun leerCondiciones(expresion: String?): List<TerminoCondicion> {
    if (expresion.isNullOrBlank()) return emptyList()
    val alternativas = expresion.split('@')
        .map { rama -> rama.split('&').mapNotNull { leerTermino(it.trim()) }.filter { it.texto.isNotBlank() } }
        .filter { it.isNotEmpty() }
    if (alternativas.isEmpty()) return emptyList()
    if (alternativas.size == 1) return alternativas.first()
    val salida = mutableListOf<TerminoCondicion>()
    for ((indice, rama) in alternativas.withIndex()) {
        if (indice > 0) salida.add(TerminoCondicion("o", true, separador = true))
        salida.addAll(rama)
    }
    return salida
}

private fun leerTermino(termino: String): TerminoCondicion? {
    if (termino.isBlank()) return null
    val operador = OPERADORES.firstOrNull { termino.contains(it) }
        ?: return TerminoCondicion(termino, false)
    val clave = termino.substringBefore(operador).trim()
    val valor = termino.substringAfter(operador).trim()

    ETIQUETAS_FASE[clave]?.let { plantilla ->
        val fase = FASES[valor] ?: return TerminoCondicion(termino, false)
        if (operador != "==" && operador != "!=") return TerminoCondicion(termino, false)
        val texto = plantilla.replace("%s", fase)
        return TerminoCondicion(if (operador == "==") texto else "no $texto", true)
    }

    /* "corner_random==3" es una curva concreta, la tercera, no una cualquiera. */
    if (clave == "corner_random") {
        val numero = valor.toIntOrNull() ?: return TerminoCondicion(termino, false)
        if (operador != "==") return TerminoCondicion(termino, false)
        return TerminoCondicion("en la curva $numero (cualquiera)", true)
    }

    val etiqueta = ETIQUETAS[clave] ?: return TerminoCondicion(termino, false)

    /* `corner` es la excepción: 0 y 1 se leen como sí/no ("no en curva"), pero
       un número mayor es la curva enésima ("en la curva 3", "no en la curva 3"). */
    if (clave == "corner" && (operador == "==" || operador == "!=") && valor != "0" && valor != "1") {
        val numero = valor.toIntOrNull() ?: return TerminoCondicion(termino, false)
        return TerminoCondicion(if (operador == "==") "en la curva $numero" else "no en la curva $numero", true)
    }

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
