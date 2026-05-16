package com.example.sicedroidmultiplatform.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sicedroidmultiplatform.ui.viewmodels.SicenetViewModel

@Composable
fun ProfileScreen(viewModel: SicenetViewModel, onLogout: () -> Unit) {
    val perfil by viewModel.profileState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Perfil académico", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onLogout) { Text("Cerrar sesión") }
        }
        HorizontalDivider()

        if (perfil == null) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            InfoRow("Nombre",              perfil!!.nombre)
            InfoRow("Matrícula",           perfil!!.matricula)
            InfoRow("Carrera",             perfil!!.carrera)
            InfoRow("Especialidad",        perfil!!.especialidad)
            InfoRow("Semestre actual",     perfil!!.semActual)
            InfoRow("Créditos acumulados", perfil!!.cdtosAcumulados)
            InfoRow("Créditos actuales",   perfil!!.cdtosActuales)
            InfoRow("Estatus",             perfil!!.estatus)
            InfoRow("Inscrito",            perfil!!.inscrito)
            InfoRow("Adeudo",              perfil!!.adeudo)
            InfoRow("Fecha reinscripción", perfil!!.fechaReins)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
