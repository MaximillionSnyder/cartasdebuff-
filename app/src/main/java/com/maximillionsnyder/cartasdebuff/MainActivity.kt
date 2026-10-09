package com.maximillionsnyder.cartasdebuff

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.cartasdebuff.data.Preferencias
import com.maximillionsnyder.cartasdebuff.domain.INICIO_AJUSTES
import com.maximillionsnyder.cartasdebuff.domain.INICIO_APOYOS
import com.maximillionsnyder.cartasdebuff.domain.INICIO_PERSONAJES
import com.maximillionsnyder.cartasdebuff.domain.Modelo
import com.maximillionsnyder.cartasdebuff.ui.AppViewModel
import com.maximillionsnyder.cartasdebuff.ui.cards.ApoyoDetalleScreen
import com.maximillionsnyder.cartasdebuff.ui.cards.ApoyosScreen
import com.maximillionsnyder.cartasdebuff.ui.cards.PersonajeDetalleScreen
import com.maximillionsnyder.cartasdebuff.ui.cards.PersonajesScreen
import com.maximillionsnyder.cartasdebuff.ui.settings.AjustesScreen
import com.maximillionsnyder.cartasdebuff.ui.skills.SkillDetalleScreen
import com.maximillionsnyder.cartasdebuff.ui.skills.SkillsScreen
import com.maximillionsnyder.cartasdebuff.ui.theme.CartasDebuffTheme

class MainActivity : ComponentActivity() {

    private val vm by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            /* Tema, idioma y pantalla inicial viven acá y se guardan en el dispositivo. */
            val prefs = remember { Preferencias(applicationContext) }
            var tema by rememberSaveable { mutableStateOf(prefs.tema) }
            var idioma by rememberSaveable { mutableStateOf(prefs.idioma) }
            var inicio by rememberSaveable { mutableStateOf(prefs.pantallaInicial) }

            CartasDebuffTheme(tema) {
                val modelo by vm.modelo.collectAsState()
                val actual = modelo
                if (actual == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Text(
                                stringResource(R.string.app_name),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            CircularProgressIndicator()
                            Text(
                                "Cargando skills y cartas…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    App(
                        modelo = actual,
                        idioma = idioma,
                        onIdioma = {
                            idioma = it
                            prefs.idioma = it
                        },
                        tema = tema,
                        onTema = {
                            tema = it
                            prefs.tema = it
                        },
                        pantallaInicial = inicio,
                        onPantallaInicial = {
                            inicio = it
                            prefs.pantallaInicial = it
                        },
                        onRestablecerAjustes = {
                            prefs.restablecer()
                            tema = prefs.tema
                            idioma = prefs.idioma
                            inicio = prefs.pantallaInicial
                        },
                    )
                }
            }
        }
    }
}

sealed interface Pantalla {
    data object Skills : Pantalla
    data object Personajes : Pantalla
    data object Apoyos : Pantalla
    data object Ajustes : Pantalla
    data class SkillDetalle(val skillId: Int) : Pantalla
    data class PersonajeDetalle(val cardId: Int) : Pantalla
    data class ApoyoDetalle(val supportId: Int) : Pantalla
}

/* Clave estable por pantalla: permite restaurar scroll y filtros al volver. */
private fun claveDePantalla(pantalla: Pantalla): String = when (pantalla) {
    Pantalla.Skills -> "tab-skills"
    Pantalla.Personajes -> "tab-personajes"
    Pantalla.Apoyos -> "tab-apoyos"
    Pantalla.Ajustes -> "tab-ajustes"
    is Pantalla.SkillDetalle -> "skill-${pantalla.skillId}"
    is Pantalla.PersonajeDetalle -> "personaje-${pantalla.cardId}"
    is Pantalla.ApoyoDetalle -> "apoyo-${pantalla.supportId}"
}

/* Pestaña con la que abre la app; se elige en Ajustes. */
private fun pantallaDeInicio(clave: String): Pantalla = when (clave) {
    INICIO_PERSONAJES -> Pantalla.Personajes
    INICIO_APOYOS -> Pantalla.Apoyos
    INICIO_AJUSTES -> Pantalla.Ajustes
    else -> Pantalla.Skills
}

@Composable
private fun App(
    modelo: Modelo,
    idioma: String,
    onIdioma: (String) -> Unit,
    tema: String,
    onTema: (String) -> Unit,
    pantallaInicial: String,
    onPantallaInicial: (String) -> Unit,
    onRestablecerAjustes: () -> Unit,
) {
    /* La pantalla inicial se lee una sola vez: cambiarla en Ajustes no salta de
       pestaña, se aplica en el próximo arranque. */
    val pila = remember { mutableStateListOf(pantallaDeInicio(pantallaInicial)) }
    val holder = rememberSaveableStateHolder()
    val actual = pila.last()
    val enDetalle = actual is Pantalla.SkillDetalle ||
        actual is Pantalla.PersonajeDetalle ||
        actual is Pantalla.ApoyoDetalle

    BackHandler(enabled = pila.size > 1) { pila.removeAt(pila.lastIndex) }

    fun abrir(destino: Pantalla) {
        pila.add(destino)
    }

    fun irATab(tab: Pantalla) {
        if (pila.size == 1 && pila[0] == tab) return
        pila.clear()
        pila.add(tab)
    }

    Scaffold(
        bottomBar = {
            if (!enDetalle) {
                NavigationBar {
                    NavigationBarItem(
                        selected = actual == Pantalla.Skills,
                        onClick = { irATab(Pantalla.Skills) },
                        icon = { Icon(painterResource(R.drawable.ic_tab_skills), contentDescription = null) },
                        label = { Text("Skills") },
                    )
                    NavigationBarItem(
                        selected = actual == Pantalla.Personajes,
                        onClick = { irATab(Pantalla.Personajes) },
                        icon = { Icon(painterResource(R.drawable.ic_tab_personajes), contentDescription = null) },
                        label = { Text("Personajes") },
                    )
                    NavigationBarItem(
                        selected = actual == Pantalla.Apoyos,
                        onClick = { irATab(Pantalla.Apoyos) },
                        icon = { Icon(painterResource(R.drawable.ic_tab_apoyos), contentDescription = null) },
                        label = { Text("Apoyos") },
                    )
                    NavigationBarItem(
                        selected = actual == Pantalla.Ajustes,
                        onClick = { irATab(Pantalla.Ajustes) },
                        icon = { Icon(painterResource(R.drawable.ic_tab_ajustes), contentDescription = null) },
                        label = { Text("Ajustes") },
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            holder.SaveableStateProvider(claveDePantalla(actual)) {
                when (val pantalla = actual) {
                Pantalla.Skills -> SkillsScreen(
                    modelo = modelo,
                    idioma = idioma,
                    onIdioma = onIdioma,
                    onAbrirSkill = { abrir(Pantalla.SkillDetalle(it)) },
                )
                Pantalla.Personajes -> PersonajesScreen(
                    modelo = modelo,
                    idioma = idioma,
                    onIdioma = onIdioma,
                    onAbrirCarta = { abrir(Pantalla.PersonajeDetalle(it)) },
                )
                Pantalla.Apoyos -> ApoyosScreen(
                    modelo = modelo,
                    idioma = idioma,
                    onIdioma = onIdioma,
                    onAbrirCarta = { abrir(Pantalla.ApoyoDetalle(it)) },
                )
                Pantalla.Ajustes -> AjustesScreen(
                    modelo = modelo,
                    idioma = idioma,
                    onIdioma = onIdioma,
                    tema = tema,
                    onTema = onTema,
                    pantallaInicial = pantallaInicial,
                    onPantallaInicial = onPantallaInicial,
                    onRestablecer = onRestablecerAjustes,
                )
                is Pantalla.SkillDetalle -> SkillDetalleScreen(
                    modelo = modelo,
                    skillId = pantalla.skillId,
                    idioma = idioma,
                    onVolver = { pila.removeAt(pila.lastIndex) },
                    onAbrirPersonaje = { abrir(Pantalla.PersonajeDetalle(it)) },
                    onAbrirApoyo = { abrir(Pantalla.ApoyoDetalle(it)) },
                )
                is Pantalla.PersonajeDetalle -> PersonajeDetalleScreen(
                    modelo = modelo,
                    cardId = pantalla.cardId,
                    idioma = idioma,
                    onVolver = { pila.removeAt(pila.lastIndex) },
                    onAbrirSkill = { abrir(Pantalla.SkillDetalle(it)) },
                )
                is Pantalla.ApoyoDetalle -> ApoyoDetalleScreen(
                    modelo = modelo,
                    supportId = pantalla.supportId,
                    idioma = idioma,
                    onVolver = { pila.removeAt(pila.lastIndex) },
                    onAbrirSkill = { abrir(Pantalla.SkillDetalle(it)) },
                )
                }
            }
        }
    }
}
