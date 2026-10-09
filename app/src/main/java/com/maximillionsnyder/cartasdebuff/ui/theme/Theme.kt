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
    /* Fondo gris muy claro y tarjetas blancas: así las filas se despegan del fondo. */
    background = Color(0xFFF4F5F7),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFEFF1F4),
    onSurfaceVariant = Color(0xFF6B7280),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFF7F8FA),
    surfaceContainerLow = Color(0xFFFAFBFC),
    outline = Color(0xFFD6D8DE),
    outlineVariant = Color(0xFFE6E8EC),
)

private val EsquemaOscuro = darkColorScheme(
    primary = Color(0xFFFF8C42),
    onPrimary = Color(0xFF4F2500),
    primaryContainer = Color(0xFF7A3E00),
    onPrimaryContainer = Color(0xFFFFDBC2),
    secondaryContainer = Color(0xFF3A2F27),
    onSecondaryContainer = Color(0xFFFFDCC2),
    background = Color(0xFF0A0A0A),
    onBackground = Color(0xFFE7E7EA),
    surface = Color(0xFF17181C),
    onSurface = Color(0xFFE7E7EA),
    surfaceVariant = Color(0xFF232529),
    onSurfaceVariant = Color(0xFF9CA3AF),
    surfaceContainer = Color(0xFF131418),
    surfaceContainerHigh = Color(0xFF1D1F23),
    surfaceContainerLow = Color(0xFF121317),
    outline = Color(0xFF3A3D44),
    outlineVariant = Color(0xFF2A2C31),
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
