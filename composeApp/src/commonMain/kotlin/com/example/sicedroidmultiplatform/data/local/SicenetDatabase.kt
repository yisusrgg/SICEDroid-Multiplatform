package com.example.sicedroidmultiplatform.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

@Database(
    entities = [
        PerfilEntity::class,
        CalificacionFinalEntity::class,
        CalificacionUnidadEntity::class,
        CardexEntity::class,
        MateriaEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SicenetDatabase : RoomDatabase() {
    abstract fun sicenetDao(): SicenetDao
}

// Promesa KMP: Cada plataforma deberá entregarnos un constructor de Room
expect fun getDatabaseBuilder(): RoomDatabase.Builder<SicenetDatabase>

// unico punto DE acceso desde repository
fun getRoomDatabase(): SicenetDatabase {
    return getDatabaseBuilder()
        .fallbackToDestructiveMigration(dropAllTables = true)
        // SetDriver es obligatorio en KMP para decirle a Room que use SQLite nativo
        .setDriver(BundledSQLiteDriver())
        .build()
}
