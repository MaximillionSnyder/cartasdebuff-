package com.maximillionsnyder.cartasdebuff.domain

/* Tabla de textos de la app por idioma: los mismos cuatro idiomas que los datos
   de GameTora (no hay español en los datos). Los tests comprueban que toda
   clave está en los cuatro idiomas. */
class Textos(private val tabla: Map<String, Map<String, String>>) {
    operator fun get(clave: String, idioma: String): String {
        val textos = tabla[clave] ?: return clave
        return textos[idioma]?.takeIf { it.isNotBlank() }
            ?: textos["en"]?.takeIf { it.isNotBlank() }
            ?: clave
    }
    val claves: Set<String> get() = tabla.keys
}
