package com.example.redrubies2.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.redrubies2.R
import com.example.redrubies2.datalayer.Despacho
import kotlinx.android.synthetic.main.general_row_2.view.*
import java.lang.IllegalArgumentException

class ListaDespachoAdapter(val listaDespacho: List<Despacho>, private val listener: Listener)  : RecyclerView.Adapter<BaseViewHolder<*>> (){
    private lateinit var context: Context
    interface Listener{
        fun onItemClick(despacho: Despacho)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        context = parent.context
        return ListaDespachoViewHolder(LayoutInflater.from(context).inflate(R.layout.general_row_2, parent, false))
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {

        when(holder){
            is ListaDespachoViewHolder -> holder.bind(listaDespacho[position],position)
            else -> throw IllegalArgumentException("Se olvido de pasar el viewholder en el bind")
        }
    }

    override fun getItemCount(): Int = listaDespacho.size
    inner class ListaDespachoViewHolder(itemView: View) : BaseViewHolder<Despacho>(itemView) {

        override fun bind(item: Despacho, position: Int) {
            itemView.setOnClickListener{listener.onItemClick(item)}
            itemView.labelCodigo.text = "Oferta: "
            itemView.labelCantidad.text = "${item.cantidad} kg de ${item.oferta} "
            itemView.labelFecha.text = ("Completa: " + item.sentera)
            itemView.labelComun.text = "Finca: ${item.finca}"
        }
    }
}