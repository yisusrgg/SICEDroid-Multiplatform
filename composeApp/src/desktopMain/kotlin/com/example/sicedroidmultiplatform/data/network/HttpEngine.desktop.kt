package com.example.sicedroidmultiplatform.data.network

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.HttpRedirect

actual fun provideHttpClient(): HttpClient = HttpClient(CIO) {
    install(HttpCookies) {
        storage = AcceptAllCookiesStorage()
    }
    install(HttpRedirect) {
        checkHttpMethod = false  // sigue el redirect aunque sea POST (convierte a GET, igual que OkHttp)
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 15_000
    }
    install(Logging) {
        level = LogLevel.NONE
    }
}