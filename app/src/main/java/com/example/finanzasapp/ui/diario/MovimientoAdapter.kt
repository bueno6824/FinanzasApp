package com.example.finanzasapp.ui.diario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.finanzasapp.R
import com.example.finanzasapp.data.model.Movimiento
import java.text.NumberFormat
import java.util.Locale


class MovimientoAdapter(
    private val onEditar: (Movimiento) -> Unit,
    private val onEliminar: (Movimiento) -> Unit
) : RecyclerView.Adapter<MovimientoAdapter.ViewHolder>(){
    private var lista = listOf<Movimiento>()



    fun actualizarLista(nuevaLista: List<Movimiento>){
        lista = nuevaLista
        notifyDataSetChanged()
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val txtDescripcion: TextView = view.findViewById(R.id.txtDescripcion)
        val txtCategoria: TextView = view.findViewById(R.id.txtCategoria)
        val txtMonto : TextView = view.findViewById(R.id.txtMonto)
        val btnEditar: ImageButton  = view.findViewById(R.id.btnEditar)
        val btnEliminar: ImageButton  = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movimiento, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item =lista[position]
        holder.txtDescripcion.text = item.descripcion
        holder.txtCategoria.text = item.categoria
        holder.txtMonto.text = "${item.monto}"

        val formato = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

        val montoTexto = if (item.tipo == "ingreso") {
            "+${formato.format(item.monto)}"
        } else {
            "-${formato.format(item.monto)}"
        }

        holder.txtMonto.text = montoTexto

        val monto = item.monto

        holder.txtMonto.text = if (item.tipo == "ingreso") {
            "+$${monto}"
        } else {
            "-$${monto}"
        }

        val color = if (item.tipo == "ingreso") {
            android.R.color.holo_green_dark
        } else {
            android.R.color.holo_red_dark
        }

        holder.txtMonto.setTextColor(
            ContextCompat.getColor(holder.itemView.context, color)
        )

        holder.btnEditar.setOnClickListener {
            onEditar(item)
        }

        holder.btnEliminar.setOnClickListener {
            onEliminar(item)
        }
    }
    }