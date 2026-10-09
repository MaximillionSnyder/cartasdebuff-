package com.maximillionsnyder.cartasdebuff.ui.cards

import com.maximillionsnyder.cartasdebuff.domain.CartaApoyo

/* Filtros y orden de la pantalla de cartas de apoyo, sin dependencias de Android
   para poder cubrirlos con los tests unitarios. */
data class FiltroApoyos(
    val consulta: String = "",
    val tipo: String = "todos",
    val estrellas: Int = 0,
    val descendente: Boolean = false,
)

/* Orden por estrellas (1★→3★ o 3★→1★); el desempate por ID siempre es ascendente
   para que la lista sea estable al alternar la dirección. */
fun ordenApoyos(descendente: Boolean): Comparator<CartaApoyo> {
    val porRareza = if (descendente) {
        compareByDescending<CartaApoyo> { it.rarity ?: 0 }
    } else {
        compareBy<CartaApoyo> { it.rarity ?: 0 }
    }
    return porRareza.thenBy { it.supportId }
}

fun filtrarApoyos(cartas: List<CartaApoyo>, filtro: FiltroApoyos): List<CartaApoyo> {
    val consulta = filtro.consulta.trim()
    val filtradas = cartas.filter { carta ->
        val coincideTipo = filtro.tipo == "todos" || carta.type == filtro.tipo
        val coincideEstrellas = filtro.estrellas == 0 || carta.rarity == filtro.estrellas
        val coincideBusqueda = consulta.isBlank() ||
            carta.name.values.any { it?.contains(consulta, ignoreCase = true) == true } ||
            carta.title.values.any { it?.contains(consulta, ignoreCase = true) == true }
        coincideTipo && coincideEstrellas && coincideBusqueda
    }
    return filtradas.sortedWith(ordenApoyos(filtro.descendente))
}
