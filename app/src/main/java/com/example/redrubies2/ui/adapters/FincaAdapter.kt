package com.example.redrubies2.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.redrubies2.R
import com.example.redrubies2.datalayer.Finca
import kotlinx.android.synthetic.main.general_row.view.*
import java.lang.IllegalArgumentException

class FincaAdapter(val listaFincas: ArrayList<Finca>, private val listener: Listener)  : RecyclerView.Adapter<BaseViewHolder<*>> (){
    private lateinit var context:Context
    //Se crea una interfaz para el click de la row, se podrían usar lambda incluso haciendolo mas fácil
    // but quiero usar interfaces cuz ¿why not?
    interface Listener{
        fun onItemClick(idFinca: String)
    }

    //Aqui se infla la lista que le mandamos cuando se crea la actividad
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        context = parent.context
        return FincasViewHolder(LayoutInflater.from(context).inflate(R.layout.general_row, parent,false))
    }

    //Esta vara toma cada dato y se lo pone a cada componente en la vista
    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        /**
         * Bindea, o poner la información en cada uno de los componentes, dentro de la celda
         * Aca se le dice que ponga la info dentro de cada celda, encuentra la info de la lista de
         * fincas que le pasamos y como ya tenemos position podemos jugar con eso
         */
        when(holder){
            is FincasViewHolder -> holder.bind(listaFincas[position],position)
            else -> throw IllegalArgumentException("Se olvido de pasar el viewholder en el bind")
        }
    }

    //Aqui la cantidad de datos
    override fun getItemCount(): Int = listaFincas.size

    /**
     *Usamos inner class para decir que es hija de FincaAdapter, esto permite que cuando Adapter se
     * destruya también se vaya consigo la inner class, y no dejar espacios en memoria nadando
     */

    inner class FincasViewHolder(itemView: View) : BaseViewHolder<Finca>(itemView) {
        /**
         * Este metodo bind toma cada Finca que pasa BaseViewHolder por medio de T
         * y lo bindea
         */
        override fun bind(item: Finca, position: Int) {
            //Item view es el LinearLayout (la vista completa)
            itemView.setOnClickListener{listener.onItemClick(item.id)}
            Glide.with(context).load(item.imagen).into(itemView.fincaImage)
            itemView.labelNombre.text = item.nombre
            itemView.labelCodigo.text = item.codigo
        }
    }
}