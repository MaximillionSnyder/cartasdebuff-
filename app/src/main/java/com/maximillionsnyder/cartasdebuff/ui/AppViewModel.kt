package com.maximillionsnyder.cartasdebuff.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.maximillionsnyder.cartasdebuff.data.SkillsRepository
import com.maximillionsnyder.cartasdebuff.domain.Modelo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = SkillsRepository(application)

    private val _modelo = MutableStateFlow<Modelo?>(null)
    val modelo: StateFlow<Modelo?> = _modelo.asStateFlow()

    init {
        viewModelScope.launch {
            _modelo.value = withContext(Dispatchers.Default) { repositorio.modelo }
        }
    }
}
