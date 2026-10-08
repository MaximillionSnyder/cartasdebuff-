package com.maximillionsnyder.cartasdebuff.ui.skills

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.cartasdebuff.domain.EvoOrigen
import com.maximillionsnyder.cartasdebuff.domain.Modelo
import com.maximillionsnyder.cartasdebuff.domain.Skill
import com.maximillionsnyder.cartasdebuff.domain.VersionGen
import com.maximillionsnyder.cartasdebuff.domain.etiquetaIdioma
import com.maximillionsnyder.cartasdebuff.domain.texto
import com.maximillionsnyder.cartasdebuff.ui.components.BarraDetalle
import com.maximillionsnyder.cartasdebuff.ui.components.EstadoVacio
import com.maximillionsnyder.cartasdebuff.ui.components.Etiqueta
import com.maximillionsnyder.cartasdebuff.ui.components.EtiquetaRareza
import com.maximillionsnyder.cartasdebuff.ui.components.FilaApoyo
import com.maximillionsnyder.cartasdebuff.ui.components.FilaPersonaje
import com.maximillionsnyder.cartasdebuff.ui.components.ImagenRemota
import com.maximillionsnyder.cartasdebuff.ui.components.SeccionTitulo
import com.maximillionsnyder.cartasdebuff.ui.components.etiquetaTipo

@Composable
fun SkillDetalleScreen(
    modelo: Modelo,
    skillId: Int,
    idioma: String,
    onVolver: () -> Unit,
    onAbrirPersonaje: (Int) -> Unit,
    onAbrirApoyo: (Int) -> Unit,
) {
    val skill = modelo.skill(skillId)
    if (skill == null) {
        Column(Modifier.fillMaxSize()) {
            BarraDetalle("Skill $skillId", onVolver)
            EstadoVacio("No se encontró la skill")
        }
        return
    }

    Column(Modifier.fillMaxSize()) {
        BarraDetalle(skill.nombre(idioma), onVolver)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { EncabezadoSkill(skill, idioma) }
            if (skill.types.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        for (tipo in skill.types) Etiqueta(etiquetaTipo(tipo))
                    }
                }
            }
            val fuentes = skill.sources
            if (fuentes.characterCards.isNotEmpty()) {
                item { SeccionTitulo("Cartas de personaje", fuentes.characterCards.size) }
                items(fuentes.characterCards, key = { "carta-${it.cardId}" }) { fuente ->
                    val carta = modelo.cartaPersonaje(fuente.cardId)
                    if (carta != null) {
                        FilaPersonaje(carta, idioma, fuente.kinds) { onAbrirPersonaje(fuente.cardId) }
                    } else {
                        FilaReferencia("Carta de personaje ${fuente.cardId}")
                    }
                }
            }
            item { SeccionTitulo("Descripciones") }
            item { TarjetaDescripciones(skill) }
            if (skill.conditions.isNotEmpty()) {
                item { SeccionTitulo("Condiciones", skill.conditions.size) }
                items(skill.conditions.size) { indice ->
                    TarjetaCondicion(
                        skill.conditions[indice].precondition,
                        skill.conditions[indice].condition,
                        skill.conditions[indice].baseTime,
                        skill.conditions[indice].effects.map { "Efecto ${it.type}: ${if (it.value >= 0) "+" else ""}${it.value}" },
                    )
                }
            }
            val gen = skill.geneVersion
            if (gen != null) {
                item { SeccionTitulo("Versión gen") }
                item { TarjetaGen(gen, idioma) }
            }
            val evolucion = skill.evolution
            if (evolucion.preEvo != null || evolucion.evo.isNotEmpty()) {
                item { SeccionTitulo("Evolución") }
                item { TarjetaEvolucion(evolucion.preEvo, evolucion.evo, modelo, idioma) }
            }

            if (fuentes.characterEvents.isNotEmpty()) {
                item { SeccionTitulo("Eventos de personaje", fuentes.characterEvents.size) }
                items(fuentes.characterEvents, key = { "evento-$it" }) { cardId ->
                    val carta = modelo.cartaPersonaje(cardId)
                    if (carta != null) {
                        FilaPersonaje(carta, idioma, null) { onAbrirPersonaje(cardId) }
                    } else {
                        FilaReferencia("Carta de personaje $cardId")
                    }
                }
            }
            if (fuentes.supportHints.isNotEmpty()) {
                item { SeccionTitulo("Hints de cartas de apoyo", fuentes.supportHints.size) }
                items(fuentes.supportHints, key = { "hint-$it" }) { supportId ->
                    val carta = modelo.cartaApoyo(supportId)
                    if (carta != null) {
                        FilaApoyo(carta, idioma, "Hint") { onAbrirApoyo(supportId) }
                    } else {
                        FilaReferencia("Carta de apoyo $supportId")
                    }
                }
            }
            if (fuentes.supportEvents.isNotEmpty()) {
                item { SeccionTitulo("Eventos de cartas de apoyo", fuentes.supportEvents.size) }
                items(fuentes.supportEvents, key = { "evento-apoyo-$it" }) { supportId ->
                    val carta = modelo.cartaApoyo(supportId)
                    if (carta != null) {
                        FilaApoyo(carta, idioma, "Evento") { onAbrirApoyo(supportId) }
                    } else {
                        FilaReferencia("Carta de apoyo $supportId")
                    }
                }
            }
            if (fuentes.scenarioEvents.isNotEmpty()) {
                item { SeccionTitulo("Eventos de escenario", fuentes.scenarioEvents.size) }
                items(fuentes.scenarioEvents, key = { "escenario-$it" }) { escenarioId ->
                    FilaReferencia(modelo.escenario(escenarioId)?.nombre(idioma) ?: "Escenario $escenarioId")
                }
            }
        }
    }
}

@Composable
private fun EncabezadoSkill(skill: Skill, idioma: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp)) {
            ImagenRemota(skill.urlIcono, null, Modifier.size(64.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(skill.nombre(idioma), style = MaterialTheme.typography.titleMedium)
                val japones = skill.name["ja"]
                if (!japones.isNullOrBlank()) {
                    Text(japones, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    EtiquetaRareza(skill.rarity)
                    Text("ID ${skill.id}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (skill.cost != null) {
                        Text("Coste ${skill.cost}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (skill.unreleased.isNotEmpty()) {
                    Text(
                        "Sin lanzar en: ${skill.unreleased.joinToString(", ")}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaDescripciones(skill: Skill) {
    var expandido by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BloqueIdioma(skill, "en")
            AnimatedVisibility(visible = expandido, enter = expandVertically(), exit = shrinkVertically()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (id in listOf("ja", "ko", "tw")) {
                        HorizontalDivider()
                        BloqueIdioma(skill, id)
                    }
                }
            }
            TextButton(onClick = { expandido = !expandido }) {
                Text(if (expandido) "Ocultar otros idiomas" else "Ver más idiomas (日本語 · 한국어 · 中文)")
            }
        }
    }
}

@Composable
private fun BloqueIdioma(skill: Skill, id: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(etiquetaIdioma(id), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(skill.nombre(id), style = MaterialTheme.typography.titleSmall)
        Text(
            skill.descripcion(id).ifBlank { "—" },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TarjetaCondicion(
    precondition: String?,
    condition: String?,
    baseTime: Long?,
    efectos: List<String>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (precondition != null) {
                Text("Requisito: $precondition", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            }
            if (condition != null) {
                Text("Condición: $condition", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            }
            if (baseTime != null && baseTime > 0) {
                Text(
                    "Duración base: $baseTime ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            for (efecto in efectos) Text(efecto, style = MaterialTheme.typography.bodySmall)
            if (precondition == null && condition == null && efectos.isEmpty()) {
                Text("—", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun TarjetaGen(gen: VersionGen, idioma: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(gen.name.texto(idioma) ?: "Versión gen", style = MaterialTheme.typography.titleSmall)
            Text(
                gen.desc.texto(idioma).orEmpty().ifBlank { "—" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (gen.id != null) {
                    Text("ID ${gen.id}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (gen.cost != null) {
                    Text("Coste ${gen.cost}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun TarjetaEvolucion(preEvo: EvoOrigen?, evos: List<EvoOrigen>, modelo: Modelo, idioma: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (preEvo != null) {
                Text("Evoluciona desde la skill ${preEvo.old}", style = MaterialTheme.typography.bodySmall)
                Text(origen(preEvo, modelo, idioma), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            for (evo in evos) {
                Text("→ ${evo.evos.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                Text(origen(evo, modelo, idioma), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun origen(evo: EvoOrigen, modelo: Modelo, idioma: String): String = when {
    evo.cardId != null -> "Carta: ${modelo.cartaPersonaje(evo.cardId)?.nombre(idioma) ?: evo.cardId}"
    evo.scenarioId != null -> "Escenario: ${modelo.escenario(evo.scenarioId)?.nombre(idioma) ?: evo.scenarioId}"
    else -> "—"
}

@Composable
private fun FilaReferencia(texto: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Text(
            texto,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
