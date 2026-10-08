package com.maximillionsnyder.cartasdebuff

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
            CartasDebuffTheme {
                val modelo by vm.modelo.collectAsState()
                val actual = modelo
                if (actual == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    App(actual)
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

@Composable
private fun App(modelo: Modelo) {
    var idioma by rememberSaveable { mutableStateOf("en") }
    val pila = remember { mutableStateListOf<Pantalla>(Pantalla.Skills) }
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
            when (val pantalla = actual) {
                Pantalla.Skills -> SkillsScreen(
                    modelo = modelo,
                    idioma = idioma,
                    onIdioma = { idioma = it },
                    onAbrirSkill = { abrir(Pantalla.SkillDetalle(it)) },
                )
                Pantalla.Personajes -> PersonajesScreen(
                    modelo = modelo,
                    idioma = idioma,
                    onIdioma = { idioma = it },
                    onAbrirCarta = { abrir(Pantalla.PersonajeDetalle(it)) },
                )
                Pantalla.Apoyos -> ApoyosScreen(
                    modelo = modelo,
                    idioma = idioma,
                    onIdioma = { idioma = it },
                    onAbrirCarta = { abrir(Pantalla.ApoyoDetalle(it)) },
                )
                Pantalla.Ajustes -> AjustesScreen(
                    modelo = modelo,
                    idioma = idioma,
                    onIdioma = { idioma = it },
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
