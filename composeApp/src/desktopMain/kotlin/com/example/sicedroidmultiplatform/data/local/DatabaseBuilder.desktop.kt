package com.example.sicedroidmultiplatform.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual fun getDatabaseBuilder(): RoomDatabase.Builder<SicenetDatabase> {
    // Crea una carpeta oculta en Windows (C:\Users\TuUsuario\.sicenet)
    val appFolder = File(System.getProperty("user.home"), ".sicenet")
    if (!appFolder.exists()) {
        appFolder.mkdirs()
    }

    val dbFile = File(appFolder, "sicenet_database.db")
    return Room.databaseBuilder<SicenetDatabase>(
        name = dbFile.absolutePath
    )
}