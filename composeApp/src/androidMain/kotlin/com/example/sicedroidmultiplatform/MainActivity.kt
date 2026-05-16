package com.example.sicedroidmultiplatform

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sicedroidmultiplatform.data.local.appContext
import com.example.sicedroidmultiplatform.ui.viewmodels.SicenetViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        appContext = this.applicationContext
        setContent {
            val viewModel: SicenetViewModel = viewModel { SicenetViewModel.create() }
            App(viewModel = viewModel)
        }
    }
}
