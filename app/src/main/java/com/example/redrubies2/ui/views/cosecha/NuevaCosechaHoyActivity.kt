package com.example.redrubies2.ui.views.cosecha

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.redrubies2.R
import com.example.redrubies2.businesslayer.libraries.Conectividad
import com.example.redrubies2.businesslayer.libraries.Resquest
import com.example.redrubies2.businesslayer.repository.FireBaseModel
import com.example.redrubies2.datalayer.*
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.MetadataChanges
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_nueva_cosecha_hoy.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.*
import kotlin.collections.ArrayList


@RequiresApi(Build.VERSION_CODES.O)
class NuevaCosechaHoyActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    //Verificar conexion a internet
    val conectividad: Conectividad = Conectividad()
    private val TAG = "NuevaCosechaHoyActivity"
    val firebaseModel: FireBaseModel = FireBaseModel()

    private val request: Resquest = Resquest()
    var arrayListDetailsComunes: ArrayList<Comun> = ArrayList()
    var arrayListDetailsTrabajadores: ArrayList<Trabajador> = ArrayList()
    var arrayListDetailsInvernaderos: ArrayList<Invernadero> = ArrayList()
    var arrayListDetailsVariedades: ArrayList<Variedades> = ArrayList()
    var context: Context = this

    /**
     * Esta funcion está encargada de inicializar los repositorios y traer los datos necesarios
     * para llenar los selects (spinners)
     */

    var sidComun: String = ""
    var sidVariedad: String = ""
    var sidTrabajador: String = ""
    var sidInvernadero: String = ""
    var nTrabajador: String = ""
    var nComun: String = ""
    val myCalendar: Calendar = Calendar.getInstance()
    private lateinit var str : String
    private var htr = ""

    fun initFirebase() {
        firebaseModel.setupCacheSize()
        firebaseModel.enableNetwork()
    }

    private fun initSpinners() {
        loadComunes(true)
        loadTrabajadores(true)
        loadInvernaderos(true)
    }

    private fun initSpinnersFromFirestore() {
        //Traer datos de firebase (o de cache)
        initSpinnerComunFromFirestore()
        initSpinnerTrabajadorFromFirestore()
        initSpinnerInvernaderoFromFirestore()

    }

    private fun initSpinnerTrabajadorFromFirestore() {
        firebaseModel.db.collection("Trabajadores")
            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen error", e)
                    return@addSnapshotListener
                }

                for (change in querySnapshot!!.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        Log.d(TAG, "Trabajador: ${change.document.data}")
                    }

                    if (querySnapshot.metadata.isFromCache) {
                        Log.d(
                            TAG,
                            "Información obtenida del cache en la vista NuevaCosechaHoyActivity"
                        )
                        val trabajador = Trabajador()
                        trabajador.primerNombre = change.document["PrimerNombre"].toString()
                        trabajador.primerApellido = change.document["PrimerApellido"].toString()
                        trabajador.codigo = change.document.id
                        arrayListDetailsTrabajadores.add(trabajador)
                        arrayListDetailsTrabajadores.distinct()
                    } else {
                        Log.d(
                            TAG,
                            "Información obtenida del servidor en la vista NuevaCosechaHoyActivity"
                        )
                        val trabajador = Trabajador()
                        trabajador.primerNombre = change.document["PrimerNombre"].toString()
                        trabajador.primerApellido = change.document["PrimerApellido"].toString()
                        trabajador.codigo = change.document.id
                        arrayListDetailsTrabajadores.add(trabajador)
                        arrayListDetailsTrabajadores.distinct()
                    }
                }
                spinnerTrabajador.adapter = ArrayAdapter(
                    context, android.R.layout.simple_spinner_item, generarValoresStrings(
                        arrayListDetailsTrabajadores,
                        "Trabajador"
                    )
                )
            }
    }

    private fun initSpinnerComunFromFirestore() {
        firebaseModel.db.collection("Comunes")
            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen error", e)
                    return@addSnapshotListener
                }

                for (change in querySnapshot!!.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        Log.d(TAG, "Comun: ${change.document.data}")
                    }

                    if (querySnapshot.metadata.isFromCache) {
                        Log.d(
                            TAG,
                            "Información obtenida del cache en la vista NuevaCosechaHoyActivity"
                        )
                        val comun = Comun()
                        comun.nombre = change.document["Nombre"].toString()
                        comun.id = change.document.id
                        arrayListDetailsComunes.add(comun)
                        arrayListDetailsComunes.distinct()
                    } else {
                        Log.d(
                            TAG,
                            "Información obtenida del servidor en la vista NuevaCosechaHoyActivity"
                        )
                        val comun = Comun()
                        comun.nombre = change.document["Nombre"].toString()
                        comun.id = change.document.id
                        arrayListDetailsComunes.add(comun)
                        arrayListDetailsComunes.distinct()
                    }
                }
                spinnerComun.adapter = ArrayAdapter(
                    context, android.R.layout.simple_spinner_item, generarValoresStrings(
                        arrayListDetailsComunes,
                        "Comun"
                    )
                )
            }
    }

    private fun initSpinnerInvernaderoFromFirestore() {
        firebaseModel.db.collection("Invernaderos")
            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen error", e)
                    return@addSnapshotListener
                }

                for (change in querySnapshot!!.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        Log.d(TAG, "Invernadero: ${change.document.data}")
                    }

                    if (querySnapshot.metadata.isFromCache) {
                        Log.d(
                            TAG,
                            "Información obtenida del cache en la vista NuevaCosechaHoyActivity"
                        )
                        val invernadero = Invernadero()
                        invernadero.descripcion = change.document["Descripcion"].toString()
                        invernadero.fincaId = change.document["FincaId"].toString()
                        invernadero.id = change.document.id
                        arrayListDetailsInvernaderos.add(invernadero)
                        arrayListDetailsInvernaderos.distinct()
                    } else {
                        val invernadero = Invernadero()
                        invernadero.descripcion = change.document["Descripcion"].toString()
                        invernadero.fincaId = change.document["FincaId"].toString()
                        invernadero.id = change.document.id
                        arrayListDetailsInvernaderos.add(invernadero)
                        arrayListDetailsInvernaderos.distinct()
                    }
                }
                spinnerInvernadero.adapter = ArrayAdapter(
                    context, android.R.layout.simple_spinner_item, generarValoresStrings(
                        arrayListDetailsInvernaderos,
                        "Invernadero"
                    )
                )
            }
    }

    private fun loadSQLDataToFirestore() {
        loadComunes(false)
        loadTrabajadores(false)
        loadInvernaderos(false)
    }

    private fun loadComunes(bandera: Boolean) {
        request.client.newCall(request.getRequestEndPoint("Comun")).
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val str_response = response.body()!!.string()
                //creating json object
                val json_contact: JSONObject = JSONObject(str_response)
                //creating json array
                val jsonarrayInfo: JSONArray = json_contact.getJSONArray("Data")
                val size: Int = jsonarrayInfo.length()
                arrayListDetailsComunes = ArrayList();
                for (i in 0 until size) {
                    val jsonObjectdetail: JSONObject = jsonarrayInfo.getJSONObject(i)
                    val model: Comun = Comun();
                    model.nombre = jsonObjectdetail.getString("Nombre")
                    model.id = jsonObjectdetail.getString("Id")
                    arrayListDetailsComunes.add(model)

                }
                runOnUiThread {
                    if (bandera) {
                        spinnerComun.adapter = ArrayAdapter(
                            context, android.R.layout.simple_spinner_item, generarValoresStrings(
                                arrayListDetailsComunes,
                                "Comun"
                            )
                        )
                    } else {
                        firebaseModel.addDocumentComunes(arrayListDetailsComunes)
                    }
                }
            }
        })
    }

    fun generarValoresStrings(array: ArrayList<*>, objeto: String): ArrayList<String> {
        val valores: ArrayList<String> = ArrayList(array.size)

        for (obj in array) {
            when (objeto) {
                "Comun" -> valores.add((obj as Comun).nombre)
                "Trabajador" -> valores.add("${(obj as Trabajador).primerNombre} ${obj.primerApellido}")
                "Invernadero" -> valores.add((obj as Invernadero).descripcion)
                "Variedad" -> valores.add((obj as Variedades).nombre)
            }
        }
        return valores
    }

    private fun loadTrabajadores(bandera: Boolean) {
        request.client.newCall(request.getRequestEndPoint("Trabajadores"))
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {}
                override fun onResponse(call: Call, response: Response) {
                    val str_response = response.body()!!.string()
                    //creating json object
                    val json_contact: JSONObject = JSONObject(str_response)
                    //creating json array
                    val jsonarrayInfo: JSONArray = json_contact.getJSONArray("Data")
                    val size: Int = jsonarrayInfo.length()
                    arrayListDetailsTrabajadores = ArrayList();
                    for (i in 0 until size) {
                        val jsonObjectdetail: JSONObject = jsonarrayInfo.getJSONObject(i)
                        val model: Trabajador = Trabajador();
                        model.primerNombre = jsonObjectdetail.getString("PrimerNombre")
                        model.primerApellido = jsonObjectdetail.getString("PrimerApellido")
                        model.codigo = jsonObjectdetail.getString("Codigo")
                        arrayListDetailsTrabajadores.add(model)
                    }
                    runOnUiThread {
                        if (bandera) {
                            spinnerTrabajador.adapter = ArrayAdapter(
                                context,
                                android.R.layout.simple_spinner_item,
                                generarValoresStrings(
                                    arrayListDetailsTrabajadores,
                                    "Trabajador"
                                )
                            )
                        } else {
                            firebaseModel.addDocumentTrabajadores(arrayListDetailsTrabajadores)
                        }
                    }
                }
            })
    }

    private fun loadInvernaderos(bandera: Boolean) {
        request.client.newCall(request.getRequestEndPoint("Invernadero"))
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {}
                override fun onResponse(call: Call, response: Response) {
                    val str_response = response.body()!!.string()
                    //creating json object
                    val json_contact: JSONObject = JSONObject(str_response)
                    //creating json array
                    val jsonarrayInfo: JSONArray = json_contact.getJSONArray("Data")
                    val size: Int = jsonarrayInfo.length()
                    arrayListDetailsInvernaderos = ArrayList();
                    for (i in 0 until size) {
                        val jsonObjectdetail: JSONObject = jsonarrayInfo.getJSONObject(i)
                        val model: Invernadero = Invernadero();
                        model.descripcion = jsonObjectdetail.getString("Descripcion")
                        model.id = jsonObjectdetail.getString("Id")
                        model.codigo = jsonObjectdetail.getString("CodigoInvernadero")
                        model.fincaId = jsonObjectdetail.getString("FincaId")
                        val valor = intent.getStringExtra("idFinca")
                        if (model.fincaId == valor)
                            arrayListDetailsInvernaderos.add(model)
                    }
                    runOnUiThread {
                        if (bandera) {
                            //Se añade esta validacion porque cuando se clickea el spinner vacio, la activity crashea
                            if (arrayListDetailsInvernaderos.size == 0) {
                                spinnerInvernadero.isEnabled = false
                                sidInvernadero = ""

                            } else {
                                spinnerInvernadero.adapter = ArrayAdapter(
                                    context,
                                    android.R.layout.simple_spinner_item,
                                    generarValoresStrings(
                                        arrayListDetailsInvernaderos,
                                        "Invernadero"
                                    )
                                )
                                spinnerInvernadero.isEnabled = true
                            }
                        } else {
                            firebaseModel.addDocumentInvernadero(arrayListDetailsInvernaderos)
                        }
                        ////////////////
                    }
                }
            })
    }


    //El spinner de variedades se inicializa por aparte ya que depende del id de los comunes
    private fun loadVariedades(idComun: String) {
        request.client.newCall(request.getRequestEndPoint("Variedad?comun=$idComun"))
            .enqueue(object :
                Callback {
                override fun onFailure(call: Call, e: IOException) {}
                override fun onResponse(call: Call, response: Response) {
                    val str_response = response.body()!!.string()
                    //creating json object
                    val json_contact: JSONObject = JSONObject(str_response)
                    //creating json array
                    val jsonarrayInfo: JSONArray = json_contact.getJSONArray("Data")
                    val size: Int = jsonarrayInfo.length()
                    arrayListDetailsVariedades = ArrayList();
                    for (i in 0 until size) {
                        val jsonObjectdetail: JSONObject = jsonarrayInfo.getJSONObject(i)
                        val model: Variedades = Variedades();
                        model.nombre = jsonObjectdetail.getString("Nombre")
                        model.id = jsonObjectdetail.getString("Id")
                        model.comunId = jsonObjectdetail.getString("ComunId")
                        arrayListDetailsVariedades.add(model)
                    }
                    runOnUiThread {
                        //Se añade esta validacion porque cuando se clickea el spinner vacio, la activity crashea
                        if (arrayListDetailsVariedades.size == 0) {
                            spinnerVariedad.adapter = ArrayAdapter(
                                context,
                                android.R.layout.simple_spinner_item,
                                generarValoresStrings(
                                    arrayListDetailsVariedades,
                                    "Variedad"
                                )
                            )
                            spinnerVariedad.isEnabled = false
                            sidVariedad = ""
                        } else {
                            spinnerVariedad.adapter = ArrayAdapter(
                                context,
                                android.R.layout.simple_spinner_item,
                                generarValoresStrings(
                                    arrayListDetailsVariedades,
                                    "Variedad"
                                )
                            )
                            spinnerVariedad.isEnabled = true
                        }
                    }
                }
            })
    }

    private fun onClickVariedad() {
        spinnerVariedad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sidVariedad = arrayListDetailsVariedades[position].id

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
    }

    private fun onClickTrabajador() {
        spinnerTrabajador.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sidTrabajador = arrayListDetailsTrabajadores[position].codigo
                nTrabajador = arrayListDetailsTrabajadores[position].primerNombre

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun onClickInvernadero() {
        spinnerInvernadero.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sidInvernadero = arrayListDetailsInvernaderos[position].id

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
    }

    /*
    * Esta función se encarga de validar los campos y de aprobar el guardado
    */
    private fun guardarDatosSQL() {

        val thread = Thread {
            try {
                val inventario = Inventario()
                inventario.cantidad = editTextCantidad.text.toString()
                inventario.idComun = sidComun
                inventario.idInvernadero = sidInvernadero
                inventario.codigoTrabajador = sidTrabajador
                inventario.idVariedad = sidVariedad
                inventario.dateTime = "$str $htr:00.0"
                request.postRequestEndPoint(inventario, "api/Cosecha")
            } catch (e: Exception) {
                println(e.message)
            }
        }

        thread.start()
    }

    private fun initSpinnerVariedadesFromFirestore(bandera: Boolean) {
        if(bandera)
            arrayListDetailsVariedades.clear()
        firebaseModel.db.collection("Variedades").whereEqualTo("ComunId", sidComun)
            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen error", e)
                    return@addSnapshotListener
                }
                for (change in querySnapshot!!.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        Log.d(TAG, "Variedadd: ${change.document.data}")
                    }

                    if (querySnapshot.metadata.isFromCache) {
                        Log.d(
                            TAG,
                            "Información obtenida del cache en la vista NuevaCosechaHoyActivity"
                        )
                        val variedades = Variedades()
                        variedades.nombre = change.document["Nombre"].toString()
                        variedades.id = change.document.id
                        variedades.comunId = change.document["ComunId"].toString()
                        arrayListDetailsVariedades.add(variedades)
                        arrayListDetailsVariedades.distinct()
                    } else {
                        Log.d(
                            TAG,
                            "Información obtenida del servidor en la vista NuevaCosechaHoyActivity"
                        )
                        val variedades = Variedades()
                        variedades.nombre = change.document["Nombre"].toString()
                        variedades.id = change.document.id
                        variedades.comunId = change.document["ComunId"].toString()
                        arrayListDetailsVariedades.add(variedades)
                        arrayListDetailsVariedades.distinct()
                    }
                }
                if (bandera) {
                    //Se añade esta validacion porque cuando se clickea el spinner vacio, la activity crashea
                    if (arrayListDetailsVariedades.size == 0) {
                        spinnerVariedad.adapter = ArrayAdapter(
                            context,
                            android.R.layout.simple_spinner_item,
                            generarValoresStrings(
                                arrayListDetailsVariedades,
                                "Variedad"
                            )
                        )
                        spinnerVariedad.isEnabled = false
                        sidVariedad = ""
                    } else {
                        spinnerVariedad.adapter = ArrayAdapter(
                            context,
                            android.R.layout.simple_spinner_item,
                            generarValoresStrings(
                                arrayListDetailsVariedades,
                                "Variedad"
                            )
                        )
                        spinnerVariedad.isEnabled = true
                    }
                }
            }
    }

    /**<
     * Las siguientes funciones onClick están pendientes de escuchar cuando se presione un select
     * para reaccionar a estos
     * */
    private fun onClickComun() {

        spinnerComun.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sidComun = arrayListDetailsComunes[position].id
                nComun = arrayListDetailsComunes[position].nombre
                if (conectividad.connectedTo(context)) {
                    //Actualiza el spinner desde SQL
                    loadVariedades(sidComun)
                    //Actualiza la info de SQL hacía firebase
                    updateSQLToFirestore(sidComun)
                    initSpinnerVariedadesFromFirestore(false)
                } else {
                    //Actualiza el spinner desde el firebase (cache)
                    initSpinnerVariedadesFromFirestore(true)
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }


    }

    private fun updateSQLToFirestore(id: String) {
        var arrayListDetailsVariedadesSQL: ArrayList<Variedades>
        request.client.newCall(request.getRequestEndPoint("Variedad?comun=$id"))
            .enqueue(object :
                Callback {
                override fun onFailure(call: Call, e: IOException) {}
                override fun onResponse(call: Call, response: Response) {
                    val str_response = response.body()!!.string()
                    //creating json object
                    val json_contact: JSONObject = JSONObject(str_response)
                    //creating json array
                    val jsonarrayInfo: JSONArray = json_contact.getJSONArray("Data")
                    val size: Int = jsonarrayInfo.length()
                    arrayListDetailsVariedadesSQL = ArrayList();
                    for (i in 0 until size) {
                        val jsonObjectdetail: JSONObject = jsonarrayInfo.getJSONObject(i)
                        val model = Variedades();
                        model.nombre = jsonObjectdetail.getString("Nombre")
                        model.id = jsonObjectdetail.getString("Id")
                        model.comunId = jsonObjectdetail.getString("ComunId")
                        arrayListDetailsVariedadesSQL.add(model)
                    }
                    runOnUiThread {
                        firebaseModel.addDocumentVariedades(arrayListDetailsVariedadesSQL)
                    }
                }
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nueva_cosecha_hoy)
        AndroidThreeTen.init(application);
        str = LocalDate.now(ZoneId.of("America/Tortola")).toString()
        initFirebase()
        val dateTimeActual = LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
        val fecha = dateTimeActual.format(formatter)
        textFecha.text = "Fecha: $fecha"

        setearHora("6", "0")
        if (conectividad.connectedTo(context)) {
            initSpinners()
            loadSQLDataToFirestore()
        } else {
            initSpinnersFromFirestore()
        }

        onClickComun()
        onClickVariedad()
        onClickTrabajador()
        onClickInvernadero()
        onClickFecha()
        onClickHora()
        onClickGuardar()
    }

    private fun setearHora(hora: String, minutos: String) {
        var h = hora
        var m = minutos
        if (hora.toInt() < 10)
            h = "0${hora}"
        if (minutos.toInt() < 10)
            m = "0${minutos}"
        textHora.text = "Hora: $h:$m"
        htr = "$h:$m"
    }

    private fun onClickFecha() {
        textFecha.setOnClickListener {
            val dialog = DatePickerDialog(
                this, R.style.DatePickerDialogTheme, this, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            )
            dialog.show()
        }
    }

    //cuando se ingresa horas fuera de la jornada, no lo deja, pero ... no se muestra un mensaje retroalimentativo tampoco
    @SuppressLint("ShowToast")
    private fun onClickHora() {
        var b = false
        textHora.setOnClickListener {
            val dialog = TimePickerDialog(this, R.style.DatePickerDialogTheme,
                { _, sHour, sMinute
                    ->
                    run {
                        if(sHour < 6 || (sHour == 18 && sMinute > 0) || sHour > 18){
                            b = true
                            setearHora("6", "0")
                        }else{
                            setearHora(sHour.toString(), sMinute.toString())
                        }
                    }
                },
                myCalendar.get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE),true)
            dialog.show()
        }
        if(b){
            runOnUiThread {
                Toast.makeText(context, "La hora debe estar entre las 6:00 y las 18:00", Toast.LENGTH_SHORT)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        var smonth = ""
        if ((month + 1) < 10) {
            smonth = "0${month + 1}"
        } else {
            smonth = (month + 1).toString()
        }
        var sday = ""
        if ((dayOfMonth) < 10) {
            sday = "0${dayOfMonth}"
        } else {
            sday = dayOfMonth.toString()
        }

        str = "$year-$smonth-${sday}"
        val dateTimeActual = LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
        val fecha = dateTimeActual.format(formatter)
        textFecha.text = "Fecha: $fecha"
    }

    private fun onClickGuardar() {
        guardar.setOnClickListener {
            try {
                val cantidad = editTextCantidad.text.toString().toDouble()
                if (cantidad <= 0 || cantidad > 999){
                    Toast.makeText(
                        applicationContext,
                        "La cantidad en kilos debe ser mayor a 0 y menor a 999",
                        Toast.LENGTH_SHORT
                    ).show()
                }else{
                    guardarDatos()
                }
            } catch (nfe: NumberFormatException) {
                Toast.makeText(
                    applicationContext,
                    "Debe ingresar un valor válido",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun guardarDatos() {
        if (sidComun.equals("") || sidInvernadero.equals("") || sidTrabajador.equals("") || sidVariedad.equals(
                ""
            )) {
            Toast.makeText(
                applicationContext,
                "Uno o más datos están vacíos, verifique los campos",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            if (conectividad.connectedTo(context)) {
                guardarDatosSQL()
                Toast.makeText(
                    applicationContext,
                    "Datos guardados correctamente",
                    Toast.LENGTH_LONG
                ).show()
                val intent = (Intent(this, ListaInventarioActivity::class.java)).putExtra(
                    "idFinca", intent.getStringExtra(
                        "idFinca"
                    )
                )
                startActivity(intent)
            } else {
                guardarDatosFirestore()
                Toast.makeText(
                    applicationContext,
                    "Guardado en rezagos, cuando exista conexión al servidor deberá cargarlos",
                    Toast.LENGTH_LONG
                ).show()
                val intent = (Intent(this, RezagosCosechaDiaActivity::class.java)).putExtra(
                    "idFinca", intent.getStringExtra("idFinca")
                )
                startActivity(intent)
            }

        }
    }

    private fun guardarDatosFirestore() {
        val inventario = ListaInventario()
        inventario.cantidad = (editTextCantidad.text.toString())
        inventario.comun = sidComun
        inventario.invernadero = (sidInvernadero)
        inventario.trabajador = sidTrabajador
        inventario.variedad = (sidVariedad)
        inventario.subido = "PENDIENTE"
        inventario.ncomun = nComun
        inventario.ntrabajador = nTrabajador
        inventario.fecha = "$str $htr:00.0"
        firebaseModel.addDocumentInventarioRezagos(inventario)
    }

    override fun onResume() {
        super.onResume()
        offline.isVisible = !conectividad.connectedTo(context)
    }
}