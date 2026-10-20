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
import com.maximillionsnyder.cartasdebuff.domain.VISTAS
import com.maximillionsnyder.cartasdebuff.domain.etiquetaVista
import com.maximillionsnyder.cartasdebuff.domain.etiquetaIdioma
import com.maximillionsnyder.cartasdebuff.domain.etiquetaPantallaInicio
import com.maximillionsnyder.cartasdebuff.domain.etiquetaTema
import com.maximillionsnyder.cartasdebuff.ui.components.EstadoVacio
import com.maximillionsnyder.cartasdebuff.ui.components.TarjetaApp
import com.maximillionsnyder.cartasdebuff.ui.components.imagenesEnCache
import com.maximillionsnyder.cartasdebuff.ui.components.imagenesEnDisco
import com.maximillionsnyder.cartasdebuff.ui.components.limpiarCacheImagenes
import com.maximillionsnyder.cartasdebuff.ui.i18n.TX_UI
import com.maximillionsnyder.cartasdebuff.ui.i18n.t
import com.maximillionsnyder.cartasdebuff.ui.i18n.textoUi

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
    vista: String,
    onVista: (String) -> Unit,
    onRestablecer: () -> Unit,
) {
    var pestana by rememberSaveable { mutableStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        Text(
            t(TX_UI, "tab_ajustes"),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
        )
        Text(
            t(TX_UI, "ajustes_subtitulo"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 2.dp),
        )
        PrimaryTabRow(selectedTabIndex = pestana, modifier = Modifier.padding(top = 8.dp)) {
            Tab(
                selected = pestana == 0,
                onClick = { pestana = 0 },
                text = { Text(t(TX_UI, "ajustes_pestana_preferencias")) },
            )
            Tab(
                selected = pestana == 1,
                onClick = { pestana = 1 },
                text = { Text(t(TX_UI, "ajustes_pestana_reportes")) },
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
                vista = vista,
                onVista = onVista,
                onRestablecer = onRestablecer,
            )
            else -> PestanaReportes(idioma)
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
    vista: String,
    onVista: (String) -> Unit,
    onRestablecer: () -> Unit,
) {
    val context = LocalContext.current
    var cache by remember { mutableStateOf(imagenesEnCache()) }
    var enDisco by remember { mutableStateOf(imagenesEnDisco(context)) }
    var confirmarRestablecer by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TarjetaSeccion(t(TX_UI, "ajustes_apariencia"), Icons.Filled.Settings) {
            FilaSegmentada(
                titulo = t(TX_UI, "ajustes_tema"),
                opciones = TEMAS,
                valor = tema,
                etiqueta = { etiquetaTema(it, idioma) },
                onValor = onTema,
            )
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            FilaSegmentada(
                titulo = t(TX_UI, "ajustes_listas"),
                opciones = VISTAS,
                valor = vista,
                etiqueta = { etiquetaVista(it, idioma) },
                onValor = onVista,
            )
            Text(
                t(TX_UI, "ajustes_listas_desc"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            )
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            FilaDesplegable(
                titulo = t(TX_UI, "ajustes_idioma_datos"),
                valor = idioma,
                opciones = IDIOMAS,
                etiqueta = ::etiquetaIdioma,
                onValor = onIdioma,
            )
        }

        TarjetaSeccion(t(TX_UI, "ajustes_inicio"), Icons.Filled.Home) {
            FilaDesplegable(
                titulo = t(TX_UI, "ajustes_pantalla_inicial"),
                valor = pantallaInicial,
                opciones = PANTALLAS_INICIO,
                etiqueta = { etiquetaPantallaInicio(it, idioma) },
                onValor = onPantallaInicial,
            )
            Text(
                t(TX_UI, "ajustes_pantalla_inicial_desc"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            )
        }

        TarjetaSeccion(t(TX_UI, "ajustes_datos"), Icons.Filled.Star) {
            FilaDato(t(TX_UI, "tab_skills"), modelo.skills.size.toString())
            FilaDato(t(TX_UI, "datos_cartas_personaje"), modelo.cartasPersonaje.size.toString())
            FilaDato(t(TX_UI, "datos_cartas_apoyo"), modelo.cartasApoyo.size.toString())
            FilaDato(t(TX_UI, "datos_escenarios"), modelo.escenarios.size.toString())
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            FilaAccion(
                titulo = t(TX_UI, "ajustes_fuente_datos"),
                subtitulo = t(TX_UI, "ajustes_fuente_datos_desc"),
                icono = Icons.Filled.Search,
            ) { abrirEnlace(context, URL_GAMETORA, idioma) }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            FilaAccion(
                titulo = t(TX_UI, "ajustes_cache"),
                subtitulo = t(TX_UI, "ajustes_cache_desc", cache, enDisco),
                icono = Icons.Filled.Delete,
            ) {
                val borradas = limpiarCacheImagenes(context)
                cache = imagenesEnCache()
                enDisco = imagenesEnDisco(context)
                Toast.makeText(
                    context,
                    if (borradas == 1) {
                        textoUi("cache_borrada_una", idioma)
                    } else {
                        textoUi("cache_borradas_varias", idioma, borradas)
                    },
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }

        TarjetaSeccion(t(TX_UI, "ajustes_acerca"), Icons.Filled.Info) {
            FilaDato(t(TX_UI, "ajustes_version"), "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            FilaAccion(
                titulo = t(TX_UI, "ajustes_compartir_app"),
                subtitulo = t(TX_UI, "ajustes_compartir_app_desc"),
                icono = Icons.Filled.Share,
            ) { compartirApp(context, idioma) }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            FilaAccion(
                titulo = t(TX_UI, "ajustes_buscar_actualizaciones"),
                subtitulo = t(TX_UI, "ajustes_buscar_actualizaciones_desc"),
                icono = Icons.Filled.Refresh,
            ) { abrirEnlace(context, URL_RELEASES, idioma) }
        }

        OutlinedButton(
            onClick = { confirmarRestablecer = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
            Text(t(TX_UI, "ajustes_restablecer"))
        }
        Text(
            t(TX_UI, "ajustes_restablecer_desc"),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }

    if (confirmarRestablecer) {
        AlertDialog(
            onDismissRequest = { confirmarRestablecer = false },
            title = { Text(t(TX_UI, "ajustes_restablecer_pregunta")) },
            text = { Text(t(TX_UI, "ajustes_restablecer_detalle")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRestablecer()
                        confirmarRestablecer = false
                        Toast.makeText(context, textoUi("ajustes_restablecidos", idioma), Toast.LENGTH_SHORT).show()
                    },
                ) { Text(t(TX_UI, "ajustes_restablecer_confirmar")) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarRestablecer = false }) { Text(t(TX_UI, "cancelar")) }
            },
        )
    }
}

@Composable
private fun PestanaReportes(idioma: String) {
    val context = LocalContext.current
    var reportes by remember { mutableStateOf(CrashReporter.listar(context)) }
    var abierto by remember { mutableStateOf<ReporteCrash?>(null) }

    fun refrescar() {
        reportes = CrashReporter.listar(context)
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { refrescar() }) { Text(t(TX_UI, "reportes_actualizar")) }
            OutlinedButton(
                onClick = {
                    CrashReporter.reporteDePrueba(context)
                    refrescar()
                },
            ) { Text(t(TX_UI, "reportes_prueba")) }
            if (reportes.isNotEmpty()) {
                TextButton(
                    onClick = {
                        CrashReporter.borrarTodo(context)
                        refrescar()
                    },
                ) { Text(t(TX_UI, "reportes_borrar_todos")) }
            }
        }
        Text(
            t(TX_UI, "reportes_contador", reportes.size),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (reportes.isEmpty()) {
            EstadoVacio(t(TX_UI, "reportes_vacio"))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(reportes, key = { it.nombre }) { reporte ->
                    TarjetaReporte(reporte, idioma) { abierto = reporte }
                }
            }
        }
    }

    abierto?.let { reporte ->
        DialogoReporte(reporte, idioma, onCerrar = { abierto = null })
    }
}

/* ---------- Piezas de la pantalla ---------- */

@Composable
private fun TarjetaSeccion(titulo: String, icono: ImageVector, contenido: @Composable () -> Unit) {
    TarjetaApp(modifier = Modifier.fillMaxWidth()) {
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
private fun TarjetaReporte(reporte: ReporteCrash, idioma: String, onAbrir: () -> Unit) {
    val resumen = reporte.contenido
        .lineSequence()
        .firstOrNull { it.contains("Exception") || it.contains("Error") }
        ?: textoUi("reportes_sin_detalle", idioma)

    TarjetaApp(onClick = onAbrir, modifier = Modifier.fillMaxWidth()) {
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
                    t(TX_UI, "reportes_tocar_detalle"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DialogoReporte(reporte: ReporteCrash, idioma: String, onCerrar: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(t(TX_UI, "reportes_dialogo_titulo", reporte.titulo)) },
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
            TextButton(onClick = { compartir(context, reporte.contenido, idioma) }) {
                Text(t(TX_UI, "compartir"))
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val etiqueta = textoUi("reportes_portapapeles", idioma)
                        clipboard.setPrimaryClip(ClipData.newPlainText(etiqueta, reporte.contenido))
                    },
                ) { Text(t(TX_UI, "copiar")) }
                TextButton(onClick = onCerrar) { Text(t(TX_UI, "cerrar")) }
            }
        },
    )
}

private fun abrirEnlace(context: Context, url: String, idioma: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    runCatching { context.startActivity(intent) }
        .onFailure {
            Toast.makeText(context, textoUi("enlace_no_abierto", idioma), Toast.LENGTH_SHORT).show()
        }
}

private fun compartirApp(context: Context, idioma: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Cartas Debuff")
        putExtra(Intent.EXTRA_TEXT, textoUi("compartir_app_texto", idioma, URL_RELEASES))
    }
    val titulo = textoUi("ajustes_compartir_app", idioma)
    context.startActivity(Intent.createChooser(intent, titulo))
}

private fun compartir(context: Context, texto: String, idioma: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, textoUi("compartir_reporte_asunto", idioma))
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    val titulo = textoUi("compartir_reporte", idioma)
    context.startActivity(Intent.createChooser(intent, titulo))
}
