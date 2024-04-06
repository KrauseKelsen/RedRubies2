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
import com.example.redrubies2.ui.adapters.RezagosDespachoAdapter
import com.example.redrubies2.ui.views.menus.MenuProcesoActivity
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.MetadataChanges
import kotlinx.android.synthetic.main.activity_rezagos_despacho.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RezagosDespachoActivity : AppCompatActivity(), RezagosDespachoAdapter.Listener {
    //Verificar conexion a internet
    val conectividad : Conectividad = Conectividad()
    private val TAG = "RezagosDespachoActivity"
    val context = this
    val firebaseModel : FireBaseModel = FireBaseModel()
    private val request: Resquest = Resquest()
    var arrayListDetails:ArrayList<Despacho> = ArrayList();
    //Cuando se cambia de activity esto pasa por debajo (en resumen)
    override fun onResume() {
        super.onResume()
        offline.isVisible = !conectividad.connectedTo(context)
    }

    fun initFirebase(){
        firebaseModel.setupCacheSize()
        firebaseModel.enableNetwork()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rezagos_despacho)
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

    private fun getRezagosFromFirestore() {
        firebaseModel.db.collection("DespachoRezagos").whereEqualTo("Subido", "PENDIENTE").whereEqualTo("Fecha", SimpleDateFormat("yyyy-MM-dd").format(
            Date()
        ))
            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen error", e)
                    return@addSnapshotListener
                }

                for (change in querySnapshot!!.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        Log.d(TAG, "DespachoRezagos: ${change.document.data}")
                    }

                    if (querySnapshot.metadata.isFromCache){
                        Log.d(TAG, "Información obtenida del cache en la vista RezagosDespachoActivity")
                        val despacho = Despacho()
                        despacho.cantidad = change.document["Cantidad"].toString().toInt()
                        despacho.empacador = change.document["Empacador"].toString()
                        if ((change.document["Entera"].toString()).equals("1")){
                            despacho.sentera = "Si"
                        }else{
                            despacho.sentera = "No"
                        }
                        despacho.subido = change.document["Subido"].toString()
                        despacho.finca = change.document["Finca"].toString()
                        despacho.idTrabajador = change.document["IdTrabajador"].toString()
                        despacho.presentacion = change.document["Nombre"].toString()
                        despacho.oferta = change.document["Oferta"].toString()
                        despacho.pedido = change.document["Pedido"].toString()
                        despacho.idDespacho = change.document.id
                        arrayListDetails.add(despacho)
                        arrayListDetails.distinct()
                    }
                    else{
                        Log.d(TAG, "Información obtenida del cache en la vista RezagosDespachoActivity")
                        val despacho = Despacho()
                        despacho.cantidad = change.document["Cantidad"].toString().toInt()
                        despacho.empacador = change.document["Empacador"].toString()
                        despacho.entera = change.document["Entera"].toString().toInt()
                        if ((change.document["Entera"].toString()).equals("1")){
                            despacho.sentera = "Si"
                        }else{
                            despacho.sentera = "No"
                        }
                        despacho.subido = change.document["Subido"].toString()
                        despacho.finca = change.document["Finca"].toString()
                        despacho.idTrabajador = change.document["IdTrabajador"].toString()
                        despacho.presentacion = change.document["Nombre"].toString()
                        despacho.oferta = change.document["Oferta"].toString()
                        despacho.pedido = change.document["Pedido"].toString()
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
            guardarDatos(obj)
            Toast.makeText(
                applicationContext,
                "Datos guardados",
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

    private fun cambiarEstadoRezago(obj: Despacho) {
        firebaseModel.updateDocumentRezagoDespacho(obj)
    }

    fun guardarDatos(obj: Despacho) {
        //Se validan los campos
        val thread = Thread {
            try {
                val despacho: Despacho = Despacho()
                despacho.cantidad = obj.cantidad
                despacho.empacador = obj.idTrabajador
                despacho.oferta = obj.oferta
                despacho.pedido = ""
                despacho.finca = obj.finca
                despacho.entera = obj.entera
                request.postRequestEndPoint(despacho,"Despacho")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        thread.start()
    }
}