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
                CardexItem(
                    semestre    = it.semestre,
                    materia     = it.materia,
                    creditos    = it.creditos,
                    calificacion = it.calificacion,
                    acreditada  = it.estatus
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
                settings.putBoolean("isLoggedIn", true)
                saveCredentials(matricula, password)
                LoginResponse(success = true, message = "Login exitoso")
            } else {
                LoginResponse(success = false, message = "Credenciales incorrectas")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoginResponse(success = false, message = e.message?.takeIf { it.isNotBlank() } ?: "Error de red")
        }
    }

    override fun saveCredentials(matricula: String, password: String) {
        settings.putString("cred_mat", matricula)
        settings.putString("cred_pwd", password)
    }

    override fun getStoredCredentials(): Pair<String, String>? {
        val mat = settings.getStringOrNull("cred_mat") ?: return null
        val pwd = settings.getStringOrNull("cred_pwd") ?: return null
        return if (mat.isNotBlank() && pwd.isNotBlank()) mat to pwd else null
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

                // "UnidadesActivas" en SICENET es una cadena de "1"s: su LONGITUD es el
                // número real de unidades de esa materia ("111" → 3 unidades).
                // SICENET siempre envía C1..C13 para todas las materias, así que usar la
                // longitud de esta cadena es la única forma fiable de saber el límite real.
                val numUnidades = obj["UnidadesActivas"]?.jsonPrimitive?.contentOrNull?.length ?: 0
                val limite = if (numUnidades > 0) numUnidades else 20

                val raw = mutableListOf<String>()
                for (u in 1..limite) {
                    val calEntry = obj["C$u"] ?: break
                    val cal = calEntry.jsonPrimitive.contentOrNull
                    // null / "null" / "0" / vacío → sin calificar
                    raw.add(if (cal == null || cal == "null" || cal.isEmpty() || cal == "0") "—" else cal)
                }

                // Si UnidadesActivas no estaba disponible, eliminamos los "—" de cola para
                // no guardar campos de relleno que SICENET envía más allá de las unidades reales.
                val unidades: List<String> = if (numUnidades > 0) {
                    raw
                } else {
                    val last = raw.indexOfLast { it != "—" }
                    if (last >= 0) raw.take(last + 1) else raw
                }

                // Promedio solo con calificaciones numéricas reales (excluye "—")
                val grades = unidades.mapNotNull { g -> g.toIntOrNull()?.takeIf { it > 0 } }
                val promedio = if (grades.isNotEmpty()) grades.average().toInt().toString() else "—"
                CalificacionUnidadEntity(
                    materia = obj["Materia"]?.jsonPrimitive?.contentOrNull ?: "",
                    unidades = unidades,
                    promedio = promedio
                )
            }
            sicenetDao.deleteCalificacionesUnidades()
            sicenetDao.insertCalificacionesUnidades(entities)
        } catch (e: Exception) {
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