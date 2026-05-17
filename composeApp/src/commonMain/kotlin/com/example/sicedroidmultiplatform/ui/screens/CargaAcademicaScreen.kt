package com.example.sicedroidmultiplatform.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sicedroidmultiplatform.data.model.Materia
import com.example.sicedroidmultiplatform.ui.viewmodels.SicenetViewModel

private val DIAS = listOf("LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO")

private fun horarioDia(materia: Materia, dayIndex: Int): String = when (dayIndex) {
    0 -> materia.lunes
    1 -> materia.martes
    2 -> materia.miercoles
    3 -> materia.jueves
    4 -> materia.viernes
    5 -> materia.sabado
    else -> ""
}

private fun tieneClase(horario: String) =
    horario.isNotBlank() && horario.lowercase() != "null"

@Composable
fun CargaAcademicaScreen(viewModel: SicenetViewModel) {
    val materias   by viewModel.cargaState.collectAsState()
    var selectedDay by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tabs de días
        ScrollableTabRow(
            selectedTabIndex = selectedDay,
            containerColor   = MaterialTheme.colorScheme.primary,
            contentColor     = Color.White,
            edgePadding      = 0.dp
        ) {
            DIAS.forEachIndexed { index, dia ->
                Tab(
                    selected = selectedDay == index,
                    onClick  = { selectedDay = index },
                    text = {
                        Text(
                            dia,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedDay == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selectedContentColor   = Color.White,
                    unselectedContentColor = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        if (materias.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Cargando horario...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            val materiasDia = materias
                .filter { tieneClase(horarioDia(it, selectedDay)) }
                .sortedBy   { horarioDia(it, selectedDay) }

            if (materiasDia.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Sin clases este día",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(materiasDia) { materia ->
                        MateriaCard(
                            materia = materia,
                            horario = horarioDia(materia, selectedDay)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MateriaCard(materia: Materia, horario: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = horario,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Datos de la materia
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    materia.materia,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    materia.docente,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (materia.grupo.isNotBlank() && materia.grupo.lowercase() != "null") {
                    Text(
                        "Grupo: ${materia.grupo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}