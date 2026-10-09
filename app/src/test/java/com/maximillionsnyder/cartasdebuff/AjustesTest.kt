package com.maximillionsnyder.cartasdebuff

import com.maximillionsnyder.cartasdebuff.domain.duracionEnSegundos
import com.maximillionsnyder.cartasdebuff.domain.etiquetaPantallaInicio
import com.maximillionsnyder.cartasdebuff.domain.etiquetaTema
import org.junit.Assert.assertEquals
import org.junit.Test

class AjustesTest {

    @Test
    fun duracionesExactasSeMuestranEnSegundosEnteros() {
        assertEquals("1 s", duracionEnSegundos(1000))
        assertEquals("9 s", duracionEnSegundos(9000))
        assertEquals("60 s", duracionEnSegundos(60000))
        assertEquals("130 s", duracionEnSegundos(130000))
    }

    @Test
    fun duracionesConFraccionUsanUnDecimal() {
        assertEquals("1,5 s", duracionEnSegundos(1500))
        assertEquals("2,5 s", duracionEnSegundos(2500))
        assertEquals("0,3 s", duracionEnSegundos(250))
    }

    @Test
    fun etiquetasDeTemaIncluyenValoresDesconocidos() {
        assertEquals("Sistema", etiquetaTema("sistema"))
        assertEquals("Claro", etiquetaTema("claro"))
        assertEquals("Oscuro", etiquetaTema("oscuro"))
        assertEquals("Sistema", etiquetaTema("cualquiera"))
    }

    @Test
    fun etiquetasDePantallaInicialIncluyenValoresDesconocidos() {
        assertEquals("Skills", etiquetaPantallaInicio("skills"))
        assertEquals("Personajes", etiquetaPantallaInicio("personajes"))
        assertEquals("Apoyos", etiquetaPantallaInicio("apoyos"))
        assertEquals("Ajustes", etiquetaPantallaInicio("ajustes"))
        assertEquals("Skills", etiquetaPantallaInicio("otra"))
    }
}
