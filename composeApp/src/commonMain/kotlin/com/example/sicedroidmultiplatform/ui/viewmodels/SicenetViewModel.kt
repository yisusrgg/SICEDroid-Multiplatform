package com.example.sicedroidmultiplatform.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _profileState = MutableStateFlow<PerfilAcademico?>(null)
    val profileState: StateFlow<PerfilAcademico?> = _profileState.asStateFlow()

    fun login(matricula: String, password: String) {
        viewModelScope.launch(Dispatchers.Default) {
            _loginState.value = LoginUiState.Loading
            val result = repository.login(matricula, password)
            if (result.success) {
                _loginState.value = LoginUiState.Success()
                cargarPerfil()
            } else {
                _loginState.value = LoginUiState.Error(result.message)
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginUiState.Idle
    }

    private suspend fun cargarPerfil() {
        _profileState.value = repository.getPerfil()
    }

    fun logout() {
        repository.clearSession()
        _loginState.value = LoginUiState.Idle
        _profileState.value = null
    }

    companion object {
        fun create(): SicenetViewModel {
            val client = provideHttpClient()
            val service = SicenetService(client)
            val repository = SicenetRepositoryImpl(service)
            return SicenetViewModel(repository)
        }
    }
}
