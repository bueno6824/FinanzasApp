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
import com.example.finanzasapp.ui.util.ExcelExporter
import com.example.finanzasapp.util.NotificationWorker
import com.example.finanzasapp.viewmodel.MovimientoViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MayorFragment : Fragment(R.layout.activity_libro_mayor), ResumenAdapter.OnItemActionListener {

    private val viewModel: MovimientoViewModel by viewModels()
    private val adapter by lazy { ResumenAdapter(this) }

    private var anioSeleccionado = Calendar.getInstance().get(Calendar.YEAR)
    private var mesSeleccionado: Int? = null

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

    private fun prepararExportacion() {
        // 1. Obtenemos la lista actual de movimientos del ViewModel
        val movimientosActuales = viewModel.movimientos.value ?: emptyList()

        if (movimientosActuales.isEmpty()) {
            Toast.makeText(requireContext(), "No hay datos para exportar", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // 2. Obtenemos los valores seleccionados de tus Spinners
        val anio = binding.spinnerAnio.selectedItem.toString().toInt()
        val mesPosicion = binding.spinnerMes.selectedItemPosition // 0 = "Todos", 1 = "Enero"...

        // 3. Filtramos la lista para que el Excel coincida con lo que ves en pantalla
        val listaFiltrada = movimientosActuales.filter { mov ->
            val cal = Calendar.getInstance().apply { timeInMillis = mov.fecha }
            val coincideAnio = cal.get(Calendar.YEAR) == anio
            val coincideMes =
                if (mesPosicion == 0) true else cal.get(Calendar.MONTH) == (mesPosicion - 1)

            coincideAnio && coincideMes
        }

        // 4. Creamos un nombre dinámico para el archivo
        val nombreMes = binding.spinnerMes.selectedItem.toString()
        val nombreArchivo = "Reporte_${nombreMes}_$anio"

        // 5. Llamamos a tu clase utilitaria
        // CAMBIO AQUÍ: Llama a la función que SÍ tiene el contenido y usa MediaStore
        ExcelExporter.exportarAMediastore(requireContext(), listaFiltrada, nombreArchivo)
    }

    override fun onEdit(item: ResumenAdapter.ResumenItem) {
        mostrarDialogoSeleccion(item.categoria)
    }

    override fun onDelete(item: ResumenAdapter.ResumenItem) {
        mostrarDialogoSeleccion(item.categoria)
    }

    private fun mostrarDialogoSeleccion(categoria: String) {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_lista_detalle, null)
        val rvDetalle = dialogView.findViewById<RecyclerView>(R.id.rvDetalle)
        val tvTitulo = dialogView.findViewById<TextView>(R.id.txtTituloDialogo)

        tvTitulo?.text = "Movimientos: $categoria"

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.setOnDismissListener {
            if (arguments?.containsKey("categoria_filtro") == true) {
                arguments?.remove("categoria_filtro")
                findNavController().popBackStack(R.id.dashboardFragment, false)
            }
        }

        val detalleAdapter =
            MovimientoDetalleAdapter(object : MovimientoDetalleAdapter.OnMovimientoClickListener {
                override fun onEdit(movimiento: Movimiento) {
                    dialog.dismiss()
                    val bundle = Bundle().apply { putSerializable("movimiento", movimiento) }
                    findNavController().navigate(
                        R.id.action_mayorFragment_to_movimientosFragment,
                        bundle
                    )
                }

                override fun onDelete(movimiento: Movimiento) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Confirmar")
                        .setMessage("¿Borrar '${movimiento.descripcion}'?")
                        .setPositiveButton("Sí") { _, _ -> viewModel.eliminar(movimiento) }
                        .setNegativeButton("No", null)
                        .show()
                }
            })

        rvDetalle.layoutManager = LinearLayoutManager(requireContext())
        rvDetalle.adapter = detalleAdapter

        viewModel.movimientos.observe(viewLifecycleOwner) { lista ->
            val filtrados = lista.filter { it.categoria == categoria }
            detalleAdapter.actualizar(filtrados)
            if (filtrados.isEmpty()) dialog.dismiss()
        }

        dialog.show()
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
        val cal = Calendar.getInstance()
        val desde: Long;
        val hasta: Long
        if (mesSeleccionado == null) {
            cal.set(anioSeleccionado, 0, 1, 0, 0, 0); desde = cal.timeInMillis
            cal.set(anioSeleccionado, 11, 31, 23, 59, 59); hasta = cal.timeInMillis
        } else {
            cal.set(anioSeleccionado, mesSeleccionado!!, 1, 0, 0, 0); desde = cal.timeInMillis
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); hasta =
                cal.timeInMillis
        }
        viewModel.actualizarFiltro(desde, hasta)
    }

    private fun aplicarFiltroHoy() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0);
        val d = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23);
        val h = cal.timeInMillis
        viewModel.actualizarFiltro(d, h)
    }

    private fun aplicarFiltroSemana() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek);
        val d = cal.timeInMillis
        cal.add(Calendar.DAY_OF_WEEK, 6);
        val h = cal.timeInMillis
        viewModel.actualizarFiltro(d, h)
    }

    private fun aplicarFiltroMesActual() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1);
        val d = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        val h = cal.timeInMillis
        viewModel.actualizarFiltro(d, h)
    }

    private fun programarRecordatorio() {
        val request = PeriodicWorkRequestBuilder<NotificationWorker>(
            8, TimeUnit.HOURS // Se ejecutará cada 8 horas
        ).build()

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            "RecordatorioDiario",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP, // Mantiene el programa si ya existe
            request
        )
    }



}