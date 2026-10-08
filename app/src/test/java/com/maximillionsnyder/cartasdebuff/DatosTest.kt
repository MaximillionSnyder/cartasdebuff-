package com.maximillionsnyder.cartasdebuff

import com.maximillionsnyder.cartasdebuff.data.CharacterCardDto
import com.maximillionsnyder.cartasdebuff.data.ScenarioDto
import com.maximillionsnyder.cartasdebuff.data.SkillDto
import com.maximillionsnyder.cartasdebuff.data.SupportCardDto
import com.maximillionsnyder.cartasdebuff.data.jsonParser
import com.maximillionsnyder.cartasdebuff.data.toDomain
import kotlinx.serialization.decodeFromString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DatosTest {

    private val skills: List<SkillDto> = jsonParser.decodeFromString(leer("data/skills.json"))
    private val personajes: Map<String, CharacterCardDto> = jsonParser.decodeFromString(leer("data/character-cards.json"))
    private val apoyos: Map<String, SupportCardDto> = jsonParser.decodeFromString(leer("data/support-cards.json"))
    private val escenarios: Map<String, ScenarioDto> = jsonParser.decodeFromString(leer("data/scenarios.json"))

    @Test
    fun conteos() {
        assertEquals(1921, skills.size)
        assertEquals(270, personajes.size)
        assertEquals(563, apoyos.size)
        assertEquals(14, escenarios.size)
    }

    @Test
    fun uniqueDeTokaiTeio() {
        val skill = skills.first { it.id == 110031 }
        assertEquals(1, skill.sources.characterCards.size)
        assertEquals(100302, skill.sources.characterCards[0].cardId)
        assertEquals(listOf("unique"), skill.sources.characterCards[0].kinds)
    }

    @Test
    fun totalesDeFuentes() {
        assertEquals(2825, skills.sumOf { it.sources.characterCards.size })
        assertEquals(1282, skills.sumOf { it.sources.characterEvents.size })
        assertEquals(1540, skills.sumOf { it.sources.supportEvents.size })
        assertEquals(4015, skills.sumOf { it.sources.supportHints.size })
        assertEquals(109, skills.sumOf { it.sources.scenarioEvents.size })
    }

    @Test
    fun referenciasResueltas() {
        val cartasSinMetadata = skills
            .flatMap { it.sources.characterCards.map { fuente -> fuente.cardId } + it.sources.characterEvents }
            .filterNot { personajes.containsKey(it.toString()) }
            .distinct()
        assertTrue("Cartas sin metadata: $cartasSinMetadata", cartasSinMetadata.isEmpty())

        val apoyosSinMetadata = skills
            .flatMap { it.sources.supportHints + it.sources.supportEvents }
            .filterNot { apoyos.containsKey(it.toString()) }
            .distinct()
        assertTrue("Apoyos sin metadata: $apoyosSinMetadata", apoyosSinMetadata.isEmpty())

        val escenariosSinMetadata = skills
            .flatMap { it.sources.scenarioEvents }
            .filterNot { escenarios.containsKey(it.toString()) }
            .distinct()
        assertTrue("Escenarios sin metadata: $escenariosSinMetadata", escenariosSinMetadata.isEmpty())
    }

    @Test
    fun efectosDeVelocidadYAceleracion() {
        val dominio = skills.map { it.toDomain() }
        assertEquals(1301, dominio.count { it.daVelocidad })
        assertEquals(389, dominio.count { it.daAceleracion })
    }

    private fun leer(ruta: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream(ruta)) { "Falta $ruta en el classpath" }
            .bufferedReader().use { it.readText() }
}
