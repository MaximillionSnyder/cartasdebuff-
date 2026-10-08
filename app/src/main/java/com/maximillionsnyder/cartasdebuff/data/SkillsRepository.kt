package com.maximillionsnyder.cartasdebuff.data

import android.content.Context
import com.maximillionsnyder.cartasdebuff.domain.Modelo
import com.maximillionsnyder.cartasdebuff.domain.Skill
import kotlinx.serialization.decodeFromString

/* Carga una única vez los JSON embebidos en assets/data y arma el modelo,
   incluyendo los índices inversos carta → skills. */
class SkillsRepository(private val context: Context) {

    val modelo: Modelo by lazy { cargar() }

    private fun cargar(): Modelo {
        val skills = jsonParser
            .decodeFromString<List<SkillDto>>(leer("data/skills.json"))
            .map { it.toDomain() }
        val personajes = jsonParser
            .decodeFromString<Map<String, CharacterCardDto>>(leer("data/character-cards.json"))
            .values.map { it.toDomain() }
            .sortedBy { it.cardId }
        val apoyos = jsonParser
            .decodeFromString<Map<String, SupportCardDto>>(leer("data/support-cards.json"))
            .values.map { it.toDomain() }
            .sortedBy { it.supportId }
        val escenarios = jsonParser
            .decodeFromString<Map<String, ScenarioDto>>(leer("data/scenarios.json"))
            .values.map { it.toDomain() }
            .sortedBy { it.id }

        val porCarta = mutableMapOf<Int, MutableList<Skill>>()
        val porApoyo = mutableMapOf<Int, MutableList<Skill>>()
        for (skill in skills) {
            for (fuente in skill.sources.characterCards) {
                porCarta.getOrPut(fuente.cardId) { mutableListOf() }.add(skill)
            }
            for (cardId in skill.sources.characterEvents) {
                porCarta.getOrPut(cardId) { mutableListOf() }.add(skill)
            }
            for (supportId in skill.sources.supportHints) {
                porApoyo.getOrPut(supportId) { mutableListOf() }.add(skill)
            }
            for (supportId in skill.sources.supportEvents) {
                porApoyo.getOrPut(supportId) { mutableListOf() }.add(skill)
            }
        }

        return Modelo(
            skills = skills,
            cartasPersonaje = personajes,
            cartasApoyo = apoyos,
            escenarios = escenarios,
            skillsPorCarta = porCarta,
            skillsPorApoyo = porApoyo,
        )
    }

    private fun leer(ruta: String): String =
        context.assets.open(ruta).bufferedReader().use { it.readText() }
}
