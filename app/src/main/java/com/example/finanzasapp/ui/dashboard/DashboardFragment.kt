package com.example.finanzasapp.ui.dashboard

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.finanzasapp.MainActivity
import com.example.finanzasapp.R
import com.example.finanzasapp.databinding.ActivityDashboardBinding
import com.example.finanzasapp.ui.util.BackupManager // 🔥 Asegúrate que la ruta sea correcta
import com.example.finanzasapp.ui.util.applyTopSystemInset
import com.example.finanzasapp.viewmodel.MovimientoViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class DashboardFragment : Fragment(R.layout.activity_dashboard) {

    private val viewModel: MovimientoViewModel by viewModels()

    private var _binding: ActivityDashboardBinding? = null
    private val binding get() = _binding!!

    // Usaremos directamente el binding para evitar lateinit innecesarios
    private lateinit var pieChart: PieChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 1. REGISTRAR EL SELECTOR DE ARCHIVOS
    private val seleccionarBackupLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { confirmarRestauracion(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.applyTopSystemInset()

        // Inicializamos la gráfica que es la única que requiere configuración especial
        pieChart = binding.pieChart

        // --- CONFIGURACIÓN DE BOTONES (BACKUP, RESTAURAR, TEMA) ---
        binding.btnBackup.setOnClickListener {
            BackupManager.exportarBaseDeDatos(requireContext(), "finanzas_db")
        }

        binding.btnRestaurar.setOnClickListener {
            seleccionarBackupLauncher.launch("*/*")
        }

        binding.btnConfigTema.setOnClickListener {
            mostrarDialogoTemas()
        }

        // --- CONFIGURACIÓN DE SPINNERS ---
        val meses = listOf("Todos", "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
        val adapterMes = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, meses)
        adapterMes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMes.adapter = adapterMes
        binding.spinnerMes.setSelection(Calendar.getInstance().get(Calendar.MONTH) + 1)

        val tipos = listOf("Gastos", "Ingresos")
        val adapterTipo = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tipos)
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTipo.adapter = adapterTipo

        // --- LISTENERS DE SPINNERS ---
        binding.spinnerMes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                enviarFiltroAlViewModel(pos)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        binding.spinnerTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                refrescarGrafica()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // --- OBSERVADORES ---
        viewModel.movimientos.observe(viewLifecycleOwner) { lista ->
            val ingresos = lista.filter { it.tipo == "ingreso" }.sumOf { it.monto }
            val gastos = lista.filter { it.tipo == "gasto" }.sumOf { it.monto }
            val formato = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

            binding.txtBalance.text = formato.format(ingresos - gastos)
            binding.txtIngresos.text = formato.format(ingresos)
            binding.txtGastos.text = formato.format(gastos)
        }

        configurarEstiloInicialGrafica()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.resumenFiltrado.collect {
                    refrescarGrafica()
                }
            }
        }
    }

    private fun confirmarRestauracion(uri: android.net.Uri) {
        AlertDialog.Builder(requireContext())
            .setTitle("¡Atención!")
            .setMessage("Al restaurar, se borrarán todos los datos actuales. La app se reiniciará.\n\n¿Deseas continuar?")
            .setPositiveButton("Restaurar") { _, _ ->
                val exito = BackupManager.restaurarBaseDeDatos(requireContext(), uri, "finanzas_db")
                if (exito) {
                    android.os.Process.killProcess(android.os.Process.myPid())
                } else {
                    Toast.makeText(requireContext(), "Error al restaurar", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun enviarFiltroAlViewModel(position: Int) {
        val mes = if (position == 0) null else position - 1
        val cal = Calendar.getInstance()
        if (mes == null) {
            cal.set(Calendar.MONTH, 0)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val desde = cal.timeInMillis
            cal.set(Calendar.MONTH, 11)
            cal.set(Calendar.DAY_OF_MONTH, 31)
            viewModel.actualizarFiltro(desde, cal.timeInMillis)
        } else {
            cal.set(Calendar.MONTH, mes)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val desde = cal.timeInMillis
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            viewModel.actualizarFiltro(desde, cal.timeInMillis)
        }
    }

    private fun refrescarGrafica() {
        val resumenActual = viewModel.resumenFiltrado.value
        val tipoSeleccionado = binding.spinnerTipo.selectedItem.toString()

        val entries = resumenActual.mapNotNull {
            val valor = if (tipoSeleccionado == "Ingresos") it.ingresos else it.gastos
            if (valor > 0) PieEntry(valor.toFloat(), it.categoria) else null
        }

        if (entries.isEmpty()) {
            pieChart.clear()
            pieChart.setNoDataText("No hay $tipoSeleccionado en este periodo")
            pieChart.invalidate()
            return
        }

        actualizarGrafica(entries)
        pieChart.centerText = if (tipoSeleccionado == "Ingresos") "Ingresos\npor categoría" else "Gastos\npor categoría"
    }

    private fun actualizarGrafica(entries: List<PieEntry>) {
        val esModoNoche = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val colorTexto = if (esModoNoche) Color.WHITE else Color.parseColor("#64748B")

        val materialColors = listOf(
            Color.parseColor("#3B82F6"), Color.parseColor("#10B981"),
            Color.parseColor("#F59E0B"), Color.parseColor("#EF4444"),
            Color.parseColor("#8B5CF6")
        )

        pieChart.apply {
            setBackgroundColor(Color.TRANSPARENT)
            setHoleColor(Color.TRANSPARENT)
            setCenterTextColor(colorTexto)
            legend.textColor = colorTexto
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = materialColors
            sliceSpace = 3f
            valueTextSize = 12f
            valueTextColor = Color.WHITE
            selectionShift = 15f
        }

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))
        pieChart.data = data
        pieChart.animateY(1400, Easing.EaseOutBack)
        pieChart.invalidate()
    }

    private fun configurarEstiloInicialGrafica() {
        pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setDrawEntryLabels(false)
            isDrawHoleEnabled = true
            setHoleRadius(70f)
            setCenterTextSize(16f)

            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    if (e == null) return
                    val pieEntry = e as PieEntry
                    val categoriaTocada = pieEntry.label ?: return

                    viewLifecycleOwner.lifecycleScope.launch {
                        delay(200)
                        val bundle = Bundle().apply { putString("categoria_filtro", categoriaTocada) }
                        findNavController().navigate(R.id.action_dashboard_to_mayor, bundle)
                        pieChart.highlightValue(null)
                    }
                }
                override fun onNothingSelected() {}
            })
        }
    }

    private fun mostrarDialogoTemas() {
        val opciones = arrayOf("Claro", "Oscuro", "Defecto del sistema")
        AlertDialog.Builder(requireContext())
            .setTitle("Selecciona el tema")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
                (activity as? MainActivity)?.guardarPreferenciaTema(which)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        refrescarGrafica()
    }
}