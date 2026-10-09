package com.maximillionsnyder.cartasdebuff.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.cartasdebuff.BuildConfig
import com.maximillionsnyder.cartasdebuff.crash.CrashReporter
import com.maximillionsnyder.cartasdebuff.crash.ReporteCrash
import com.maximillionsnyder.cartasdebuff.domain.IDIOMAS
import com.maximillionsnyder.cartasdebuff.domain.Modelo
import com.maximillionsnyder.cartasdebuff.domain.PANTALLAS_INICIO
import com.maximillionsnyder.cartasdebuff.domain.TEMAS
import com.maximillionsnyder.cartasdebuff.domain.etiquetaIdioma
import com.maximillionsnyder.cartasdebuff.domain.etiquetaPantallaInicio
import com.maximillionsnyder.cartasdebuff.domain.etiquetaTema
import com.maximillionsnyder.cartasdebuff.ui.components.EstadoVacio
import com.maximillionsnyder.cartasdebuff.ui.components.imagenesEnCache
import com.maximillionsnyder.cartasdebuff.ui.components.limpiarCacheImagenes

private const val URL_RELEASES = "https://github.com/MaximillionSnyder/cartasdebuff-/releases/latest"
private const val URL_GAMETORA = "https://gametora.com/umamusume"

@Composable
fun AjustesScreen(
    modelo: Modelo,
    idioma: String,
    onIdioma: (String) -> Unit,
    tema: String,
    onTema: (String) -> Unit,
    pantallaInicial: String,
    onPantallaInicial: (String) -> Unit,
    onRestablecer: () -> Unit,
) {
    var pestana by rememberSaveable { mutableStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        Text(
            "Ajustes",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
        )
        Text(
            "Personalizá la app y revisá los datos cargados.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 2.dp),
        )
        PrimaryTabRow(selectedTabIndex = pestana, modifier = Modifier.padding(top = 8.dp)) {
            Tab(
                selected = pestana == 0,
                onClick = { pestana = 0 },
                text = { Text("Preferencias") },
            )
            Tab(
                selected = pestana == 1,
                onClick = { pestana = 1 },
                text = { Text("Reportes") },
            )
        }
        when (pestana) {
            0 -> PestanaPreferencias(
                modelo = modelo,
                idioma = idioma,
                onIdioma = onIdioma,
                tema = tema,
                onTema = onTema,
                pantallaInicial = pantallaInicial,
                onPantallaInicial = onPantallaInicial,
                onRestablecer = onRestablecer,
            )
            else -> PestanaReportes()
        }
    }
}

@Composable
private fun PestanaPreferencias(
    modelo: Modelo,
    idioma: String,
    onIdioma: (String) -> Unit,
    tema: String,
    onTema: (String) -> Unit,
    pantallaInicial: String,
    onPantallaInicial: (String) -> Unit,
    onRestablecer: () -> Unit,
) {
    val context = LocalContext.current
    var cache by remember { mutableStateOf(imagenesEnCache()) }
    var confirmarRestablecer by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TarjetaSeccion("Apariencia", Icons.Filled.Settings) {
            FilaSegmentada(
                titulo = "Tema",
                opciones = TEMAS,
                valor = tema,
                etiqueta = ::etiquetaTema,
                onValor = onTema,
            )
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            FilaDesplegable(
                titulo = "Idioma de los datos",
                valor = idioma,
                opciones = IDIOMAS,
                etiqueta = ::etiquetaIdioma,
                onValor = onIdioma,
            )
        }

        TarjetaSeccion("Inicio", Icons.Filled.Home) {
            FilaDesplegable(
                titulo = "Pantalla inicial",
                valor = pantallaInicial,
                opciones = PANTALLAS_INICIO,
                etiqueta = ::etiquetaPantallaInicio,
                onValor = onPantallaInicial,
            )
            Text(
                "Se aplica la próxima vez que abras la app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            )
        }

        TarjetaSeccion("Datos", Icons.Filled.Star) {
            FilaDato("Skills", modelo.skills.size.toString())
            FilaDato("Cartas de personaje", modelo.cartasPersonaje.size.toString())
            FilaDato("Cartas de apoyo", modelo.cartasApoyo.size.toString())
            FilaDato("Escenarios", modelo.escenarios.size.toString())
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            FilaAccion(
                titulo = "Fuente de datos",
                subtitulo = "Tablas datamined de GameTora",
                icono = Icons.Filled.Search,
            ) { abrirEnlace(context, URL_GAMETORA) }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            FilaAccion(
                titulo = "Caché de imágenes",
                subtitulo = if (cache == 1) "1 imagen en memoria" else "$cache imágenes en memoria",
                icono = Icons.Filled.Delete,
            ) {
                val borradas = limpiarCacheImagenes()
                cache = imagenesEnCache()
                Toast.makeText(
                    context,
                    if (borradas == 1) "1 imagen borrada" else "$borradas imágenes borradas",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }

        TarjetaSeccion("Acerca de", Icons.Filled.Info) {
            FilaDato("Versión", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            FilaAccion(
                titulo = "Compartir app",
                subtitulo = "Enviar el enlace de descarga",
                icono = Icons.Filled.Share,
            ) { compartirApp(context) }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            FilaAccion(
                titulo = "Buscar actualizaciones",
                subtitulo = "Última versión publicada en GitHub",
                icono = Icons.Filled.Refresh,
            ) { abrirEnlace(context, URL_RELEASES) }
        }

        OutlinedButton(
            onClick = { confirmarRestablecer = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
            Text("Restablecer ajustes")
        }
        Text(
            "Vuelve al tema del sistema, los datos en inglés y la pantalla inicial Skills.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }

    if (confirmarRestablecer) {
        AlertDialog(
            onDismissRequest = { confirmarRestablecer = false },
            title = { Text("¿Restablecer ajustes?") },
            text = { Text("Se borrarán tus preferencias de tema, idioma y pantalla inicial.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRestablecer()
                        confirmarRestablecer = false
                        Toast.makeText(context, "Ajustes restablecidos", Toast.LENGTH_SHORT).show()
                    },
                ) { Text("Restablecer") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarRestablecer = false }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun PestanaReportes() {
    val context = LocalContext.current
    var reportes by remember { mutableStateOf(CrashReporter.listar(context)) }
    var abierto by remember { mutableStateOf<ReporteCrash?>(null) }

    fun refrescar() {
        reportes = CrashReporter.listar(context)
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { refrescar() }) { Text("Actualizar") }
            OutlinedButton(
                onClick = {
                    CrashReporter.reporteDePrueba(context)
                    refrescar()
                },
            ) { Text("Reporte de prueba") }
            if (reportes.isNotEmpty()) {
                TextButton(
                    onClick = {
                        CrashReporter.borrarTodo(context)
                        refrescar()
                    },
                ) { Text("Borrar todos") }
            }
        }
        Text(
            "${reportes.size} reporte(s) guardado(s)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (reportes.isEmpty()) {
            EstadoVacio("No hay crashes registrados. Si la app se cierra sola, el detalle aparecerá acá.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(reportes, key = { it.nombre }) { reporte ->
                    TarjetaReporte(reporte) { abierto = reporte }
                }
            }
        }
    }

    abierto?.let { reporte ->
        DialogoReporte(reporte, onCerrar = { abierto = null })
    }
}

/* ---------- Piezas de la pantalla ---------- */

@Composable
private fun TarjetaSeccion(titulo: String, icono: ImageVector, contenido: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    icono,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            contenido()
        }
    }
}

@Composable
private fun FilaDato(titulo: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(titulo, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(
            valor,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FilaAccion(
    titulo: String,
    subtitulo: String? = null,
    icono: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(titulo, style = MaterialTheme.typography.bodyMedium)
            if (subtitulo != null) {
                Text(
                    subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(
            Icons.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun FilaSegmentada(
    titulo: String,
    opciones: List<String>,
    valor: String,
    etiqueta: (String) -> String,
    onValor: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(titulo, style = MaterialTheme.typography.bodyMedium)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            opciones.forEachIndexed { indice, opcion ->
                SegmentedButton(
                    selected = valor == opcion,
                    onClick = { onValor(opcion) },
                    shape = SegmentedButtonDefaults.itemShape(index = indice, count = opciones.size),
                ) { Text(etiqueta(opcion)) }
            }
        }
    }
}

@Composable
private fun FilaDesplegable(
    titulo: String,
    valor: String,
    opciones: List<String>,
    etiqueta: (String) -> String,
    onValor: (String) -> Unit,
) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { abierto = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(titulo, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(
                etiqueta(valor),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            for (opcion in opciones) {
                DropdownMenuItem(
                    text = { Text(etiqueta(opcion)) },
                    onClick = {
                        onValor(opcion)
                        abierto = false
                    },
                )
            }
        }
    }
}

@Composable
private fun TarjetaReporte(reporte: ReporteCrash, onAbrir: () -> Unit) {
    val resumen = reporte.contenido
        .lineSequence()
        .firstOrNull { it.contains("Exception") || it.contains("Error") }
        ?: "Sin detalle"

    Card(
        onClick = onAbrir,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(reporte.titulo, style = MaterialTheme.typography.titleSmall)
                Text(
                    resumen,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.error,
                )
                Text(
                    "Tocar para ver el detalle completo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DialogoReporte(reporte: ReporteCrash, onCerrar: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Crash ${reporte.titulo}") },
        text = {
            Box(
                Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                SelectionContainer {
                    Text(
                        reporte.contenido,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { compartir(context, reporte.contenido) }) { Text("Compartir") }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Reporte de crash", reporte.contenido))
                    },
                ) { Text("Copiar") }
                TextButton(onClick = onCerrar) { Text("Cerrar") }
            }
        },
    )
}

private fun abrirEnlace(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    runCatching { context.startActivity(intent) }
        .onFailure { Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show() }
}

private fun compartirApp(context: Context) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Cartas Debuff")
        putExtra(Intent.EXTRA_TEXT, "Cartas Debuff, visor de skills y cartas de Uma Musume:\n$URL_RELEASES")
    }
    context.startActivity(Intent.createChooser(intent, "Compartir app"))
}

private fun compartir(context: Context, texto: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Reporte de crash — Cartas Debuff")
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir reporte"))
}
