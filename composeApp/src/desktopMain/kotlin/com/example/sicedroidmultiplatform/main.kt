package com.example.sicedroidmultiplatform

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.sicedroidmultiplatform.ui.viewmodels.SicenetViewModel

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SICEDroid - SICENET",
    ) {
        val viewModel = remember { SicenetViewModel.create() }
        App(viewModel = viewModel)
    }
}
