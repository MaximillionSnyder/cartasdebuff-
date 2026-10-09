package com.maximillionsnyder.cartasdebuff.ui.cards

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.cartasdebuff.domain.CartaApoyo
import com.maximillionsnyder.cartasdebuff.domain.Modelo
import com.maximillionsnyder.cartasdebuff.ui.components.BarraDetalle
import com.maximillionsnyder.cartasdebuff.ui.components.TarjetaApp
import com.maximillionsnyder.cartasdebuff.ui.components.ChipFiltro
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresEstrella1
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresEstrella2
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresEstrella3
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresTodas
import com.maximillionsnyder.cartasdebuff.ui.components.EstadoVacio
import com.maximillionsnyder.cartasdebuff.ui.components.EstadoVacioBusqueda
import com.maximillionsnyder.cartasdebuff.ui.components.Estrellas
import com.maximillionsnyder.cartasdebuff.ui.components.EtiquetaTipoApoyo
import com.maximillionsnyder.cartasdebuff.ui.components.FilaApoyo
import com.maximillionsnyder.cartasdebuff.ui.components.FilaSkill
import com.maximillionsnyder.cartasdebuff.ui.components.ImagenRemota
import com.maximillionsnyder.cartasdebuff.ui.components.SeccionTitulo
import com.maximillionsnyder.cartasdebuff.ui.components.SelectorIdioma
import com.maximillionsnyder.cartasdebuff.ui.components.TIPOS_APOYO
import com.maximillionsnyder.cartasdebuff.ui.components.coloresTipoApoyo
import com.maximillionsnyder.cartasdebuff.ui.components.etiquetaObtencion
import com.maximillionsnyder.cartasdebuff.ui.components.etiquetaTipoApoyo

private val CHIPS_ESTRELLAS = listOf(
    Triple(0, "Todas", ColoresTodas),
    Triple(3, "3★", ColoresEstrella3),
    Triple(2, "2★", ColoresEstrella2),
    Triple(1, "1★", ColoresEstrella1),
)

@Composable
fun ApoyosScreen(
    modelo: Modelo,
    idioma: String,
    onIdioma: (String) -> Unit,
    onAbrirCarta: (Int) -> Unit,
) {
    var busqueda by rememberSaveable { mutableStateOf("") }
    var tipoFiltro by rememberSaveable { mutableStateOf("todos") }
    var estrellas by rememberSaveable { mutableStateOf(0) }
    var descendente by rememberSaveable { mutableStateOf(false) }

    val filtradas = filtrarApoyos(
        modelo.cartasApoyo,
        FiltroApoyos(
            consulta = busqueda,
            tipo = tipoFiltro,
            estrellas = estrellas,
            descendente = descendente,
        ),
    )

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Cartas de apoyo", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            SelectorIdioma(idioma, onIdioma)
        }
        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            placeholder = { Text("Buscar por nombre o título") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChipFiltro(
                texto = "Todas",
                seleccionado = tipoFiltro == "todos",
                colores = ColoresTodas,
                onClick = { tipoFiltro = "todos" },
            )
            for (tipo in TIPOS_APOYO) {
                ChipFiltro(
                    texto = etiquetaTipoApoyo(tipo),
                    seleccionado = tipoFiltro == tipo,
                    colores = coloresTipoApoyo(tipo),
                    onClick = { tipoFiltro = if (tipoFiltro == tipo) "todos" else tipo },
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for ((valor, etiqueta, colores) in CHIPS_ESTRELLAS) {
                ChipFiltro(
                    texto = etiqueta,
                    seleccionado = estrellas == valor,
                    colores = colores,
                    onClick = { estrellas = if (estrellas == valor) 0 else valor },
                )
            }
            ChipFiltro(
                texto = if (descendente) "↓ 3★ → 1★" else "↑ 1★ → 3★",
                seleccionado = true,
                colores = ColoresTodas,
                onClick = { descendente = !descendente },
            )
        }
        Text(
            "${filtradas.size} de ${modelo.cartasApoyo.size} cartas",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        if (filtradas.isEmpty()) {
            EstadoVacioBusqueda()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filtradas, key = { it.supportId }) { carta ->
                    FilaApoyo(carta, idioma) { onAbrirCarta(carta.supportId) }
                }
            }
        }
    }
}

@Composable
fun ApoyoDetalleScreen(
    modelo: Modelo,
    supportId: Int,
    idioma: String,
    onVolver: () -> Unit,
    onAbrirSkill: (Int) -> Unit,
) {
    val carta = modelo.cartaApoyo(supportId)
    if (carta == null) {
        Column(Modifier.fillMaxSize()) {
            BarraDetalle("Carta de apoyo", onVolver)
            EstadoVacio("No se encontró la carta")
        }
        return
    }

    val skills = modelo.skillsPorApoyo[supportId].orEmpty().distinct()
    val hints = skills.filter { supportId in it.sources.supportHints }
    val eventos = skills.filter { supportId in it.sources.supportEvents }

    Column(Modifier.fillMaxSize()) {
        BarraDetalle(carta.nombre(idioma), onVolver)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { EncabezadoCartaApoyo(carta, idioma) }
            if (hints.isNotEmpty()) {
                item(key = "titulo-hints") { SeccionTitulo("Skills por hint", hints.size) }
                items(hints, key = { "hint-${it.id}" }) { skill ->
                    FilaSkill(skill, idioma) { onAbrirSkill(skill.id) }
                }
            }
            if (eventos.isNotEmpty()) {
                item(key = "titulo-eventos") { SeccionTitulo("Skills por evento", eventos.size) }
                items(eventos, key = { "evento-${it.id}" }) { skill ->
                    FilaSkill(skill, idioma) { onAbrirSkill(skill.id) }
                }
            }
            if (skills.isEmpty()) {
                item { EstadoVacio("Sin skills registradas para esta carta") }
            }
        }
    }
}

@Composable
private fun EncabezadoCartaApoyo(carta: CartaApoyo, idioma: String) {
    TarjetaApp(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(12.dp)) {
            ImagenRemota(carta.image, null, Modifier.size(96.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(carta.nombre(idioma), style = MaterialTheme.typography.titleMedium)
                val japones = carta.name["ja"]
                if (idioma != "ja" && !japones.isNullOrBlank()) {
                    Text(japones, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                val titulo = carta.titulo(idioma)
                if (titulo.isNotBlank()) {
                    Text(titulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Estrellas(carta.rarity)
                    EtiquetaTipoApoyo(carta.type)
                }
                Text(
                    etiquetaObtencion(carta.obtained),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
