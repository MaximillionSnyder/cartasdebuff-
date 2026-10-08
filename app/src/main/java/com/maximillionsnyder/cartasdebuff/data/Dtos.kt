package com.maximillionsnyder.cartasdebuff.data

import com.maximillionsnyder.cartasdebuff.domain.CartaApoyo
import com.maximillionsnyder.cartasdebuff.domain.CartaPersonaje
import com.maximillionsnyder.cartasdebuff.domain.Efecto
import com.maximillionsnyder.cartasdebuff.domain.Escenario
import com.maximillionsnyder.cartasdebuff.domain.Evolution
import com.maximillionsnyder.cartasdebuff.domain.EvoOrigen
import com.maximillionsnyder.cartasdebuff.domain.FuentePersonaje
import com.maximillionsnyder.cartasdebuff.domain.Fuentes
import com.maximillionsnyder.cartasdebuff.domain.GrupoCondicion
import com.maximillionsnyder.cartasdebuff.domain.Skill
import com.maximillionsnyder.cartasdebuff.domain.VersionGen
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal data class SkillDto(
    val id: Int,
    val iconId: Int? = null,
    val rarity: Int? = null,
    val types: List<String> = emptyList(),
    val cost: Int? = null,
    val activation: Int? = null,
    val name: Map<String, String?> = emptyMap(),
    val desc: Map<String, String?> = emptyMap(),
    val unreleased: List<String> = emptyList(),
    val conditions: List<ConditionDto> = emptyList(),
    val geneVersion: GeneVersionDto? = null,
    val evolution: EvolutionDto = EvolutionDto(),
    val sources: SourcesDto = SourcesDto(),
)

@Serializable
internal data class ConditionDto(
    @SerialName("base_time") val baseTime: Long? = null,
    val condition: String? = null,
    val precondition: String? = null,
    val effects: List<EffectDto> = emptyList(),
)

@Serializable
internal data class EffectDto(val type: Int = 0, val value: Int = 0)

@Serializable
internal data class GeneVersionDto(
    val id: Int? = null,
    val iconId: Int? = null,
    val cost: Int? = null,
    val name: Map<String, String?> = emptyMap(),
    val desc: Map<String, String?> = emptyMap(),
)

@Serializable
internal data class EvoOrigenDto(
    @SerialName("card_id") val cardId: Int? = null,
    @SerialName("scenario_id") val scenarioId: Int? = null,
    val old: Int? = null,
    val evos: List<Int> = emptyList(),
)

@Serializable
internal data class EvolutionDto(
    val evo: List<EvoOrigenDto>? = null,
    val preEvo: EvoOrigenDto? = null,
)

@Serializable
internal data class SourcesDto(
    val characterCards: List<CharacterSourceDto> = emptyList(),
    val characterEvents: List<Int> = emptyList(),
    val supportHints: List<Int> = emptyList(),
    val supportEvents: List<Int> = emptyList(),
    val scenarioEvents: List<Int> = emptyList(),
)

@Serializable
internal data class CharacterSourceDto(val cardId: Int, val kinds: List<String> = emptyList())

@Serializable
internal data class CharacterCardDto(
    val cardId: Int,
    val charId: Int,
    val rarity: Int? = null,
    val obtained: String? = null,
    val urlName: String? = null,
    val name: Map<String, String?> = emptyMap(),
    val title: Map<String, String?> = emptyMap(),
    val image: String = "",
)

@Serializable
internal data class SupportCardDto(
    val supportId: Int,
    val charId: Int,
    val type: String? = null,
    val rarity: Int? = null,
    val obtained: String? = null,
    val urlName: String? = null,
    val name: Map<String, String?> = emptyMap(),
    val title: Map<String, String?> = emptyMap(),
    val image: String = "",
)

@Serializable
internal data class ScenarioDto(
    val id: Int,
    val name: Map<String, String?> = emptyMap(),
    val urlName: String? = null,
)

val jsonParser: Json = Json { ignoreUnknownKeys = true }

internal fun SkillDto.toDomain() = Skill(
    id = id,
    iconId = iconId,
    rarity = rarity,
    types = types,
    cost = cost,
    activation = activation,
    name = name,
    desc = desc,
    unreleased = unreleased,
    conditions = conditions.map { it.toDomain() },
    geneVersion = geneVersion?.toDomain(),
    evolution = Evolution(
        evo = evolution.evo.orEmpty().map { it.toDomain() },
        preEvo = evolution.preEvo?.toDomain(),
    ),
    sources = Fuentes(
        characterCards = sources.characterCards.map { FuentePersonaje(it.cardId, it.kinds) },
        characterEvents = sources.characterEvents,
        supportHints = sources.supportHints,
        supportEvents = sources.supportEvents,
        scenarioEvents = sources.scenarioEvents,
    ),
)

internal fun ConditionDto.toDomain() = GrupoCondicion(
    baseTime = baseTime,
    condition = condition,
    precondition = precondition,
    effects = effects.map { Efecto(it.type, it.value) },
)

internal fun GeneVersionDto.toDomain() = VersionGen(
    id = id,
    iconId = iconId,
    cost = cost,
    name = name,
    desc = desc,
)

internal fun EvoOrigenDto.toDomain() = EvoOrigen(cardId, scenarioId, old, evos)

internal fun CharacterCardDto.toDomain() = CartaPersonaje(
    cardId = cardId,
    charId = charId,
    rarity = rarity,
    obtained = obtained,
    urlName = urlName,
    name = name,
    title = title,
    image = image,
)

internal fun SupportCardDto.toDomain() = CartaApoyo(
    supportId = supportId,
    charId = charId,
    type = type,
    rarity = rarity,
    obtained = obtained,
    urlName = urlName,
    name = name,
    title = title,
    image = image,
)

internal fun ScenarioDto.toDomain() = Escenario(id = id, name = name, urlName = urlName)
