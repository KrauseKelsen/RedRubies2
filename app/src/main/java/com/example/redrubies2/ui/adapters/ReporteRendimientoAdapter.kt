package com.example.redrubies2.ui.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.redrubies2.R
import com.example.redrubies2.datalayer.ListaRendimiento
import kotlinx.android.synthetic.main.general_row_2.view.*
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.math.RoundingMode

class ReporteRendimientoAdapter(val listaRendimiento: List<ListaRendimiento>, private val listener: Listener)  : RecyclerView.Adapter<BaseViewHolder<*>> (){
    private lateinit var context: Context
    interface Listener{
        fun onItemClick(item: ListaRendimiento)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        context = parent.context
        return ListaRendimientoViewHolder(LayoutInflater.from(context).inflate(R.layout.general_row_2, parent, false))
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {

        when(holder){
            is ListaRendimientoViewHolder -> holder.bind(listaRendimiento[position],position)
            else -> throw IllegalArgumentException("Se olvido de pasar el viewholder en el bind")
        }
    }

    override fun getItemCount(): Int = listaRendimiento.size
    inner class ListaRendimientoViewHolder(itemView: View) : BaseViewHolder<ListaRendimiento>(itemView) {
        override fun bind(item: ListaRendimiento, position: Int) {
            itemView.setOnClickListener{listener.onItemClick(item)}
            itemView.labelCodigo.text = item.comun.nombre
            itemView.labelCantidad.text = "${BigDecimal(item.sum).setScale(1, RoundingMode.HALF_EVEN)} kg de ${item.comun.estandar.toDouble()*item.jornadas} kg"
            itemView.labelFecha.text = "Total: ${BigDecimal(item.rendimiento).setScale(1, RoundingMode.HALF_EVEN)}%"
            itemView.labelComun.text = "Horas: ${item.jornadas}"

            if(item.rendimiento < 100.0){
                itemView.labelCantidad.setTextColor(Color.parseColor("#FF4746"))
                itemView.labelFecha.setTextColor(Color.parseColor("#FF4746"))
            }else{
                itemView.labelCantidad.setTextColor(Color.parseColor("#00C851"))
                itemView.labelFecha.setTextColor(Color.parseColor("#00C851"))
            }


        }
    }
}