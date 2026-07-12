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

class MovimientoDetalleAdapter(private val listener: OnMovimientoClickListener) :
    RecyclerView.Adapter<MovimientoDetalleAdapter.ViewHolder>() {

    interface OnMovimientoClickListener {
        fun onEdit(movimiento: Movimiento)
        fun onDelete(movimiento: Movimiento)
    }

    private var lista = listOf<Movimiento>()

    fun actualizar(nueva: List<Movimiento>) {
        lista = nueva
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtDesc: TextView = view.findViewById(R.id.txtDescripcion)
        val txtCat: TextView = view.findViewById(R.id.txtCategoria)
        val txtMonto: TextView = view.findViewById(R.id.txtMonto)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEditar)
        val btnDelete: ImageButton = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_movimiento, parent, false))

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mov = lista[position]
        holder.txtDesc.text = mov.descripcion
        holder.txtCat.text = mov.categoria
        holder.txtMonto.text = String.format("$%,.2f", mov.monto)
        holder.txtMonto.setTextColor(if(mov.tipo == "ingreso") Color.parseColor("#10B981") else Color.parseColor("#EF4444"))

        holder.btnEdit.setOnClickListener { listener.onEdit(mov) }
        holder.btnDelete.setOnClickListener { listener.onDelete(mov) }
    }
}