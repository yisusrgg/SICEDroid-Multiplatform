package com.example.sicedroidmultiplatform.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

// Variable global para que Room pueda usar el Context de tu app.
// Tienes que inicializarla en el onCreate() de tu MainActivity:
// appContext = this.applicationContext
lateinit var appContext: Context

actual fun getDatabaseBuilder(): RoomDatabase.Builder<SicenetDatabase> {
    val dbFile = appContext.getDatabasePath("sicenet_database.db")
    return Room.databaseBuilder<SicenetDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}