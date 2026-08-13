package com.example.finanzasapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.finanzasapp.data.local.AppDatabase
import com.example.finanzasapp.data.model.Movimiento
import com.example.finanzasapp.data.repository.MovimientoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FiltroFecha(
    val desde: Long? = null,
    val hasta: Long? = null
)

data class ResumenItem(
    val categoria: String,
    val ingresos: Double,
    val gastos: Double
)

class MovimientoViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).movimientoDao()
    private val repository = MovimientoRepository(dao)

    val movimientos = repository.movimientos

    // 🔥 FILTRO GLOBAL
    private val _filtro = MutableStateFlow(FiltroFecha())
    val filtro: StateFlow<FiltroFecha> = _filtro

    fun actualizarFiltro(desde: Long?, hasta: Long?) {
        _filtro.value = FiltroFecha(desde, hasta)
    }

    // 🔥 CONVERTIR A FLOW
    private val movimientosFlow = movimientos.asFlow()

    // 🔥 FILTRADO
    val resumenFiltrado: StateFlow<List<ResumenItem>> =
        combine(movimientosFlow, filtro) { lista, filtro ->

            val filtrados = lista.filter { m ->
                val okDesde = filtro.desde?.let { m.fecha >= it } ?: true
                val okHasta = filtro.hasta?.let { m.fecha <= it } ?: true
                okDesde && okHasta
            }

            filtrados
                .groupBy { it.categoria }
                .map { (cat, items) ->
                    val ingresos = items.filter { it.tipo == "ingreso" }.sumOf { it.monto }
                    val gastos = items.filter { it.tipo == "gasto" }.sumOf { it.monto }

                    ResumenItem(cat, ingresos, gastos)
                }

        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertar(movimiento: Movimiento) {
        viewModelScope.launch {
            repository.insertar(movimiento)
        }
    }

    fun actualizar(movimiento: Movimiento) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.actualizar(movimiento)
        }
    }

    // Dentro de tu MovimientoViewModel.kt

    /**
     * Elimina un movimiento específico de la base de datos.
     * Se utiliza el objeto completo porque Room identifica el registro por su @PrimaryKey (id).
     */
    fun eliminar(movimiento: Movimiento) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.eliminar(movimiento)
        }
    }

    /**
     * (Opcional) Elimina todos los movimientos vinculados a una categoría.
     * Útil si decides borrar el resumen completo desde el Libro Mayor.
     */
    fun eliminarPorCategoria(categoria: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.eliminarPorCategoria(categoria)
        }
    }

    suspend fun obtenerPorId(id: Int): Movimiento? {
        return repository.obtenerPorId(id)
    }

    fun restaurarMovimientos(
        movimientos: List<Movimiento>,
        onResultado: (Result<Int>) -> Unit
    ) {

        viewModelScope.launch {

            val resultado =
                kotlinx.coroutines.withContext(
                    Dispatchers.IO
                ) {

                    runCatching {

                        repository.restaurarMovimientos(
                            movimientos
                        )

                        movimientos.size
                    }
                }

            onResultado(resultado)
        }
    }



}