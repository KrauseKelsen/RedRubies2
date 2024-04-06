package com.example.redrubies2.datalayer

import android.content.Context
import android.widget.Toast

class Excepciones (){

    fun OkDelete(msj: String, applicationContext: Context) {
        Toast.makeText(
            applicationContext,
            "$msj eliminado exitosamente",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun fail(applicationContext: Context) {
        Toast.makeText(
            applicationContext,
            "Ha ocurrido un error, comuniquese con soporte técnico",
            Toast.LENGTH_SHORT
        ).show()
    }
}