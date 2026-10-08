package com.maximillionsnyder.cartasdebuff.ui.skills

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.cartasdebuff.domain.Modelo
import com.maximillionsnyder.cartasdebuff.ui.components.EstadoVacio
import com.maximillionsnyder.cartasdebuff.ui.components.FilaSkill
import com.maximillionsnyder.cartasdebuff.ui.components.SelectorIdioma

private val FUENTES = listOf(
    "char" to "Personaje",
    "char_e" to "Evento personaje",
    "sup_hint" to "Hint apoyo",
    "sup_e" to "Evento apoyo",
    "sce_e" to "Escenario",
)

@Composable
fun SkillsScreen(
    modelo: Modelo,
    idioma: String,
    onIdioma: (String) -> Unit,
    onAbrirSkill: (Int) -> Unit,
) {
    var busqueda by rememberSaveable { mutableStateOf("") }
    var rarezaFiltro by rememberSaveable { mutableStateOf("todas") }
    var soloDebuff by rememberSaveable { mutableStateOf(false) }
    var ocultarSinEn by rememberSaveable { mutableStateOf(false) }
    val fuentes = remember { mutableStateListOf<String>() }

    val consulta = busqueda.trim()
    val filtradas = modelo.skills.filter { skill ->
        val coincideBusqueda = consulta.isBlank() ||
            skill.id.toString() == consulta ||
            skill.name.values.any { it?.contains(consulta, ignoreCase = true) == true }
        val coincideRareza = when (rarezaFiltro) {
            "normal" -> skill.rarity == 1
            "rara" -> skill.rarity == 2
            "unica" -> skill.rarity == 3 || skill.rarity == 5
            "evolucion" -> skill.rarity == 4 || skill.rarity == 6
            else -> true
        }
        val coincideDebuff = !soloDebuff || "dbf" in skill.types
        val coincideLanzamiento = !ocultarSinEn || "en" !in skill.unreleased
        val coincideFuente = fuentes.isEmpty() || fuentes.any { fuente ->
            when (fuente) {
                "char" -> skill.sources.characterCards.isNotEmpty()
                "char_e" -> skill.sources.characterEvents.isNotEmpty()
                "sup_hint" -> skill.sources.supportHints.isNotEmpty()
                "sup_e" -> skill.sources.supportEvents.isNotEmpty()
                "sce_e" -> skill.sources.scenarioEvents.isNotEmpty()
                else -> true
            }
        }
        coincideBusqueda && coincideRareza && coincideDebuff && coincideLanzamiento && coincideFuente
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Skills", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            SelectorIdioma(idioma, onIdioma)
        }
        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            placeholder = { Text("Buscar por nombre o ID") },
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
            listOf(
                "todas" to "Todas",
                "normal" to "Normal",
                "rara" to "Rara",
                "unica" to "Única",
                "evolucion" to "Evolución",
            ).forEach { (id, etiqueta) ->
                FilterChip(
                    selected = rarezaFiltro == id,
                    onClick = { rarezaFiltro = id },
                    label = { Text(etiqueta) },
                )
            }
            FilterChip(
                selected = soloDebuff,
                onClick = { soloDebuff = !soloDebuff },
                label = { Text("Debuff") },
            )
            FilterChip(
                selected = ocultarSinEn,
                onClick = { ocultarSinEn = !ocultarSinEn },
                label = { Text("Ocultar sin EN") },
            )
            FUENTES.forEach { (id, etiqueta) ->
                FilterChip(
                    selected = id in fuentes,
                    onClick = { if (id in fuentes) fuentes.remove(id) else fuentes.add(id) },
                    label = { Text(etiqueta) },
                )
            }
        }
        Text(
            "${filtradas.size} de ${modelo.skills.size} skills",
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
                items(filtradas, key = { it.id }) { skill ->
                    FilaSkill(skill, idioma, onClick = { onAbrirSkill(skill.id) })
                }
            }
        }
    }
}
