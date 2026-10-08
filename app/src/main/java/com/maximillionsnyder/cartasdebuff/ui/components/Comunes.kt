package com.maximillionsnyder.cartasdebuff.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.cartasdebuff.R
import com.maximillionsnyder.cartasdebuff.domain.CartaApoyo
import com.maximillionsnyder.cartasdebuff.domain.CartaPersonaje
import com.maximillionsnyder.cartasdebuff.domain.IDIOMAS
import com.maximillionsnyder.cartasdebuff.domain.Skill
import com.maximillionsnyder.cartasdebuff.domain.etiquetaIdioma

val ETIQUETAS_TIPO: Map<String, String> = mapOf(
    "nac" to "Sin condición",
    "run" to "Escapada",
    "ldr" to "Líder",
    "btw" to "Intermedia",
    "cha" to "Perseguidora",
    "sho" to "Corta",
    "mil" to "Milla",
    "med" to "Media",
    "lng" to "Larga",
    "dir" to "Arena",
    "tur" to "Césped",
    "l_0" to "Tramo inicial",
    "l_1" to "Tramo medio",
    "l_2" to "Tramo final",
    "l_3" to "Último esprint",
    "cor" to "Curva",
    "str" to "Recta",
    "f_c" to "Última curva",
    "f_s" to "Última recta",
    "slo" to "Subida/bajada",
    "dbf" to "Debuff",
)

fun etiquetaTipo(tipo: String): String = ETIQUETAS_TIPO[tipo] ?: tipo

fun etiquetaRareza(rarity: Int?): String = when (rarity) {
    1 -> "Normal"
    2 -> "Rara"
    3, 5 -> "Única"
    4, 6 -> "Evolución"
    else -> "?"
}

fun colorRareza(rarity: Int?): Color = when (rarity) {
    1 -> Color(0xFF9CA3AF)
    2 -> Color(0xFFD4A017)
    3, 5 -> Color(0xFFEC4899)
    4, 6 -> Color(0xFF8B5CF6)
    else -> Color(0xFF6B7280)
}

fun etiquetaKind(kind: String): String = when (kind) {
    "unique" -> "Única"
    "innate" -> "Innata"
    "awakening" -> "Despertar"
    "evolution" -> "Evolución"
    else -> kind
}

fun etiquetaTipoApoyo(type: String?): String = when (type) {
    "speed" -> "Velocidad"
    "stamina" -> "Aguante"
    "power" -> "Fuerza"
    "guts" -> "Coraje"
    "intelligence" -> "Ingenio"
    "friend" -> "Amistad"
    "group" -> "Grupo"
    else -> type ?: "?"
}

fun etiquetaObtencion(obtained: String?): String = when (obtained) {
    "gacha" -> "Gacha"
    "main_story" -> "Historia principal"
    "story_event" -> "Evento de historia"
    "promo" -> "Promoción"
    "limited_quest" -> "Misión limitada"
    "shop" -> "Tienda"
    else -> obtained ?: "?"
}

fun resumenFuentes(skill: Skill): String {
    val partes = buildList {
        val f = skill.sources
        if (f.characterCards.isNotEmpty()) add("${f.characterCards.size} pers.")
        if (f.characterEvents.isNotEmpty()) add("${f.characterEvents.size} ev. pers.")
        if (f.supportHints.isNotEmpty()) add("${f.supportHints.size} hints")
        if (f.supportEvents.isNotEmpty()) add("${f.supportEvents.size} ev. apoyo")
        if (f.scenarioEvents.isNotEmpty()) add("${f.scenarioEvents.size} esc.")
    }
    return if (partes.isEmpty()) "Sin fuentes" else partes.joinToString(" · ")
}

@Composable
fun SelectorIdioma(idioma: String, onCambia: (String) -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { abierto = true }) {
            Text(etiquetaIdioma(idioma), style = MaterialTheme.typography.labelLarge)
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            for (id in IDIOMAS) {
                DropdownMenuItem(
                    text = { Text(etiquetaIdioma(id)) },
                    onClick = {
                        onCambia(id)
                        abierto = false
                    },
                )
            }
        }
    }
}

@Composable
fun EtiquetaRareza(rarity: Int?) {
    Text(
        text = etiquetaRareza(rarity),
        color = Color.White,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .background(colorRareza(rarity), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
fun Estrellas(rarity: Int?) {
    val cantidad = (rarity ?: 0).coerceIn(0, 3)
    if (cantidad > 0) {
        Text("★".repeat(cantidad), color = Color(0xFFD4A017), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun SeccionTitulo(texto: String, contador: Int? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(texto, style = MaterialTheme.typography.titleMedium)
        if (contador != null) {
            Spacer(Modifier.width(6.dp))
            Text(
                contador.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun EstadoVacio(texto: String) {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(texto, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun BarraDetalle(titulo: String, onVolver: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onVolver) {
            Icon(painterResource(R.drawable.ic_atras), contentDescription = "Volver")
        }
        Text(
            titulo,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun Etiqueta(texto: String, color: Color = MaterialTheme.colorScheme.secondaryContainer, colorTexto: Color = MaterialTheme.colorScheme.onSecondaryContainer) {
    Text(
        texto,
        color = colorTexto,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .background(color, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
fun FilaSkill(skill: Skill, idioma: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ImagenRemota(skill.urlIcono, null, Modifier.size(44.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    skill.nombre(idioma),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val japones = skill.name["ja"]
                if (idioma != "ja" && !japones.isNullOrBlank()) {
                    Text(
                        japones,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    resumenFuentes(skill),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                EtiquetaRareza(skill.rarity)
                if ("dbf" in skill.types) {
                    Text("debuff", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
                if ("en" in skill.unreleased) {
                    Text("sin EN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun FilaPersonaje(carta: CartaPersonaje, idioma: String, kinds: List<String>?, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ImagenRemota(carta.image, null, Modifier.size(48.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    carta.nombre(idioma),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val titulo = carta.titulo(idioma)
                if (titulo.isNotBlank()) {
                    Text(
                        titulo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (!kinds.isNullOrEmpty()) {
                    Text(
                        kinds.joinToString(" · ") { etiquetaKind(it) },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Estrellas(carta.rarity)
        }
    }
}

@Composable
fun FilaApoyo(carta: CartaApoyo, idioma: String, insignia: String? = null, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ImagenRemota(carta.image, null, Modifier.size(48.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    carta.nombre(idioma),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val titulo = carta.titulo(idioma)
                if (titulo.isNotBlank()) {
                    Text(
                        titulo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    listOfNotNull(etiquetaTipoApoyo(carta.type), insignia).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            Estrellas(carta.rarity)
        }
    }
}
