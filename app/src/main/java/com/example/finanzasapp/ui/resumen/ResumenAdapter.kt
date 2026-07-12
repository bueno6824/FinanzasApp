package com.example.finanzasapp.ui.resumen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.graphics.Color
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.finanzasapp.R


class ResumenAdapter(private val listener: OnItemActionListener): RecyclerView.Adapter<ResumenAdapter.ViewHolder>(){

    // Interfaz para comunicar las acciones hacia afuera del adaptador
    interface OnItemActionListener {
        fun onEdit(item: ResumenItem)
        fun onDelete(item: ResumenItem)
    }

    data class ResumenItem(
        val categoria: String,
        val ingresos: Double,
        val gastos: Double
    )

    private var lista = listOf<ResumenItem>()

    fun actualizar(nueva: List<ResumenItem>){
        lista = nueva
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val txtCategoria : TextView = view.findViewById(R.id.txtCategoria)
        val txtBalance: TextView = view.findViewById(R.id.txtBalance)
        // Agregamos la referencia al botón de opciones
        val btnOpciones: ImageButton = view.findViewById(R.id.btnOpciones)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_resumen, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        val balance = item.ingresos - item.gastos

        // 1. Definimos el MAPA de colores (El "Manual de identidad" de tu app)
        // Usamos colores pastel para que el texto de la categoría no compita con el saldo
        val colorCategoria = when(item.categoria) {
            "Otros"      -> Color.parseColor("#60A5FA") // Azul claro
            "Trabajo"    -> Color.parseColor("#34D399") // Esmeralda
            "Gustos"     -> Color.parseColor("#FBBF24") // Ámbar
            "Salud"      -> Color.parseColor("#FB7185") // Rosa Coral (No choca con el rojo)
            "Transporte" -> Color.parseColor("#A78BFA") // Violeta
            "Comida"     -> Color.parseColor("#2DD4BF") // Turquesa
            else         -> Color.WHITE                 // Color por defecto
        }
        // 2. Aplicamos el color al título de la categoría
        holder.txtCategoria.text = item.categoria
        holder.txtCategoria.setTextColor(colorCategoria)

        // 3. Lógica del Balance (Monto)
        // Aquí sí usamos Rojo/Verde estándar porque es dinero
        if (balance >= 0) {
            holder.txtBalance.setTextColor(Color.parseColor("#2E7D32")) // Verde
            holder.txtBalance.text = "+${String.format("$%,.2f", balance)}"
        } else {
            holder.txtBalance.setTextColor(Color.parseColor("#EF4444")) // Rojo vibrante
            holder.txtBalance.text = String.format("$%,.2f", balance)
        }





        if (balance >= 0) {
            holder.txtBalance.setTextColor(Color.parseColor("#2E7D32")) // verde pro
        } else {
            holder.txtBalance.setTextColor(Color.parseColor("#C62828")) // rojo pro
        }

        val balanceTexto = if (balance >= 0) {
            "+${String.format("$%,.2f", balance)}"
        } else {
            String.format("$%,.2f", balance)
        }
        holder.txtBalance.text = balanceTexto

        // --- Lógica del ImageButton con PopupMenu ---
        holder.btnOpciones.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            // Agregamos las opciones programáticamente para no depender de un XML de menú extra si no quieres
            popup.menu.add("Editar")
            popup.menu.add("Eliminar")

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.title) {
                    "Editar" -> {
                        listener.onEdit(item)
                        true
                    }
                    "Eliminar" -> {
                        listener.onDelete(item)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }
}