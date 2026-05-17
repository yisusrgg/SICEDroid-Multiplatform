package com.example.sicedroidmultiplatform.data.network

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.HttpRedirect

actual fun provideHttpClient(): HttpClient = HttpClient(OkHttp) {
    engine {
        config {
            // Deshabilitamos el redirect de OkHttp para que sea el plugin HttpRedirect
            // de Ktor quien lo maneje. Así HttpCookies captura las cookies de las
            // respuestas intermedias (301) antes de que lleguen a la pantalla de login.
            followRedirects(false)
            followSslRedirects(false)
        }
    }
    install(HttpCookies) {
        storage = AcceptAllCookiesStorage()
    }
    install(HttpRedirect) {
        checkHttpMethod = false
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 15_000
    }
    install(Logging) {
        level = LogLevel.NONE
    }
}