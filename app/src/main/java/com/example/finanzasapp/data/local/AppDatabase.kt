package com.example.finanzasapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.finanzasapp.data.model.Movimiento

    @Database(entities = [Movimiento::class], version = 1)
    abstract  class AppDatabase : RoomDatabase(){
        abstract fun movimientoDao() : MovimientoDao

        companion object{
            @Volatile

            private var INSTANCE: AppDatabase? = null

            fun getDatabase(context: Context): AppDatabase {
                return INSTANCE ?: synchronized(this){
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "finanzas_db"
                    ).build()
                    INSTANCE = instance
                    instance
                }
            }
            // 🔥 AÑADE ESTA FUNCIÓN para cerrar la DB antes del Backup
            fun cerrarDatabase() {
                INSTANCE?.close()
                INSTANCE = null
            }

        }
    }