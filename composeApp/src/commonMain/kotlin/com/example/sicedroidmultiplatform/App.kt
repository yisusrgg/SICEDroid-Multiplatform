package com.example.sicedroidmultiplatform

import androidx.compose.runtime.*
import com.example.sicedroidmultiplatform.ui.screens.HomeScreen
import com.example.sicedroidmultiplatform.ui.screens.LoginScreen
import com.example.sicedroidmultiplatform.ui.theme.SicenetTheme
import com.example.sicedroidmultiplatform.ui.viewmodels.LoginUiState
import com.example.sicedroidmultiplatform.ui.viewmodels.SicenetViewModel

@Composable
fun App(viewModel: SicenetViewModel) {
    SicenetTheme {
        var showHome by remember { mutableStateOf(false) }
        val loginState by viewModel.loginState.collectAsState()

        if (loginState is LoginUiState.Success) {
            showHome = true
        }

        if (showHome) {
            HomeScreen(
                viewModel = viewModel,
                onLogout = {
                    viewModel.logout()
                    showHome = false
                }
            )
        } else {
            LoginScreen(viewModel = viewModel, onLoginSuccess = { showHome = true })
        }
    }
}
