package com.example.redrubies2.datalayer

class ListaRendimientoSemanal {
    var lunes = ArrayList<ListaRendimiento>()
    var hayLunes = false
    var martes = ArrayList<ListaRendimiento>()
    var miercoles = ArrayList<ListaRendimiento>()
    var jueves = ArrayList<ListaRendimiento>()
    var viernes = ArrayList<ListaRendimiento>()
    var sabado = ArrayList<ListaRendimiento>()


    fun inicializarArrayList(dia :  ArrayList<ListaRendimiento>){
        dia.add(inicializarComun(18.0,"FRESA" ))
        dia.add(inicializarComun(3.5,"ARANDANOS" ))
        dia.add(inicializarComun(3.5,"FRAMBUESA" ))
        dia.add(inicializarComun(3.0,"MORA" ))
        dia.add(inicializarComun(18.0,"UCHUVA" ))
    }


    fun inicializarComun(estandar : Double, nombre : String) : ListaRendimiento{
        var rendimiento = ListaRendimiento()
        var comun = Comun()
        comun.nombre = nombre
        comun.estandar = estandar
        rendimiento.comun = comun
        return rendimiento
    }

    fun calcularLunes(){
        for (r in lunes){
            if (r.listaInventario.size != 0){
                hayLunes = true
            }
        }
    }
}