package com.example.sicedroidmultiplatform.data.repository

import com.example.sicedroidmultiplatform.data.model.*
import com.example.sicedroidmultiplatform.data.network.SicenetService
import com.example.sicedroidmultiplatform.data.network.extractTagValue
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.*

class SicenetRepositoryImpl(
    private val service: SicenetService
) : SicenetRepository {

    override suspend fun login(matricula: String, password: String): LoginResponse {
        return try {
            val response = service.login(matricula, password)
            val result = extractTagValue(response, "accesoLoginResult")
                ?: return LoginResponse(
                    success = false,
                    message = "Tag no encontrado. Respuesta del servidor: ${response.take(300)}"
                )
            val acceso = result.contains("\"acceso\":true", ignoreCase = true)
            LoginResponse(
                success = acceso,
                message = if (acceso) "Autenticación exitosa" else "Credenciales incorrectas"
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoginResponse(success = false, message = e.message ?: "Error de conexión")
        }
    }

    override suspend fun getPerfil(): PerfilAcademico {
        return try {
            val response = service.getProfile()
            val result = extractTagValue(response, "getAlumnoAcademicoWithLineamientoResult") ?: "{}"
            val json = Json.parseToJsonElement(result).jsonObject
            PerfilAcademico(
                nombre = json["nombre"]?.jsonPrimitive?.contentOrNull ?: "",
                matricula = json["matricula"]?.jsonPrimitive?.contentOrNull ?: "",
                carrera = json["carrera"]?.jsonPrimitive?.contentOrNull ?: "",
                especialidad = json["especialidad"]?.jsonPrimitive?.contentOrNull ?: "",
                semActual = json["semActual"]?.jsonPrimitive?.contentOrNull ?: "",
                cdtosAcumulados = json["cdtosAcumulados"]?.jsonPrimitive?.contentOrNull ?: "",
                cdtosActuales = json["cdtosActuales"]?.jsonPrimitive?.contentOrNull ?: "",
                estatus = json["estatus"]?.jsonPrimitive?.contentOrNull ?: "",
                inscrito = json["inscrito"]?.jsonPrimitive?.contentOrNull ?: "",
                adeudo = json["adeudo"]?.jsonPrimitive?.contentOrNull ?: "",
                fechaReins = json["fechaReins"]?.jsonPrimitive?.contentOrNull ?: "",
                modEducativo = json["modEducativo"]?.jsonPrimitive?.contentOrNull ?: "",
                urlFoto = json["urlFoto"]?.jsonPrimitive?.contentOrNull ?: "",
                lineamiento = json["lineamiento"]?.jsonPrimitive?.contentOrNull ?: ""
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            PerfilAcademico()
        }
    }

    override suspend fun getCargaAcademica(): List<Materia> {
        return try {
            val response = service.getCargaAcademica()
            val result = extractTagValue(response, "getCargaAcademicaByAlumnoResult") ?: "[]"
            val array = Json.parseToJsonElement(result).jsonArray
            array.map { element ->
                val obj = element.jsonObject
                Materia(
                    clvOficial = obj["clvOficial"]?.jsonPrimitive?.contentOrNull ?: "",
                    docente = obj["Docente"]?.jsonPrimitive?.contentOrNull ?: "",
                    materia = obj["Materia"]?.jsonPrimitive?.contentOrNull ?: "",
                    grupo = obj["Grupo"]?.jsonPrimitive?.contentOrNull ?: "",
                    creditos = obj["CreditosMateria"]?.jsonPrimitive?.contentOrNull ?: "",
                    estadoMateria = obj["EstadoMateria"]?.jsonPrimitive?.contentOrNull ?: "",
                    lunes = obj["Lunes"]?.jsonPrimitive?.contentOrNull ?: "",
                    martes = obj["Martes"]?.jsonPrimitive?.contentOrNull ?: "",
                    miercoles = obj["Miercoles"]?.jsonPrimitive?.contentOrNull ?: "",
                    jueves = obj["Jueves"]?.jsonPrimitive?.contentOrNull ?: "",
                    viernes = obj["Viernes"]?.jsonPrimitive?.contentOrNull ?: "",
                    sabado = obj["Sabado"]?.jsonPrimitive?.contentOrNull ?: ""
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getCardex(lineamiento: String): List<CardexItem> {
        return try {
            val response = service.getCardex(lineamiento)
            val result = extractTagValue(response, "getAllKardexConPromedioByAlumnoResult") ?: "[]"
            val json = Json.parseToJsonElement(result).jsonObject
            val array = json["lstKardex"]?.jsonArray ?: return emptyList()
            array.map { element ->
                val obj = element.jsonObject
                CardexItem(
                    semestre = obj["S1"]?.jsonPrimitive?.contentOrNull ?: "",
                    materia = obj["Materia"]?.jsonPrimitive?.contentOrNull ?: "",
                    creditos = obj["Cdts"]?.jsonPrimitive?.contentOrNull ?: "",
                    calificacion = obj["Calif"]?.jsonPrimitive?.contentOrNull ?: "",
                    acreditada = obj["Acred"]?.jsonPrimitive?.contentOrNull ?: ""
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getCalificacionesFinales(modEducativo: String): List<CalificacionFinal> {
        return try {
            val response = service.getCalifFinal(modEducativo)
            val result = extractTagValue(response, "getAllCalifFinalByAlumnosResult") ?: "[]"
            val array = Json.parseToJsonElement(result).jsonArray
            array.map { element ->
                val obj = element.jsonObject
                CalificacionFinal(
                    materia = obj["Materia"]?.jsonPrimitive?.contentOrNull
                        ?: obj["materia"]?.jsonPrimitive?.contentOrNull ?: "",
                    calificacion = obj["Calif"]?.jsonPrimitive?.contentOrNull
                        ?: obj["calif"]?.jsonPrimitive?.contentOrNull ?: ""
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getCalificacionesUnidad(): List<CalificacionUnidad> {
        return try {
            val response = service.getCalifUnidad()
            val result = extractTagValue(response, "getCalifUnidadesByAlumnoResult") ?: "[]"
            val array = Json.parseToJsonElement(result).jsonArray
            array.map { element ->
                val obj = element.jsonObject
                val materia = obj["Materia"]?.jsonPrimitive?.contentOrNull ?: ""
                val unidadesActivas = obj["UnidadesActivas"]?.jsonPrimitive?.intOrNull ?: 0
                val califs = (1..unidadesActivas).mapNotNull { i ->
                    obj["C$i"]?.jsonPrimitive?.contentOrNull
                }
                val promedio = if (califs.isNotEmpty()) {
                    val sum = califs.mapNotNull { it.toDoubleOrNull() }.sum()
                    String.format("%.1f", sum / califs.size)
                } else ""
                CalificacionUnidad(materia = materia, unidades = califs, promedio = promedio)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun clearSession() {
        // Se limpia al recrear el HttpClient; la sesión vive en memoria (HttpCookies)
    }
}