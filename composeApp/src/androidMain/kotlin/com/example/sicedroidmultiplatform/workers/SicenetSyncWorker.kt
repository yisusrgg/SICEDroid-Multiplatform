package com.example.sicedroidmultiplatform.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sicedroidmultiplatform.data.local.getRoomDatabase
import com.example.sicedroidmultiplatform.data.network.SicenetService
import com.example.sicedroidmultiplatform.data.network.provideHttpClient
import com.example.sicedroidmultiplatform.data.repository.SicenetRepositoryImpl
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SicenetSyncWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Instanciamos las dependencias
            val client = provideHttpClient()
            val service = SicenetService(client)
            val dao = getRoomDatabase().sicenetDao()
            val repository = SicenetRepositoryImpl(service, dao)
            val settings = Settings()

            // Extraemos los parámetros
            val tipoSync = inputData.getString("TIPO_SYNC") ?: return@withContext Result.failure()
            val lineamiento = inputData.getInt("LINEAMIENTO", 0)
            val modEducativo = inputData.getInt("MOD_EDUCATIVO", 1)

            // recuperamos la sesión antes de pedir datos
            val creds = repository.getStoredCredentials()
            if (creds == null) {
                // Si por alguna razón cerraron sesión, cancelamos el worker
                return@withContext Result.failure()
            }

            val loginResult = repository.login(creds.first, creds.second)
            if (!loginResult.success) {
                // Si el SICE está caído o no hay buen internet, pedimos que reintente luego
                return@withContext Result.retry()
            }

            // FETCH (Descarga ya autenticada)
            val jsonDescargado = when (tipoSync) {
                "PERFIL" -> repository.getUserProfile()
                "CARGA_ACADEMICA" -> repository.getCargaAcademica()
                "CARDEX" -> repository.getCardex(lineamiento)
                "CALIF_UNIDAD" -> repository.getCalificacionesUnidad()
                "CALIF_FINAL" -> repository.getCalificacionesFinales(modEducativo)
                else -> null
            }

            if (jsonDescargado.isNullOrEmpty()) return@withContext Result.retry()

            // SAVE (Guardado local)
            when (tipoSync) {
                "PERFIL" -> repository.saveUserPerfilDb(jsonDescargado)
                "CARDEX" -> repository.saveCardexDb(jsonDescargado)
                "CARGA_ACADEMICA" -> repository.saveCargaAcademicaDb(jsonDescargado)
                "CALIF_UNIDAD" -> repository.saveCalificacionesUnidadDb(jsonDescargado)
                "CALIF_FINAL" -> repository.saveCalificacionesFinalesDb(jsonDescargado)
            }

            // Guardar la fecha
            val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            settings.putString("FECHA_ACT_$tipoSync", fecha)

            Result.success()
        } catch (throwable: Throwable) {
            Result.retry()
        }
    }
}