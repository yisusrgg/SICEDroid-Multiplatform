package com.example.sicedroidmultiplatform.data.repository

import com.example.sicedroidmultiplatform.data.local.CalificacionFinalEntity
import com.example.sicedroidmultiplatform.data.local.CalificacionUnidadEntity
import com.example.sicedroidmultiplatform.data.local.CardexEntity
import com.example.sicedroidmultiplatform.data.local.MateriaEntity
import com.example.sicedroidmultiplatform.data.model.*
import com.example.sicedroidmultiplatform.data.network.SicenetService
import com.example.sicedroidmultiplatform.data.network.extractTagValue
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.*
import com.example.sicedroidmultiplatform.data.local.SicenetDao
import com.example.sicedroidmultiplatform.data.local.PerfilEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.russhwolf.settings.Settings

class SicenetRepositoryImpl(
    private val service: SicenetService,
    private val sicenetDao: SicenetDao
) : SicenetRepository {

    //LECTURA LOCAL =================================================================
    //Las usa ViewModel
    override fun getProfileFromDb(): Flow<PerfilAcademico?> = sicenetDao.getPerfil().map { entity ->
        entity?.let {
            PerfilAcademico(
                nombre = it.nombre,
                matricula = it.matricula,
                carrera = it.carrera,
                especialidad = it.especialidad,
                semActual = it.semActual,
                cdtosAcumulados = it.cdtosAcumulados,
                cdtosActuales = it.cdtosActuales,
                estatus = it.estatus,
                inscrito = it.inscrito,
                adeudo = it.adeudo,
                fechaReins = it.fechaReins,
                modEducativo = it.modEducativo,
                urlFoto = it.urlFoto,
                lineamiento = it.lineamiento,
                adeudoDescripcion = it.adeudoDescripcion,
            )
        }
    }

    override fun getCalificacionesFinalesFromDb(): Flow<List<CalificacionFinal>> =
        sicenetDao.getCalificacionesFinales().map { list ->
            list.map { CalificacionFinal(it.materia, it.calificacion) }
        }

    override fun getCalificacionesUnidadFromDb(): Flow<List<CalificacionUnidad>> =
        sicenetDao.getCalificacionesUnidades().map { list ->
            list.map { CalificacionUnidad(it.materia, it.unidades, it.promedio) }
        }

    override fun getCardexFromDb(): Flow<List<CardexItem>> =
        sicenetDao.getCardex().map { list ->
            list.map {
                CardexItem(it.materia, it.calificacion,
                    it.semestre, it.creditos,it.estatus
                )
            }
        }

    override fun getCargaAcademicaFromDb(): Flow<List<Materia>> =
        sicenetDao.getCargaAcademica().map { list ->
            list.map {
                Materia(
                    it.docente, it.clvOficial,
                    it.estadoMateria, it.creditosMateria,
                    it.materia, it.grupo, it.lunes,
                    it.martes, it.miercoles,
                    it.jueves, it.viernes, it.sabado
                )
            }
        }



    //PETICIONES DE RED ==============================================================

    override suspend fun login(matricula: String, password: String): LoginResponse {
        return try {
            val responseString = service.login(matricula, password)
            val result = extractTagValue(responseString, "accesoLoginResult")
            if (result != null && result.contains("\"acceso\":true", ignoreCase = true)) {
                sicenetDao.clearAllData()
                settings.putBoolean("isLoggedIn", true) //guardamos la sesion como activa
                LoginResponse(true, "Login exitoso")
            } else {
                LoginResponse(false, "Credenciales incorrectas")
            }
        } catch (e: Exception) {
            LoginResponse(false, e.message ?: "Error de red")
        }
    }

    override suspend fun getUserProfile(): String? {
        return try {
            val response = service.getProfile()
            extractTagValue(response, "getAlumnoAcademicoWithLineamientoResult")
        } catch (e: Exception) { null }
    }

    override suspend fun getCalificacionesFinales(modEducativo: Int): String? {
        return try {
            val response = service.getCalifFinal(modEducativo.toString())
            extractTagValue(response, "getAllCalifFinalByAlumnosResult")
        } catch (e: Exception) { null }
    }

    override suspend fun getCalificacionesUnidad(): String? {
        return try {
            val response = service.getCalifUnidad()
            val jsonResult = extractTagValue(response, "getCalifUnidadesByAlumnoResult")
            jsonResult
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getCardex(lineamiento: Int): String? {
        return try {
            val response = service.getCardex(lineamiento.toString())
            val jsonResult = extractTagValue(response, "getAllKardexConPromedioByAlumnoResult")
            return jsonResult
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getCargaAcademica(): String? {
        return try {
            val response = service.getCargaAcademica()
            val jsonResult = extractTagValue(response, "getCargaAcademicaByAlumnoResult")
            return jsonResult
        } catch (e: Exception) {
            null
        }
    }


    //GUARDAR LA RESPUESTA DE INTERNTE EN LA BASE DE DATOS ---------------------------
    override suspend fun saveUserPerfilDb(jsonString: String) {
        if (jsonString.isEmpty()) return
        val json = Json.parseToJsonElement(jsonString).jsonObject
        val entity = PerfilEntity(
            matricula = json["matricula"]?.jsonPrimitive?.contentOrNull ?: "",
            nombre = json["nombre"]?.jsonPrimitive?.contentOrNull ?: "",
            carrera = json["carrera"]?.jsonPrimitive?.contentOrNull ?: "",
            especialidad = json["especialidad"]?.jsonPrimitive?.contentOrNull ?: "",
            semActual = json["semActual"]?.jsonPrimitive?.intOrNull ?: 0,
            cdtosAcumulados = json["cdtosAcumulados"]?.jsonPrimitive?.intOrNull ?: 0,
            cdtosActuales = json["cdtosActuales"]?.jsonPrimitive?.intOrNull ?: 0,
            estatus = json["estatus"]?.jsonPrimitive?.contentOrNull ?: "",
            inscrito = json["inscrito"]?.jsonPrimitive?.booleanOrNull ?: false,
            adeudo = json["adeudo"]?.jsonPrimitive?.booleanOrNull ?: false,
            fechaReins = json["fechaReins"]?.jsonPrimitive?.contentOrNull ?: "",
            modEducativo = json["modEducativo"]?.jsonPrimitive?.intOrNull ?: 0,
            urlFoto = json["urlFoto"]?.jsonPrimitive?.contentOrNull ?: "",
            adeudoDescripcion = json["adeudoDescripcion"]?.jsonPrimitive?.contentOrNull ?: "",
            lineamiento = json["lineamiento"]?.jsonPrimitive?.intOrNull ?: 0
        )
        sicenetDao.insertPerfil(entity)
    }

    override suspend fun saveCargaAcademicaDb(jsonString: String) {
        if (jsonString.isEmpty()) return
        try {
            val jsonArray = Json.parseToJsonElement(jsonString).jsonArray
            val entities = jsonArray.map { element ->
                val obj = element.jsonObject
                MateriaEntity(
                    clvOficial = obj["clvOficial"]?.jsonPrimitive?.contentOrNull ?: "",
                    docente = obj["Docente"]?.jsonPrimitive?.contentOrNull ?: "",
                    materia = obj["Materia"]?.jsonPrimitive?.contentOrNull ?: "",
                    grupo = obj["Grupo"]?.jsonPrimitive?.contentOrNull ?: "",
                    creditosMateria = obj["CreditosMateria"]?.jsonPrimitive?.intOrNull ?: 0,
                    estadoMateria = obj["EstadoMateria"]?.jsonPrimitive?.contentOrNull ?: "",
                    lunes = obj["Lunes"]?.jsonPrimitive?.contentOrNull ?: "",
                    martes = obj["Martes"]?.jsonPrimitive?.contentOrNull ?: "",
                    miercoles = obj["Miercoles"]?.jsonPrimitive?.contentOrNull ?: "",
                    jueves = obj["Jueves"]?.jsonPrimitive?.contentOrNull ?: "",
                    viernes = obj["Viernes"]?.jsonPrimitive?.contentOrNull ?: "",
                    sabado = obj["Sabado"]?.jsonPrimitive?.contentOrNull ?: ""
                )
            }
            sicenetDao.deleteCargaAcademica()
            sicenetDao.insertCargaAcademica(entities)
        } catch (e: Exception) {
            println("Error guardando carga académica: ${e.message}")
        }
    }

    override suspend fun saveCardexDb(jsonString: String){
        if (jsonString.isEmpty()) return
        try {
            val jsonObject = Json.parseToJsonElement(jsonString).jsonObject
            val jsonArray = jsonObject["lstKardex"]?.jsonArray ?: return
            val entities = jsonArray.map { element ->
                val obj = element.jsonObject
                CardexEntity(
                    materia = obj["Materia"]?.jsonPrimitive?.contentOrNull ?: "",
                    calificacion = obj["Calif"]?.jsonPrimitive?.contentOrNull ?: "",
                    semestre = obj["S1"]?.jsonPrimitive?.contentOrNull ?: "",
                    creditos = obj["Cdts"]?.jsonPrimitive?.contentOrNull ?: "",
                    estatus = obj["Acred"]?.jsonPrimitive?.contentOrNull ?: ""
                )
            }
            sicenetDao.deleteCardex()
            sicenetDao.insertCardex(entities)
        }catch (e: Exception) {
            println("Error guardando Cardex: ${e.message}")
        }
    }

    override suspend fun saveCalificacionesFinalesDb(jsonString: String){
        if (jsonString.isEmpty()) return
        try {
            val jsonArray = Json.parseToJsonElement(jsonString).jsonArray
            val entities = jsonArray.map { element ->
                val obj = element.jsonObject
                val materia = obj["Materia"]?.jsonPrimitive?.contentOrNull
                    ?: obj["materia"]?.jsonPrimitive?.contentOrNull ?: ""
                val calif = obj["Calif"]?.jsonPrimitive?.contentOrNull
                    ?: obj["calif"]?.jsonPrimitive?.contentOrNull ?: ""

                CalificacionFinalEntity(
                    materia = materia,
                    calificacion = calif
                )
            }
            sicenetDao.deleteCalificacionesFinales()
            sicenetDao.insertCalificacionesFinales(entities)
        }catch (e: Exception) {
            println("Error guardando Calificaciones Finales: ${e.message}")
        }
    }

    override suspend fun saveCalificacionesUnidadDb(jsonString: String){
        if (jsonString.isEmpty()) return
        try {
            val jsonArray = Json.parseToJsonElement(jsonString).jsonArray
            val entities = jsonArray.map { element ->
                val obj = element.jsonObject
                val unidadesActivas = obj["UnidadesActivas"]?.jsonPrimitive?.intOrNull ?: 0
                val unidades = mutableListOf<String>()
                for (u in 1..unidadesActivas) {
                    val cal = obj["C$u"]?.jsonPrimitive?.contentOrNull
                    unidades.add(if (cal == "null" || cal.isNullOrEmpty()) "0" else cal)
                }
                val validGrades = unidades.mapNotNull { it.toIntOrNull() }
                val promedio = if (validGrades.isNotEmpty()) {
                    validGrades.average().toInt().toString()
                } else "0"
                CalificacionUnidadEntity(
                    materia = obj["Materia"]?.jsonPrimitive?.contentOrNull ?: "",
                    unidades = unidades,
                    promedio = promedio
                )
            }
            sicenetDao.deleteCalificacionesUnidades()
            sicenetDao.insertCalificacionesUnidades(entities)
        }catch (e: Exception) {
            println("Error guardando Calificaciones por Unidad: ${e.message}")
        }
    }

    //CONFIGURACIONES ===================================================
    private val settings = Settings()
    override fun isLoggedIn(): Boolean {
        // Devuelve 'true' si existe la sesión, si no, 'false' por defecto
        return settings.getBoolean("isLoggedIn", false)
    }

    override fun clearSession() {
        //Borramos las preferencias al cerar sesion
        settings.clear()
        // En lugar de SharedPreferences (que no existe en Windows), simplemente limpiamos la BD
        kotlinx.coroutines.MainScope().launch {
            sicenetDao.clearAllData()
        }
    }

    private fun extractTagValue(xml: String, tag: String): String? {
        val openTag = "<$tag>"
        val closeTag = "</$tag>"
        val startIndex = xml.indexOf(openTag)
        val endIndex = xml.indexOf(closeTag)
        return if (startIndex != -1 && endIndex != -1) xml.substring(startIndex + openTag.length, endIndex) else null
    }
}