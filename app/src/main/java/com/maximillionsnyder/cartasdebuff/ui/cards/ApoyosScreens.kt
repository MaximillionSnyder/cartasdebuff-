package com.maximillionsnyder.cartasdebuff.ui.cards

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
import androidx.compose.foundation.lazy.items
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
import com.maximillionsnyder.cartasdebuff.domain.CartaApoyo
import com.maximillionsnyder.cartasdebuff.domain.Modelo
import com.maximillionsnyder.cartasdebuff.ui.components.BarraDetalle
import com.maximillionsnyder.cartasdebuff.ui.components.TarjetaApp
import com.maximillionsnyder.cartasdebuff.ui.components.CampoBusqueda
import com.maximillionsnyder.cartasdebuff.ui.components.ChipFiltro
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresEstrella1
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresGacha
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresEstrella2
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresEstrella3
import com.maximillionsnyder.cartasdebuff.ui.components.ColoresTodas
import com.maximillionsnyder.cartasdebuff.ui.components.ContadorResultados
import com.maximillionsnyder.cartasdebuff.ui.components.EstadoVacio
import com.maximillionsnyder.cartasdebuff.ui.components.EstadoVacioBusqueda
import com.maximillionsnyder.cartasdebuff.ui.components.Estrellas
import com.maximillionsnyder.cartasdebuff.ui.components.EtiquetaTipoApoyo
import com.maximillionsnyder.cartasdebuff.ui.components.FilaApoyo
import com.maximillionsnyder.cartasdebuff.ui.components.FilaChips
import com.maximillionsnyder.cartasdebuff.ui.components.FilaSkill
import com.maximillionsnyder.cartasdebuff.ui.components.ImagenRemota
import com.maximillionsnyder.cartasdebuff.ui.components.ListaConArriba
import com.maximillionsnyder.cartasdebuff.ui.components.PanelFiltros
import com.maximillionsnyder.cartasdebuff.ui.components.SeccionTitulo
import com.maximillionsnyder.cartasdebuff.ui.components.SelectorIdioma
import com.maximillionsnyder.cartasdebuff.domain.TIPOS_APOYO
import com.maximillionsnyder.cartasdebuff.ui.components.coloresTipoApoyo
import com.maximillionsnyder.cartasdebuff.domain.etiquetaObtencion
import com.maximillionsnyder.cartasdebuff.domain.etiquetaTipoApoyo
import com.maximillionsnyder.cartasdebuff.ui.i18n.TX_PANTALLAS
import com.maximillionsnyder.cartasdebuff.ui.i18n.t

/* Chips de estrellas: el número filtra y la etiqueta no cambia con el idioma. */
private val CHIPS_ESTRELLAS = listOf(
    Triple(3, "3★", ColoresEstrella3),
    Triple(2, "2★", ColoresEstrella2),
    Triple(1, "1★", ColoresEstrella1),
)

@Composable
fun ApoyosScreen(
    modelo: Modelo,
    idioma: String,
    compacta: Boolean,
    onIdioma: (String) -> Unit,
    onAbrirCarta: (Int) -> Unit,
) {
    var busqueda by rememberSaveable { mutableStateOf("") }
    var tipoFiltro by rememberSaveable { mutableStateOf("todos") }
    var estrellas by rememberSaveable { mutableStateOf(0) }
    var descendente by rememberSaveable { mutableStateOf(false) }
    var sinGacha by rememberSaveable { mutableStateOf(false) }
    var filtrosAbiertos by rememberSaveable { mutableStateOf(true) }

    val filtradas = filtrarApoyos(
        modelo.cartasApoyo,
        FiltroApoyos(
            consulta = busqueda,
            tipo = tipoFiltro,
            estrellas = estrellas,
            descendente = descendente,
            sinGacha = sinGacha,
        ),
    )

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                t(TX_PANTALLAS, "titulo_apoyos"),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            SelectorIdioma(idioma, onIdioma)
        }
        CampoBusqueda(
            valor = busqueda,
            onValor = { busqueda = it },
            placeholder = t(TX_PANTALLAS, "buscar_por_nombre_o_titulo"),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        PanelFiltros(
            activos = (if (tipoFiltro != "todos") 1 else 0) +
                (if (estrellas != 0) 1 else 0) +
                (if (sinGacha) 1 else 0),
            abierto = filtrosAbiertos,
            onAlternar = { filtrosAbiertos = !filtrosAbiertos },
            onLimpiar = {
                tipoFiltro = "todos"
                estrellas = 0
                sinGacha = false
            },
        ) {
            FilaChips {
                ChipFiltro(
                    texto = t(TX_PANTALLAS, "todas"),
                    seleccionado = tipoFiltro == "todos",
                    colores = ColoresTodas,
                    onClick = { tipoFiltro = "todos" },
                )
                for (tipo in TIPOS_APOYO) {
                    ChipFiltro(
                        texto = etiquetaTipoApoyo(tipo, idioma),
                        seleccionado = tipoFiltro == tipo,
                        colores = coloresTipoApoyo(tipo),
                        onClick = { tipoFiltro = if (tipoFiltro == tipo) "todos" else tipo },
                    )
                }
            }
            FilaChips {
                for ((valor, etiqueta, colores) in CHIPS_ESTRELLAS) {
                    ChipFiltro(
                        texto = etiqueta,
                        seleccionado = estrellas == valor,
                        colores = colores,
                        onClick = { estrellas = if (estrellas == valor) 0 else valor },
                    )
                }
                ChipFiltro(
                    texto = t(TX_PANTALLAS, "chip_sin_gacha"),
                    seleccionado = sinGacha,
                    colores = ColoresGacha,
                    onClick = { sinGacha = !sinGacha },
                )
                ChipFiltro(
                    texto = if (descendente) "↓ 3★ → 1★" else "↑ 1★ → 3★",
                    seleccionado = true,
                    colores = ColoresTodas,
                    onClick = { descendente = !descendente },
                )
            }
        }
        ContadorResultados(
            t(TX_PANTALLAS, "contador_cartas", "${filtradas.size} / ${modelo.cartasApoyo.size}"),
        )
        if (filtradas.isEmpty()) {
            EstadoVacioBusqueda()
        } else {
            ListaConArriba(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
            ) {
                items(filtradas, key = { it.supportId }) { carta ->
                    FilaApoyo(
                        carta = carta,
                        idioma = idioma,
                        compacta = compacta,
                        numeroSkills = modelo.skillsPorApoyo[carta.supportId]?.distinct()?.size,
                    ) { onAbrirCarta(carta.supportId) }
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
            BarraDetalle(t(TX_PANTALLAS, "titulo_carta_apoyo"), onVolver)
            EstadoVacio(t(TX_PANTALLAS, "carta_no_encontrada"))
        }
        return
    }

    val skills = modelo.skillsPorApoyo[supportId].orEmpty().distinct()
    val hints = skills.filter { supportId in it.sources.supportHints }
    val eventos = skills.filter { supportId in it.sources.supportEvents }

    Column(Modifier.fillMaxSize()) {
        BarraDetalle(carta.nombre(idioma), onVolver)
        /* Mismo botón de volver arriba que en las listas: las fichas también
           se hacen largas (fuentes, condiciones, evoluciones). */
        ListaConArriba(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        ) {
            item { EncabezadoCartaApoyo(carta, idioma, skills.size) }
            if (hints.isNotEmpty()) {
                item(key = "titulo-hints") {
                    SeccionTitulo(t(TX_PANTALLAS, "seccion_skills_hint"), hints.size)
                }
                items(hints, key = { "hint-${it.id}" }) { skill ->
                    FilaSkill(skill, idioma) { onAbrirSkill(skill.id) }
                }
            }
            if (eventos.isNotEmpty()) {
                item(key = "titulo-eventos") {
                    SeccionTitulo(t(TX_PANTALLAS, "seccion_skills_evento"), eventos.size)
                }
                items(eventos, key = { "evento-${it.id}" }) { skill ->
                    FilaSkill(skill, idioma) { onAbrirSkill(skill.id) }
                }
            }
            if (skills.isEmpty()) {
                item { EstadoVacio(t(TX_PANTALLAS, "sin_skills_carta")) }
            }
        }
    }
}

@Composable
private fun EncabezadoCartaApoyo(carta: CartaApoyo, idioma: String, numeroSkills: Int) {
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
                    Text(
                        if (numeroSkills == 1) {
                            t(TX_PANTALLAS, "una_skill")
                        } else {
                            t(TX_PANTALLAS, "n_skills", numeroSkills.toString())
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    etiquetaObtencion(carta.obtained, idioma),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
