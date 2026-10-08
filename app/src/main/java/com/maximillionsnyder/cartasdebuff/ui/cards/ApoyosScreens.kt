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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.maximillionsnyder.cartasdebuff.ui.components.ChipTextura
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresApoyoFriend
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresApoyoGroup
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresApoyoGuts
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresApoyoIntelligence
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresApoyoPower
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresApoyoSpeed
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresApoyoStamina
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresTodas
import com.maximillionsnyder.cartasdebuff.ui.components.EstadoVacio
import com.maximillionsnyder.cartasdebuff.ui.components.Estrellas
import com.maximillionsnyder.cartasdebuff.ui.components.FilaApoyo
import com.maximillionsnyder.cartasdebuff.ui.components.FilaSkill
import com.maximillionsnyder.cartasdebuff.ui.components.ImagenRemota
import com.maximillionsnyder.cartasdebuff.ui.components.SeccionTitulo
import com.maximillionsnyder.cartasdebuff.ui.components.SelectorIdioma
import com.maximillionsnyder.cartasdebuff.ui.components.etiquetaObtencion
import com.maximillionsnyder.cartasdebuff.ui.components.etiquetaTipoApoyo

private val TIPOS_APOYO = listOf("speed", "stamina", "power", "guts", "intelligence", "friend", "group")

private val COLORES_TIPO_APOYO = mapOf(
    "speed" to ColoresApoyoSpeed,
    "stamina" to ColoresApoyoStamina,
    "power" to ColoresApoyoPower,
    "guts" to ColoresApoyoGuts,
    "intelligence" to ColoresApoyoIntelligence,
    "friend" to ColoresApoyoFriend,
    "group" to ColoresApoyoGroup,
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

    val consulta = busqueda.trim()
    val filtradas = modelo.cartasApoyo.filter { carta ->
        val coincideTipo = tipoFiltro == "todos" || carta.type == tipoFiltro
        val coincideBusqueda = consulta.isBlank() ||
            carta.name.values.any { it?.contains(consulta, ignoreCase = true) == true } ||
            carta.title.values.any { it?.contains(consulta, ignoreCase = true) == true }
        coincideTipo && coincideBusqueda
    }

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
            ChipTextura(
                texto = "Todas",
                seleccionado = tipoFiltro == "todos",
                colores = ColoresTodas,
                onClick = { tipoFiltro = "todos" },
            )
            for (tipo in TIPOS_APOYO) {
                ChipTextura(
                    texto = etiquetaTipoApoyo(tipo),
                    seleccionado = tipoFiltro == tipo,
                    colores = COLORES_TIPO_APOYO.getValue(tipo),
                    onClick = { tipoFiltro = tipo },
                )
            }
        }
        Text(
            "${filtradas.size} de ${modelo.cartasApoyo.size} cartas",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        if (filtradas.isEmpty()) {
            EstadoVacio("Sin resultados")
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
            BarraDetalle("Carta $supportId", onVolver)
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
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
                Estrellas(carta.rarity)
                Text(
                    "${etiquetaTipoApoyo(carta.type)} · ID ${carta.supportId} · ${etiquetaObtencion(carta.obtained)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
