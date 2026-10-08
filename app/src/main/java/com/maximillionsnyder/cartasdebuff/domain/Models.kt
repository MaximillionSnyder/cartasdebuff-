package com.maximillionsnyder.cartasdebuff.domain

val IDIOMAS = listOf("en", "ja", "ko", "tw")

fun etiquetaIdioma(idioma: String): String = when (idioma) {
    "en" -> "EN"
    "ja" -> "日本語"
    "ko" -> "한국어"
    "tw" -> "中文"
    else -> idioma.uppercase()
}

fun Map<String, String?>.texto(idioma: String): String? =
    this[idioma]?.takeIf { it.isNotBlank() }
        ?: this["en"]?.takeIf { it.isNotBlank() }
        ?: this["ja"]?.takeIf { it.isNotBlank() }

data class Efecto(val type: Int, val value: Int)

data class GrupoCondicion(
    val baseTime: Long?,
    val condition: String?,
    val precondition: String?,
    val effects: List<Efecto>,
)

data class VersionGen(
    val id: Int?,
    val iconId: Int?,
    val cost: Int?,
    val name: Map<String, String?>,
    val desc: Map<String, String?>,
)

data class EvoOrigen(
    val cardId: Int?,
    val scenarioId: Int?,
    val old: Int?,
    val evos: List<Int>,
)

data class Evolution(val evo: List<EvoOrigen>, val preEvo: EvoOrigen?)

data class FuentePersonaje(val cardId: Int, val kinds: List<String>)

data class Fuentes(
    val characterCards: List<FuentePersonaje>,
    val characterEvents: List<Int>,
    val supportHints: List<Int>,
    val supportEvents: List<Int>,
    val scenarioEvents: List<Int>,
)

data class Skill(
    val id: Int,
    val iconId: Int?,
    val rarity: Int?,
    val types: List<String>,
    val cost: Int?,
    val activation: Int?,
    val name: Map<String, String?>,
    val desc: Map<String, String?>,
    val unreleased: List<String>,
    val conditions: List<GrupoCondicion>,
    val geneVersion: VersionGen?,
    val evolution: Evolution,
    val sources: Fuentes,
) {
    fun nombre(idioma: String): String = name.texto(idioma) ?: "?"
    fun descripcion(idioma: String): String = desc.texto(idioma).orEmpty()

    val urlIcono: String? = iconId?.let { "https://media.gametora.com/umamusume/skills/icon/$it.png" }

    /* Mismo criterio que GameTora: velocidad actual (22) + velocidad objetivo (27),
       aceleración (31) + aceleración zenkai (48); solo efectos positivos. */
    val daVelocidad: Boolean = conditions.any { grupo ->
        grupo.effects.any { it.value > 0 && (it.type == 22 || it.type == 27) }
    }

    val daAceleracion: Boolean = conditions.any { grupo ->
        grupo.effects.any { it.value > 0 && (it.type == 31 || it.type == 48) }
    }
}

data class CartaPersonaje(
    val cardId: Int,
    val charId: Int,
    val rarity: Int?,
    val obtained: String?,
    val urlName: String?,
    val name: Map<String, String?>,
    val title: Map<String, String?>,
    val image: String,
) {
    fun nombre(idioma: String): String = name.texto(idioma) ?: "?"
    fun titulo(idioma: String): String = title.texto(idioma).orEmpty()
}

data class CartaApoyo(
    val supportId: Int,
    val charId: Int,
    val type: String?,
    val rarity: Int?,
    val obtained: String?,
    val urlName: String?,
    val name: Map<String, String?>,
    val title: Map<String, String?>,
    val image: String,
) {
    fun nombre(idioma: String): String = name.texto(idioma) ?: "?"
    fun titulo(idioma: String): String = title.texto(idioma).orEmpty()
}

data class Escenario(
    val id: Int,
    val name: Map<String, String?>,
    val urlName: String?,
) {
    fun nombre(idioma: String): String = name.texto(idioma) ?: "Escenario $id"
}

class Modelo(
    val skills: List<Skill>,
    val cartasPersonaje: List<CartaPersonaje>,
    val cartasApoyo: List<CartaApoyo>,
    val escenarios: List<Escenario>,
    val skillsPorCarta: Map<Int, List<Skill>>,
    val skillsPorApoyo: Map<Int, List<Skill>>,
) {
    private val skillPorId = skills.associateBy { it.id }
    private val personajePorId = cartasPersonaje.associateBy { it.cardId }
    private val apoyoPorId = cartasApoyo.associateBy { it.supportId }
    private val escenarioPorId = escenarios.associateBy { it.id }

    fun skill(id: Int): Skill? = skillPorId[id]
    fun cartaPersonaje(id: Int): CartaPersonaje? = personajePorId[id]
    fun cartaApoyo(id: Int): CartaApoyo? = apoyoPorId[id]
    fun escenario(id: Int): Escenario? = escenarioPorId[id]
}
