package com.example.sicedroidmultiplatform.data.repository

import com.example.sicedroidmultiplatform.data.model.*
import kotlinx.coroutines.flow.Flow

interface SicenetRepository {
    suspend fun login(matricula: String, password: String): LoginResponse
    fun clearSession()
    fun isLoggedIn(): Boolean
    fun saveCredentials(matricula: String, password: String)
    fun getStoredCredentials(): Pair<String, String>?

    // Local Data Flows
    fun getProfileFromDb(): Flow<PerfilAcademico?>
    fun getCardexFromDb(): Flow<List<CardexItem>>
    fun getCargaAcademicaFromDb(): Flow<List<Materia>>
    fun getCalificacionesFinalesFromDb(): Flow<List<CalificacionFinal>>
    fun getCalificacionesUnidadFromDb(): Flow<List<CalificacionUnidad>>

    // Peticiones a red
    suspend fun getUserProfile(): String?
    suspend fun getCardex(lineamiento: Int): String?
    suspend fun getCalificacionesFinales(modEducativo: Int): String?
    suspend fun getCalificacionesUnidad(): String?
    suspend fun getCargaAcademica(): String?

    //Guardar en la base de datos local
    suspend fun saveUserPerfilDb(jsonString : String)
    suspend fun saveCardexDb(jsonString : String)
    suspend fun saveCargaAcademicaDb(jsonString : String)
    suspend fun saveCalificacionesFinalesDb(jsonString : String)
    suspend fun saveCalificacionesUnidadDb(jsonString : String)
}