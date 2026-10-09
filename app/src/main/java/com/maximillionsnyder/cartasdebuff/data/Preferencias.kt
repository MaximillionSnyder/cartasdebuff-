package com.maximillionsnyder.cartasdebuff.data

import android.content.Context
import com.maximillionsnyder.cartasdebuff.domain.IDIOMAS
import com.maximillionsnyder.cartasdebuff.domain.INICIO_SKILLS
import com.maximillionsnyder.cartasdebuff.domain.PANTALLAS_INICIO
import com.maximillionsnyder.cartasdebuff.domain.TEMAS
import com.maximillionsnyder.cartasdebuff.domain.TEMA_SISTEMA

/* Ajustes del usuario guardados en el dispositivo: tema, idioma de los datos y
   pantalla inicial. Se leen al abrir la app y se escriben al cambiarlos.
   Cualquier valor desconocido o ausente cae en el valor por defecto. */
class Preferencias(context: Context) {

    private val prefs = context.getSharedPreferences("cartasdebuff-ajustes", Context.MODE_PRIVATE)

    var tema: String
        get() = prefs.getString(CLAVE_TEMA, null)?.takeIf { it in TEMAS } ?: TEMA_SISTEMA
        set(valor) = prefs.edit().putString(CLAVE_TEMA, valor).apply()

    var idioma: String
        get() = prefs.getString(CLAVE_IDIOMA, null)?.takeIf { it in IDIOMAS } ?: IDIOMAS.first()
        set(valor) = prefs.edit().putString(CLAVE_IDIOMA, valor).apply()

    var pantallaInicial: String
        get() = prefs.getString(CLAVE_INICIO, null)?.takeIf { it in PANTALLAS_INICIO } ?: INICIO_SKILLS
        set(valor) = prefs.edit().putString(CLAVE_INICIO, valor).apply()

    fun restablecer() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val CLAVE_TEMA = "tema"
        const val CLAVE_IDIOMA = "idioma"
        const val CLAVE_INICIO = "pantallaInicial"
    }
}
