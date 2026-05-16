package com.example.sicedroidmultiplatform.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.sicedroidmultiplatform.ui.viewmodels.SicenetViewModel

private enum class HomeTab { PERFIL, CARGA, CARDEX, UNIDAD, FINAL }

private data class TabItem(val tab: HomeTab, val label: String, val icon: ImageVector)

private val tabItems = listOf(
    TabItem(HomeTab.PERFIL,  "Perfil",  Icons.Default.AccountCircle),
    TabItem(HomeTab.CARGA,   "Carga",   Icons.Default.DateRange),
    TabItem(HomeTab.CARDEX,  "Cardex",  Icons.Default.Menu),
    TabItem(HomeTab.UNIDAD,  "Unidad",  Icons.Default.List),
    TabItem(HomeTab.FINAL,   "Final",   Icons.Default.Star)
)

@Composable
fun HomeScreen(viewModel: SicenetViewModel, onLogout: () -> Unit) {
    var currentTab by remember { mutableStateOf(HomeTab.PERFIL) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentTab == item.tab,
                        onClick = { currentTab = item.tab },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentTab) {
                HomeTab.PERFIL  -> ProfileScreen(viewModel = viewModel, onLogout = onLogout)
                HomeTab.CARGA   -> PlaceholderScreen("Carga académica")
                HomeTab.CARDEX  -> PlaceholderScreen("Cardex")
                HomeTab.UNIDAD  -> PlaceholderScreen("Calificaciones por unidad")
                HomeTab.FINAL   -> PlaceholderScreen("Calificaciones finales")
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                "Próximamente",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
