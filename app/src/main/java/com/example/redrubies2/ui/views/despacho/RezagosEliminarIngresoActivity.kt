package com.example.redrubies2.ui.views.despacho

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.redrubies2.R
import com.example.redrubies2.businesslayer.libraries.Conectividad
import com.example.redrubies2.businesslayer.repository.FireBaseModel
import com.example.redrubies2.businesslayer.libraries.Resquest
import com.example.redrubies2.datalayer.Despacho
import com.example.redrubies2.datalayer.Excepciones
import com.example.redrubies2.ui.adapters.RezagosDespachoAdapter
import com.example.redrubies2.ui.views.menus.MenuProcesoActivity
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.MetadataChanges
import kotlinx.android.synthetic.main.activity_rezagos_eliminar_ingreso.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class RezagosEliminarIngresoActivity : AppCompatActivity(), RezagosDespachoAdapter.Listener{
    //Verificar conexion a internet
    val conectividad : Conectividad = Conectividad()
    private val TAG = "RezagosEliminarIngresoActivity"
    val context = this
    val firebaseModel : FireBaseModel = FireBaseModel()
    private val request: Resquest = Resquest()
    var arrayListDetails:ArrayList<Despacho> = ArrayList();
    var excepciones: Excepciones = Excepciones()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rezagos_eliminar_ingreso)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        initFirebase()
        getRezagosFromFirestore()
    }

    override fun onBackPressed() {
        // INTENT FOR YOUR HOME ACTIVITY
        val intent = (Intent(this, MenuProcesoActivity::class.java)).putExtra("idFinca", intent.getStringExtra("idFinca"))
        startActivity(intent)
    }


    //Cuando se cambia de activity esto pasa por debajo (en resumen)
    override fun onResume() {
        super.onResume()
        offline.isVisible = !conectividad.connectedTo(context)
    }

    fun initFirebase(){
        firebaseModel.setupCacheSize()
        firebaseModel.enableNetwork()

    }

    private fun getRezagosFromFirestore() {
        firebaseModel.db.collection("EliminarIngresoRezagos").whereEqualTo("Subido", "NO ELIMINADO")
            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen error", e)
                    return@addSnapshotListener
                }

                for (change in querySnapshot!!.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        Log.d(TAG, "Eliminar Rezagos: ${change.document.data}")
                    }

                    if (querySnapshot.metadata.isFromCache){
                        Log.d(TAG, "Información obtenida del cache en la vista RezagosEliminarIngresoActivity")
                        val despacho = Despacho()
                        despacho.empacador = change.document["Empacador"].toString()
                        despacho.subido = change.document["Subido"].toString()
                        despacho.presentacion = change.document["Nombre"].toString()
                        despacho.sentera = change.document["Entera"].toString()
                        despacho.idDespacho = change.document.id
                        arrayListDetails.add(despacho)
                        arrayListDetails.distinct()
                    }
                    else{
                        Log.d(TAG, "Información obtenida del cache en la vista RezagosEliminarIngresoActivity")
                        val despacho = Despacho()
                        despacho.empacador = change.document["Empacador"].toString()
                        despacho.subido = change.document["Subido"].toString()
                        despacho.presentacion = change.document["Nombre"].toString()
                        despacho.sentera = change.document["Entera"].toString()
                        despacho.idDespacho = change.document.id
                        arrayListDetails.add(despacho)
                        arrayListDetails.distinct()
                    }
                }
                setupRecyclerView(arrayListDetails)
            }
    }

    private fun setupRecyclerView(arrayListDetails: ArrayList<Despacho>) {
        recyclerView.adapter = RezagosDespachoAdapter(ArrayList(arrayListDetails), this)
    }

    override fun onItemClick(obj: Despacho) {
        if (conectividad.connectedTo(context)){
            cambiarEstadoRezago(obj)
            eliminarDespacho(obj)
            actualizaFirebaseDespacho(obj)
            Toast.makeText(
                applicationContext,
                "Despacho eliminado exitosamente",
                Toast.LENGTH_LONG
            ).show()
            val intent = (Intent(this, ListaDespachoActivity::class.java)).putExtra("idFinca", intent.getStringExtra("idFinca"))
            startActivity(intent)
        }else{
            Toast.makeText(
                applicationContext,
                "Verifique su conexión con el servidor e intente de nuevo",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun actualizaFirebaseDespacho(obj: Despacho) {
        firebaseModel.db.collection("Despachos").whereEqualTo("Empacador", obj.empacador)
            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen error", e)
                    return@addSnapshotListener
                }

                for (change in querySnapshot!!.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        Log.d(TAG, "Despachos: ${change.document.data}")
                    }
                }
            }
    }

    private fun cambiarEstadoRezago(obj: Despacho) {
        firebaseModel.deleteDocumentRezagoDespacho(obj.idDespacho)
    }

    fun eliminarDespacho(obj: Despacho) {
        val thread = Thread {
            try {
                request.client.newCall(request.getRequestEndPoint("Despacho?id=${obj.idDespacho}")).enqueue(object :
                    Callback {
                    override fun onFailure(call: Call, e: IOException) {}
                    override fun onResponse(call: Call, response: Response) {
                        val str_response = response.body()!!.string()
                        val json_contact: JSONObject = JSONObject(str_response)
                        //creating json array
                        val jsonInfo: String = json_contact.getString("Data")
                        runOnUiThread {
                            excepciones.OkDelete("Despacho", applicationContext)
                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }


        }
        thread.start()
    }
}