package com.maximillionsnyder.cartasdebuff.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.maximillionsnyder.cartasdebuff.domain.TEMA_CLARO
import com.maximillionsnyder.cartasdebuff.domain.TEMA_OSCURO
import com.maximillionsnyder.cartasdebuff.domain.TEMA_SISTEMA

private val EsquemaClaro = lightColorScheme(
    primary = Color(0xFFC2410C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE8D6),
    onPrimaryContainer = Color(0xFF4F2500),
    secondaryContainer = Color(0xFFFFDCC2),
    onSecondaryContainer = Color(0xFF544239),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF9F9F9),
    surfaceVariant = Color(0xFFF1F1F1),
    onSurfaceVariant = Color(0xFF6B7280),
)

private val EsquemaOscuro = darkColorScheme(
    primary = Color(0xFFFF8C42),
    onPrimary = Color(0xFF4F2500),
    primaryContainer = Color(0xFF7A3E00),
    onPrimaryContainer = Color(0xFFFFDBC2),
    secondaryContainer = Color(0xFF3A2F27),
    onSecondaryContainer = Color(0xFFFFDCC2),
    background = Color(0xFF0A0A0A),
    surface = Color(0xFF121212),
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFF9CA3AF),
)

/* El tema puede seguir al sistema (por defecto) o forzarse desde Ajustes. */
@Composable
fun CartasDebuffTheme(modo: String = TEMA_SISTEMA, content: @Composable () -> Unit) {
    val oscuro = when (modo) {
        TEMA_CLARO -> false
        TEMA_OSCURO -> true
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (oscuro) EsquemaOscuro else EsquemaClaro,
        content = content,
    )
}
