package com.example.sicedroidmultiplatform.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sicedroidmultiplatform.data.model.CalificacionUnidad
import com.example.sicedroidmultiplatform.ui.theme.*
import com.example.sicedroidmultiplatform.ui.viewmodels.SicenetViewModel

@Composable
fun CalificacionesUnidadScreen(viewModel: SicenetViewModel) {
    val califs   by viewModel.califUnidadState.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (califs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isOnline) {
                        CircularProgressIndicator()
                        Text("Cargando calificaciones...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text("Sin datos disponibles", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            // Column + verticalScroll en lugar de LazyColumn para evitar conflicto de
            // medición cuando AnimatedVisibility cambia el tamaño de un ítem
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                califs.forEach { item ->
                    UnidadCard(item)
                }
            }
        }
    }
}

@Composable
private fun UnidadCard(item: CalificacionUnidad) {
    var expanded by remember { mutableStateOf(false) }

    val displayUnidades = item.unidades

    val promedioValue = item.promedio.toFloatOrNull() ?: -1f
    val (promBg, promFg) = when {
        promedioValue >= 85 -> GradeGreenBg   to GradeGreenFg
        promedioValue >= 70 -> GradeAmberBg   to GradeAmberFg
        promedioValue >  0  -> GradeRedBg     to GradeRedFg
        else                -> GradeNeutralBg to GradeNeutralFg
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(promBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        item.promedio.ifBlank { "—" },
                        color = promFg,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    item.materia,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Colapsar" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit  = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                    displayUnidades.forEachIndexed { index, grade ->
                        UnidadRow(number = index + 1, grade = grade)
                    }
                }
            }
        }
    }
}

@Composable
private fun UnidadRow(number: Int, grade: String) {
    val value = grade.toFloatOrNull() ?: -1f
    val sinCalif = grade == "—" || grade == "0" || grade.isBlank()
    val (bg, fg) = when {
        sinCalif    -> GradeNeutralBg to GradeNeutralFg
        value >= 70 -> GradeGreenBg   to GradeGreenFg
        else        -> GradeRedBg     to GradeRedFg
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "Unidad $number",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            modifier = Modifier
                .background(bg, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                grade.ifBlank { "—" },
                color = fg,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
