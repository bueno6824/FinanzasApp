package com.example.finanzasapp.ui.resumen

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.finanzasapp.R
import com.example.finanzasapp.ui.util.MoneyFormatter

class ResumenAdapter(
    private val listener: OnItemActionListener
) : ListAdapter<ResumenAdapter.ResumenItem, ResumenAdapter.ViewHolder>(
    DIFF_CALLBACK
) {

    interface OnItemActionListener {
        fun onEdit(item: ResumenItem)
        fun onDelete(item: ResumenItem)
    }

    data class ResumenItem(
        val categoria: String,
        val ingresos: Double,
        val gastos: Double
    )

    companion object {

        private val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<ResumenItem>() {

                override fun areItemsTheSame(
                    oldItem: ResumenItem,
                    newItem: ResumenItem
                ): Boolean {
                    return oldItem.categoria ==
                            newItem.categoria
                }

                override fun areContentsTheSame(
                    oldItem: ResumenItem,
                    newItem: ResumenItem
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }

    fun actualizar(
        nueva: List<ResumenItem>
    ) {
        submitList(nueva)
    }

    class ViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        val txtCategoria: TextView =
            view.findViewById(R.id.txtCategoria)

        val txtBalance: TextView =
            view.findViewById(R.id.txtBalance)

        val btnOpciones: ImageButton =
            view.findViewById(R.id.btnOpciones)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view =
            LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.item_resumen,
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
        val balance = item.ingresos - item.gastos

        configurarCategoria(
            holder = holder,
            categoria = item.categoria
        )

        configurarBalance(
            holder = holder,
            balance = balance
        )

        configurarMenu(
            holder = holder,
            item = item
        )
    }

    private fun configurarCategoria(
        holder: ViewHolder,
        categoria: String
    ) {
        val colorCategoria =
            when (categoria) {
                "Otros" ->
                    Color.parseColor("#60A5FA")

                "Trabajo" ->
                    Color.parseColor("#34D399")

                "Gustos" ->
                    Color.parseColor("#FBBF24")

                "Salud" ->
                    Color.parseColor("#FB7185")

                "Transporte" ->
                    Color.parseColor("#A78BFA")

                "Comida" ->
                    Color.parseColor("#2DD4BF")

                else ->
                    Color.WHITE
            }

        holder.txtCategoria.text =
            categoria

        holder.txtCategoria.setTextColor(
            colorCategoria
        )
    }

    private fun configurarBalance(
        holder: ViewHolder,
        balance: Double
    ) {
        val colorBalance =
            if (balance >= 0) {
                Color.parseColor("#2E7D32")
            } else {
                Color.parseColor("#C62828")
            }

        holder.txtBalance.setTextColor(
            colorBalance
        )

        holder.txtBalance.text =
            MoneyFormatter.moneda(balance)
    }

    private fun configurarMenu(
        holder: ViewHolder,
        item: ResumenItem
    ) {
        holder.btnOpciones.setOnClickListener { view ->

            val popup =
                PopupMenu(
                    view.context,
                    view
                )

            popup.menu.add("Editar")
            popup.menu.add("Eliminar")

            popup.setOnMenuItemClickListener { menuItem ->

                when (menuItem.title.toString()) {
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