package com.maximillionsnyder.cartasdebuff.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.cartasdebuff.BuildConfig
import com.maximillionsnyder.cartasdebuff.crash.CrashReporter
import com.maximillionsnyder.cartasdebuff.crash.ReporteCrash
import com.maximillionsnyder.cartasdebuff.domain.Modelo
import com.maximillionsnyder.cartasdebuff.domain.etiquetaIdioma
import com.maximillionsnyder.cartasdebuff.ui.components.EstadoVacio
import com.maximillionsnyder.cartasdebuff.ui.components.SelectorIdioma

@Composable
fun AjustesScreen(
    modelo: Modelo,
    idioma: String,
    onIdioma: (String) -> Unit,
) {
    var pestana by rememberSaveable { mutableStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        Text(
            "Ajustes",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
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
                text = { Text("Reportes de crash") },
            )
        }
        when (pestana) {
            0 -> PestanaPreferencias(modelo, idioma, onIdioma)
            else -> PestanaReportes()
        }
    }
}

@Composable
private fun PestanaPreferencias(modelo: Modelo, idioma: String, onIdioma: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Tarjeta("Idioma de los datos") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Mostrar nombres y descripciones en:", Modifier.weight(1f))
                SelectorIdioma(idioma, onIdioma)
            }
            Text(
                "Idioma actual: ${etiquetaIdioma(idioma)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Tarjeta("Datos cargados") {
            Dato("Skills", modelo.skills.size.toString())
            Dato("Cartas de personaje", modelo.cartasPersonaje.size.toString())
            Dato("Cartas de apoyo", modelo.cartasApoyo.size.toString())
            Dato("Escenarios", modelo.escenarios.size.toString())
            Text(
                "Fuente: tablas datamined de GameTora (manifiesto público).",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Tarjeta("Aplicación") {
            Dato("Versión", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            Dato("Paquete", BuildConfig.APPLICATION_ID)
        }
        Tarjeta("Reportes de crash") {
            Text(
                "Si la app se cierra inesperadamente, el detalle queda guardado en la pestaña " +
                    "\"Reportes de crash\" para que puedas revisarlo o compartirlo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Tarjeta(titulo: String, contenido: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            contenido()
        }
    }
}

@Composable
private fun Dato(nombre: String, valor: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(nombre, style = MaterialTheme.typography.bodyMedium)
        Text(
            valor,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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

@Composable
private fun TarjetaReporte(reporte: ReporteCrash, onAbrir: () -> Unit) {
    val resumen = reporte.contenido
        .lineSequence()
        .firstOrNull { it.contains("Exception") || it.contains("Error") }
        ?: "Sin detalle"

    Card(
        onClick = onAbrir,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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

private fun compartir(context: Context, texto: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Reporte de crash — Cartas Debuff")
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir reporte"))
}
