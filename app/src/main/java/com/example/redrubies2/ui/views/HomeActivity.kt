package com.example.redrubies2.ui.views

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.redrubies2.R
import com.example.redrubies2.businesslayer.libraries.Conectividad
import com.example.redrubies2.businesslayer.repository.FireBaseModel
import com.example.redrubies2.businesslayer.libraries.Resquest
import com.example.redrubies2.datalayer.Finca
import com.example.redrubies2.ui.adapters.FincaAdapter
import com.example.redrubies2.ui.views.menus.MenuActivity
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.MetadataChanges
import kotlinx.android.synthetic.main.activity_home.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class HomeActivity : AppCompatActivity() , FincaAdapter.Listener {

    val conectividad: Conectividad = Conectividad()
    private val TAG = "HomeActivity"
    val context: Context = this
    val firebaseModel: FireBaseModel = FireBaseModel()
    private val request: Resquest = Resquest()
    var arrayListDetailsSQL: ArrayList<Finca> = ArrayList()
    var arrayListDetailsFB: ArrayList<Finca> = ArrayList()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        initFirebase()
        if (conectividad.connectedTo(context))
            loadSQLDataToFirestore()
        getFincasFromFirestore()
    }

    private fun loadSQLDataToFirestore() {
        request.client.newCall(request.getRequestEndPoint("Finca")).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val str_response = response.body()!!.string()
                //creating json object
                val json_contact: JSONObject = JSONObject(str_response)
                //creating json array
                val jsonarrayInfo: JSONArray = json_contact.getJSONArray("Data")
                val size: Int = jsonarrayInfo.length()
                arrayListDetailsSQL = ArrayList();
                for (i in 0 until size) {
                    val jsonObjectdetail: JSONObject = jsonarrayInfo.getJSONObject(i)
                    val model: Finca = Finca();
                    model.id = jsonObjectdetail.getString("Id")
                    model.nombre = jsonObjectdetail.getString("Nombre")
                    model.codigo = jsonObjectdetail.getString("Codigo")
                    model.estado = jsonObjectdetail.getString("Estado")
                    model.imagen = jsonObjectdetail.getString("Imagen")
                    model.enProduccion = jsonObjectdetail.getString("EnProduccion").toBoolean()
                    arrayListDetailsSQL.add(model)

                }
                runOnUiThread {
                    firebaseModel.addDocumentFinca(arrayListDetailsSQL)
                }
            }
        })
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

    fun getFincasFromFirestore() {
        arrayListDetailsFB = ArrayList();
        firebaseModel.db.collection("Fincas").whereEqualTo("EnProduccion", true)
            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen error", e)
                    return@addSnapshotListener
                }

                for (change in querySnapshot!!.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        if (querySnapshot.metadata.isFromCache) {
                            Log.d(TAG, "Información obtenida del cache en la vista HomeActivity")
                        }else{
                            Log.d(TAG, "Información obtenida del servidor en la vista HomeActivity")
                        }
                        val finca = Finca()
                        finca.nombre = change.document["Nombre"].toString()
                        finca.id = change.document.id
                        finca.estado = change.document["Estado"].toString()
                        finca.imagen = change.document["Imagen"].toString()
                        finca.codigo = change.document["Codigo"].toString()
                        arrayListDetailsFB.add(finca)
                        Log.d(TAG, "Se obtuvo: ${change.document.id} => ${change.document.data}")
                    }
                }
                //reiniciamos la vista por aquello que se actualice en tiempo real
                recyclerView.layoutManager = LinearLayoutManager(this)
                setupRecyclerView(arrayListDetailsFB)
            }
    }

    override fun onBackPressed() {

        // make sure you have this outcommented
        // super.onBackPressed();
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    fun getFincasFromSQL() {
        request.client.newCall(request.getRequestEndPoint("Finca")).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val str_response = response.body()!!.string()
                //creating json object
                val json_contact: JSONObject = JSONObject(str_response)
                //creating json array
                val jsonarrayInfo: JSONArray = json_contact.getJSONArray("Data")
                val size: Int = jsonarrayInfo.length()
                arrayListDetailsFB = ArrayList();
                for (i in 0 until size) {
                    val jsonObjectdetail: JSONObject = jsonarrayInfo.getJSONObject(i)
                    val model: Finca = Finca();
                    model.id = jsonObjectdetail.getString("Id")
                    model.nombre = jsonObjectdetail.getString("Nombre")
                    model.codigo = jsonObjectdetail.getString("Codigo")
                    model.estado = jsonObjectdetail.getString("Estado")
                    model.imagen = jsonObjectdetail.getString("Imagen")
                    model.enProduccion = jsonObjectdetail.getString("EnProduccion").toBoolean()
                    arrayListDetailsFB.add(model)

                }
            }
        })
    }

    /**
     * Con esta función llenamos la lista (recyclerView)
     * */
    private fun setupRecyclerView(arrayListDetails: ArrayList<Finca>) {
        recyclerView.adapter = FincaAdapter(ArrayList(arrayListDetails), this)

    }

    //Desde la vista puedo manejar el click y eso es lo correcto, ya que no debería manejar el click en el adaptador
    override fun onItemClick(idFinca: String) {
        //Este codigo permite cambiar a la vista de menu
        Log.d(TAG, "Finca seleccionada: $idFinca")
        val intent = (Intent(this, MenuActivity::class.java)).putExtra("idFinca", idFinca)
        startActivity(intent)
    }
}