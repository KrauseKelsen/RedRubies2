package com.example.redrubies2.ui.views.reporte

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.redrubies2.R
import com.example.redrubies2.businesslayer.libraries.Conectividad
import com.example.redrubies2.businesslayer.libraries.PDF.Common
import com.example.redrubies2.businesslayer.libraries.PDF.Designer
import com.example.redrubies2.businesslayer.libraries.Resquest
import com.example.redrubies2.businesslayer.repository.FireBaseModel
import com.example.redrubies2.datalayer.ListaInventario
import com.example.redrubies2.ui.adapters.ListaInventarioAdapter
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.MetadataChanges
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
import com.jakewharton.threetenabp.AndroidThreeTen
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_reporte_produccion.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.collections.ArrayList


@RequiresApi(Build.VERSION_CODES.O)
class ReporteProduccionActivity : AppCompatActivity(), ListaInventarioAdapter.Listener {
    val conectividad: Conectividad = Conectividad()
    private val TAG = "ReporteProduccionActivity"
    private lateinit var formatter: DateTimeFormatter

    val context: Context = this

    val firebaseModel: FireBaseModel = FireBaseModel()
    private val request: Resquest = Resquest()
    var arrayListDetailsSQL: ArrayList<ListaInventario> = ArrayList();
    var inventarioDiario: ArrayList<ListaInventario> = ArrayList();
    var inventarioSemanal: ArrayList<ListaInventario> = ArrayList();
    var sum: Double = 0.0
    var sumSemana: Double = 0.0
    var estimacionDiaria = 0.0
    var estimacionSemana = 0.0
    var lunes = 0.0
    var martes = 0.0
    var miercoles = 0.0
    var jueves = 0.0
    var viernes = 0.0
    var sabado = 0.0
    var designer = Designer()
    var contador = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporte_produccion)
        AndroidThreeTen.init(application);
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        initFirebase()
        if (conectividad.connectedTo(context))
            loadSQLDataToFirestore()
        getInventariosFromFirestore()
        generarPDF.setOnClickListener {
            Dexter.withActivity(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        designer = Designer()
                        designer.setFileName("ReporteProduccion_${Random().nextInt(10000*10000)}.pdf")
                        if (sumSemana == 0.0){
                            Toast.makeText(
                                this@ReporteProduccionActivity,
                                "No se pudo generar el reporte por que no hay cosechas durante la semana",
                                Toast.LENGTH_LONG
                            ).show()
                        }else{
                            crearPDFFile(Common.getAppPath(this@ReporteProduccionActivity) + designer.getFileName())
                        }
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        Toast.makeText(
                            this@ReporteProduccionActivity,
                            "Permiso denegado, no se podrán generar reportes",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken?
                    ) {
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
            designer.crearTitulo(18.0F, "Reporte de Produccion")

            val diasDeCosecha = intent.getStringArrayExtra("diasDeCosecha") // array con dias seleccionados

            val primerDia = diasDeCosecha.first()
            val ultimoDia = diasDeCosecha.last()

            val date1 = LocalDate.parse(primerDia, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val date2 = LocalDate.parse(ultimoDia, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            val dateFormmat1 = date1.format(formatter)
            val dateFormmat2 = date2.format(formatter)

            val contenido = ArrayList<String>()
            contenido.add(variedad.text.toString())
            contenido.add(invernadero.text.toString())
            contenido.add("$dateFormmat1 al $dateFormmat2")
            generarEncabezado(contenido)

            for (dia in diasDeCosecha){ //Lunes (hay una cosecha)
                var totalDia = 0.0
                for (cosecha in inventarioSemanal){ // hay 4 cosechas pero debe pasar solo por las de lunes
                    if(cosecha.fecha == dia){ // la cosecha lunes coincide
                        generarTabla(cosecha) // asi que se agrega a la tabla
                        totalDia += cosecha.cantidad.toDouble() // y se suma al total diario del lunes
                    }
                }//se terminaron de listar las 4 y se setearon solo las necesarias por lo tanto ya se puede poner el total del diario recogido
                if (totalDia != 0.0){
                    designer.crearGrupo("Total del dia:", "${BigDecimal(totalDia).setScale(2,RoundingMode.HALF_EVEN).toDouble()} kg de $estimacionDiaria kg estimados", !calcularEstandarColor(totalDia, estimacionDiaria))
                    designer.espacio()
                }
            }

            designer.crearTitulo("Total de la semana: ${BigDecimal(sumSemana).setScale(2,RoundingMode.HALF_EVEN).toDouble()} kg de $estimacionSemana kg estimados", !calcularEstandarColor(sumSemana, estimacionSemana))

            designer.getDocument().close()

            Toast.makeText(this, "Ha sido guardado en $path", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun generarEncabezado(contenido : ArrayList<String>) {
        designer.crearEncabezado("Variedad:", contenido[0])
        designer.crearEncabezado("Invernadero:", contenido[1])
        designer.crearEncabezado("Semana:", contenido[2])
        designer.espacio()
    }

    private fun generarTabla(cosecha: ListaInventario) {
        //Cosechass
        designer.crearGrupo("Trabajador:", cosecha.trabajador, "Cantidad: ", cosecha.cantidad)
        designer.crearGrupo("Cosecha:", cosecha.comun, "Dia: ", cosecha.ntrabajador)
        designer.espacio()
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

    override fun onBackPressed() {
        // INTENT FOR YOUR HOME ACTIVITY
        Log.d(
            TAG, "Finca seleccionada en ReporteProduccionActivity.onBack(): ${
                intent.getStringExtra(
                    "idFinca"
                )
            }"
        )

        val intent = (Intent(this, GenerarReporteProduccionActivity::class.java)).putExtra(
            "idFinca", intent.getStringExtra(
                "idFinca"
            )
        )
        startActivity(intent)
    }


    private fun getInventariosFromFirestore() {
        firebaseModel.db.collection("InventarioCosechaDia")
            .whereEqualTo("Comun", intent.getStringExtra("comun"))
            .whereEqualTo("Variedad", intent.getStringExtra("variedad"))
            .whereEqualTo("Invernadero", intent.getStringExtra("invernadero"))
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
                                "Información obtenida del cache en la vista ReporteProduccionActivity"
                            )
                        } else {
                            Log.d(
                                TAG,
                                "Información obtenida del servidor en la vista ReporteProduccionActivity"
                            )
                        }
                        buildListaInventario(change)
                        //imprime lo que obtuvo
                        Log.d(TAG, "Se obtuvo: ${change.document.id} => ${change.document.data}")
                    }
                }


                //aquí se llega cuando ya el recorrido ha finalizado exitosamente entonces está listo para seterarlo
                for (cosecha in inventarioSemanal){
                    // Se llena el arraylist para la lista de hoy
                    if(LocalDate.parse(cosecha.fecha, formatter) == LocalDate.parse(intent.getStringExtra("fecha"), formatter)){
                        //aprovecha y empieza a sumar las cantidades de hoy para el rendimiento diario
                        sum += BigDecimal(cosecha.cantidad.toDouble()).setScale(2,RoundingMode.HALF_EVEN).toDouble()
                        sum = BigDecimal(sum).setScale(2,RoundingMode.HALF_EVEN).toDouble()
                        //lo añade al arraylist
                        inventarioDiario.add(cosecha)
                    }
                }
                if(contador==0){
                    //reiniciamos la vista por aquello que se actualice en tiempo real
                    recyclerView.layoutManager = LinearLayoutManager(this)
                    setupRecyclerView(inventarioDiario)
                    contador++
                }
            }
    }


    //Arma el inventario de la semana
    private fun buildListaInventario(change: DocumentChange) {
        val inventario = ListaInventario()
        val diasDeCosecha = intent.getStringArrayExtra("diasDeCosecha") // array con dias seleccionados
        // Recorre los días seleccionados en el checkboxs+-+------------------------------
        for(diaDeCosecha in diasDeCosecha) {
            // Verificamos si el día seleccionado es igual a la cosecha para meterlo en la lista
            // y para sumar la cantidad semanal
            val diaDeCosechaActual = LocalDate.parse(diaDeCosecha, formatter)
            if(diaDeCosechaActual == LocalDate.parse(change.document["Fecha"].toString(), formatter)){
                inventario.cantidad = change.document["Cantidad"].toString()
                inventario.id = change.document.id
                inventario.comun = change.document["Comun"].toString()
                inventario.trabajador = change.document["Trabajador"].toString()
                inventario.hora = change.document["Hora"].toString()
                inventario.ntrabajador = traducirDia(LocalDate.parse(change.document["Fecha"].toString(), formatter).dayOfWeek.toString()) // el adapter construye el dia con la variable "hora"
                // asi que me veo obligado a echar la variable hora en ntrabajador para luego meterla en el reporte
                inventario.invernadero = change.document["Invernadero"].toString()
                inventario.fecha = change.document["Fecha"].toString()
                inventario.variedad = change.document["Variedad"].toString()
                sumSemana += BigDecimal(inventario.cantidad.toDouble()).setScale(2,RoundingMode.HALF_EVEN).toDouble()
                sumSemana = BigDecimal(sumSemana).setScale(2,RoundingMode.HALF_EVEN).toDouble()
                inventarioSemanal.add(inventario)
            }
        }
    }

    private fun traducirDia(pdia : String): String {
        return when(pdia){
            "MONDAY" -> "Lunes"
            "TUESDAY" -> "Martes"
            "WEDNESDAY" -> "Miercoles"
            "THURSDAY" -> "Jueves"
            "FRIDAY" -> "Viernes"
            "SATURDAY" -> "Sábado"
            else -> ""
        }
    }

    private fun loadSQLDataToFirestore() {
        request.client.newCall(request.getRequestEndPoint("Inventario")).enqueue(object : Callback {
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
                    val model = ListaInventario();
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

    private fun calcularCantidadesSemanales(): Int {
        var diasContados = 0
        val diasDeCosecha = intent.getStringArrayExtra("diasDeCosecha")
        for (dia in diasDeCosecha){
            if(LocalDate.parse(dia, formatter).dayOfWeek.ordinal < LocalDate.parse(intent.getStringExtra("fecha").toString(), formatter).dayOfWeek.ordinal){
                diasContados += 1
            }else{
                diasContados += 1
                return diasContados
            }
        }
        return diasContados
    }

    @SuppressLint("SetTextI18n")
    private fun setupRecyclerView(arrayListDetails: ArrayList<ListaInventario>) {
        estimacionDiaria = BigDecimal(intent.getStringExtra("estimacion").toDouble() / intent.getStringArrayExtra("diasDeCosecha").size).setScale(2,RoundingMode.HALF_EVEN).toDouble()
        estimacionSemana = BigDecimal(estimacionDiaria * calcularCantidadesSemanales()).setScale(2,RoundingMode.HALF_EVEN).toDouble()
        Log.d(TAG, "Totales: $lunes , $martes , $miercoles , $jueves , $viernes , $sabado")
        Log.d(TAG, "Dias trabajados: ${calcularCantidadesSemanales()}")

        variedad.text = intent.getStringExtra("nvariedad").toString()
        invernadero.text = intent.getStringExtra("ninvernadero").toString()

        val str = intent.getStringExtra("fecha").toString()
        val dateTimeActual = LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
        val fechaFormateada = dateTimeActual.format(formatter)
        if (sum != 0.0) {
            dia.text = "${sum} kg de $estimacionDiaria kg"
        } else {
            dia.text = "Sin cosechas el ${traducirDia(dateTimeActual.dayOfWeek.toString())}"
        }

        if (sumSemana != 0.0) {
            semana.text = "$sumSemana kg de $estimacionSemana kg"
        } else {
            semana.text = "Sin cosechas durante la semana"
        }


        if(calcularEstandarColor(sum, estimacionDiaria)){
            dia.setTextColor(Color.parseColor("#FF4746"))
        }else{
            dia.setTextColor(Color.parseColor("#00C851"))
        }

        if(calcularEstandarColor(sumSemana, estimacionSemana)){
            semana.setTextColor(Color.parseColor("#FF4746"))
        }else{
            semana.setTextColor(Color.parseColor("#00C851"))
        }

        labelProduccion.text = fechaFormateada.toString()
        recyclerView.adapter = ListaInventarioAdapter(ArrayList(arrayListDetails), this)

    }

    private fun calcularEstandarColor(total: Double, estimacion: Double) : Boolean = total < estimacion


    override fun onItemClick(item: ListaInventario) {
    }
}