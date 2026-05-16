package com.example.sicedroidmultiplatform.data.network

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

private const val HOST = "https://sicenet.itsur.edu.mx"
private const val BASE_URL = "$HOST/ws/wsalumnos.asmx"

fun extractTagValue(xml: String, tag: String): String? {
    val openMatch = Regex("<(?:[a-zA-Z0-9_]+:)?$tag(?:\\s[^>]*)?>").find(xml) ?: return null
    val contentStart = openMatch.range.last + 1
    val closeMatch = Regex("</?(?:[a-zA-Z0-9_]+:)?$tag>").find(xml, contentStart) ?: return null
    return xml.substring(contentStart, closeMatch.range.first)
}

class SicenetService(private val client: HttpClient) {

    // El servidor ASP.NET redirige el primer POST a una URL de sesión (301).
    // HttpRedirect sigue el redirect como GET (igual que OkHttp), lo que establece las cookies.
    // Luego el servidor devuelve HTML (listado del servicio SOAP para peticiones GET).
    // Cuando no hay "Envelope" en la respuesta, se reintenta el POST: ahora con cookie de sesión,
    // el servidor acepta el POST directamente y devuelve el XML SOAP correcto.
    private suspend fun soapPost(soapAction: String, body: String): String {
        var lastResponse = ""
        repeat(3) {
            lastResponse = client.post(BASE_URL) {
                header("SOAPAction", "\"http://tempuri.org/$soapAction\"")
                contentType(ContentType.parse("text/xml; charset=utf-8"))
                setBody(body)
            }.bodyAsText()
            if (lastResponse.contains("Envelope", ignoreCase = true)) return lastResponse
        }
        return lastResponse
    }

    suspend fun login(matricula: String, password: String): String {
        return soapPost("accesoLogin", """<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xmlns:xsd="http://www.w3.org/2001/XMLSchema"
               xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <accesoLogin xmlns="http://tempuri.org/">
      <strMatricula>$matricula</strMatricula>
      <strContrasenia>$password</strContrasenia>
      <tipoUsuario>ALUMNO</tipoUsuario>
    </accesoLogin>
  </soap:Body>
</soap:Envelope>""")
    }

    suspend fun getProfile(): String {
        return soapPost("getAlumnoAcademicoWithLineamiento", """<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xmlns:xsd="http://www.w3.org/2001/XMLSchema"
               xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <getAlumnoAcademicoWithLineamiento xmlns="http://tempuri.org/" />
  </soap:Body>
</soap:Envelope>""")
    }

    suspend fun getCalifFinal(modEducativo: String): String {
        return soapPost("getAllCalifFinalByAlumnos", """<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xmlns:xsd="http://www.w3.org/2001/XMLSchema"
               xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <getAllCalifFinalByAlumnos xmlns="http://tempuri.org/">
      <bytModEducativo>$modEducativo</bytModEducativo>
    </getAllCalifFinalByAlumnos>
  </soap:Body>
</soap:Envelope>""")
    }

    suspend fun getCalifUnidad(): String {
        return soapPost("getCalifUnidadesByAlumno", """<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xmlns:xsd="http://www.w3.org/2001/XMLSchema"
               xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <getCalifUnidadesByAlumno xmlns="http://tempuri.org/" />
  </soap:Body>
</soap:Envelope>""")
    }

    suspend fun getCardex(lineamiento: String): String {
        return soapPost("getAllKardexConPromedioByAlumno", """<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xmlns:xsd="http://www.w3.org/2001/XMLSchema"
               xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <getAllKardexConPromedioByAlumno xmlns="http://tempuri.org/">
      <aluLineamiento>$lineamiento</aluLineamiento>
    </getAllKardexConPromedioByAlumno>
  </soap:Body>
</soap:Envelope>""")
    }

    suspend fun getCargaAcademica(): String {
        return soapPost("getCargaAcademicaByAlumno", """<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xmlns:xsd="http://www.w3.org/2001/XMLSchema"
               xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <getCargaAcademicaByAlumno xmlns="http://tempuri.org/" />
  </soap:Body>
</soap:Envelope>""")
    }
}