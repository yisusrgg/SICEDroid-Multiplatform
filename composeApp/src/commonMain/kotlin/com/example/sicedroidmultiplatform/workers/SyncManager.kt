package com.example.sicedroidmultiplatform.workers

import com.example.sicedroidmultiplatform.data.repository.SicenetRepository

expect class SyncManager(
    repository: SicenetRepository
) {
    fun sincronizarDato(tipoSync: String, lineamiento: Int = 0, modEducativo: Int = 0)
}