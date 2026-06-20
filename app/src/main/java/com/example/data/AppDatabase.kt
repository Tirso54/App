package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Task::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "timeblock_database"
                )
                    // NOTA IMPORTANTE: fallbackToDestructiveMigration() está aceptable
                    // SOLO mientras la app esté en version = 1 (sin usuarios con datos
                    // que migrar todavía, o donde perder los datos es aceptable).
                    //
                    // EN CUANTO cambies el esquema de Task (añadir/quitar/renombrar
                    // un campo) y subas version = 2:
                    //   1. NO subas la versión sin antes generar el JSON de schemas/
                    //      (ya se exporta automáticamente en app/schemas/ al compilar,
                    //      gracias a exportSchema = true + room.schemaLocation).
                    //   2. Escribe una Migration explícita, ej.:
                    //        val MIGRATION_1_2 = object : Migration(1, 2) {
                    //            override fun migrate(db: SupportSQLiteDatabase) {
                    //                db.execSQL("ALTER TABLE tasks ADD COLUMN nuevoCampo TEXT NOT NULL DEFAULT ''")
                    //            }
                    //        }
                    //   3. Añádela con .addMigrations(MIGRATION_1_2) y entonces SÍ
                    //      puedes quitar fallbackToDestructiveMigration(), o dejarlo
                    //      solo como red de seguridad para saltos de versión no cubiertos.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
