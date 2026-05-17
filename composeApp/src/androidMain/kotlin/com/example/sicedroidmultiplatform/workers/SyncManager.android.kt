package com.example.sicedroidmultiplatform.workers

import androidx.work.*
import com.example.sicedroidmultiplatform.data.local.appContext
import com.example.sicedroidmultiplatform.data.repository.SicenetRepository

actual class SyncManager actual constructor(
    private val repository: SicenetRepository
) {
    private val workManager = WorkManager.getInstance(appContext)

    actual fun sincronizarDato(tipoSync: String, lineamiento: Int, modEducativo: Int) {
        val input = workDataOf(
            "TIPO_SYNC" to tipoSync,
            "LINEAMIENTO" to lineamiento,
            "MOD_EDUCATIVO" to modEducativo
        )

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Como unificamos el worker, ahora solo ocupas UNA petición
        val request = OneTimeWorkRequestBuilder<SicenetSyncWorker>()
            .setInputData(input)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            "Sync_$tipoSync",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}