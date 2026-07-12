package com.example.finanzasapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "movimientos")

data class Movimiento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fecha: Long,
    val categoria: String,
    val descripcion: String,
    val tipo: String,
    val monto: Double
): Serializable
