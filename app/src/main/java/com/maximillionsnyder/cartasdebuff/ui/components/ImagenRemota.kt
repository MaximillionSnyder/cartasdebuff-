package com.maximillionsnyder.cartasdebuff.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

/* Caché en memoria compartida por todos los íconos/retratos de la app. */
private val cacheImagenes = LruCache<String, Bitmap>(160)

/* Cantidad de imágenes guardadas en memoria (se muestra en Ajustes). */
fun imagenesEnCache(): Int = cacheImagenes.size()

/* Vacía la caché en memoria y devuelve cuántas imágenes se descartaron. */
fun limpiarCacheImagenes(): Int {
    val cantidad = cacheImagenes.size()
    cacheImagenes.evictAll()
    return cantidad
}

/* Imagen remota mínima (sin dependencias): descarga una vez y cachea.
   Se recorta con esquinas redondeadas para que combine con las tarjetas. */
@Composable
fun ImagenRemota(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    forma: Shape = RoundedCornerShape(10.dp),
) {
    var bitmap by remember(url) { mutableStateOf(url?.let { cacheImagenes.get(it) }) }

    LaunchedEffect(url) {
        if (url.isNullOrBlank() || bitmap != null) return@LaunchedEffect
        val descargado = withContext(Dispatchers.IO) {
            runCatching {
                URL(url).openStream().use { BitmapFactory.decodeStream(it) }
            }.getOrNull()
        }
        if (descargado != null) {
            cacheImagenes.put(url, descargado)
            bitmap = descargado
        }
    }

    val recorte = modifier.clip(forma)
    val actual = bitmap
    if (actual != null) {
        Image(
            bitmap = actual.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = recorte,
            contentScale = ContentScale.Fit,
        )
    } else {
        Box(recorte.background(MaterialTheme.colorScheme.surfaceVariant))
    }
}
