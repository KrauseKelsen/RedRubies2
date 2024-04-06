package com.example.redrubies2.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.redrubies2.R
import com.example.redrubies2.datalayer.Comun
import kotlinx.android.synthetic.main.general_row.view.*
import java.lang.IllegalArgumentException

class ComunAdapter(private val context: Context, val listaComunes: List<Comun>, private val itemClickListener: onComunClickListener)  : RecyclerView.Adapter<BaseViewHolder<*>> (){
    interface onComunClickListener{
        fun onItemClick(finca: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> = ComunesViewHolder(
        LayoutInflater.from(context).inflate(R.layout.general_row, parent,false))

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when(holder){
            is ComunesViewHolder -> holder.bind(listaComunes[position],position)
            else -> throw IllegalArgumentException("Se olvido de pasar el viewholder en el bind")
        }
    }

    override fun getItemCount(): Int = listaComunes.size

    inner class ComunesViewHolder(itemView: View) : BaseViewHolder<Comun>(itemView) {
        override fun bind(item: Comun, position: Int) {
            //Item view es el LinearLayout (la vista completa)
            itemView.setOnClickListener{itemClickListener.onItemClick(item.nombre)}
            itemView.labelNombre.text = item.nombre
            itemView.labelCodigo.text = item.codigo
        }
    }
}