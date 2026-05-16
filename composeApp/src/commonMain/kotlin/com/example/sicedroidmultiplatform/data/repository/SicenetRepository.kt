package com.example.sicedroidmultiplatform.data.repository

import com.example.sicedroidmultiplatform.data.model.*
import kotlinx.coroutines.flow.Flow

interface SicenetRepository {
    suspend fun login(matricula: String, password: String): LoginResponse
    suspend fun getPerfil(): PerfilAcademico
    suspend fun getCargaAcademica(): List<Materia>
    suspend fun getCardex(lineamiento: String): List<CardexItem>
    suspend fun getCalificacionesFinales(modEducativo: String): List<CalificacionFinal>
    suspend fun getCalificacionesUnidad(): List<CalificacionUnidad>
    fun clearSession()
}