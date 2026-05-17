package com.example.sicedroidmultiplatform.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sicedroidmultiplatform.data.model.CardexItem
import com.example.sicedroidmultiplatform.ui.theme.*
import com.example.sicedroidmultiplatform.ui.viewmodels.SicenetViewModel

private data class GradeColors(val bg: Color, val fg: Color)

private fun gradeColors(grade: String): GradeColors {
    val value = grade.toFloatOrNull() ?: -1f
    return when {
        value < 0   -> GradeColors(GradeNeutralBg, GradeNeutralFg)
        value >= 85 -> GradeColors(GradeGreenBg,   GradeGreenFg)
        value >= 70 -> GradeColors(GradeAmberBg,   GradeAmberFg)
        else        -> GradeColors(GradeRedBg,      GradeRedFg)
    }
}

@Composable
fun CardexScreen(viewModel: SicenetViewModel) {
    val cardex by viewModel.cardexState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (cardex.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Cargando kárdex...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cardex) { item ->
                    CardexCard(item)
                }
            }
        }
    }
}

@Composable
private fun CardexCard(item: CardexItem) {
    val colors = gradeColors(item.calificacion)

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
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Círculo con calificación
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(colors.bg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    item.calificacion.ifBlank { "—" },
                    color = colors.fg,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Datos
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    item.materia,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    GradeChip("Sem ${item.semestre}")
                    GradeChip("${item.creditos} créditos")
                }
            }
        }
    }
}

@Composable
private fun GradeChip(text: String) {
    Box(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}