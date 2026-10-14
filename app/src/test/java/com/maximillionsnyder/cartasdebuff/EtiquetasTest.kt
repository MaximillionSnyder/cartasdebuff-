package com.maximillionsnyder.cartasdebuff

import com.maximillionsnyder.cartasdebuff.domain.etiquetaIdioma
import com.maximillionsnyder.cartasdebuff.domain.etiquetaKind
import com.maximillionsnyder.cartasdebuff.domain.etiquetaObtencion
import com.maximillionsnyder.cartasdebuff.domain.etiquetaTipo
import com.maximillionsnyder.cartasdebuff.domain.etiquetaTipoApoyo
import org.junit.Assert.assertEquals
import org.junit.Test

/* Etiquetas de las fichas de personaje y apoyo. El guardián de que el dato no
   traiga códigos nuevos sin traducir es EtiquetasDatosTest; aquí se fijan los
   textos y, sobre todo, los dos falsos amigos que estaban mal. */
class EtiquetasTest {

    /* "dir" es DIRT: las descripciones dicen "if it's a dirt race" / "in Dirt
       races". Estaba como "Arena", que es otra cosa (y la tabla de condiciones
       ya lo llamaba "tierra"). */
    @Test
    fun elTerrenoDeTierraNoEsArena() {
        assertEquals("Tierra", etiquetaTipo("dir"))
        assertEquals("Césped", etiquetaTipo("tur"))
    }

    @Test
    fun losTiposDeSkillSeTraducenConLaEscalaDelJuego() {
        assertEquals("Corta", etiquetaTipo("sho"))
        assertEquals("Milla", etiquetaTipo("mil"))
        assertEquals("Media", etiquetaTipo("med"))
        assertEquals("Larga", etiquetaTipo("lng"))
        assertEquals("Escapada", etiquetaTipo("run"))
        assertEquals("Perseguidora", etiquetaTipo("cha"))
        assertEquals("Tramo final", etiquetaTipo("l_2"))
        assertEquals("Último esprint", etiquetaTipo("l_3"))
    }

    /* El dato marca los idiomas que faltan con "zh_tw" (326 skills), no con
       "tw": la pastilla salía como "sin ZH_TW". */
    @Test
    fun elChinoTradicionalSeLlamaIgualQueTw() {
        assertEquals("中文", etiquetaIdioma("zh_tw"))
        assertEquals("中文", etiquetaIdioma("tw"))
        assertEquals("EN", etiquetaIdioma("en"))
        assertEquals("한국어", etiquetaIdioma("ko"))
    }

    @Test
    fun lasEtiquetasDeApoyoYObtencionCubrenLosCodigosDelDato() {
        assertEquals("Velocidad", etiquetaTipoApoyo("speed"))
        assertEquals("Coraje", etiquetaTipoApoyo("guts"))
        assertEquals("Amistad", etiquetaTipoApoyo("friend"))
        assertEquals("Gacha", etiquetaObtencion("gacha"))
        assertEquals("Evento de historia", etiquetaObtencion("story_event"))
        assertEquals("Misión limitada", etiquetaObtencion("limited_quest"))
        assertEquals("Innata", etiquetaKind("innate"))
        assertEquals("Despertar", etiquetaKind("awakening"))
    }

    /* Lo que no se conoce se deja tal cual, para que se vea que falta: nunca un
       texto inventado. */
    @Test
    fun loDesconocidoSeDejaTalCual() {
        assertEquals("zz", etiquetaTipo("zz"))
        assertEquals("nuevo_kind", etiquetaKind("nuevo_kind"))
        assertEquals("otro_tipo", etiquetaTipoApoyo("otro_tipo"))
        assertEquals("otra_cosa", etiquetaObtencion("otra_cosa"))
        assertEquals("ZZ", etiquetaIdioma("zz"))
    }
}
