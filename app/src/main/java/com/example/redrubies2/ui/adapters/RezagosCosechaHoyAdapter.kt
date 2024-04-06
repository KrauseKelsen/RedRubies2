package com.example.redrubies2.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.redrubies2.R
import com.example.redrubies2.datalayer.ListaInventario
import kotlinx.android.synthetic.main.general_row.view.labelCodigo
import kotlinx.android.synthetic.main.general_row_2.view.*
import java.lang.IllegalArgumentException

class RezagosCosechaHoyAdapter(val listaInventario: List<ListaInventario>, private val listener: RezagosCosechaHoyAdapter.Listener)  : RecyclerView.Adapter<BaseViewHolder<*>> (){
    private lateinit var context:Context
    interface Listener{
        fun onItemClick(obj: ListaInventario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        context = parent.context
        return ListaInventarioViewHolder(LayoutInflater.from(context).inflate(R.layout.general_row_3, parent, false))
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {

        when(holder){
            is ListaInventarioViewHolder -> holder.bind(listaInventario[position],position)
            else -> throw IllegalArgumentException("Se olvido de pasar el viewholder en el bind")
        }
    }

    override fun getItemCount(): Int = listaInventario.size
    inner class ListaInventarioViewHolder(itemView: View) : BaseViewHolder<ListaInventario>(itemView) {

        override fun bind(item: ListaInventario, position: Int) {
            itemView.setOnClickListener{listener.onItemClick(item)}
            itemView.labelCodigo.text = item.ntrabajador
            itemView.labelCantidad.text = "Cantidad: ${item.cantidad} kg"
            itemView.labelFecha.text = item.subido
            itemView.labelComun.text = item.ncomun
        }
    }
}