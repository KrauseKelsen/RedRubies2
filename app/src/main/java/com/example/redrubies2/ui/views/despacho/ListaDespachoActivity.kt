package com.example.redrubies2.ui.views.despacho

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.redrubies2.R
import com.example.redrubies2.businesslayer.libraries.Conectividad
import com.example.redrubies2.businesslayer.repository.FireBaseModel
import com.example.redrubies2.businesslayer.libraries.Resquest
import com.example.redrubies2.datalayer.Despacho
import com.example.redrubies2.datalayer.Trabajador
import com.example.redrubies2.ui.adapters.ListaDespachoAdapter
import com.example.redrubies2.ui.views.menus.MenuProcesoActivity
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.MetadataChanges
import kotlinx.android.synthetic.main.activity_lista_ingreso.*
import kotlinx.android.synthetic.main.activity_lista_ingreso.offline
import kotlinx.android.synthetic.main.activity_lista_ingreso.recyclerView
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ListaDespachoActivity : AppCompatActivity(), ListaDespachoAdapter.Listener {
    //Verificar conexion a internet
    val conectividad: Conectividad = Conectividad()
    private val TAG = "ListaDespachoActivity"

    val firebaseModel: FireBaseModel = FireBaseModel()
    private val request: Resquest = Resquest()
    var arrayListDetailsTrabajadoresSQL: ArrayList<Trabajador> = ArrayList();
    var arrayListDetailsTrabajadoresFB: ArrayList<Trabajador> = ArrayList();
    var arrayListDetailsDespachoFB: ArrayList<Despacho> = ArrayList();
    var arrayListDetailsDespachoSQL: ArrayList<Despacho> = ArrayList();
    var context: Context = this
    var sidTrabajador: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_ingreso)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        initFirebase()
        if (conectividad.connectedTo(context))
            loadSQLDataToFirestore()
        getEmpacadoresFromFirestore()
        onClickTrabajador()
    }

    private fun getEmpacadoresFromFirestore() {
        arrayListDetailsTrabajadoresFB = ArrayList()
        firebaseModel.db.collection("Trabajadores")
            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen error", e)
                    return@addSnapshotListener
                }

                for (change in querySnapshot!!.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        if (querySnapshot.metadata.isFromCache) {
                            Log.d(TAG, "Información obtenida del cache en la vista ListaDespacho")
                        } else {
                            Log.d(TAG, "Información obtenida del servidor en la vista ListaDespacho")
                        }
                        val trabajador = Trabajador()
                        trabajador.primerNombre = change.document["PrimerNombre"].toString()
                        trabajador.primerApellido = change.document["PrimerApellido"].toString()
                        trabajador.codigo = change.document.id
                        arrayListDetailsTrabajadoresFB.add(trabajador)
                        Log.d(TAG, "Se obtuvo: ${change.document.id} => ${change.document.data}")
                    }
                }
                //reiniciamos la vista por aquello que se actualice en tiempo real
                recyclerView.layoutManager = LinearLayoutManager(this)
                spinnerTrabajador.adapter = ArrayAdapter(
                    context, android.R.layout.simple_spinner_item, generarValoresStrings(
                        arrayListDetailsTrabajadoresFB,
                        "Trabajador"
                    )
                )
            }
    }

    private fun loadSQLDataToFirestore() {
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
                arrayListDetailsTrabajadoresSQL = ArrayList();
                for (i in 0 until size) {
                    val jsonObjectdetail: JSONObject = jsonarrayInfo.getJSONObject(i)
                    val model: Trabajador = Trabajador();
                    model.primerNombre = jsonObjectdetail.getString("PrimerNombre")
                    model.primerApellido = jsonObjectdetail.getString("PrimerApellido")
                    model.codigo = jsonObjectdetail.getString("Codigo")
                    arrayListDetailsTrabajadoresSQL.add(model)


                }
                runOnUiThread {
                    firebaseModel.addDocumentTrabajadores(arrayListDetailsTrabajadoresSQL)
                }

            }
        })
    }

    override fun onBackPressed() {
        // INTENT FOR YOUR HOME ACTIVITY
        Log.d(
            TAG,
            "Finca seleccionada en ListaDespachoActivity.onBack ${intent.getStringExtra("idFinca")}"
        )
        val intent = (Intent(this, MenuProcesoActivity::class.java)).putExtra(
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

    private fun getEmpacadoresFromSQL() {
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
                arrayListDetailsTrabajadoresFB = ArrayList();
                for (i in 0 until size) {
                    val jsonObjectdetail: JSONObject = jsonarrayInfo.getJSONObject(i)
                    val model: Trabajador = Trabajador();
                    model.primerNombre = jsonObjectdetail.getString("PrimerNombre")
                    model.codigo = jsonObjectdetail.getString("Codigo")
                    model.primerApellido = jsonObjectdetail.getString("PrimerApellido")
                    arrayListDetailsTrabajadoresFB.add(model)


                }
                runOnUiThread {
                    spinnerTrabajador.adapter = ArrayAdapter(
                        context, android.R.layout.simple_spinner_item, generarValoresStrings(
                            arrayListDetailsTrabajadoresFB,
                            "Trabajador"
                        )
                    )
                }
            }
        })
    }

    fun generarValoresStrings(array: ArrayList<*>, objeto: String): ArrayList<String> {
        val valores: ArrayList<String> = ArrayList(array.size)

        for (obj in array) {
            when (objeto) {
                "Trabajador" -> valores.add("${(obj as Trabajador).primerNombre} ${obj.primerApellido}")
            }
        }
        return valores
    }

    private fun onClickTrabajador() {
        spinnerTrabajador.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sidTrabajador = arrayListDetailsTrabajadoresFB[position].codigo
                labelIngresos.text =
                    ("Oferta de ${(arrayListDetailsTrabajadoresFB[position].primerNombre)} ${(arrayListDetailsTrabajadoresFB[position].primerApellido)}")
                if (conectividad.connectedTo(context))
                //hay internet así que se carga la tabla con SQL
                    loadDespachosSQLDataToFirestore(sidTrabajador)
                //actualizamos en firebase todos los trabajadores que sacamos de sql
                //Una vez clickeado el trabajador, se debe cargar los despachos de ese trabajador
                getDespachosFromFirestore(sidTrabajador)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun getDespachosFromFirestore(sidTrabajador: String) {
        arrayListDetailsDespachoFB = ArrayList()
        firebaseModel.db.collection("Despachos").whereEqualTo("idTrabajador", sidTrabajador)
            .whereEqualTo("Fecha", SimpleDateFormat("yyyy-MM-dd").format(Date()))
            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen error", e)
                    return@addSnapshotListener
                }

                for (change in querySnapshot!!.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        if (querySnapshot.metadata.isFromCache) {
                            Log.d(TAG, "Información obtenida del cache en la vista ListaDespachos")
                        } else {
                            Log.d(TAG,"Información obtenida del servidor en la vista ListaDespachos")
                        }
                        val despacho = Despacho()
                        despacho.cantidad = change.document["Cantidad"].toString().toInt()
                        despacho.empacador = change.document["Empacador"].toString()
                        despacho.finca = change.document["Finca"].toString()
                        despacho.oferta = change.document["Oferta"].toString()
                        despacho.pedido = change.document["Pedido"].toString()
                        despacho.presentacion = change.document["Nombre"].toString()
                        despacho.idTrabajador = change.document["idTrabajador"].toString()
                        despacho.idDespacho = change.document.id
                        despacho.sentera = change.document["Entera"].toString()
                        if (despacho.sentera.equals("No")) {
                            despacho.entera = 1
                        } else {
                            despacho.entera = 0
                        }
                        despacho.idDespacho = change.document.id
                        arrayListDetailsDespachoFB.add(despacho)
                        Log.d(TAG, "Se obtuvo: ${change.document.id} => ${change.document.data}")
                    }
                }
                //reiniciamos la vista por aquello que se actualice en tiempo real
                recyclerView.layoutManager = LinearLayoutManager(this)
                setupRecyclerView(arrayListDetailsDespachoFB)
            }
    }

    private fun loadDespachosSQLDataToFirestore(sidTrabajador: String) {
        request.client.newCall(request.getRequestEndPoint("Despacho/Info?empacador=$sidTrabajador"))
            .enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {}
                    override fun onResponse(call: Call, response: Response) {
                        val str_response = response.body()!!.string()
                        //creating json object
                        val json_contact: JSONObject = JSONObject(str_response)
                        //creating json array
                        val jsonarrayInfo: JSONArray = json_contact.getJSONArray("Data")
                        val size: Int = jsonarrayInfo.length()
                        arrayListDetailsDespachoSQL = ArrayList()
                        for (i in 0 until size) {
                            val jsonObjectdetail: JSONObject = jsonarrayInfo.getJSONObject(i)
                            val model: Despacho = Despacho();
                            model.idDespacho = jsonObjectdetail.getString("Id")
                            model.cantidad = jsonObjectdetail.getString("Cantidad").toInt()
                            model.sentera = jsonObjectdetail.getString("Entera")
                            model.pedido = jsonObjectdetail.getString("Pedido")
                            model.oferta = jsonObjectdetail.getString("Oferta")
                            model.empacador = jsonObjectdetail.getString("Empacador")
                            model.finca = jsonObjectdetail.getString("Finca")
                            model.presentacion = jsonObjectdetail.getString("Nombre")
                            model.idTrabajador = jsonObjectdetail.getString("idTrabajador")
                            arrayListDetailsDespachoSQL.add(model)
                        }
                        runOnUiThread {
                            firebaseModel.addDocumentDespacho(arrayListDetailsDespachoSQL)
                        }

                    }
                })
    }

    private fun setupRecyclerView(arrayListDetails: ArrayList<Despacho>) {
        recyclerView.adapter = ListaDespachoAdapter(ArrayList(arrayListDetails), this)

    }

    override fun onItemClick(despacho: Despacho) {
        Log.d(
            TAG,
            "Finca seleccionada en ListaDespachoActivity.onItemClick(): ${intent.getStringExtra("idFinca")}"
        )

        val intent1 = (Intent(this, EliminarIngresoActivity::class.java)).putExtra(
            "despacho", request.convertToJson(
                despacho
            ).toString()
        )
        intent1.putExtra("idFinca", intent.getStringExtra("idFinca"))
        startActivity(intent1)
    }
}