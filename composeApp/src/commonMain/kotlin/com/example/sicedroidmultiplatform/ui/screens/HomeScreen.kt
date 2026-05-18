package com.example.sicedroidmultiplatform.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.sicedroidmultiplatform.ui.theme.SicenetGreenDarker
import com.example.sicedroidmultiplatform.ui.theme.SicenetGreenPale
import com.example.sicedroidmultiplatform.ui.viewmodels.SicenetViewModel

private enum class HomeTab { PERFIL, CARGA, CARDEX, UNIDAD, FINAL }

private data class TabItem(
    val tab: HomeTab,
    val label: String,
    val icon: ImageVector,
    val title: String
)

private val tabItems = listOf(
    TabItem(HomeTab.PERFIL,  "Perfil",  Icons.Default.AccountCircle, "Perfil Académico"),
    TabItem(HomeTab.CARGA,   "Carga",   Icons.Default.DateRange,     "Carga Académica"),
    TabItem(HomeTab.CARDEX,  "Kárdex",  Icons.Default.School,        "Kárdex"),
    TabItem(HomeTab.UNIDAD,  "Unidad",  Icons.Default.List,          "Calif. por Unidad"),
    TabItem(HomeTab.FINAL,   "Final",   Icons.Default.Star,          "Calif. Finales")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: SicenetViewModel, onLogout: () -> Unit) {
    var currentTab   by remember { mutableStateOf(HomeTab.PERFIL) }
    val currentTitle  = tabItems.first { it.tab == currentTab }.title
    val isOnline     by viewModel.isOnline.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTitle, color = Color.White) },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Salir", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.secondary,  // emerald-700 footer
                tonalElevation = 0.dp
            ) {
                tabItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentTab == item.tab,
                        onClick  = { currentTab = item.tab },
                        icon  = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = Color.White,
                            selectedTextColor   = Color.White,
                            unselectedIconColor = SicenetGreenPale,
                            unselectedTextColor = SicenetGreenPale,
                            indicatorColor      = SicenetGreenDarker
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Banner de última actualización — aparece justo debajo del título, solo sin internet
            if (!isOnline) {
                val tipo = when (currentTab) {
                    HomeTab.PERFIL -> "PERFIL"
                    HomeTab.CARGA  -> "CARGA_ACADEMICA"
                    HomeTab.CARDEX -> "CARDEX"
                    HomeTab.UNIDAD -> "CALIF_UNIDAD"
                    HomeTab.FINAL  -> "CALIF_FINAL"
                }
                val fecha = viewModel.getFechaActualizacion(tipo)
                if (fecha.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "Última actualización: $fecha",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (currentTab) {
                    HomeTab.PERFIL  -> ProfileScreen(viewModel = viewModel)
                    HomeTab.CARGA   -> CargaAcademicaScreen(viewModel = viewModel)
                    HomeTab.CARDEX  -> CardexScreen(viewModel = viewModel)
                    HomeTab.UNIDAD  -> CalificacionesUnidadScreen(viewModel = viewModel)
                    HomeTab.FINAL   -> CalificacionFinalScreen(viewModel = viewModel)
                }
            }
        }
    }
}