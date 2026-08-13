package com.example.finanzasapp.ui.diario

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finanzasapp.R
import com.example.finanzasapp.ui.util.applyTopSystemInset
import com.example.finanzasapp.viewmodel.MovimientoViewModel

class MovimientosFragment :
    Fragment(R.layout.fragment_movimientos) {

    private val viewModel: MovimientoViewModel by viewModels()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        view.applyTopSystemInset()
        val recycler =
            view.findViewById<RecyclerView>(R.id.recycler)

        val btnAgregar =
            view.findViewById<Button>(R.id.btnAgregar)

        val adapter = MovimientoAdapter(
            onEditar = { movimiento ->

                val bundle = Bundle().apply {
                    putSerializable(
                        "movimiento",
                        movimiento
                    )
                }

                findNavController().navigate(
                    R.id.agregarMovimientoFragment,
                    bundle
                )
            },

            onEliminar = { movimiento ->
                viewModel.eliminar(movimiento)
            }
        )

        recycler.layoutManager =
            LinearLayoutManager(requireContext())

        recycler.adapter = adapter

        viewModel.movimientos.observe(
            viewLifecycleOwner
        ) { lista ->
            adapter.actualizarLista(lista)
        }

        btnAgregar.setOnClickListener {
            findNavController().navigate(
                R.id.agregarMovimientoFragment
            )
        }
    }
}