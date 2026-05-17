package com.example.sicedroidmultiplatform.workers

import com.example.sicedroidmultiplatform.data.repository.SicenetRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual class SyncManager actual constructor(
    private val repository: SicenetRepository
) {
    private val settings = Settings()

    actual fun sincronizarDato(tipoSync: String, lineamiento: Int, modEducativo: Int) {
        // En Desktop simplemente lanzamos una tarea en segundo plano nativa
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val jsonDescargado = when (tipoSync) {
                    "PERFIL" -> repository.getUserProfile()
                    "CARGA_ACADEMICA" -> repository.getCargaAcademica()
                    "CARDEX" -> repository.getCardex(lineamiento)
                    "CALIF_UNIDAD" -> repository.getCalificacionesUnidad()
                    "CALIF_FINAL" -> repository.getCalificacionesFinales(modEducativo)
                    else -> null
                }

                if (!jsonDescargado.isNullOrEmpty()) {
                    when(tipoSync) {
                        "PERFIL" -> repository.saveUserPerfilDb(jsonDescargado)
                        "CARDEX" -> repository.saveCardexDb(jsonDescargado)
                        "CARGA_ACADEMICA" -> repository.saveCargaAcademicaDb(jsonDescargado)
                        "CALIF_UNIDAD" -> repository.saveCalificacionesUnidadDb(jsonDescargado)
                        "CALIF_FINAL" -> repository.saveCalificacionesFinalesDb(jsonDescargado)
                    }

                    // Guardamos la fecha de la misma forma que en Android
                    val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                    settings.putString("FECHA_ACT_$tipoSync", fecha)
                }
            } catch (e: Exception) {
                println("Error en Worker Desktop: ${e.message}")
            }
        }
    }
}