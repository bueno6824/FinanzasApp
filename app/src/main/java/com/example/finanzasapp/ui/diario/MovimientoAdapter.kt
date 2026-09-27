package com.example.finanzasapp.ui.diario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.finanzasapp.R
import com.example.finanzasapp.data.model.Movimiento
import com.example.finanzasapp.ui.util.MoneyFormatter

class MovimientoAdapter(
    private val onEditar: (Movimiento) -> Unit,
    private val onEliminar: (Movimiento) -> Unit
) : ListAdapter<Movimiento, MovimientoAdapter.ViewHolder>(
    DIFF_CALLBACK
) {

    companion object {

        private val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<Movimiento>() {

                override fun areItemsTheSame(
                    oldItem: Movimiento,
                    newItem: Movimiento
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: Movimiento,
                    newItem: Movimiento
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }

    fun actualizarLista(
        nuevaLista: List<Movimiento>
    ) {
        submitList(nuevaLista)
    }

    class ViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        val txtDescripcion: TextView =
            view.findViewById(R.id.txtDescripcion)

        val txtCategoria: TextView =
            view.findViewById(R.id.txtCategoria)

        val txtMonto: TextView =
            view.findViewById(R.id.txtMonto)

        val btnEditar: ImageButton =
            view.findViewById(R.id.btnEditar)

        val btnEliminar: ImageButton =
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

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = getItem(position)

        /* ==============================
           DATOS
        ============================== */

        holder.txtDescripcion.text =
            item.descripcion

        holder.txtCategoria.text =
            item.categoria

        /* ==============================
           MONTO
        ============================== */

        holder.txtMonto.text =
            MoneyFormatter.movimiento(
                monto = item.monto,
                tipo = item.tipo
            )

        /* ==============================
           COLOR DEL MONTO
        ============================== */

        val color =
            if (
                item.tipo.equals(
                    other = "ingreso",
                    ignoreCase = true
                )
            ) {
                android.R.color.holo_green_dark
            } else {
                android.R.color.holo_red_dark
            }

        holder.txtMonto.setTextColor(
            ContextCompat.getColor(
                holder.itemView.context,
                color
            )
        )

        /* ==============================
           ACCIONES
        ============================== */

        holder.btnEditar.setOnClickListener {
            onEditar(item)
        }

        holder.btnEliminar.setOnClickListener {
            onEliminar(item)
        }
    }
}