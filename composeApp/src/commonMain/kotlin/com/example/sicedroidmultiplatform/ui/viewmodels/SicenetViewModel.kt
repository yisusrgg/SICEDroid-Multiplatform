
package com.example.sicedroidmultiplatform.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sicedroidmultiplatform.data.local.getRoomDatabase
import com.example.sicedroidmultiplatform.data.model.*
import com.example.sicedroidmultiplatform.data.network.SicenetService
import com.example.sicedroidmultiplatform.data.network.provideHttpClient
import com.example.sicedroidmultiplatform.data.repository.SicenetRepository
import com.example.sicedroidmultiplatform.data.repository.SicenetRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val cookie: String = "") : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class SicenetViewModel(
    private val repository: SicenetRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    val profileState: StateFlow<PerfilAcademico?> = repository.getProfileFromDb()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val cardexState: StateFlow<List<CardexItem>> = repository.getCardexFromDb()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cargaState: StateFlow<List<Materia>> = repository.getCargaAcademicaFromDb()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val califFinalesState: StateFlow<List<CalificacionFinal>> = repository.getCalificacionesFinalesFromDb()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val califUnidadState: StateFlow<List<CalificacionUnidad>> = repository.getCalificacionesUnidadFromDb()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        checkSession()
    }

    private fun checkSession() {
        if (repository.isLoggedIn()) {
            _loginState.value = LoginUiState.Success()
            // Re-login silencioso en background para refrescar datos con credenciales guardadas.
            // Las cookies Ktor son en memoria: se pierden al reiniciar la app, así que hay que
            // volver a hacer login para establecer la sesión antes de llamar a la API.
            val creds = repository.getStoredCredentials()
            if (creds != null) {
                viewModelScope.launch(Dispatchers.Default) {
                    try {
                        val result = repository.login(creds.first, creds.second)
                        if (result.success) sincronizarDatos()
                    } catch (_: Exception) { /* fallo silencioso — datos del DB siguen visibles */ }
                }
            }
        }
    }

    fun login(matricula: String, password: String) {
        viewModelScope.launch(Dispatchers.Default) {
            _loginState.value = LoginUiState.Loading
            val result = repository.login(matricula, password)
            if (result.success) {
                _loginState.value = LoginUiState.Success()
                sincronizarDatos()
            } else {
                _loginState.value = LoginUiState.Error(result.message)
            }
        }
    }

    private suspend fun sincronizarDatos() {
        val perfilJson = repository.getUserProfile() ?: return
        repository.saveUserPerfilDb(perfilJson)

        // Extraemos lineamiento y modEducativo del perfil para las demás peticiones
        val json = Json.parseToJsonElement(perfilJson).jsonObject
        val lineamiento  = json["lineamiento"]?.jsonPrimitive?.intOrNull  ?: 0
        val modEducativo = json["modEducativo"]?.jsonPrimitive?.intOrNull ?: 0

        // Sincronizamos el resto en paralelo
        coroutineScope {
            launch {
                val j = repository.getCargaAcademica()
                if (j != null) repository.saveCargaAcademicaDb(j)
            }
            launch {
                val j = repository.getCardex(lineamiento)
                if (j != null) repository.saveCardexDb(j)
            }
            launch {
                val j = repository.getCalificacionesFinales(modEducativo)
                if (j != null) repository.saveCalificacionesFinalesDb(j)
            }
            launch {
                val j = repository.getCalificacionesUnidad()
                if (j != null) repository.saveCalificacionesUnidadDb(j)
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginUiState.Idle
    }

    fun logout() {
        repository.clearSession()
        _loginState.value = LoginUiState.Idle
    }

    companion object {
        fun create(): SicenetViewModel {
            val client     = provideHttpClient()
            val service    = SicenetService(client)
            val database   = getRoomDatabase()
            val dao        = database.sicenetDao()
            val repository = SicenetRepositoryImpl(service, dao)
            return SicenetViewModel(repository)
        }
    }
}