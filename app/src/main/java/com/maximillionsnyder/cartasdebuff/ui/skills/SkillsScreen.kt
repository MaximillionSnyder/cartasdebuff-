package com.maximillionsnyder.cartasdebuff.ui.skills

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
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
import com.maximillionsnyder.cartasdebuff.ui.components.COLADOR_ES
import com.maximillionsnyder.cartasdebuff.ui.components.CampoBusqueda
import com.maximillionsnyder.cartasdebuff.ui.components.ChipFiltro
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresAceleracion
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresDebuff
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresEscenario
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresFavorito
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresEventoApoyo
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresEventoPersonaje
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresEvolucion
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresHint
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresNormal
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresOcultar
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresOrden
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresPersonaje
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresRara
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresTodas
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresUnica
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresVelocidad
import com.maximillionsnyder.cartasdebuff.ui.components.ContadorResultados
import com.maximillionsnyder.cartasdebuff.ui.components.EstadoVacioBusqueda
import com.maximillionsnyder.cartasdebuff.ui.components.EstadoVacio
import com.maximillionsnyder.cartasdebuff.ui.components.FilaChips
import com.maximillionsnyder.cartasdebuff.ui.components.FilaSkill
import com.maximillionsnyder.cartasdebuff.ui.components.ListaConArriba
import com.maximillionsnyder.cartasdebuff.ui.components.PanelFiltros
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
    compacta: Boolean,
    favoritos: Set<String>,
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
    var alfabetico by rememberSaveable { mutableStateOf(false) }
    var soloFavoritos by rememberSaveable { mutableStateOf(false) }
    var filtrosAbiertos by rememberSaveable { mutableStateOf(true) }

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
        val coincideFavorito = !soloFavoritos || skill.id.toString() in favoritos
        coincideBusqueda && coincideRareza && coincideDebuff && coincideEfecto &&
            coincideLanzamiento && coincideFuente && coincideFavorito
    }

    /* El orden de los datos originales no dice nada al usuario: poder ponerlo
       alfabético (respetando acentos y ñ) ayuda a encontrar una skill. */
    val ordenadas = if (alfabetico) {
        filtradas.sortedWith(compareBy(COLADOR_ES, { it.nombre(idioma) }))
    } else {
        filtradas
    }

    /* Cuántos filtros hay puestos: lo que muestra la pastilla del panel. */
    val filtrosActivos = (if (rarezaFiltro != "todas") 1 else 0) +
        (if (soloDebuff) 1 else 0) +
        (if (soloVelocidad) 1 else 0) +
        (if (soloAceleracion) 1 else 0) +
        (if (ocultarSinEn) 1 else 0) +
        fuentes.size

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Skills", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            SelectorIdioma(idioma, onIdioma)
        }
        CampoBusqueda(
            valor = busqueda,
            onValor = { busqueda = it },
            placeholder = "Buscar por nombre",
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        PanelFiltros(
            activos = filtrosActivos,
            abierto = filtrosAbiertos,
            onAlternar = { filtrosAbiertos = !filtrosAbiertos },
            onLimpiar = {
                rarezaFiltro = "todas"
                soloDebuff = false
                soloVelocidad = false
                soloAceleracion = false
                ocultarSinEn = false
                fuentes = emptyList()
            },
        ) {
            FilaChips {
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
            FilaChips {
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
            FilaChips {
                ChipFiltro(
                    texto = "Favoritas",
                    seleccionado = soloFavoritos,
                    colores = ColoresFavorito,
                    onClick = { soloFavoritos = !soloFavoritos },
                )
                ChipFiltro(
                    texto = "Orden A-Z",
                    seleccionado = alfabetico,
                    colores = ColoresOrden,
                    onClick = { alfabetico = !alfabetico },
                )
            }
        }
        ContadorResultados("${ordenadas.size} de ${modelo.skills.size} skills")
        if (ordenadas.isEmpty()) {
            /* Si el filtro que deja la lista vacía es el de favoritas, el mensaje
               genérico no ayuda: hay que explicar cómo marcar una. */
            if (soloFavoritos && favoritos.isEmpty()) {
                EstadoVacio(
                    "Todavía no hay favoritas",
                    "Abrí una skill y tocá la estrella de arriba para guardarla acá.",
                    Icons.Filled.Star,
                )
            } else {
                EstadoVacioBusqueda()
            }
        } else {
            ListaConArriba(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
            ) {
                items(ordenadas, key = { it.id }) { skill ->
                    FilaSkill(
                        skill = skill,
                        idioma = idioma,
                        compacta = compacta,
                        favorito = skill.id.toString() in favoritos,
                        onClick = { onAbrirSkill(skill.id) },
                    )
                }
            }
        }
    }
}
