package com.example.sicedroidmultiplatform.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sicedroidmultiplatform.data.local.getRoomDatabase
import com.example.sicedroidmultiplatform.data.model.PerfilAcademico
import com.example.sicedroidmultiplatform.data.network.SicenetService
import com.example.sicedroidmultiplatform.data.network.provideHttpClient
import com.example.sicedroidmultiplatform.data.repository.SicenetRepository
import com.example.sicedroidmultiplatform.data.repository.SicenetRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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



    /*private val _profileState = MutableStateFlow<PerfilAcademico?>(null)
    val profileState: StateFlow<PerfilAcademico?> = _profileState.asStateFlow()

    private suspend fun cargarPerfil() {
        _profileState.value = repository.getPerfil()
    }
    */

    val profileState: StateFlow<PerfilAcademico?> = repository.getProfileFromDb()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun login(matricula: String, password: String) {
        viewModelScope.launch(Dispatchers.Default) {
            _loginState.value = LoginUiState.Loading
            val result = repository.login(matricula, password)
            if (result.success) {
                _loginState.value = LoginUiState.Success()
                // 2. Si el login es correcto, arrancamos la sincronización manual
                sincronizarDatos()
            } else {
                _loginState.value = LoginUiState.Error(result.message)
            }
        }
    }

    private suspend fun sincronizarDatos() {
        // Pide el JSON a internet
        val perfilJson = repository.getUserProfile()
        if (perfilJson != null) {
            repository.saveUserPerfilDb(perfilJson)
        }
        // Cuando habilites las demás pantallas, descomentas esto:
        /*
        val cargaJson = repository.getCargaAcademica()
        if (cargaJson != null) repository.saveCargaAcademicaDb(cargaJson)
        // ... etc
        */
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
            val client = provideHttpClient()
            val service = SicenetService(client)

            val database = getRoomDatabase()
            val dao = database.sicenetDao()

            val repository = SicenetRepositoryImpl(service, dao)
            return SicenetViewModel(repository)
        }
    }
}
