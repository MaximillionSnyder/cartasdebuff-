package com.maximillionsnyder.cartasdebuff

import com.maximillionsnyder.cartasdebuff.domain.CartaApoyo
import com.maximillionsnyder.cartasdebuff.ui.cards.FiltroApoyos
import com.maximillionsnyder.cartasdebuff.ui.cards.filtrarApoyos
import org.junit.Assert.assertEquals
import org.junit.Test

class FiltroApoyosTest {

    private fun carta(
        supportId: Int,
        type: String,
        rarity: Int,
        nombre: String,
        titulo: String = "",
    ) = CartaApoyo(
        supportId = supportId,
        charId = supportId / 10,
        type = type,
        rarity = rarity,
        obtained = "gacha",
        urlName = "$supportId-x",
        name = mapOf("en" to nombre, "ja" to "$nombre-jp"),
        title = mapOf("en" to titulo),
        image = "https://example.invalid/$supportId.png",
    )

    /* Lista desordenada a propósito: 2★, 3★, 1★, 3★, 1★. */
    private val cartas = listOf(
        carta(10002, "speed", 2, "Silence Suzuka", "[Tracen Academy]"),
        carta(10003, "stamina", 3, "Tokai Teio", "[Miracle Runner]"),
        carta(10001, "guts", 1, "Special Week", "[Tracen Academy]"),
        carta(20001, "speed", 3, "Gold Ship", "[Festival]"),
        carta(20002, "intelligence", 1, "Mejiro McQueen", "[Wit]"),
    )

    @Test
    fun estrellasCeroNoFiltra() {
        val resultado = filtrarApoyos(cartas, FiltroApoyos(estrellas = 0))
        assertEquals(5, resultado.size)
    }

    @Test
    fun filtraPorCadaCantidadDeEstrellas() {
        assertEquals(
            listOf(10001, 20002),
            filtrarApoyos(cartas, FiltroApoyos(estrellas = 1)).map { it.supportId },
        )
        assertEquals(
            listOf(10002),
            filtrarApoyos(cartas, FiltroApoyos(estrellas = 2)).map { it.supportId },
        )
        assertEquals(
            listOf(10003, 20001),
            filtrarApoyos(cartas, FiltroApoyos(estrellas = 3)).map { it.supportId },
        )
    }

    @Test
    fun ordenAscendenteVaDeUnaATresEstrellasConDesempatePorId() {
        val ids = filtrarApoyos(cartas, FiltroApoyos(descendente = false)).map { it.supportId }
        assertEquals(listOf(10001, 20002, 10002, 10003, 20001), ids)
    }

    @Test
    fun ordenDescendenteVaDeTresAUnaEstrellaConDesempatePorId() {
        val ids = filtrarApoyos(cartas, FiltroApoyos(descendente = true)).map { it.supportId }
        assertEquals(listOf(10003, 20001, 10002, 10001, 20002), ids)
    }

    @Test
    fun filtraPorTipo() {
        val ids = filtrarApoyos(cartas, FiltroApoyos(tipo = "speed")).map { it.supportId }
        assertEquals(listOf(10002, 20001), ids)
    }

    @Test
    fun buscaEnNombreSinImportarMayusculas() {
        val ids = filtrarApoyos(cartas, FiltroApoyos(consulta = "  teio ")).map { it.supportId }
        assertEquals(listOf(10003), ids)
    }

    @Test
    fun buscaEnTitulo() {
        val ids = filtrarApoyos(cartas, FiltroApoyos(consulta = "tracen")).map { it.supportId }
        assertEquals(listOf(10001, 10002), ids)
    }

    @Test
    fun combinaTipoEstrellasYBusqueda() {
        val filtro = FiltroApoyos(consulta = "g", tipo = "speed", estrellas = 3, descendente = true)
        assertEquals(listOf(20001), filtrarApoyos(cartas, filtro).map { it.supportId })
    }

    @Test
    fun sinCoincidenciasDevuelveListaVacia() {
        val filtro = FiltroApoyos(consulta = "no existe", tipo = "friend", estrellas = 2)
        assertEquals(emptyList<Int>(), filtrarApoyos(cartas, filtro).map { it.supportId })
    }
}
