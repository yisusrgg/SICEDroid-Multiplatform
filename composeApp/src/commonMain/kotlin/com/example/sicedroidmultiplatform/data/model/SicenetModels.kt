package com.example.sicedroidmultiplatform.data.model

data class LoginResponse(
    val success: Boolean,
    val cookie: String? = null,
    val message: String = ""
)

data class PerfilAcademico(
    val nombre: String = "",
    val matricula: String = "",
    val carrera: String = "",
    val especialidad: String = "",
    val semActual: String = "",
    val cdtosAcumulados: String = "",
    val cdtosActuales: String = "",
    val estatus: String = "",
    val inscrito: String = "",
    val adeudo: String = "",
    val fechaReins: String = "",
    val modEducativo: String = "",
    val urlFoto: String = "",
    val lineamiento: String = ""
)

data class Materia(
    val clvOficial: String = "",
    val docente: String = "",
    val materia: String = "",
    val grupo: String = "",
    val creditos: String = "",
    val estadoMateria: String = "",
    val lunes: String = "",
    val martes: String = "",
    val miercoles: String = "",
    val jueves: String = "",
    val viernes: String = "",
    val sabado: String = ""
)

data class CalificacionFinal(
    val materia: String = "",
    val calificacion: String = ""
)

data class CalificacionUnidad(
    val materia: String = "",
    val unidades: List<String> = emptyList(),
    val promedio: String = ""
)

data class CardexItem(
    val semestre: String = "",
    val materia: String = "",
    val creditos: String = "",
    val calificacion: String = "",
    val acreditada: String = ""
)