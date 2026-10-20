package com.maximillionsnyder.cartasdebuff.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.maximillionsnyder.cartasdebuff.domain.Textos
import java.util.Locale

/* Idioma visible de la app: el mismo que el de los datos (en/ja/ko/tw).
   MainActivity lo provee; los componentes que no reciben `idioma` lo leen de aquí. */
val LocalIdioma = staticCompositionLocalOf { "en" }

@Composable
fun t(tabla: Textos, clave: String, vararg args: Any): String {
    val texto = tabla[clave, LocalIdioma.current]
    return if (args.isEmpty()) texto else String.format(Locale.US, texto, *args)
}
