package com.example.finanzasapp.data.repository

import com.example.finanzasapp.data.local.MovimientoDao
import com.example.finanzasapp.data.model.Movimiento

class MovimientoRepository (private val dao:MovimientoDao){

    val movimientos = dao.obtenerTodos()
    val resumenCategoria = dao.obtenerResumenPorCategoria()

    suspend fun insertar(movimiento: Movimiento) {
        dao.insertar(movimiento)

    }
    suspend fun actualizar(movimiento: Movimiento){
        dao.actualizar(movimiento)
    }

    suspend fun eliminar(movimiento: Movimiento){
        dao.eliminar(movimiento)
    }
    // En MovimientoRepository
    suspend fun eliminarPorCategoria(nombreCategoria: String) {
        dao.eliminarPorCategoria(nombreCategoria)
    }


}