package com.example.sicedroidmultiplatform.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ============================================================
// Paleta SICENET — Esmeralda Tecnológica (Tailwind Emerald)
// ============================================================
// Basada en la paleta emerald de Tailwind CSS, una de las más
// queridas por usuarios en apps modernas (Robinhood, fintech,
// edutech). Se ajustaron los tonos de header/footer para pasar
// WCAG AA de contraste (ratio ≥ 4.5:1 con texto blanco).
//
//  #10B981  emerald-500  → acento / estados activos (vivo)
//  #059669  emerald-600  → header / AppBar
//  #047857  emerald-700  → botones / footer (profundidad Opción 3)
//  #064E3B  emerald-900  → texto principal (elegante, casi negro)
//  #D1FAE5  emerald-100  → containers / chips
//  #ECFDF5  emerald-50   → superficies con tinte mínimo

val SicenetGreen        = Color(0xFF059669)  // emerald-600 — header / AppBar
val SicenetGreenDark    = Color(0xFF047857)  // emerald-700 — botones / footer
val SicenetGreenDarker  = Color(0xFF064E3B)  // emerald-900 — indicador activo / texto
val SicenetGreenLight   = Color(0xFF10B981)  // emerald-500 — acentos brillantes
val SicenetGreenPale    = Color(0xFFD1FAE5)  // emerald-100 — containers / chips
val SicenetGreenSurface = Color(0xFFECFDF5)  // emerald-50  — superficies
val SicenetGreenOnDark  = Color(0xFF064E3B)  // emerald-900 — texto sobre fondos claros

// Aliases de compatibilidad (LoginScreen los usa con nombres viejos)
val SicenetBlue      = SicenetGreen
val SicenetBlueDark  = SicenetGreenDark
val SicenetBlueLight = SicenetGreenLight

// === Colores semánticos para calificaciones ===
// Se mantienen distintos de la paleta esmeralda para no confundirse
val GradeGreenBg   = Color(0xFFDCFCE7)  // green-100
val GradeGreenFg   = Color(0xFF166534)  // green-800
val GradeAmberBg   = Color(0xFFFEF3C7)  // amber-100
val GradeAmberFg   = Color(0xFF92400E)  // amber-800
val GradeRedBg     = Color(0xFFFFEBEE)
val GradeRedFg     = Color(0xFFC62828)
val GradeNeutralBg = Color(0xFFF1F5F9)  // slate-100
val GradeNeutralFg = Color(0xFF475569)  // slate-600

private val SicenetColorScheme = lightColorScheme(
    primary              = SicenetGreen,          // #059669 — AppBar / título
    onPrimary            = Color.White,
    primaryContainer     = SicenetGreenPale,      // #D1FAE5 — chips y badges
    onPrimaryContainer   = SicenetGreenDarker,    // #064E3B
    secondary            = SicenetGreenDark,      // #047857 — footer / botones
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFA7F3D0),     // emerald-200
    onSecondaryContainer = SicenetGreenDarker,
    tertiary             = SicenetGreenDarker,    // #064E3B — estados profundos
    onTertiary           = Color.White,
    tertiaryContainer    = Color(0xFF6EE7B7),     // emerald-300
    onTertiaryContainer  = SicenetGreenDarker,
    background           = Color(0xFFF8FAFC),     // blanco-grisáceo (Opción 1, sin fatiga visual)
    onBackground         = Color(0xFF064E3B),     // verde casi-negro — títulos elegantes
    surface              = Color.White,
    onSurface            = Color(0xFF064E3B),
    surfaceVariant       = Color(0xFFECFDF5),     // emerald-50 — fondos de cards
    onSurfaceVariant     = Color(0xFF374151),     // gray-700 — texto secundario neutro
    outline              = Color(0xFF9CA3AF),     // gray-400 — bordes sutiles
    error                = Color(0xFFDC2626),     // red-600
    onError              = Color.White,
    errorContainer       = Color(0xFFFFDAD6),
    onErrorContainer     = Color(0xFF410002),
)

@Composable
fun SicenetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SicenetColorScheme,
        content = content
    )
}