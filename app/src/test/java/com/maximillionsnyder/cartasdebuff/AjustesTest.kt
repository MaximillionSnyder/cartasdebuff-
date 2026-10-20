package com.maximillionsnyder.cartasdebuff

import com.maximillionsnyder.cartasdebuff.domain.FILAS_AJUSTES
import com.maximillionsnyder.cartasdebuff.domain.IDIOMAS
import com.maximillionsnyder.cartasdebuff.domain.duracionEnSegundos
import com.maximillionsnyder.cartasdebuff.domain.etiquetaPantallaInicio
import com.maximillionsnyder.cartasdebuff.domain.etiquetaTema
import com.maximillionsnyder.cartasdebuff.domain.etiquetaVista
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AjustesTest {

    /* Los segundos se enseñan con la unidad de cada idioma; el número es el
       mismo. Entero cuando el dato es exacto. */
    @Test
    fun duracionesExactasSeMuestranEnSegundosEnteros() {
        assertEquals("1 s", duracionEnSegundos(1000, "en"))
        assertEquals("9 s", duracionEnSegundos(9000, "en"))
        assertEquals("60 s", duracionEnSegundos(60000, "en"))
        assertEquals("130 s", duracionEnSegundos(130000, "en"))
        assertEquals("60秒", duracionEnSegundos(60000, "ja"))
        assertEquals("60초", duracionEnSegundos(60000, "ko"))
        assertEquals("60秒", duracionEnSegundos(60000, "tw"))
    }

    /* Decimales con punto en los cuatro idiomas: es el separador de los datos y
       el que no hay que escapar en ja/ko/tw (el español sí pedía coma). */
    @Test
    fun duracionesConFraccionUsanUnDecimal() {
        assertEquals("1.5 s", duracionEnSegundos(1500, "en"))
        assertEquals("2.5 s", duracionEnSegundos(2500, "en"))
        assertEquals("0.3 s", duracionEnSegundos(250, "en"))
        assertEquals("1.5秒", duracionEnSegundos(1500, "ja"))
        assertEquals("1.5초", duracionEnSegundos(1500, "ko"))
        assertEquals("1.5秒", duracionEnSegundos(1500, "tw"))
    }

    /* Un idioma que no sea de los cuatro cae al inglés, como el resto de textos. */
    @Test
    fun unIdiomaDesconocidoUsaElIngles() {
        assertEquals("60 s", duracionEnSegundos(60000, "fr"))
    }

    @Test
    fun etiquetasDeTemaIncluyenValoresDesconocidos() {
        assertEquals("System", etiquetaTema("sistema", "en"))
        assertEquals("Light", etiquetaTema("claro", "en"))
        assertEquals("Dark", etiquetaTema("oscuro", "en"))
        assertEquals("System", etiquetaTema("cualquiera", "en"))
        assertEquals("システム", etiquetaTema("sistema", "ja"))
        assertEquals("라이트", etiquetaTema("claro", "ko"))
        assertEquals("深色", etiquetaTema("oscuro", "tw"))
        assertEquals("システム", etiquetaTema("otro", "ja"))
    }

    @Test
    fun etiquetasDeVistaSeTraducen() {
        assertEquals("Comfortable", etiquetaVista("comoda", "en"))
        assertEquals("Compact", etiquetaVista("compacta", "en"))
        assertEquals("Comfortable", etiquetaVista("otra", "en"))
        assertEquals("標準", etiquetaVista("comoda", "ja"))
        assertEquals("컴팩트", etiquetaVista("compacta", "ko"))
        assertEquals("緊湊", etiquetaVista("compacta", "tw"))
    }

    @Test
    fun etiquetasDePantallaInicialIncluyenValoresDesconocidos() {
        assertEquals("Skills", etiquetaPantallaInicio("skills", "en"))
        assertEquals("Characters", etiquetaPantallaInicio("personajes", "en"))
        assertEquals("Support Cards", etiquetaPantallaInicio("apoyos", "en"))
        assertEquals("Settings", etiquetaPantallaInicio("ajustes", "en"))
        assertEquals("Skills", etiquetaPantallaInicio("otra", "en"))
        assertEquals("スキル", etiquetaPantallaInicio("skills", "ja"))
        assertEquals("캐릭터", etiquetaPantallaInicio("personajes", "ko"))
        assertEquals("支援卡", etiquetaPantallaInicio("apoyos", "tw"))
    }

    /* Las tres tablas de Ajustes tienen que estar en los cuatro idiomas: el motor
       cae al inglés en silencio y media pantalla se quedaría sin traducir. */
    @Test
    fun lasEtiquetasDeAjustesEstanEnLosCuatroIdiomas() {
        val faltan = FILAS_AJUSTES.flatMap { (nombre, textos) ->
            IDIOMAS.filter { textos[it].isNullOrBlank() }.map { "$nombre ($it)" }
        }
        assertTrue("ajustes sin fila en algún idioma:\n" + faltan.joinToString("\n"), faltan.isEmpty())
        assertTrue("no se leyó ninguna etiqueta de ajustes", FILAS_AJUSTES.size >= 9)
    }
}
