package com.example.redrubies2.ui.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.redrubies2.R
import com.example.redrubies2.businesslayer.libraries.Utils
import com.example.redrubies2.datalayer.ListaInventario
import kotlinx.android.synthetic.main.general_row_2.view.*
import java.util.stream.Collectors

class ListaInventarioAdapter(val listaInventario: List<ListaInventario>, private val listener: ListaInventarioAdapter.Listener)  : RecyclerView.Adapter<BaseViewHolder<*>> (){
    private lateinit var context:Context
    private var originalItems = ArrayList<ListaInventario>()

    init {
        originalItems.addAll(listaInventario)
    }
    interface Listener{
        fun onItemClick(item: ListaInventario)
    }

    fun filter(strSearch: String, arrayListDetailsFB: ArrayList<ListaInventario>) : ArrayList<ListaInventario> {
        var list = ArrayList<ListaInventario>()
        arrayListDetailsFB.forEach { listaInventario -> list.add(listaInventario) }
        if (strSearch.isEmpty()) {
            list.clear()
            list.addAll(arrayListDetailsFB)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                list.clear()
                val collect: List<ListaInventario> = arrayListDetailsFB.stream()
                    .filter { i: ListaInventario ->
                        Utils.containsIgnoreCase(
                            i.comun,
                            strSearch
                        ) || Utils.containsIgnoreCase(
                            i.trabajador,
                            strSearch
                        )
                    }
                    .collect(Collectors.toList<Any>()) as List<ListaInventario>
                list.addAll(collect)
            } else {
                list.clear()
                for (inventario in arrayListDetailsFB) {
                    if (inventario.comun.toLowerCase().contains(strSearch) || inventario.trabajador.toLowerCase().contains(strSearch)) {
                        list.add(inventario)
                    }
                }
            }
        }
        return list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        context = parent.context
        return ListaInventarioViewHolder(LayoutInflater.from(context).inflate(R.layout.general_row_2, parent, false))
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
            itemView.labelCodigo.text = item.trabajador
            itemView.labelCantidad.text = "${item.cantidad} kg"
            itemView.labelFecha.text = item.hora
            itemView.labelComun.text = item.comun

        }
    }
}