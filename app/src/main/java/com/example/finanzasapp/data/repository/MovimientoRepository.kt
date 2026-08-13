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

    suspend fun obtenerPorId(id: Int): Movimiento? {
        return dao.obtenerPorId(id)
    }

    suspend fun insertarTodos(
        movimientos: List<Movimiento>
    ) {
        dao.insertarTodos(movimientos)
    }

    suspend fun eliminarTodos() {
        dao.eliminarTodos()
    }

    suspend fun restaurarMovimientos(
        movimientos: List<Movimiento>
    ) {
        dao.reemplazarTodos(movimientos)
    }



}