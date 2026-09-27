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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.finanzasapp.R
import com.example.finanzasapp.data.model.Movimiento
import com.example.finanzasapp.viewmodel.MovimientoViewModel
import kotlinx.coroutines.launch

class AgregarMovimientoFragment :
    Fragment(R.layout.activity_agregar_movimiento) {

    private val viewModel: MovimientoViewModel by viewModels()

    private lateinit var etDescripcion: EditText
    private lateinit var etMonto: EditText
    private lateinit var spinnerCategoria: Spinner
    private lateinit var spinnerTipo: Spinner
    private lateinit var btnGuardar: Button
    private lateinit var tvTitulo: TextView

    /*
     * Si es null:
     * estamos agregando un movimiento nuevo.
     *
     * Si contiene un Movimiento:
     * estamos editando uno existente.
     */
    private var movimientoExistente: Movimiento? = null

    private val categorias = listOf(
        "Trabajo",
        "Comida",
        "Transporte",
        "Salud",
        "Gustos",
        "Otros"
    )

    private val tipos = listOf(
        "ingreso",
        "gasto"
    )

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        /* ==============================
           VISTAS
        ============================== */

        etDescripcion =
            view.findViewById(R.id.etDescripcion)

        etMonto =
            view.findViewById(R.id.etMonto)

        spinnerCategoria =
            view.findViewById(R.id.spinnerCategoria)

        spinnerTipo =
            view.findViewById(R.id.spinnerTipo)

        btnGuardar =
            view.findViewById(R.id.btnGuardar)

        tvTitulo =
            view.findViewById(R.id.tvTitulo)

        /* ==============================
           SPINNERS
        ============================== */

        spinnerCategoria.adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                categorias
            )

        spinnerTipo.adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                tipos
            )

        /* ==============================
           MODO EDICIÓN
        ============================== */

        cargarMovimientoParaEdicion()

        /* ==============================
           GUARDAR
        ============================== */

        btnGuardar.setOnClickListener {

            guardarMovimiento()
        }
    }

    /*
     * Ahora recibimos solamente el ID.
     *
     * Ya NO recibimos el objeto Movimiento
     * completo mediante Serializable.
     */
    private fun cargarMovimientoParaEdicion() {

        val movimientoId =
            arguments?.getInt(
                "movimientoId",
                -1
            ) ?: -1

        /*
         * -1 significa que no recibimos ningún ID.
         *
         * Por lo tanto estamos agregando
         * un movimiento nuevo.
         */
        if (movimientoId == -1) {
            return
        }

        /*
         * Si recibimos un ID buscamos
         * el movimiento directamente en Room.
         */
        viewLifecycleOwner.lifecycleScope.launch {

            val movimiento =
                viewModel.obtenerPorId(
                    movimientoId
                )

            if (movimiento != null) {

                movimientoExistente =
                    movimiento

                cargarMovimiento(
                    movimiento
                )
            }
        }
    }

    /*
     * Coloca en pantalla los datos del
     * movimiento recuperado desde Room.
     */
    private fun cargarMovimiento(
        movimiento: Movimiento
    ) {

        tvTitulo.text =
            "Editar Movimiento"

        etDescripcion.setText(
            movimiento.descripcion
        )

        etMonto.setText(
            movimiento.monto.toString()
        )

        val indexCategoria =
            categorias.indexOf(
                movimiento.categoria
            )

        if (indexCategoria >= 0) {

            spinnerCategoria.setSelection(
                indexCategoria
            )
        }

        val indexTipo =
            tipos.indexOf(
                movimiento.tipo
            )

        if (indexTipo >= 0) {

            spinnerTipo.setSelection(
                indexTipo
            )
        }
    }

    private fun guardarMovimiento() {

        val descripcion =
            etDescripcion
                .text
                .toString()
                .trim()

        val montoTexto =
            etMonto
                .text
                .toString()
                .trim()

        /* ==============================
           VALIDACIÓN
        ============================== */

        if (
            descripcion.isEmpty() ||
            montoTexto.isEmpty()
        ) {

            Toast.makeText(
                requireContext(),
                "Por favor, llena todos los campos",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val monto =
            montoTexto.toDoubleOrNull()

        if (
            monto == null ||
            monto <= 0
        ) {

            Toast.makeText(
                requireContext(),
                "El monto debe ser mayor a 0",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val categoria =
            spinnerCategoria
                .selectedItem
                .toString()

        val tipo =
            spinnerTipo
                .selectedItem
                .toString()

        /* ==============================
           EDITAR O INSERTAR
        ============================== */

        val existente =
            movimientoExistente

        if (existente != null) {

            // EDITAR

            val actualizado =
                existente.copy(
                    descripcion = descripcion,
                    monto = monto,
                    categoria = categoria,
                    tipo = tipo
                )

            viewModel.actualizar(
                actualizado
            )

        } else {

            // INSERTAR NUEVO

            val nuevo =
                Movimiento(
                    fecha =
                        System.currentTimeMillis(),

                    categoria =
                        categoria,

                    descripcion =
                        descripcion,

                    tipo =
                        tipo,

                    monto =
                        monto
                )

            viewModel.insertar(
                nuevo
            )
        }

        Toast.makeText(
            requireContext(),
            "¡Guardado con éxito!",
            Toast.LENGTH_SHORT
        ).show()

        findNavController()
            .popBackStack()
    }
}