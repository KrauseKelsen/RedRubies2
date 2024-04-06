package com.example.redrubies2.ui.views.despacho

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.redrubies2.R
import com.example.redrubies2.businesslayer.libraries.Conectividad
import com.example.redrubies2.businesslayer.repository.FireBaseModel
import com.example.redrubies2.businesslayer.libraries.Resquest
import com.example.redrubies2.datalayer.Cliente
import com.example.redrubies2.datalayer.Despacho
import com.example.redrubies2.datalayer.Oferta
import com.example.redrubies2.datalayer.Trabajador
import com.example.redrubies2.ui.views.menus.MenuProcesoActivity
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.MetadataChanges
import kotlinx.android.synthetic.main.activity_nuevo_ingreso.*
import kotlinx.android.synthetic.main.activity_nuevo_ingreso.offline
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class NuevoIngresoActivity : AppCompatActivity() {


    //Verificar conexion a internet
    val conectividad: Conectividad = Conectividad()
    private val TAG = "ListaDespachoActivity"

    val firebaseModel: FireBaseModel = FireBaseModel()


    private val request: Resquest = Resquest()
    var arrayListDetailsTrabajadores: ArrayList<Trabajador> = ArrayList()
    var arrayListDetailsClientes: ArrayList<Cliente> = ArrayList()
    var arrayListDetailsOfertas: ArrayList<Oferta> = ArrayList()
    var context: Context = this
    var sidTrabajador: String = ""
    var sidCliente: String = ""
    var sidOfertas: String = ""
    var sidFinca: String = ""
    var nOferta: String = ""
    var nCliente: String = ""
    var nTrabajador: String = ""

    private fun initSpinners() {
        loadTrabajadores(true)
        loadClientes(true)
    }

    override fun onBackPressed() {
        // INTENT FOR YOUR HOME ACTIVITY
        Log.d(
            TAG,
            "Finca seleccionada en MenuProcesoActivity.onBack(): ${intent.getStringExtra("idFinca")}"
        )

        val intent = (Intent(this, MenuProcesoActivity::class.java)).putExtra(
            "idFinca",
            intent.getStringExtra("idFinca")
        )
        startActivity(intent)
    }

    private fun loadTrabajadores(bandera: Boolean) {
        request.client.newCall(request.getRequestEndPoint("Trabajadores")).enqueue(object :
            Callback {
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

                        spinnerTrabajadores.adapter = ArrayAdapter(
                            context, android.R.layout.simple_spinner_item, generarValoresStrings(
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

    private fun loadClientes(bandera: Boolean) {
        request.client.newCall(request.getRequestEndPoint("Cliente")).enqueue(object :
            Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val str_response = response.body()!!.string()
                //creating json object
                val json_contact: JSONObject = JSONObject(str_response)
                //creating json array
                val jsonarrayInfo: JSONArray = json_contact.getJSONArray("Data")
                val size: Int = jsonarrayInfo.length()
                arrayListDetailsClientes = ArrayList();
                for (i in 0 until size) {
                    val jsonObjectdetail: JSONObject = jsonarrayInfo.getJSONObject(i)
                    val model: Cliente = Cliente();
                    model.nombre = jsonObjectdetail.getString("Nombre")
                    model.codigo = jsonObjectdetail.getString("Id")
                    arrayListDetailsClientes.add(model)

                }

                runOnUiThread {
                    if (bandera) {

                        spinnerClientes.adapter = ArrayAdapter(
                            context, android.R.layout.simple_spinner_item, generarValoresStrings(
                                arrayListDetailsClientes,
                                "Cliente"
                            )
                        )
                    } else {
                        firebaseModel.addDocumentClientes(arrayListDetailsClientes)
                    }
                }
            }
        })
    }

    fun generarValoresStrings(array: ArrayList<*>, objeto: String): ArrayList<String> {
        var valores: ArrayList<String> = ArrayList(array.size)

        for (obj in array) {
            when (objeto) {
                "Cliente" -> valores.add((obj as Cliente).nombre)
                "Trabajador" -> valores.add("${(obj as Trabajador).primerNombre} ${obj.primerApellido}")
                "Oferta" -> valores.add((obj as Oferta).descripcion)
            }
        }
        return valores
    }

    private fun onClickTrabajador() {
        spinnerTrabajadores.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

    private fun onClickCliente() {
        spinnerClientes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sidCliente = arrayListDetailsClientes[position].codigo
                nCliente = arrayListDetailsClientes[position].nombre
                if (conectividad.connectedTo(context)) {
                    //Actualiza el spinner desde SQL
                    loadOfertas(sidCliente)
                    //Actualiza la info de SQL hacía firebase
                    updateSQLToFirestore()
                    initSpinnerOfertasFromFirestore(false)
                } else {
                    //Actualiza el spinner desde el firebase (cache)
                    initSpinnerOfertasFromFirestore(true)
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun initSpinnerOfertasFromFirestore(bandera: Boolean) {
        if (bandera)
            arrayListDetailsOfertas.clear()
        firebaseModel.db.collection("Presentaciones").whereEqualTo("IdCliente", sidCliente)
            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen error", e)
                    return@addSnapshotListener
                }
                for (change in querySnapshot!!.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        Log.d(TAG, "Presentacion: ${change.document.data}")
                    }

                    if (querySnapshot.metadata.isFromCache) {
                        Log.d(
                            TAG,
                            "Información obtenida del cache en la vista NuevoIngresoActivity"
                        )
                        val oferta = Oferta()
                        oferta.presentaciones = change.document["Presentaciones"].toString()
                        oferta.descripcion = change.document["Descripcion"].toString()
                        oferta.id = change.document.id.toInt()
                        oferta.idCliente = sidCliente
                        arrayListDetailsOfertas.add(oferta)
                        arrayListDetailsOfertas.distinct()
                    } else {
                        Log.d(
                            TAG,
                            "Información obtenida del servidor en la vista NuevoIngresoActivity"
                        )
                        val oferta = Oferta()
                        oferta.presentaciones = change.document["Presentaciones"].toString()
                        oferta.descripcion = change.document["Descripcion"].toString()
                        oferta.id = change.document.id.toInt()
                        oferta.idCliente = sidCliente
                        arrayListDetailsOfertas.add(oferta)
                        arrayListDetailsOfertas.distinct()
                    }
                }
                if (bandera) {
                    spinnerOfertas.adapter = ArrayAdapter(
                        context, android.R.layout.simple_spinner_item, generarValoresStrings(
                            arrayListDetailsOfertas,
                            "Oferta"
                        )
                    )
                }
            }
    }

    private fun updateSQLToFirestore() {
        firebaseModel.addDocumentOfertas(arrayListDetailsOfertas)
    }

    //El spinner de ofertas                  se inicializa por aparte ya que depende del id de los comunes
    private fun loadOfertas(idCliente: String) {
        request.client.newCall(request.getRequestEndPoint("presentacion?cliente=$idCliente"))
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
                    arrayListDetailsOfertas = ArrayList();
                    for (i in 0 until size) {
                        val jsonObjectdetail: JSONObject = jsonarrayInfo.getJSONObject(i)
                        val model: Oferta = Oferta();
                        model.descripcion = jsonObjectdetail.getString("Descripcion")
                        model.presentaciones = jsonObjectdetail.getString("Presentacion")
                        model.id = jsonObjectdetail.getString("Id").toInt()
                        model.idCliente = idCliente
                        arrayListDetailsOfertas.add(model)
                    }
                    runOnUiThread {
                        //Se añade esta validacion porque cuando se clickea el spinner vacio, la activity crashea
                        if (arrayListDetailsOfertas.size == 0) {
                            spinnerOfertas.adapter = ArrayAdapter(
                                context,
                                android.R.layout.simple_spinner_item,
                                generarValoresStrings(
                                    arrayListDetailsOfertas,
                                    "Oferta"
                                )
                            )
                            spinnerOfertas.isEnabled = false
                            sidOfertas = ""
                        } else {
                            spinnerOfertas.adapter = ArrayAdapter(
                                context,
                                android.R.layout.simple_spinner_item,
                                generarValoresStrings(
                                    arrayListDetailsOfertas,
                                    "Oferta"
                                )
                            )
                            spinnerOfertas.isEnabled = true
                        }
                    }
                }
            })
    }

    private fun onClickOferta() {
        spinnerOfertas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (arrayListDetailsOfertas.size == 0) {
                    Toast.makeText(
                        applicationContext,
                        "Se perdió la conexión, intente de nuevo",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    sidOfertas = arrayListDetailsOfertas[position].id.toString()
                    presentacion.text = arrayListDetailsOfertas[position].presentaciones
                    nOferta = arrayListDetailsOfertas[position].presentaciones

                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
    }

    fun guardarDatosSQL() {
        //Se validan los campos
        val thread = Thread {
            try {
                val despacho: Despacho = Despacho()
                despacho.cantidad = (editTextCantidad2.text.toString()).toInt()
                despacho.empacador = sidTrabajador
                despacho.oferta = sidOfertas
                despacho.pedido = ""
                despacho.finca = intent.getStringExtra("idFinca")
                Log.w(TAG, "Se seteo el ${intent.getStringExtra("idFinca")} para SQL")
                if (sw_completa.isChecked) {
                    despacho.entera = 0
                } else {
                    despacho.entera = 1
                }
                request.postRequestEndPoint(despacho, "Despacho")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        thread.start()
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
                            "Información obtenida del cache en la vista NuevoIngresoActivity"
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
                            "Información obtenida del servidor en la vista NuevoIngresoActivity"
                        )
                        val trabajador = Trabajador()
                        trabajador.primerNombre = change.document["PrimerNombre"].toString()
                        trabajador.primerApellido = change.document["PrimerApellido"].toString()
                        trabajador.codigo = change.document.id
                        arrayListDetailsTrabajadores.add(trabajador)
                        arrayListDetailsTrabajadores.distinct()
                    }
                }
                spinnerTrabajadores.adapter = ArrayAdapter(
                    context, android.R.layout.simple_spinner_item, generarValoresStrings(
                        arrayListDetailsTrabajadores,
                        "Trabajador"
                    )
                )
            }
    }

    private fun initSpinnerClientesFromFirestore() {
        firebaseModel.db.collection("Clientes")
            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen error", e)
                    return@addSnapshotListener
                }

                for (change in querySnapshot!!.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        Log.d(TAG, "Cliente: ${change.document.data}")
                    }

                    if (querySnapshot.metadata.isFromCache) {
                        Log.d(
                            TAG,
                            "Información obtenida del cache en la vista NuevoIngresoActivity"
                        )
                        val cliente = Cliente()
                        cliente.nombre = change.document["Nombre"].toString()
                        cliente.codigo = change.document.id
                        arrayListDetailsClientes.add(cliente)
                        arrayListDetailsClientes.distinct()
                    } else {
                        Log.d(
                            TAG,
                            "Información obtenida del cache en la vista NuevoIngresoActivity"
                        )
                        val cliente = Cliente()
                        cliente.nombre = change.document["Nombre"].toString()
                        cliente.codigo = change.document.id
                        arrayListDetailsClientes.add(cliente)
                        arrayListDetailsClientes.distinct()
                    }
                }
                spinnerClientes.adapter = ArrayAdapter(
                    context, android.R.layout.simple_spinner_item, generarValoresStrings(
                        arrayListDetailsClientes,
                        "Cliente"
                    )
                )
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_ingreso)
        val sidFinca = intent.getStringExtra("idFinca")
        Log.d(TAG, "Finca: ${sidFinca} llega al onCreate de NuevoIngresoActivity")

        initFirebase()
        if (conectividad.connectedTo(context)) {
            initSpinners()
            loadTrabajadores(false)
            loadClientes(false)
        } else {
            initSpinnerTrabajadorFromFirestore()
            initSpinnerClientesFromFirestore()
        }
        onClickTrabajador()
        onClickCliente()
        onClickOferta()
        onClickGuardar()
    }

    private fun onClickGuardar() {
        guardarIngreso.setOnClickListener {
            try {
                val cantidad = editTextCantidad2.text.toString().toDouble()
                if (cantidad <= 0 || cantidad > 999){
                    Toast.makeText(applicationContext,"La cantidad en kilos debe ser mayor a 0 y menor a 999", Toast.LENGTH_SHORT).show()
                }else{
                    guardarDatos()
                }
            } catch (nfe: NumberFormatException) {
                Toast.makeText(applicationContext,"Debe ingresar un valor válido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarDatos() {
        if (sidTrabajador == "" || sidOfertas == "") {
            Toast.makeText(applicationContext,"Uno o más datos están vacíos, verifique los campos",Toast.LENGTH_SHORT).show()
        } else {
            if (conectividad.connectedTo(context)) {
                guardarDatosSQL()
                Toast.makeText(applicationContext,"Datos guardados",Toast.LENGTH_LONG).show()
                val intent = (Intent(this, ListaDespachoActivity::class.java)).putExtra("idFinca",intent.getStringExtra("idFinca"))
                startActivity(intent)
            } else {
                guardarDatosFirestore()
                Toast.makeText(applicationContext,"Guardado en rezagos, cuando exista conexión al servidor deberá cargarlos",Toast.LENGTH_LONG).show()
                val intent = (Intent(this, RezagosDespachoActivity::class.java)).putExtra("idFinca",intent.getStringExtra("idFinca"))
                startActivity(intent)
            }

        }
    }

    private fun guardarDatosFirestore() {
        val despacho = Despacho()
        despacho.cantidad = (editTextCantidad2.text.toString()).toInt()
        despacho.idTrabajador = sidTrabajador
        despacho.empacador = nTrabajador
        despacho.oferta = sidOfertas
        despacho.pedido = ""
        despacho.finca = intent.getStringExtra("idFinca")
        Log.w(TAG, "Se seteo el ${intent.getStringExtra("idFinca")} para Firestore")
        despacho.presentacion = nOferta
        despacho.subido = "PENDIENTE"
        despacho.fecha = SimpleDateFormat("yyyy-MM-dd").format(Date())
        if (sw_completa.isChecked) {
            despacho.entera = 0
        } else {
            despacho.entera = 1
        }

        firebaseModel.addDocumentDespachoRezagos(despacho)
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