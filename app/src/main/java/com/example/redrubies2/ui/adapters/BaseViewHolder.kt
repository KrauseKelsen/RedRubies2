package com.example.redrubies2.ui.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Esta clase sirve para realizar listas en las vistas (tiene que ver con la estructura de RecyclerView,
 * se debe investigar sobre recycleView para entender)
 * Referencias: https://www.youtube.com/watch?v=QIVbnR9pQfY
 * */
abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(item:T,position:Int)
}
