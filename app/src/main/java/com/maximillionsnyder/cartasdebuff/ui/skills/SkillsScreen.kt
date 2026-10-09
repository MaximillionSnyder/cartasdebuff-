package com.maximillionsnyder.cartasdebuff.ui.skills

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.maximillionsnyder.cartasdebuff.domain.Modelo
import com.maximillionsnyder.cartasdebuff.ui.components.ChipFiltro
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresAceleracion
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresDebuff
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresEscenario
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresEventoApoyo
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresEventoPersonaje
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresEvolucion
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresHint
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresNormal
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresOcultar
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresPersonaje
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresRara
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresTodas
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresUnica
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresVelocidad
import com.maximillionsnyder.cartasdebuff.ui.components.EstadoVacioBusqueda
import com.maximillionsnyder.cartasdebuff.ui.components.FilaSkill
import com.maximillionsnyder.cartasdebuff.ui.components.SelectorIdioma

private val FUENTES = listOf(
    Triple("char", "Personaje", ColoresPersonaje),
    Triple("char_e", "Evento personaje", ColoresEventoPersonaje),
    Triple("sup_hint", "Hint apoyo", ColoresHint),
    Triple("sup_e", "Evento apoyo", ColoresEventoApoyo),
    Triple("sce_e", "Escenario", ColoresEscenario),
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
    var soloVelocidad by rememberSaveable { mutableStateOf(false) }
    var soloAceleracion by rememberSaveable { mutableStateOf(false) }
    var ocultarSinEn by rememberSaveable { mutableStateOf(false) }
    var fuentes by rememberSaveable { mutableStateOf(listOf<String>()) }

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
        val coincideEfecto = (!soloVelocidad || skill.daVelocidad) && (!soloAceleracion || skill.daAceleracion)
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
        coincideBusqueda && coincideRareza && coincideDebuff && coincideEfecto && coincideLanzamiento && coincideFuente
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
            placeholder = { Text("Buscar por nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    Triple("todas", "Todas", ColoresTodas),
                    Triple("normal", "Normal", ColoresNormal),
                    Triple("rara", "Rara", ColoresRara),
                    Triple("unica", "Única", ColoresUnica),
                    Triple("evolucion", "Evolución", ColoresEvolucion),
                ).forEach { (id, etiqueta, colores) ->
                    ChipFiltro(
                        texto = etiqueta,
                        seleccionado = rarezaFiltro == id,
                        colores = colores,
                        onClick = { rarezaFiltro = if (rarezaFiltro == id) "todas" else id },
                    )
                }
                ChipFiltro(
                    texto = "Debuff",
                    seleccionado = soloDebuff,
                    colores = ColoresDebuff,
                    onClick = { soloDebuff = !soloDebuff },
                )
                ChipFiltro(
                    texto = "Velocidad",
                    seleccionado = soloVelocidad,
                    colores = ColoresVelocidad,
                    onClick = { soloVelocidad = !soloVelocidad },
                )
                ChipFiltro(
                    texto = "Aceleración",
                    seleccionado = soloAceleracion,
                    colores = ColoresAceleracion,
                    onClick = { soloAceleracion = !soloAceleracion },
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for ((id, etiqueta, colores) in FUENTES) {
                    ChipFiltro(
                        texto = etiqueta,
                        seleccionado = id in fuentes,
                        colores = colores,
                        onClick = { fuentes = if (id in fuentes) fuentes - id else fuentes + id },
                    )
                }
                ChipFiltro(
                    texto = "Ocultar sin EN",
                    seleccionado = ocultarSinEn,
                    colores = ColoresOcultar,
                    onClick = { ocultarSinEn = !ocultarSinEn },
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
            EstadoVacioBusqueda()
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
