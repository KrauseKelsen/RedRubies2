package com.example.redrubies2.datalayer

class ListaRendimiento() {
    lateinit var comun: Comun
    var jornadasCumplidas = JornadasCumplidas()
    var jornadas = 0
    var sum = 0.0
    var rendimiento = 0.0
    lateinit var fecha : String
    var listaInventario = ArrayList<ListaInventario>()
}
