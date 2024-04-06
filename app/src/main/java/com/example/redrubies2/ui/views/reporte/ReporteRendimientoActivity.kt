package com.example.redrubies2.ui.views.reporte

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.redrubies2.R
import com.example.redrubies2.businesslayer.libraries.Conectividad
import com.example.redrubies2.businesslayer.libraries.PDF.Common
import com.example.redrubies2.businesslayer.libraries.PDF.Designer
import com.example.redrubies2.businesslayer.repository.FireBaseModel
import com.example.redrubies2.businesslayer.libraries.Resquest
import com.example.redrubies2.datalayer.*
import com.example.redrubies2.ui.adapters.ReporteRendimientoAdapter
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.MetadataChanges
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Font
import com.itextpdf.text.PageSize
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfWriter
import com.jakewharton.threetenabp.AndroidThreeTen
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_reporte_produccion.*
import kotlinx.android.synthetic.main.activity_reporte_rendimiento.*
import kotlinx.android.synthetic.main.activity_reporte_rendimiento.dia
import kotlinx.android.synthetic.main.activity_reporte_rendimiento.generarPDF
import kotlinx.android.synthetic.main.activity_reporte_rendimiento.offline
import kotlinx.android.synthetic.main.activity_reporte_rendimiento.recyclerView
import kotlinx.android.synthetic.main.general_row_2.*
import kotlinx.android.synthetic.main.general_row_2.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue

@RequiresApi(Build.VERSION_CODES.O)
class ReporteRendimientoActivity : AppCompatActivity(), ReporteRendimientoAdapter.Listener {
    private val TAG = "ReporteRendimientoActivity"
    val conectividad: Conectividad = Conectividad()

    val context: Context = this

    val firebaseModel: FireBaseModel = FireBaseModel()
    private val request: Resquest = Resquest()
    var arrayListDetailsSQL: ArrayList<ListaInventario> = ArrayList();
    var arrayListDetailsFB: ArrayList<ListaRendimiento> = ArrayList();
    var arrayInventarioSemana: ArrayList<ListaRendimiento> = ArrayList();
    var contador = 0
    var contador2 = 0
    var designer = Designer()
    var totalSolicitado = 0.0
    private lateinit var formatter: DateTimeFormatter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFirebase()
        setContentView(R.layout.activity_reporte_rendimiento)
        AndroidThreeTen.init(application);
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        generarPDF.isEnabled = false
        if (conectividad.connectedTo(context))
            loadSQLDataToFirestore()
        getComunesFromFirestore()

        getInventariosFromFirestore()

        generarPDF.setOnClickListener {
            Dexter.withActivity(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        designer = Designer()
                        designer.setFileName("ReporteRendimiento_${Random().nextInt(10000 * 10000)}.pdf")
                        if (arrayListDetailsFB.size == 0) {
                            Toast.makeText(
                                this@ReporteRendimientoActivity,
                                "No se pudo generar el reporte por que no hay cosechas durante la semana",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            crearPDFFile(Common.getAppPath(this@ReporteRendimientoActivity) + designer.getFileName())
                        }
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        Toast.makeText(
                            this@ReporteRendimientoActivity,
                            "Permiso denegado, no se podrán generar reportes",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken?
                    ) {
                        TODO("Not yet implemented")
                    }

                })
                .check()
        }
    }

    private fun crearPDFFile(path: String) {
        if (File(path).exists())
            File(path).delete()
        try {
            //Save
            PdfWriter.getInstance(designer.getDocument(), FileOutputStream(path))
            //Open file to write
            designer.getDocument().open()

            //Settings
            designer.getDocument().pageSize = PageSize.A4
            designer.getDocument().addCreationDate()
            designer.getDocument().addAuthor("RedRubbies")
            designer.getDocument().addCreator("Krause Kelsen")

            //Add title
            designer.crearTitulo(18.0F, "Reporte de Rendimiento")

            val diasDeCosecha =
                intent.getStringArrayExtra("diasDeCosecha") // array con dias seleccionados

            val primerDia = diasDeCosecha.first()
            val ultimoDia = diasDeCosecha.last()

            val date1 = LocalDate.parse(primerDia, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val date2 = LocalDate.parse(ultimoDia, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            val dateFormmat1 = date1.format(formatter)
            val dateFormmat2 = date2.format(formatter)

            val contenido = ArrayList<String>()
            contenido.add(intent.getStringExtra("nTrabajador"))
            contenido.add("$dateFormmat1 al $dateFormmat2")
            generarEncabezado(contenido)

            var arrayComunes = ArrayList<Comun>()
            for (rendimiento in arrayListDetailsFB) {
                arrayComunes.add(rendimiento.comun)
            }

            var listaRendimientoSemanal = ListaRendimientoSemanal()
            listaRendimientoSemanal.inicializarArrayList(listaRendimientoSemanal.lunes)
            listaRendimientoSemanal.inicializarArrayList(listaRendimientoSemanal.martes)
            listaRendimientoSemanal.inicializarArrayList(listaRendimientoSemanal.miercoles)
            listaRendimientoSemanal.inicializarArrayList(listaRendimientoSemanal.jueves)
            listaRendimientoSemanal.inicializarArrayList(listaRendimientoSemanal.viernes)
            listaRendimientoSemanal.inicializarArrayList(listaRendimientoSemanal.sabado)
            for (rendimiento in arrayListDetailsFB) {
                for (inventario in rendimiento.listaInventario) {
                    var diaDelInventario = LocalDate.parse(
                        inventario.fecha,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    ).dayOfWeek.ordinal
                    //Aquí tenemos todos los inventarios independiente del rendimiento
                    guardarEnDias(inventario, diaDelInventario, 0, listaRendimientoSemanal.lunes)
                    guardarEnDias(inventario, diaDelInventario, 1, listaRendimientoSemanal.martes)
                    guardarEnDias(
                        inventario,
                        diaDelInventario,
                        2,
                        listaRendimientoSemanal.miercoles
                    )
                    guardarEnDias(inventario, diaDelInventario, 3, listaRendimientoSemanal.jueves)
                    guardarEnDias(inventario, diaDelInventario, 4, listaRendimientoSemanal.viernes)
                    guardarEnDias(inventario, diaDelInventario, 5, listaRendimientoSemanal.sabado)
                }
            }




            generarDiario(listaRendimientoSemanal.lunes, 0)
            generarDiario(listaRendimientoSemanal.martes, 0)
            generarDiario(listaRendimientoSemanal.miercoles, 0)
            generarDiario(listaRendimientoSemanal.jueves, 0)
            generarDiario(listaRendimientoSemanal.viernes, 0)
            generarDiario(listaRendimientoSemanal.sabado, 0)

            designer.getDocument().close()


            Toast.makeText(this, "Ha sido guardado en $path", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun generarDiario(
        list: ArrayList<ListaRendimiento>,
        cL: Int
    ) {
        var cL1 = cL
        var kilosEsperados = 0.0
        var cantidadComunes = 0
        var acumParticion = 0.0
        var totalFinal = 0.0
        for (rendimiento in list) {
            val triple = triple(rendimiento, cL1, cantidadComunes, kilosEsperados)
            cL1 = triple.first
            cantidadComunes = triple.second
            kilosEsperados = triple.third
        }
        calcularTotalDiario(
            list,
            kilosEsperados,
            cantidadComunes,
            acumParticion,
            totalFinal
        )
    }

    private fun calcularTotalDiario(
        listaRendimiento: ArrayList<ListaRendimiento>,
        kilosEsperados: Double,
        cantidadComunes: Int,
        acumParticion: Double,
        totalFinal: Double
    ) {
        var acumParticion1 = acumParticion
        var totalFinal1 = totalFinal
        for (rendimiento in listaRendimiento) {
            if (rendimiento.sum != 0.0 && rendimiento.rendimiento != 0.0 && rendimiento.jornadas != 0) {
                var particion = kilosEsperados / cantidadComunes
                if(rendimiento.rendimiento >= 100){
                    acumParticion1 += particion
                }else{
                    acumParticion1 += (rendimiento.rendimiento * particion) / 100
                }
                totalFinal1 = (acumParticion1 * 100) / kilosEsperados
            }
        }
        if(totalFinal1 != 0.0){
            if (totalFinal1 >= 100) {
                designer.crearGrupo(
                    "Rendimiento total diario:",
                    "${BigDecimal(totalFinal1).setScale(2, RoundingMode.HALF_EVEN)}%",
                    true
                )
            } else {
                designer.crearGrupo(
                    "Rendimiento total diario:",
                    "${BigDecimal(totalFinal1).setScale(2, RoundingMode.HALF_EVEN)}%",
                    false
                )
            }
            designer.espacio()
        }
    }

    private fun triple(
        rendimiento: ListaRendimiento,
        cL: Int,
        cantidadComunes: Int,
        kilosEsperados: Double
    ): Triple<Int, Int, Double> {
        var cL1 = cL
        var cantidadComunes1 = cantidadComunes
        var kilosEsperados1 = kilosEsperados
        if (rendimiento.sum != 0.0 && rendimiento.rendimiento != 0.0 && rendimiento.jornadas != 0) {
            if (cL1 == 0) {
                val date =
                    LocalDate.parse(rendimiento.fecha, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                designer.crearTitulo(12.0F, traducirDia(date.dayOfWeek.toString()))
                cL1 += 1
            }
            cantidadComunes1 += 1
            generarTabla(rendimiento)
        }
        kilosEsperados1 += rendimiento.comun.estandar * rendimiento.jornadas
        return Triple(cL1, cantidadComunes1, kilosEsperados1)
    }

    private fun guardarEnDias(
        inventario: ListaInventario,
        diaDelInventario: Int,
        ordinal: Int,
        diaRendimiento: ArrayList<ListaRendimiento>
    ) {
        if (diaDelInventario == ordinal) {
            //Este inventario es del miercoles
            if (inventario.ncomun == "ARANDANOS") {
                // Este inventario es del x y también es de arandano, ahora armo el rendimiento
                for (l in diaRendimiento) {
                    if (l.comun.nombre == "ARANDANOS") {
                        fillRendimiento(l, inventario)
                    }
                }
            } else if (inventario.ncomun == "FRESA") {
                // Este inventario es del x y también es de fresa, ahora armo el rendimiento
                for (l in diaRendimiento) {
                    if (l.comun.nombre == "FRESA") {
                        fillRendimiento(l, inventario)
                    }
                }
            }else if (inventario.ncomun == "MORA") {
                // Este inventario es del x y también es de MORA, ahora armo el rendimiento
                for (l in diaRendimiento) {
                    if (l.comun.nombre == "MORA") {
                        fillRendimiento(l, inventario)
                    }
                }
            }else if (inventario.ncomun == "FRAMBUESA") {
                // Este inventario es del x y también es de FRAMBUESA, ahora armo el rendimiento
                for (l in diaRendimiento) {
                    if (l.comun.nombre == "FRAMBUESA") {
                        fillRendimiento(l, inventario)
                    }
                }
            }else if (inventario.ncomun == "UCHUVA") {
                // Este inventario es del x y también es de UCHUVA, ahora armo el rendimiento
                for (l in diaRendimiento) {
                    if (l.comun.nombre == "UCHUVA") {
                        fillRendimiento(l, inventario)
                    }
                }
            }
        }
    }

    private fun fillRendimiento(
        l: ListaRendimiento,
        inventario: ListaInventario
    ) {
        l.listaInventario.add(inventario)
        l.fecha = inventario.fecha
        l.sum += inventario.cantidad.toDouble()
        l.jornadas = calcularJornada(l, inventario)
        l.fecha = inventario.fecha
        val cantidadkgEsperada =
            l.comun.estandar * l.jornadas.toDouble()
        l.rendimiento =
            (l.sum * 100) / cantidadkgEsperada
    }

    private fun generarEncabezado(contenido: ArrayList<String>) {
        designer.crearEncabezado("Trabajador:", contenido[0])
        designer.crearEncabezado("Semana:", contenido[1])
        designer.espacio()
    }

    private fun generarTabla(rendimiento: ListaRendimiento) {
        val totalRojo = Font(BaseFont.createFont("assets/fonts/brandon_medium.otf", "UTF-8", BaseFont.EMBEDDED), 10.0f, Font.NORMAL, BaseColor(255, 20, 20)) // totales
        val totalVerde = Font(BaseFont.createFont("assets/fonts/brandon_medium.otf", "UTF-8", BaseFont.EMBEDDED), 10.0f, Font.NORMAL, BaseColor(0, 200, 81)) // totales
        val valueStyle = Font(BaseFont.createFont("assets/fonts/brandon_medium.otf", "UTF-8", BaseFont.EMBEDDED), 10.0f, Font.NORMAL, BaseColor.BLACK) // letra normal

        //Cosechass
        var esperado = BigDecimal(rendimiento.jornadas * rendimiento.comun.estandar).setScale(2, RoundingMode.HALF_EVEN).toDouble()
        esperado = BigDecimal(esperado).setScale(2, RoundingMode.HALF_EVEN).toDouble()
        val porcentaje = BigDecimal(rendimiento.rendimiento).setScale(2, RoundingMode.HALF_EVEN).toDouble()
        var extraOdeuda = rendimiento.sum - esperado
        extraOdeuda = BigDecimal(extraOdeuda).setScale(2, RoundingMode.HALF_EVEN).toDouble()
        rendimiento.sum = BigDecimal(rendimiento.sum).setScale(2, RoundingMode.HALF_EVEN).toDouble()
        if (rendimiento.sum >= esperado){
            designer.crearGrupo("Cosecha:", rendimiento.comun.nombre, "Jornadas laboradas:", rendimiento.jornadas.toString(), valueStyle, valueStyle)
            designer.crearGrupo("Kilos esperados:", "$esperado kg","Kilos entregados:", "${rendimiento.sum} kg", totalVerde, totalVerde)
            designer.crearGrupo( "Rendimiento:", "$porcentaje%", "Kilos extra:", "+${extraOdeuda.absoluteValue} kg", totalVerde, totalVerde)
        }else{
            designer.crearGrupo("Cosecha:", rendimiento.comun.nombre, "Jornadas laboradas:", rendimiento.jornadas.toString(), valueStyle, valueStyle)
            designer.crearGrupo("Kilos esperados:", "$esperado kg","Kilos entregados:", "${rendimiento.sum} kg", totalRojo, totalRojo)
            designer.crearGrupo("Rendimiento:", "$porcentaje%", "Kilos debidos:", "-${extraOdeuda.absoluteValue} kg", totalRojo, totalRojo)
        }

        designer.espacio()
    }

    private fun traducirDia(pdia : String): String {
        return when(pdia){
            "MONDAY" -> "Lunes"
            "TUESDAY" -> "Martes"
            "WEDNESDAY" -> "Miercoles"
            "THURSDAY" -> "Jueves"
            "FRIDAY" -> "Viernes"
            "SATURDAY" -> "Sabado"
            else -> "???"
        }
    }

    private fun getComunesFromFirestore() {
        firebaseModel.db.collection("Comunes")
            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen error", e)
                    return@addSnapshotListener
                }

                //este for recorre toda la colección y mte el doc en un "change"
                for (change in querySnapshot!!.documentChanges) {
                    //se compara si el change ha cambiado en tiempo real
                    if (change.type == DocumentChange.Type.ADDED) {
                        //pregunta de donde viene el doc (cache o server)
                        if (querySnapshot.metadata.isFromCache) {
                            Log.d(
                                TAG,
                                "Información obtenida del cache en la vista ReporteRedimientoActivity"
                            )
                        } else {
                            Log.d(
                                TAG,
                                "Información obtenida del servidor en la vista ReporteRedimientoActivity"
                            )
                        }

                        val data = Comun()
                        data.nombre = change.document["Nombre"].toString()
                        data.estandar = change.document["EstandarHora"].toString().toDouble()
                        //imprime lo que obtuvo
                        Log.d(TAG, "Se obtuvo: ${change.document.id} => ${change.document.data}")
                        val listaRendimiento = ListaRendimiento()
                        listaRendimiento.comun = data
                        val listaRendimiento2 = ListaRendimiento()
                        listaRendimiento2.comun = data
                        arrayInventarioSemana.add(listaRendimiento)
                        arrayListDetailsFB.add(listaRendimiento2)
                    }
                }
            }
    }

    private fun loadSQLDataToFirestore() {
        request.client.newCall(request.getRequestEndPoint("Inventario")).enqueue(object :
            Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val str_response = response.body()!!.string()
                //creating json object
                val json_contact: JSONObject = JSONObject(str_response)
                //creating json array
                val jsonarrayInfo: JSONArray = json_contact.getJSONArray("Data")
                val size: Int = jsonarrayInfo.length()
                arrayListDetailsSQL = ArrayList()
                for (i in 0 until size) {
                    val jsonObjectdetail: JSONObject = jsonarrayInfo.getJSONObject(i)
                    val model: ListaInventario = ListaInventario();
                    model.id = jsonObjectdetail.getString("Id")
                    model.cantidad = jsonObjectdetail.getString("Cantidad")
                    model.comun = jsonObjectdetail.getString("Comun")
                    model.trabajador = jsonObjectdetail.getString("CodigoTrabajador")
                    model.ntrabajador = jsonObjectdetail.getString("Trabajador")
                    model.hora = jsonObjectdetail.getString("Hora")
                    model.invernadero = jsonObjectdetail.getString("Invernadero")
                    model.variedad = jsonObjectdetail.getString("Variedad")
                    arrayListDetailsSQL.add(model)
                }
                runOnUiThread {
                    firebaseModel.addDocumentInventario(arrayListDetailsSQL)
                }

            }
        })
    }

    private fun getArraySemana() {
        firebaseModel.db.collection("InventarioCosechaDia")
            .whereEqualTo("CodigoTrabajador", intent.getStringExtra("idTrabajador"))

            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen error", e)
                    return@addSnapshotListener
                }

                //este for recorre toda la colección y mte el doc en un "change"
                for (change in querySnapshot!!.documentChanges) {
                    //se compara si el change ha cambiado en tiempo real
                    if (change.type == DocumentChange.Type.ADDED) {
                        //pregunta de donde viene el doc (cache o server)
                        if (querySnapshot.metadata.isFromCache) {
                            Log.d(
                                TAG,
                                "Información obtenida del cache en la vista ReporteRedimientoActivity"
                            )
                        } else {
                            Log.d(
                                TAG,
                                "Información obtenida del servidor en la vista ReporteRedimientoActivity"
                            )
                        }


                        //construye un objeto apartir del doc actual
                        //buildListaInventarioDia(change)
                        buildListaInventario(change, arrayListDetailsFB)


                        //imprime lo que obtuvo
                        Log.d(TAG, "Se obtuvo: ${change.document.id} => ${change.document.data}")
                    }
                }
                //aquí se llega cuando ya el recorrido ha finalizado exitosamente entonces está listo para seterarlo
                if (contador2 == 0) {
                    //reiniciamos la vista por aquello que se actualice en tiempo real
                    val listaFinal = ArrayList<ListaRendimiento>()
                    for (lista in arrayListDetailsFB) {
                        if (lista.sum != 0.0 && lista.rendimiento != 0.0 && lista.jornadas != 0) {
                            listaFinal.add(lista)
                        }
                    }
                    arrayListDetailsFB = listaFinal
                    generarPDF.isEnabled = true
                    contador2++
                }
            }
    }

    private fun getInventariosFromFirestore() {
        firebaseModel.db.collection("InventarioCosechaDia")
            .whereEqualTo("CodigoTrabajador", intent.getStringExtra("idTrabajador"))

            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen error", e)
                    return@addSnapshotListener
                }

                //este for recorre toda la colección y mte el doc en un "change"
                for (change in querySnapshot!!.documentChanges) {
                    //se compara si el change ha cambiado en tiempo real
                    if (change.type == DocumentChange.Type.ADDED) {
                        //pregunta de donde viene el doc (cache o server)
                        if (querySnapshot.metadata.isFromCache) {
                            Log.d(
                                TAG,
                                "Información obtenida del cache en la vista ReporteRedimientoActivity"
                            )
                        } else {
                            Log.d(
                                TAG,
                                "Información obtenida del servidor en la vista ReporteRedimientoActivity"
                            )
                        }


                        //construye un objeto apartir del doc actual
                        //buildListaInventarioDia(change)
                        buildListaInventario(change, arrayInventarioSemana)


                        //imprime lo que obtuvo
                        Log.d(TAG, "Se obtuvo: ${change.document.id} => ${change.document.data}")
                    }
                }
                //aquí se llega cuando ya el recorrido ha finalizado exitosamente entonces está listo para seterarlo
                if (contador == 0) {
                    //reiniciamos la vista por aquello que se actualice en tiempo real
                    recyclerView.layoutManager = LinearLayoutManager(this)
                    val listaFinal = ArrayList<ListaRendimiento>()
                    for (lista in arrayInventarioSemana) {
                        var bandera = debeEntrar(lista)
                        if (bandera) {
                            lista.sum = 0.0
                            lista.rendimiento = 0.0
                            lista.jornadas = 0
                            lista.jornadasCumplidas = JornadasCumplidas()
                            val listaInven = ArrayList<ListaInventario>()
                            for (inventario in lista.listaInventario){
                                if(inventario.fecha == intent.getStringExtra("Fecha")){
                                    listaInven.add(inventario)
                                    lista.sum += inventario.cantidad.toDouble()
                                    lista.jornadas = calcularJornada(
                                        lista,
                                        inventario
                                    )
                                    lista.fecha = inventario.fecha
                                    val cantidadkgEsperada =
                                        lista.comun.estandar * lista.jornadas.toDouble()
                                    lista.rendimiento =
                                        (lista.sum * 100) / cantidadkgEsperada
                                }
                            }
                            lista.listaInventario = listaInven
                            listaFinal.add(lista)
                        }
                    }
                    setupRecyclerView(listaFinal)
                    contador++
                }
            }
    }

    private fun debeEntrar(lista: ListaRendimiento): Boolean {
        lista.listaInventario.forEach { if (it.fecha == intent.getStringExtra("Fecha").toString()) return true}
        return false
    }

    private fun buildListaInventario(change: DocumentChange, arrayListDetails: ArrayList<ListaRendimiento>) {
        val inventario = ListaInventario()
        val diasDeCosecha =
            intent.getStringArrayExtra("diasDeCosecha") // array con dias seleccionados

        for (diaDeCosecha in diasDeCosecha) {
            val diaDeCosechaActual = LocalDate.parse(diaDeCosecha, formatter)
            if (diaDeCosechaActual.toString() == change.document["Fecha"].toString()) {
                inventario.cantidad = change.document["Cantidad"].toString()
                inventario.id = change.document.id
                inventario.ncomun = change.document["Comun"].toString()
                inventario.ntrabajador = change.document["Trabajador"].toString()
                inventario.trabajador = change.document["CodigoTrabajador"].toString()
                inventario.hora = change.document["Hora"].toString()
                inventario.fecha = change.document["Fecha"].toString()
                inventario.invernadero = change.document["Invernadero"].toString()
                inventario.variedad = change.document["Variedad"].toString()
            }
        }
        if (inventario.id != "") {
            //lo añade al arraylist
            for (listaRendimiento in arrayListDetails) {
                if (listaRendimiento.comun.nombre == inventario.ncomun) {
                    listaRendimiento.listaInventario.add(inventario)
                    listaRendimiento.sum += inventario.cantidad.toDouble()
                    listaRendimiento.jornadas = calcularJornada(
                        listaRendimiento,
                        inventario
                    )
                    listaRendimiento.fecha = inventario.fecha
                    val cantidadkgEsperada =
                        listaRendimiento.comun.estandar * listaRendimiento.jornadas.toDouble()
                    listaRendimiento.rendimiento =
                        (listaRendimiento.sum * 100) / cantidadkgEsperada
                }
            }
        }
    }

    private fun calcularJornada(
        listaRendimiento: ListaRendimiento,
        inventario: ListaInventario
    ): Int {
        val s = inventario.hora.split(":")
        if (s[0].toInt() == 6) {
            if (!listaRendimiento.jornadasCumplidas.seis)
                listaRendimiento.jornadas += 1
            listaRendimiento.jornadasCumplidas.seis = true
        }
        if (s[0].toInt() == 7) {
            if (!listaRendimiento.jornadasCumplidas.siete)
                listaRendimiento.jornadas += 1
            listaRendimiento.jornadasCumplidas.siete = true
        }
        if (s[0].toInt() == 8) {
            if (!listaRendimiento.jornadasCumplidas.ocho)
                listaRendimiento.jornadas += 1
            listaRendimiento.jornadasCumplidas.ocho = true
        }
        if (s[0].toInt() == 9) {
            if (!listaRendimiento.jornadasCumplidas.nueve)
                listaRendimiento.jornadas += 1
            listaRendimiento.jornadasCumplidas.nueve = true
        }
        if (s[0].toInt() == 10) {
            if (!listaRendimiento.jornadasCumplidas.diez)
                listaRendimiento.jornadas += 1
            listaRendimiento.jornadasCumplidas.diez = true
        }
        if (s[0].toInt() == 11) {
            if (!listaRendimiento.jornadasCumplidas.once)
                listaRendimiento.jornadas += 1
            listaRendimiento.jornadasCumplidas.once = true
        }
        if (s[0].toInt() == 12) {
            if (!listaRendimiento.jornadasCumplidas.doce)
                listaRendimiento.jornadas += 1
            listaRendimiento.jornadasCumplidas.doce = true
        }
        if (s[0].toInt() == 13) {
            if (!listaRendimiento.jornadasCumplidas.trece)
                listaRendimiento.jornadas += 1
            listaRendimiento.jornadasCumplidas.trece = true
        }
        if (s[0].toInt() == 14) {
            if (!listaRendimiento.jornadasCumplidas.catorce)
                listaRendimiento.jornadas += 1
            listaRendimiento.jornadasCumplidas.catorce = true
        }
        if (s[0].toInt() == 15) {
            if (!listaRendimiento.jornadasCumplidas.quince)
                listaRendimiento.jornadas += 1
            listaRendimiento.jornadasCumplidas.quince = true
        }
        if (s[0].toInt() == 16) {
            if (!listaRendimiento.jornadasCumplidas.dieciseis)
                listaRendimiento.jornadas += 1
            listaRendimiento.jornadasCumplidas.dieciseis = true
        }
        if (s[0].toInt() == 17 || s[0].toInt() == 18) {
            if (!listaRendimiento.jornadasCumplidas.diecisiete)
                listaRendimiento.jornadas += 1
            listaRendimiento.jornadasCumplidas.diecisiete = true
        }
        return listaRendimiento.jornadas
    }

    private fun setupRecyclerView(arrayListDetails: ArrayList<ListaRendimiento>) {
        contador = 0
        getArraySemana()
        labelRendimiento.text = "Rendimiento de: ${intent.getStringExtra("nTrabajador")}"
        if(arrayListDetails.size!=0){
            totalSolicitado = calcularExtras(arrayListDetails)
            val totalExtras = calcularRendimiento(arrayListDetails)

            var totalNeto =
                BigDecimal(totalSolicitado - totalExtras).setScale(1, RoundingMode.HALF_EVEN).toDouble()
            semanal.text = "+${totalNeto}%"
            dia.text = "${totalExtras}%"
        }else{
            dia.text = "Sin cosechas durante el día"
            semanal.text = "0.0%"
        }

        recyclerView.adapter = ReporteRendimientoAdapter(ArrayList(arrayListDetails), this)
    }

    private fun calcularRendimiento(arrayListDetails: ArrayList<ListaRendimiento>): Double {
        val cantComunes = arrayListDetails.size
        var deudaEnKilos = 0.0
        for (rendimiento in arrayListDetails) {
            deudaEnKilos += rendimiento.jornadas * rendimiento.comun.estandar
        }
        val particion = deudaEnKilos / cantComunes
        var porcentajeAcum = 0.0
        for (rendimiento in arrayListDetails) {
            if (rendimiento.rendimiento > 100.00) {
                porcentajeAcum += particion
            } else {
                porcentajeAcum += (rendimiento.rendimiento * particion) / 100
            }
        }

        var total = porcentajeAcum * 100 / deudaEnKilos
        total = BigDecimal(total).setScale(1, RoundingMode.HALF_EVEN).toDouble()
        if (total >= 100) {
            dia.setTextColor(Color.parseColor("#00C851"))
        } else {
            dia.setTextColor(Color.parseColor("#FF4746"))
        }
        return total
    }

    private fun calcularExtras(arrayListDetails: ArrayList<ListaRendimiento>): Double {
        val cantComunes = arrayListDetails.size
        var deudaEnKilos = 0.0
        for (rendimiento in arrayListDetails) {
            deudaEnKilos += rendimiento.jornadas * rendimiento.comun.estandar
        }
        val particion = deudaEnKilos / cantComunes
        var porcentajeAcum = 0.0
        for (rendimiento in arrayListDetails) {
            porcentajeAcum += (rendimiento.rendimiento * particion) / 100
        }

        var total = porcentajeAcum * 100 / deudaEnKilos
        total = BigDecimal(total).setScale(1, RoundingMode.HALF_EVEN).toDouble()
        if (total >= 0.0) {
            semanal.setTextColor(Color.parseColor("#00C851"))
        } else {
            semanal.setTextColor(Color.parseColor("#FF4746"))
        }
        return total
    }

    override fun onItemClick(item: ListaRendimiento) {
    }

    override fun onBackPressed() {
        // INTENT FOR YOUR HOME ACTIVITY
        Log.d(
            TAG,
            "Finca seleccionada en ReporteRendimientoActivity.onBack(): ${intent.getStringExtra("idFinca")}"
        )

        val intent = (Intent(this, GenerarReporteRendimientoActivity::class.java)).putExtra(
            "idFinca",
            intent.getStringExtra("idFinca")
        )
        startActivity(intent)
    }

    //Cuando se cambia de activity esto pasa por debajo (en resumen)
    override fun onResume() {
        super.onResume()
        offline.isVisible = !conectividad.connectedTo(context)
    }

    fun initFirebase() {
        firebaseModel.setupCacheSize()
        firebaseModel.enableNetwork()

    }
}