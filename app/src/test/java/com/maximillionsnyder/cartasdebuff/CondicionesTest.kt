package com.maximillionsnyder.cartasdebuff

import com.maximillionsnyder.cartasdebuff.domain.leerCondiciones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/* Los valores esperados de las tablas de traducción no salen del número, salen
   de la descripción oficial de las skills que usan ese valor en GameTora. Por
   eso cada caso lleva al lado la skill de la que se copió: si algún día cambia
   la tabla, el test dice contra qué se contrastó. */
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
        assertEquals(listOf("tipo de terreno: césped"), textos("ground_type==1"))
    }

    /* Steadfast (200732, innata de Winning Ticket 103501) es "Medium" y su
       condición es distance_type==3: el 3 es MEDIA, no larga. La escala es
       1 corta / 2 milla / 3 media / 4 larga —"arena" no es una distancia—, y
       se comprueba contra las etiquetas (Sprint/Mile/Medium/Long) que GameTora
       escribe en la descripción de cada skill. */
    @Test
    fun laDistanciaUsaLaEscalaDelJuego() {
        assertEquals(listOf("tipo de distancia: corta"), textos("distance_type==1"))
        assertEquals(listOf("tipo de distancia: milla"), textos("distance_type==2"))
        assertEquals(listOf("tipo de distancia: media"), textos("distance_type==3"))
        assertEquals(listOf("tipo de distancia: larga"), textos("distance_type==4"))
    }

    /* Rainy Days ◎ es weather==3 y Cloudy Days ◎ es weather==2: el 2 es nublado
       y el 3 lluvia, no al revés. */
    @Test
    fun elClimaNoIntercambiaLluviaYNieve() {
        assertEquals(listOf("clima: sol"), textos("weather==1"))
        assertEquals(listOf("clima: nublado"), textos("weather==2"))
        assertEquals(listOf("clima: lluvia"), textos("weather==3"))
        assertEquals(listOf("clima: nieve"), textos("weather==4"))
    }

    /* La motivación va de 1 a 5: las skills que piden Motivation "Great" usan
       motivation==5, así que el 5 es la perfecta y el 4 sólo la buena. */
    @Test
    fun laMotivacionTieneCincoPasos() {
        assertEquals(listOf("motivación: fatal"), textos("motivation==1"))
        assertEquals(listOf("motivación: normal"), textos("motivation==3"))
        assertEquals(listOf("motivación: buena"), textos("motivation==4"))
        assertEquals(listOf("motivación: perfecta"), textos("motivation==5"))
    }

    /* Spring Runner ◎ pide season==1@season==5: el 5 es la temporada de sakura,
       que la skill cuenta como primavera. */
    @Test
    fun laQuintaTemporadaSigueSiendoPrimavera() {
        assertEquals(listOf("temporada: primavera", "o", "temporada: primavera (sakura)"), textos("season==1@season==5"))
    }

    /* Las claves de fase llevan el número de fase en el valor. Antes
       phase_random==1 se leía "fase (cualquiera)" (justo lo contrario: la fase
       media importa) y phase_laterhalf_random==0 acababa en "no segunda mitad
       de la fase". Los textos de referencia son los de Fast & Furious (200591,
       "mid-race"), Slick Surge (200602, "early part of late-race") y Calm and
       Collected (200561, "second half of the Opening Leg"). */
    @Test
    fun lasFasesDicenDeQueParteDeLaCarreraSeHabla() {
        assertEquals(listOf("en la fase media (cualquiera)"), textos("phase_random==1"))
        assertEquals(listOf("en la fase inicial (cualquiera)"), textos("phase_random==0"))
        assertEquals(listOf("en la fase final (cualquiera)"), textos("phase_random==2"))
        assertEquals(listOf("primera mitad de la fase final (cualquiera)"), textos("phase_firsthalf_random==2"))
        assertEquals(listOf("primera mitad de la fase esprint final (cualquiera)"), textos("phase_firsthalf_random==3"))
        assertEquals(listOf("segunda mitad de la fase inicial (cualquiera)"), textos("phase_laterhalf_random==0"))
        assertEquals(listOf("en recta de la fase final (cualquiera)"), textos("phase_straight_random==2"))
    }

    /* corner_random==3 es la tercera curva (Beautiful Form, 202521: "on a third
       corner just before the final corner"), y corner==3 también. */
    @Test
    fun lasCurvasConNumeroDicenCualSon() {
        assertEquals(listOf("en la curva 3 (cualquiera)"), textos("corner_random==3"))
        assertEquals(listOf("en la curva 3"), textos("corner==3"))
        assertEquals(listOf("no en la curva 3"), textos("corner!=3"))
    }

    /* El "@" separa alternativas: la skill vale si se cumple cualquiera. */
    @Test
    fun lasAlternativasSeSeparanConUnaO() {
        assertEquals(
            listOf("tipo de distancia: media", "en la curva 3 (cualquiera)", "o", "tipo de distancia: larga", "en la curva 3 (cualquiera)"),
            textos("distance_type==3&corner_random==3@distance_type==4&corner_random==3"),
        )
        val terminos = leerCondiciones("weather==1@weather==2")
        assertEquals(3, terminos.size)
        assertTrue(terminos[1].separador)
        assertFalse(terminos[0].separador)
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

    /* Una rama con una clave desconocida no puede hacer que se pierda la otra:
       se enseña en crudo y marcada. */
    @Test
    fun unaRamaDesconocidaNoSePierdeEntreLasAlternativas() {
        val terminos = leerCondiciones("clave_rara>=3@weather==1")
        assertFalse(terminos[0].traducido)
        assertEquals("clave_rara>=3", terminos[0].texto)
        assertEquals("o", terminos[1].texto)
        assertEquals("clima: sol", terminos[2].texto)
    }

    @Test
    fun elHipodromoYLasClavesNumericasMuestranSuValor() {
        assertEquals(listOf("hipódromo = 10008"), textos("track_id==10008"))
        assertEquals(listOf("metros restantes ≥ 600"), textos("remain_distance>=600"))
    }

    /* Claves que salían en crudo y ahora se traducen. Cada texto sale de la
       descripción oficial de una skill que usa ese valor, que va al lado: si
       alguna vez se cambia la etiqueta, el comentario dice contra qué se
       contrastó. El guardián de que no quede NINGUNA en crudo es
       CondicionesDatosTest, que recorre el asset entero. */
    @Test
    fun lasClavesQueFaltabanTambienSeTraducen() {
        // No Second Thoughts: "If there's no one in front of you at the second furlong"
        assertEquals(listOf("furlong: 2.º"), textos("furlong==1"))
        // Inner Post Proficiency: "Increase performance in brackets 1–3"
        assertEquals(listOf("bracket de salida ≤ 3"), textos("post_number<=3"))
        // Raise My Soul's Blade!: "if it's a G1 or similarly major race"
        assertEquals(listOf("categoría de la carrera: G1"), textos("grade==100"))
        // Night Races ◎: "Increase performance at night"
        assertEquals(listOf("franja horaria: noche"), textos("time==4"))
        // Innate Experience: "when near the inner rail on the final corner"
        assertEquals(listOf("carril: interior"), textos("lane_type==0"))
        // Determined Stroke: "in the top 40% for the entire first half of the race"
        assertEquals(listOf("te mantienes en el top 40%"), textos("order_rate_in40_continue==1"))
        // Frenzied Front Runners: "for front runners ... when they become rushed"
        assertEquals(
            listOf("rivales de escapada tentados ≥ 1"),
            textos("running_style_temptation_opponent_count_nige>=1"),
        )
        // Fervor and Rigor: "if you've activated at least 7 green skills"
        assertEquals(listOf("skills verdes activadas ≥ 7"), textos("activate_count_param_skill>=7"))
        // Collaborative Graded Races: "Increase performance in collaborative graded races"
        assertEquals(listOf("en carrera de grado de tierra"), textos("is_dirtgrade==1"))
        // Los mismos: la condición es que NO estén tentados
        assertEquals(listOf("no tentado"), textos("is_temptation==0"))
    }
}
