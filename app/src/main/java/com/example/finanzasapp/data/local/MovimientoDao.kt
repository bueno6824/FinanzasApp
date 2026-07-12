package com.example.finanzasapp.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.finanzasapp.data.model.Movimiento
import com.example.finanzasapp.data.model.ResumenCategoria

@Dao
interface MovimientoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(movimiento: Movimiento)
    @Query("SELECT * FROM movimientos ORDER BY fecha DESC")
    fun obtenerTodos():LiveData<List<Movimiento>>

    @Query("""
        SELECT categoria, 
        SUM(CASE WHEN tipo = 'ingreso' THEN monto ELSE 0 END) as ingresos,
        SUM(CASE WHEN tipo = 'gasto' THEN monto ELSE 0 END) as gastos
        FROM movimientos
        GROUP BY categoria
    """)

    fun obtenerResumenPorCategoria(): LiveData<List<ResumenCategoria>>

    // En MovimientoDao
    @Query("DELETE FROM movimientos WHERE categoria = :nombreCategoria")
    suspend fun eliminarPorCategoria(nombreCategoria: String)

    @Update
    suspend fun actualizar(movimiento: Movimiento)

    @Delete
    suspend fun eliminar(movimiento: Movimiento)

}