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

    // true = última operación de red tuvo éxito; false = sin internet
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

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

    fun getFechaActualizacion(tipo: String): String = syncManager.getFechaActualizacion(tipo)

    init {
        checkSession()
    }

    private fun checkSession() {
        if (repository.isLoggedIn()) {
            _loginState.value = LoginUiState.Success()
            val creds = repository.getStoredCredentials()
            if (creds != null) {
                viewModelScope.launch(Dispatchers.Default) {
                    try {
                        val result = repository.login(creds.first, creds.second)
                        _isOnline.value = result.success
                        if (result.success) sincronizarDatos()
                    } catch (_: Exception) {
                        _isOnline.value = false
                    }
                }
            }
        }
    }

    fun login(matricula: String, password: String) {
        viewModelScope.launch(Dispatchers.Default) {
            _loginState.value = LoginUiState.Loading
            val result = repository.login(matricula, password)
            _isOnline.value = result.success
            if (result.success) {
                _loginState.value = LoginUiState.Success()
                sincronizarDatos()
            } else {
                _loginState.value = LoginUiState.Error(result.message)
            }
        }
    }

    private suspend fun sincronizarDatos() {
        // Leer lineamiento/modEducativo de la BD (rápido, sin red).
        // Solo si la BD está vacía (primer inicio) se hace una llamada de red puntual.
        val perfilDb = repository.getProfileFromDb().first()

        val lineamiento: Int
        val modEducativo: Int

        if (perfilDb != null) {
            lineamiento  = perfilDb.lineamiento
            modEducativo = perfilDb.modEducativo
        } else {
            // Primer inicio: BD vacía — obtener perfil de la red y guardarlo de inmediato
            val perfilJson = repository.getUserProfile() ?: return
            val json = Json.parseToJsonElement(perfilJson).jsonObject
            lineamiento  = json["lineamiento"]?.jsonPrimitive?.intOrNull  ?: 0
            modEducativo = json["modEducativo"]?.jsonPrimitive?.intOrNull ?: 0
            repository.saveUserPerfilDb(perfilJson)
        }

        syncManager.sincronizarDato("PERFIL")
        syncManager.sincronizarDato("CARGA_ACADEMICA")
        syncManager.sincronizarDato("CARDEX",      lineamiento  = lineamiento)
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
