package com.maximillionsnyder.cartasdebuff.ui.skills

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.maximillionsnyder.cartasdebuff.domain.EvoOrigen
import com.maximillionsnyder.cartasdebuff.domain.IDIOMAS
import com.maximillionsnyder.cartasdebuff.domain.Efecto
import com.maximillionsnyder.cartasdebuff.R
import com.maximillionsnyder.cartasdebuff.domain.Modelo
import com.maximillionsnyder.cartasdebuff.domain.Skill
import com.maximillionsnyder.cartasdebuff.domain.VersionGen
import com.maximillionsnyder.cartasdebuff.domain.duracionEnSegundos
import com.maximillionsnyder.cartasdebuff.domain.etiquetaIdioma
import com.maximillionsnyder.cartasdebuff.domain.texto
import com.maximillionsnyder.cartasdebuff.domain.leerCondiciones
import com.maximillionsnyder.cartasdebuff.ui.components.BarraDetalle
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresEfectoBaja
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresEfectoSube
import com.maximillionsnyder.cartasdebuff.ui.components.TarjetaApp
import com.maximillionsnyder.cartasdebuff.ui.components.EstadoVacio
import com.maximillionsnyder.cartasdebuff.ui.components.Etiqueta
import com.maximillionsnyder.cartasdebuff.ui.components.EtiquetaRareza
import com.maximillionsnyder.cartasdebuff.ui.components.FilaApoyo
import com.maximillionsnyder.cartasdebuff.ui.components.FilaPersonaje
import com.maximillionsnyder.cartasdebuff.ui.components.ImagenRemota
import com.maximillionsnyder.cartasdebuff.ui.components.ListaConArriba
import com.maximillionsnyder.cartasdebuff.ui.components.SeccionTitulo
import com.maximillionsnyder.cartasdebuff.ui.components.etiquetaTipo

@Composable
fun SkillDetalleScreen(
    modelo: Modelo,
    skillId: Int,
    idioma: String,
    favorito: Boolean,
    onFavorito: (Int) -> Unit,
    onVolver: () -> Unit,
    onAbrirPersonaje: (Int) -> Unit,
    onAbrirApoyo: (Int) -> Unit,
) {
    val skill = modelo.skill(skillId)
    if (skill == null) {
        Column(Modifier.fillMaxSize()) {
            BarraDetalle("Skill", onVolver)
            EstadoVacio("No se encontró la skill")
        }
        return
    }

    Column(Modifier.fillMaxSize()) {
        BarraDetalle(skill.nombre(idioma), onVolver) {
            IconButton(onClick = { onFavorito(skill.id) }) {
                Icon(
                    painterResource(if (favorito) R.drawable.ic_estrella else R.drawable.ic_estrella_borde),
                    contentDescription = if (favorito) "Quitar de favoritas" else "Marcar como favorita",
                    tint = if (favorito) Color(0xFFD4A017) else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        /* Mismo botón de volver arriba que en las listas: las fichas también
           se hacen largas (fuentes, condiciones, evoluciones). */
        ListaConArriba(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
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
                        FilaReferencia("Carta de personaje sin datos")
                    }
                }
            }
            item { SeccionTitulo("Descripciones") }
            item { TarjetaDescripciones(skill, idioma) }
            if (skill.conditions.isNotEmpty()) {
                item { SeccionTitulo("Condiciones", skill.conditions.size) }
                items(skill.conditions.size) { indice ->
                    TarjetaCondicion(
                        skill.conditions[indice].precondition,
                        skill.conditions[indice].condition,
                        skill.conditions[indice].baseTime,
                        skill.conditions[indice].effects,
                    )
                }
            }
            val gen = skill.geneVersion
            if (gen != null && (gen.name.texto(idioma) != null || gen.desc.texto(idioma) != null)) {
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
                        FilaReferencia("Carta de personaje sin datos")
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
                        FilaReferencia("Carta de apoyo sin datos")
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
                        FilaReferencia("Carta de apoyo sin datos")
                    }
                }
            }
            if (fuentes.scenarioEvents.isNotEmpty()) {
                item { SeccionTitulo("Eventos de escenario", fuentes.scenarioEvents.size) }
                items(fuentes.scenarioEvents, key = { "escenario-$it" }) { escenarioId ->
                    FilaReferencia(modelo.escenario(escenarioId)?.nombre(idioma) ?: "Escenario sin datos")
                }
            }
        }
    }
}

@Composable
private fun EncabezadoSkill(skill: Skill, idioma: String) {
    TarjetaApp(modifier = Modifier.fillMaxWidth()) {
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
                    if (skill.cost != null) {
                        Text("Coste ${skill.cost}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (skill.unreleased.isNotEmpty()) {
                    /* Mismas pastillas que en la lista: "sin EN", y una por idioma
                       si le faltan varios. */
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (id in skill.unreleased) {
                            Etiqueta(
                                "sin ${etiquetaIdioma(id)}",
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaDescripciones(skill: Skill, idioma: String) {
    var expandido by remember { mutableStateOf(false) }
    /* Primero el idioma elegido en la app; los demás, detrás del botón. Antes
       empezaba siempre por inglés, así que quien leía en japonés veía su idioma
       escondido en "ver más idiomas". */
    val otros = IDIOMAS.filter { it != idioma }
    TarjetaApp(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BloqueIdioma(skill, idioma)
            AnimatedVisibility(visible = expandido, enter = expandVertically(), exit = shrinkVertically()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (id in otros) {
                        HorizontalDivider()
                        BloqueIdioma(skill, id)
                    }
                }
            }
            TextButton(onClick = { expandido = !expandido }) {
                Text(
                    if (expandido) {
                        "Ocultar otros idiomas"
                    } else {
                        "Ver más idiomas (" + otros.joinToString(" · ") { etiquetaIdioma(it) } + ")"
                    },
                )
            }
        }
    }
}

@Composable
private fun BloqueIdioma(skill: Skill, id: String) {
    val descripcion = skill.descripcion(id)
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(etiquetaIdioma(id), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(skill.nombre(id), style = MaterialTheme.typography.titleSmall)
        if (descripcion.isBlank()) {
            Text(
                "Sin descripción en ${etiquetaIdioma(id)}.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/* Condición legible: un punto por término ("recorrido (%) ≥ 50"). Si alguna
   clave no está en la tabla se enseña además la expresión original, en pequeño,
   para no perder el dato. */
@Composable
private fun FilaCondicion(titulo: String, expresion: String) {
    val terminos = leerCondiciones(expresion)
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            titulo,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (terminos.isEmpty()) {
            Text(expresion, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            return@Column
        }
        for (termino in terminos) {
            /* El "o" que separa dos alternativas de la condición no es un
               término: va sin punto y en su propia línea. */
            if (termino.separador) {
                Text(
                    termino.texto,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                continue
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
                Text(termino.texto, style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (terminos.any { !it.traducido }) {
            Text(
                expresion,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/* El valor del efecto es lo que se lee de un vistazo; el código de tipo se deja
   al lado en pequeño porque sin una tabla fiable no se puede traducir. El color
   sólo dice si suma o resta, que es un dato del propio valor. */
@Composable
private fun FichaEfecto(efecto: Efecto) {
    val sube = efecto.value >= 0
    val colores = if (sube) ColoresEfectoSube else ColoresEfectoBaja
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colores.first().copy(alpha = 0.14f))
            .border(1.dp, colores.first().copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            (if (sube) "+" else "") + efecto.value.toString(),
            style = MaterialTheme.typography.titleSmall,
            color = colores.last(),
        )
        Text(
            "tipo ${efecto.type}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TarjetaCondicion(
    precondition: String?,
    condition: String?,
    baseTime: Long?,
    efectos: List<Efecto>,
) {
    TarjetaApp(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (precondition != null) FilaCondicion("Requisito", precondition)
            if (condition != null) FilaCondicion("Condición", condition)
            if (baseTime != null && baseTime > 0) {
                Text(
                    "Duración base: ${duracionEnSegundos(baseTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (efectos.isNotEmpty()) {
                Text(
                    "Efectos",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (efecto in efectos) FichaEfecto(efecto)
                }
            }
            if (precondition == null && condition == null && efectos.isEmpty()) {
                Text("—", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun TarjetaGen(gen: VersionGen, idioma: String) {
    TarjetaApp(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(gen.name.texto(idioma) ?: "Versión gen", style = MaterialTheme.typography.titleSmall)
            Text(
                gen.desc.texto(idioma).orEmpty().ifBlank { "—" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (gen.cost != null) {
                    Text("Coste ${gen.cost}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun TarjetaEvolucion(preEvo: EvoOrigen?, evos: List<EvoOrigen>, modelo: Modelo, idioma: String) {
    TarjetaApp(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (preEvo != null) {
                Text("Evoluciona desde: ${nombreSkill(preEvo.old, modelo, idioma)}", style = MaterialTheme.typography.bodySmall)
                Text(origen(preEvo, modelo, idioma), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            for (evo in evos) {
                Text("→ ${evo.evos.joinToString(", ") { nombreSkill(it, modelo, idioma) }}", style = MaterialTheme.typography.bodySmall)
                Text(origen(evo, modelo, idioma), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/* Las referencias a otras skills vienen por ID; se muestran por nombre. */
private fun nombreSkill(id: Int?, modelo: Modelo, idioma: String): String {
    if (id == null) return "—"
    return modelo.skill(id)?.nombre(idioma) ?: "Skill sin datos"
}

private fun origen(evo: EvoOrigen, modelo: Modelo, idioma: String): String = when {
    evo.cardId != null -> "Carta: ${modelo.cartaPersonaje(evo.cardId)?.nombre(idioma) ?: "sin datos"}"
    evo.scenarioId != null -> "Escenario: ${modelo.escenario(evo.scenarioId)?.nombre(idioma) ?: "sin datos"}"
    else -> "—"
}

@Composable
private fun FilaReferencia(texto: String) {
    TarjetaApp(modifier = Modifier.fillMaxWidth()) {
        Text(
            texto,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
