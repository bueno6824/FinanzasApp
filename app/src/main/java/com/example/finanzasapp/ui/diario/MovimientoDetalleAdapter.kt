package com.example.finanzasapp.ui.diario

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finanzasapp.R
import com.example.finanzasapp.data.model.Movimiento
import com.example.finanzasapp.ui.util.MoneyFormatter

class MovimientoDetalleAdapter(
    private val listener: OnMovimientoClickListener
) : RecyclerView.Adapter<MovimientoDetalleAdapter.ViewHolder>() {

    interface OnMovimientoClickListener {
        fun onEdit(movimiento: Movimiento)
        fun onDelete(movimiento: Movimiento)
    }

    private var lista = listOf<Movimiento>()

    fun actualizar(
        nueva: List<Movimiento>
    ) {
        lista = nueva
        notifyDataSetChanged()
    }

    class ViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        val txtDesc: TextView =
            view.findViewById(R.id.txtDescripcion)

        val txtCat: TextView =
            view.findViewById(R.id.txtCategoria)

        val txtMonto: TextView =
            view.findViewById(R.id.txtMonto)

        val btnEdit: ImageButton =
            view.findViewById(R.id.btnEditar)

        val btnDelete: ImageButton =
            view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view =
            LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.item_movimiento,
                    parent,
                    false
                )

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return lista.size
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val movimiento = lista[position]

        holder.txtDesc.text =
            movimiento.descripcion

        holder.txtCat.text =
            movimiento.categoria

        holder.txtMonto.text =
            MoneyFormatter.movimiento(
                monto = movimiento.monto,
                tipo = movimiento.tipo
            )

        configurarColorMonto(
            holder = holder,
            tipo = movimiento.tipo
        )

        holder.btnEdit.setOnClickListener {
            listener.onEdit(movimiento)
        }

        holder.btnDelete.setOnClickListener {
            listener.onDelete(movimiento)
        }
    }

    private fun configurarColorMonto(
        holder: ViewHolder,
        tipo: String
    ) {
        val esIngreso =
            tipo.equals(
                other = "ingreso",
                ignoreCase = true
            )

        val colorMonto =
            if (esIngreso) {
                Color.parseColor("#10B981")
            } else {
                Color.parseColor("#EF4444")
            }

        holder.txtMonto.setTextColor(colorMonto)
    }
}