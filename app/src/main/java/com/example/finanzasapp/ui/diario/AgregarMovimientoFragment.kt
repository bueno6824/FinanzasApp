package com.example.finanzasapp.ui.diario

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.finanzasapp.R
import com.example.finanzasapp.data.model.Movimiento
import com.example.finanzasapp.viewmodel.MovimientoViewModel

// ... tus imports actuales ...

class AgregarMovimientoFragment : Fragment(R.layout.activity_agregar_movimiento) {

    private val viewModel: MovimientoViewModel by viewModels()

    // Usamos el casting correcto para los nuevos inputs de Material
    private lateinit var etDescripcion: EditText
    private lateinit var etMonto: EditText
    private lateinit var spinnerCategoria: Spinner
    private lateinit var spinnerTipo: Spinner
    private lateinit var btnGuardar: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etDescripcion = view.findViewById(R.id.etDescripcion)
        etMonto = view.findViewById(R.id.etMonto)
        spinnerCategoria = view.findViewById(R.id.spinnerCategoria)
        spinnerTipo = view.findViewById(R.id.spinnerTipo)
        btnGuardar = view.findViewById(R.id.btnGuardar)
        val tvTitulo = view.findViewById<TextView>(R.id.tvTitulo)

        // Configurar Spinners
        val categorias = listOf("Trabajo", "Comida", "Transporte", "Salud", "Gustos", "Otros")
        spinnerCategoria.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categorias)

        val tipos = listOf("ingreso", "gasto")
        spinnerTipo.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tipos)

        // Recuperar objeto para edición
        val movimientoExistente = arguments?.getSerializable("movimiento") as? Movimiento

        movimientoExistente?.let {
            tvTitulo.text = "Editar Movimiento" // Cambiamos el título si es edición
            etDescripcion.setText(it.descripcion)
            etMonto.setText(it.monto.toString())

            val indexCat = categorias.indexOf(it.categoria)
            if (indexCat >= 0) spinnerCategoria.setSelection(indexCat)

            val indexTipo = tipos.indexOf(it.tipo)
            if (indexTipo >= 0) spinnerTipo.setSelection(indexTipo)
        }

        btnGuardar.setOnClickListener {
            guardarMovimiento(movimientoExistente)
        }
    }

    private fun guardarMovimiento(existente: Movimiento?) {
        val desc = etDescripcion.text.toString().trim()
        val montoTxt = etMonto.text.toString().trim()

        if (desc.isEmpty() || montoTxt.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val montoVal = montoTxt.toDoubleOrNull() ?: 0.0
        if (montoVal <= 0) {
            Toast.makeText(requireContext(), "El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show()
            return
        }

        if (existente != null) {
            // EDITAR
            val actualizado = existente.copy(
                descripcion = desc,
                monto = montoVal,
                categoria = spinnerCategoria.selectedItem.toString(),
                tipo = spinnerTipo.selectedItem.toString()
                // Mantenemos la fecha original e ID original
            )
            viewModel.actualizar(actualizado)
        } else {
            // INSERTAR NUEVO
            val nuevo = Movimiento(
                fecha = System.currentTimeMillis(),
                categoria = spinnerCategoria.selectedItem.toString(),
                descripcion = desc,
                tipo = spinnerTipo.selectedItem.toString(),
                monto = montoVal
            )
            viewModel.insertar(nuevo)
        }

        Toast.makeText(requireContext(), "¡Guardado con éxito!", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }
}

