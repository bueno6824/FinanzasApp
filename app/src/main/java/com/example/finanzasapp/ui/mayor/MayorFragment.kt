package com.example.finanzasapp.ui.mayor

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.finanzasapp.R
import com.example.finanzasapp.data.model.Movimiento
import com.example.finanzasapp.databinding.ActivityLibroMayorBinding
import com.example.finanzasapp.ui.diario.MovimientoDetalleAdapter
import com.example.finanzasapp.ui.resumen.ResumenAdapter
import com.example.finanzasapp.ui.util.DateRange
import com.example.finanzasapp.ui.util.ExcelExporter
import com.example.finanzasapp.ui.util.applyTopSystemInset
import com.example.finanzasapp.util.NotificationWorker
import com.example.finanzasapp.viewmodel.MovimientoViewModel
import com.example.finanzasapp.ui.util.DateRangeUtils
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import androidx.lifecycle.Observer
import androidx.work.ExistingPeriodicWorkPolicy

class MayorFragment : Fragment(R.layout.activity_libro_mayor), ResumenAdapter.OnItemActionListener {

    private val viewModel: MovimientoViewModel by viewModels()
    private val adapter by lazy { ResumenAdapter(this) }

    private var anioSeleccionado = Calendar.getInstance().get(Calendar.YEAR)
    private var mesSeleccionado: Int? = null


    private var rangoActivo: DateRange? = null
    private var nombreFiltroActivo = "Reporte"


    // 1. Declarar la variable
    private var _binding: ActivityLibroMayorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 2. Inicializar el binding
        _binding = ActivityLibroMayorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.applyTopSystemInset()

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerMayor)
        val spinnerAnio = view.findViewById<Spinner>(R.id.spinnerAnio)
        val spinnerMes = view.findViewById<Spinner>(R.id.spinnerMes)
        val btnHoy = view.findViewById<Button>(R.id.btnHoy)
        val btnSemana = view.findViewById<Button>(R.id.btnSemana)
        val btnMes = view.findViewById<Button>(R.id.btnMes)

        val categoriaRecibida = arguments?.getString("categoria_filtro")
        if (!categoriaRecibida.isNullOrEmpty()) {
            mostrarDialogoSeleccion(categoriaRecibida)
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        // Configuramos Spinners con la nueva lógica dinámica
        configurarSpinners(spinnerAnio, spinnerMes)

        btnHoy.setOnClickListener { aplicarFiltroHoy() }
        btnSemana.setOnClickListener { aplicarFiltroSemana() }
        btnMes.setOnClickListener { aplicarFiltroMesActual() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.resumenFiltrado.collect { lista ->
                val convertido = lista.map {
                    ResumenAdapter.ResumenItem(it.categoria, it.ingresos, it.gastos)
                }
                adapter.actualizar(convertido)
            }
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (arguments?.containsKey("categoria_filtro") == true) {
                    arguments?.remove("categoria_filtro")
                }
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        actualizarFiltroGlobal()

        programarRecordatorio() // Esto activa la programación silenciosamente

        // Configurar el botón de Excel
        binding.btnExportarExcel.setOnClickListener {
            prepararExportacion()
        }

    }

    // 4. Limpiar el binding al destruir la vista (buena práctica)
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun aplicarRango(
        rango: DateRange,
        nombreFiltro: String
    ) {

        rangoActivo = rango
        nombreFiltroActivo = nombreFiltro

        viewModel.actualizarFiltro(
            rango.desde,
            rango.hasta
        )
    }

    private fun prepararExportacion() {

        // 1. Obtenemos el rango que actualmente está activo en Libro Mayor
        val rango = rangoActivo

        if (rango == null) {
            Toast.makeText(
                requireContext(),
                "No hay un filtro activo para exportar",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        // 2. Obtenemos todos los movimientos actuales
        val movimientosActuales =
            viewModel.movimientos.value ?: emptyList()

        if (movimientosActuales.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "No hay datos para exportar",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        // 3. Filtramos usando EXACTAMENTE el mismo rango
        // que actualmente está mostrando Libro Mayor
        val listaFiltrada =
            movimientosActuales.filter { movimiento ->

                movimiento.fecha in rango.desde..rango.hasta
            }

        if (listaFiltrada.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "No hay movimientos en el periodo seleccionado",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        // 4. Nombre dinámico del archivo
        val nombreArchivo =
            "Reporte_$nombreFiltroActivo"

        // 5. Exportamos únicamente lo que corresponde al filtro activo
        ExcelExporter.exportarAMediastore(
            requireContext(),
            listaFiltrada,
            nombreArchivo
        )
    }

    override fun onEdit(item: ResumenAdapter.ResumenItem) {
        mostrarDialogoSeleccion(item.categoria)
    }

    override fun onDelete(item: ResumenAdapter.ResumenItem) {
        mostrarDialogoSeleccion(item.categoria)
    }

    private fun mostrarDialogoSeleccion(
        categoria: String
    ) {
        val dialogView =
            LayoutInflater
                .from(requireContext())
                .inflate(
                    R.layout.dialog_lista_detalle,
                    null
                )

        val rvDetalle =
            dialogView.findViewById<RecyclerView>(
                R.id.rvDetalle
            )

        val tvTitulo =
            dialogView.findViewById<TextView>(
                R.id.txtTituloDialogo
            )

        tvTitulo?.text =
            "Movimientos: $categoria"

        val dialog =
            AlertDialog.Builder(
                requireContext(),
                R.style.CustomDialogTheme
            )
                .setView(dialogView)
                .create()

        var navegandoAEdicion = false

        val detalleAdapter =
            MovimientoDetalleAdapter(
                object :
                    MovimientoDetalleAdapter.OnMovimientoClickListener {

                    override fun onEdit(
                        movimiento: Movimiento
                    ) {
                        navegandoAEdicion = true

                        dialog.dismiss()

                        val bundle =
                            Bundle().apply {
                                putInt(
                                    "movimientoId",
                                    movimiento.id
                                )
                            }

                        findNavController().navigate(
                            R.id.action_mayorFragment_to_agregarMovimientoFragment,
                            bundle
                        )
                    }

                    override fun onDelete(
                        movimiento: Movimiento
                    ) {
                        AlertDialog.Builder(
                            requireContext()
                        )
                            .setTitle("Confirmar")
                            .setMessage(
                                "¿Borrar '${movimiento.descripcion}'?"
                            )
                            .setPositiveButton("Sí") { _, _ ->
                                viewModel.eliminar(
                                    movimiento
                                )
                            }
                            .setNegativeButton(
                                "No",
                                null
                            )
                            .show()
                    }
                }
            )

        rvDetalle.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        rvDetalle.adapter =
            detalleAdapter

        val movimientosObserver =
            Observer<List<Movimiento>> { lista ->

                val filtrados =
                    lista.filter {
                        it.categoria == categoria
                    }

                detalleAdapter.actualizar(
                    filtrados
                )

                if (
                    filtrados.isEmpty() &&
                    dialog.isShowing
                ) {
                    dialog.dismiss()
                }
            }

        dialog.setOnDismissListener {

            // El observador deja de existir al cerrar el diálogo.
            viewModel.movimientos.removeObserver(
                movimientosObserver
            )

            if (
                !navegandoAEdicion &&
                arguments?.containsKey(
                    "categoria_filtro"
                ) == true
            ) {
                arguments?.remove(
                    "categoria_filtro"
                )

                findNavController().popBackStack(
                    R.id.dashboardFragment,
                    false
                )
            }
        }

        dialog.show()

        dialog.window?.setBackgroundDrawableResource(
            android.R.color.transparent
        )

        viewModel.movimientos.observe(
            viewLifecycleOwner,
            movimientosObserver
        )
    }

    private fun configurarSpinners(spinnerAnio: Spinner, spinnerMes: Spinner) {
        val anios = (2020..2030).map { it.toString() }
        val meses = listOf(
            "Todos",
            "Ene",
            "Feb",
            "Mar",
            "Abr",
            "May",
            "Jun",
            "Jul",
            "Ago",
            "Sep",
            "Oct",
            "Nov",
            "Dic"
        )

        // Usamos la función de estilo para que el texto sea dinámico
        configurarSpinnerEstilo(spinnerAnio, anios)
        configurarSpinnerEstilo(spinnerMes, meses)

        val anioActualStr = Calendar.getInstance().get(Calendar.YEAR).toString()
        spinnerAnio.setSelection(anios.indexOf(anioActualStr).coerceAtLeast(0))
        spinnerMes.setSelection(Calendar.getInstance().get(Calendar.MONTH) + 1)

        spinnerAnio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                anioSeleccionado = anios[pos].toInt()
                actualizarFiltroGlobal()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        spinnerMes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                mesSeleccionado = if (pos == 0) null else pos - 1
                actualizarFiltroGlobal()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun configurarSpinnerEstilo(spinner: Spinner, opciones: List<String>) {
        val adapterSpinner = object :
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, opciones) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                // Usamos el color de texto primario de tus recursos
                (v as TextView).setTextColor(resources.getColor(R.color.text_primary, null))
                return v
            }
        }
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapterSpinner
    }

    // --- Métodos de Filtro (Sin cambios en lógica) ---
    private fun actualizarFiltroGlobal() {

        val rango: DateRange
        val nombreFiltro: String

        if (mesSeleccionado == null) {

            rango =
                DateRangeUtils.anio(
                    anioSeleccionado
                )

            nombreFiltro =
                "Todo_$anioSeleccionado"

        } else {

            rango =
                DateRangeUtils.mes(
                    anioSeleccionado,
                    mesSeleccionado!!
                )

            val nombreMes =
                binding.spinnerMes.selectedItem.toString()

            nombreFiltro =
                "${nombreMes}_$anioSeleccionado"
        }

        aplicarRango(
            rango = rango,
            nombreFiltro = nombreFiltro
        )
    }

    private fun aplicarFiltroHoy() {

        aplicarRango(
            rango = DateRangeUtils.hoy(),
            nombreFiltro = "Hoy"
        )
    }

    private fun aplicarFiltroSemana() {

        aplicarRango(
            rango = DateRangeUtils.semanaActual(),
            nombreFiltro = "Semana"
        )
    }

    private fun aplicarFiltroMesActual() {

        aplicarRango(
            rango = DateRangeUtils.mesActual(),
            nombreFiltro = "Mes_Actual"
        )
    }

    private fun programarRecordatorio() {

        val request =
            PeriodicWorkRequestBuilder<NotificationWorker>(
                24,
                TimeUnit.HOURS
            ).build()

        WorkManager
            .getInstance(requireContext())
            .enqueueUniquePeriodicWork(
                "RecordatorioDiario",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }



}