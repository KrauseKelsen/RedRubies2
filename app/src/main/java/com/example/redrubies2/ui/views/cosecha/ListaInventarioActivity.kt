package com.example.redrubies2.ui.views.cosecha

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.redrubies2.R
import com.example.redrubies2.businesslayer.libraries.Conectividad
import com.example.redrubies2.businesslayer.libraries.Resquest
import com.example.redrubies2.businesslayer.repository.FireBaseModel
import com.example.redrubies2.datalayer.Excepciones
import com.example.redrubies2.datalayer.ListaInventario
import com.example.redrubies2.ui.adapters.ListaInventarioAdapter
import com.example.redrubies2.ui.views.despacho.RezagosEliminarIngresoActivity
import com.example.redrubies2.ui.views.menus.MenuCosechaActivity
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.MetadataChanges
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_lista_ingreso.*
import kotlinx.android.synthetic.main.activity_lista_inventario.*
import kotlinx.android.synthetic.main.activity_lista_inventario.offline
import kotlinx.android.synthetic.main.activity_lista_inventario.recyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.O)
class ListaInventarioActivity : AppCompatActivity(), ListaInventarioAdapter.Listener,
    SearchView.OnQueryTextListener{
    //Verificar conexion a internet
    val conectividad: Conectividad = Conectividad()
    private val TAG = "MenuActivity"
    private var contador = 0
    val context: Context = this
    val firebaseModel: FireBaseModel = FireBaseModel()
    private val request: Resquest = Resquest()
    var excepciones: Excepciones = Excepciones()
    var arrayListDetailsSQL: ArrayList<ListaInventario> = ArrayList();
    var arrayListDetailsFB: ArrayList<ListaInventario> = ArrayList();
    lateinit var adapter: ListaInventarioAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_inventario)
        AndroidThreeTen.init(application);
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        initFirebase()
        if (conectividad.connectedTo(context))
            loadSQLDataToFirestore()
        getInventariosFromFirestore()
        svSearch.setOnQueryTextListener(this)
    }

    override fun onBackPressed() {
        // INTENT FOR YOUR HOME ACTIVITY
        val intent = (Intent(this, MenuCosechaActivity::class.java)).putExtra("idFinca",intent.getStringExtra("idFinca"))
        startActivity(intent)
    }

    private fun getInventariosFromFirestore() {
        arrayListDetailsFB = ArrayList();
        firebaseModel.db.collection("InventarioCosechaDia").whereEqualTo("Fecha", SimpleDateFormat("yyyy-MM-dd").format(Date()))
            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen error", e)
                    return@addSnapshotListener
                }

                for (change in querySnapshot!!.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        if (querySnapshot.metadata.isFromCache) {
                            Log.d(TAG,"Información obtenida del cache en la vista ListaInventario")
                        }else{
                            Log.d(TAG,"Información obtenida del servidor en la vista ListaInventario")
                        }
                        val inventario = ListaInventario()
                        inventario.cantidad = change.document["Cantidad"].toString()
                        inventario.id = change.document.id
                        inventario.comun = change.document["Comun"].toString()
                        inventario.trabajador = change.document["Trabajador"].toString()
                        inventario.hora = change.document["Hora"].toString()
                        inventario.invernadero = change.document["Invernadero"].toString()
                        inventario.variedad = change.document["Variedad"].toString()
                        arrayListDetailsFB.add(inventario)
                        Log.d(TAG, "Se obtuvo: ${change.document.id} => ${change.document.data}")
                    }
                }
                //reiniciamos la vista por aquello que se actualice en tiempo real
                recyclerView.layoutManager = LinearLayoutManager(this)
                setupRecyclerView(arrayListDetailsFB)
            }
    }

    private fun getInvenatariosFromSQL() {
        request.client.newCall(request.getRequestEndPoint("Inventario/CosechaDia"))
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {}
                override fun onResponse(call: Call, response: Response) {
                    val str_response = response.body()!!.string()
                    //creating json object
                    val json_contact: JSONObject = JSONObject(str_response)
                    //creating json array
                    val jsonarrayInfo: JSONArray = json_contact.getJSONArray("Data")
                    val size: Int = jsonarrayInfo.length()
                    arrayListDetailsFB = ArrayList()
                    for (i in 0 until size) {
                        val jsonObjectdetail: JSONObject = jsonarrayInfo.getJSONObject(i)
                        val model: ListaInventario = ListaInventario();
                        model.id = jsonObjectdetail.getString("Id")
                        model.cantidad = jsonObjectdetail.getString("Cantidad")
                        model.comun = jsonObjectdetail.getString("Comun")
                        model.trabajador = jsonObjectdetail.getString("Trabajador")
                        model.hora = jsonObjectdetail.getString("Hora")
                        model.invernadero = jsonObjectdetail.getString("Invernadero")
                        model.variedad = jsonObjectdetail.getString("Variedad")
                        arrayListDetailsFB.add(model)
                    }
                    runOnUiThread {
                        setupRecyclerView(arrayListDetailsFB)
                    }

                }
            })
    }

    fun initFirebase() {
        firebaseModel.setupCacheSize()
        firebaseModel.enableNetwork()
    }

    override fun onResume() {
        super.onResume()
        offline.isVisible = !conectividad.connectedTo(context)
    }

    private fun loadSQLDataToFirestore() {
        request.client.newCall(request.getRequestEndPoint("Inventario"))
            .enqueue(object : Callback {
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

    private fun setupRecyclerView(arrayListDetails: ArrayList<ListaInventario>) {
        contador += 1
        if(contador==1){
            arrayListDetails.reverse()
        }
        adapter = ListaInventarioAdapter(ArrayList(arrayListDetails), this)
        recyclerView.adapter = adapter
        Log.e(TAG, "$contador")
        labelCantidad.text = "Cantidad: ${arrayListDetails.size}"
    }
    override fun onItemClick(item: ListaInventario) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Eliminar registro")
        builder.setMessage("¿Está seguro que desea eliminar el registro de ${item.comun} del trabajador ${item.trabajador}")
        builder.setPositiveButton("Ok") { _: DialogInterface, which: Int ->
        }

        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            eliminarFirebase(item)
        }

        builder.setNegativeButton(android.R.string.no) { dialog, which ->
            Toast.makeText(applicationContext,
                android.R.string.no, Toast.LENGTH_SHORT).show()
        }
        builder.show()
    }

    private fun eliminarInventario(id: String) {
//        val thread = Thread {
//            try {
//                request.client.newCall(request.getRequestEndPoint("Despacho?id=$id")).enqueue(object : Callback {
//                    override fun onFailure(call: Call, e: IOException) {}
//                    override fun onResponse(call: Call, response: Response) {
//                        val str_response = response.body()!!.string()
//                        val json_contact: JSONObject = JSONObject(str_response)
//                        //creating json array
//                        val jsonInfo: String = json_contact.getString("Data")
//                        runOnUiThread {
//                            excepciones.OkDelete("Despacho", applicationContext)
//                        }
//                    }
//                })
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//        thread.start()
    }

    fun eliminarFirebase(inventario: ListaInventario) {
        if (conectividad.connectedTo(context)){
            eliminarInventario(inventario.id)
            firebaseModel.deleteDocumentRezagoInventario(inventario.id)
            Toast.makeText(
                applicationContext,
                "Eliminado exitosamente",
                Toast.LENGTH_LONG
            ).show()
//            val intent1 = (Intent(this, ListaDespachoActivity::class.java)).putExtra("idTrabajador", inventario.trabajador)
//            intent1.putExtra("idFinca", intent.getStringExtra("idFinca"))
//
//            startActivity(intent1)
        }else{
            firebaseModel.addDocumentEliminarInventario(inventario)
            Toast.makeText(
                applicationContext,
                "Guardado en rezagos, cuando exista conexión al servidor deberá eliminarlo",
                Toast.LENGTH_LONG
            ).show()
            val intent = (Intent(this, RezagosEliminarIngresoActivity::class.java)).putExtra(
                "idFinca",
                intent.getStringExtra("idFinca")
            )
            startActivity(intent)
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            val list = adapter.filter(newText, arrayListDetailsFB)
            adapter = ListaInventarioAdapter(list, this)
            recyclerView.adapter = adapter
            labelCantidad.text = "Cantidad: ${list.size}"

        }
        return false
    }
}