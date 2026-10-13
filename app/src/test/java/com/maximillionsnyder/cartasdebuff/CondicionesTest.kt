package com.maximillionsnyder.cartasdebuff

import com.maximillionsnyder.cartasdebuff.domain.leerCondiciones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CondicionesTest {

    private fun textos(expresion: String) = leerCondiciones(expresion).map { it.texto }

    @Test
    fun expresionVaciaNoDevuelveTerminos() {
        assertTrue(leerCondiciones(null).isEmpty())
        assertTrue(leerCondiciones("").isEmpty())
        assertTrue(leerCondiciones("   ").isEmpty())
    }

    @Test
    fun porcentajesYPosicionesSeLeenConSimbolo() {
        assertEquals(
            listOf("recorrido (%) ≥ 50", "recorrido (%) ≤ 60", "puesto relativo (%) > 50"),
            textos("distance_rate>=50&distance_rate<=60&order_rate>50"),
        )
        assertEquals(listOf("posición ≤ 2", "aguante (%) ≥ 30"), textos("order<=2&hp_per>=30"))
    }

    @Test
    fun clavesDeSiONoSeLeenComoFrase() {
        assertEquals(listOf("en la recta final"), textos("is_last_straight==1"))
        assertEquals(listOf("no en curva"), textos("corner==0"))
        assertEquals(listOf("en curva"), textos("corner!=0"))
        assertEquals(listOf("siempre"), textos("always==1"))
    }

    @Test
    fun valoresConNombreSeTraducen() {
        assertEquals(listOf("fase: final"), textos("phase==2"))
        assertEquals(listOf("estilo de carrera: líder"), textos("running_style==2"))
        assertEquals(listOf("tipo de distancia: larga"), textos("distance_type==3"))
        assertEquals(listOf("tipo de terreno: césped"), textos("ground_type==1"))
    }

    @Test
    fun loQueNoSeConoceQuedaEnCrudoYMarcado() {
        val terminos = leerCondiciones("clave_rara>=3&is_overtake==1")
        assertEquals(2, terminos.size)
        assertFalse(terminos[0].traducido)
        assertEquals("clave_rara>=3", terminos[0].texto)
        assertTrue(terminos[1].traducido)
        assertEquals("al adelantar", terminos[1].texto)
    }

    @Test
    fun elHipodromoYLasClavesNumericasMuestranSuValor() {
        assertEquals(listOf("hipódromo = 10008"), textos("track_id==10008"))
        assertEquals(listOf("metros restantes ≥ 600"), textos("remain_distance>=600"))
    }
}
