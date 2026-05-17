
package com.example.sicedroidmultiplatform.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sicedroidmultiplatform.data.local.getRoomDatabase
import com.example.sicedroidmultiplatform.data.model.*
import com.example.sicedroidmultiplatform.data.network.SicenetService
import com.example.sicedroidmultiplatform.data.network.provideHttpClient
import com.example.sicedroidmultiplatform.data.repository.SicenetRepository
import com.example.sicedroidmultiplatform.data.repository.SicenetRepositoryImpl
import com.example.sicedroidmultiplatform.workers.SyncManager
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


    private val syncManager = SyncManager(repository)
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
        //worker guarda el perfil en la base de datos
        syncManager.sincronizarDato("PERFIL")

        val perfilJson = repository.getUserProfile() ?: return
        val json = Json.parseToJsonElement(perfilJson).jsonObject
        val lineamiento  = json["lineamiento"]?.jsonPrimitive?.intOrNull  ?: 0
        val modEducativo = json["modEducativo"]?.jsonPrimitive?.intOrNull ?: 0

        syncManager.sincronizarDato("CARGA_ACADEMICA")
        syncManager.sincronizarDato("CARDEX", lineamiento = lineamiento)
        syncManager.sincronizarDato("CALIF_FINAL", modEducativo = modEducativo)
        syncManager.sincronizarDato("CALIF_UNIDAD")
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