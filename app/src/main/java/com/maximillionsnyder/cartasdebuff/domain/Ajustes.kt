package com.maximillionsnyder.cartasdebuff.domain

import java.util.Locale

/* Preferencias de la app: valores válidos y etiquetas que usa la pantalla de Ajustes. */

const val TEMA_SISTEMA = "sistema"
const val TEMA_CLARO = "claro"
const val TEMA_OSCURO = "oscuro"

val TEMAS = listOf(TEMA_SISTEMA, TEMA_CLARO, TEMA_OSCURO)

fun etiquetaTema(tema: String): String = when (tema) {
    TEMA_CLARO -> "Claro"
    TEMA_OSCURO -> "Oscuro"
    else -> "Sistema"
}

const val INICIO_SKILLS = "skills"
const val INICIO_PERSONAJES = "personajes"
const val INICIO_APOYOS = "apoyos"
const val INICIO_AJUSTES = "ajustes"

val PANTALLAS_INICIO = listOf(INICIO_SKILLS, INICIO_PERSONAJES, INICIO_APOYOS, INICIO_AJUSTES)

fun etiquetaPantallaInicio(clave: String): String = when (clave) {
    INICIO_PERSONAJES -> "Personajes"
    INICIO_APOYOS -> "Apoyos"
    INICIO_AJUSTES -> "Ajustes"
    else -> "Skills"
}

/* El base_time de las condiciones viene en milisegundos desde GameTora; en la app
   se muestra en segundos (con un decimal cuando no es exacto). */
fun duracionEnSegundos(milisegundos: Long): String {
    val texto = if (milisegundos % 1000L == 0L) {
        (milisegundos / 1000L).toString()
    } else {
        String.format(Locale.US, "%.1f", milisegundos / 1000.0).replace('.', ',')
    }
    return "$texto s"
}
