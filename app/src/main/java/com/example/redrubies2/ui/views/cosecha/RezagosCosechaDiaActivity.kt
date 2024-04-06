package com.example.redrubies2.ui.views.cosecha

import android.content.Context
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
import com.example.redrubies2.datalayer.Inventario
import com.example.redrubies2.datalayer.ListaInventario
import com.example.redrubies2.ui.adapters.RezagosCosechaHoyAdapter
import com.example.redrubies2.ui.views.menus.MenuCosechaActivity
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.MetadataChanges
import kotlinx.android.synthetic.main.activity_rezagos_cosecha_dia.*

class RezagosCosechaDiaActivity : AppCompatActivity(), RezagosCosechaHoyAdapter.Listener {
    //Verificar conexion a internet
    val conectividad : Conectividad = Conectividad()
    private val TAG = "RezagosCosechaDiaActivity"
    private var contador = 0

    val context : Context = this
    val firebaseModel : FireBaseModel = FireBaseModel()
    private val request: Resquest = Resquest()
    var arrayListDetails:ArrayList<ListaInventario> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rezagos_cosecha_dia)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        initFirebase()
        getRezagosFromFirestore()
    }

    override fun onBackPressed() {
        // INTENT FOR YOUR HOME ACTIVITY
        val intent = (Intent(this, MenuCosechaActivity::class.java)).putExtra("idFinca", intent.getStringExtra("idFinca"))
        startActivity(intent)
    }

    fun initFirebase(){
        firebaseModel.setupCacheSize()
        firebaseModel.enableNetwork()
    }

    override fun onResume() {
        super.onResume()
        offline.isVisible = !conectividad.connectedTo(context)
    }

    private fun getRezagosFromFirestore() {
        firebaseModel.db.collection("InventarioCosechaDiaRezagos").whereEqualTo("Subido", "PENDIENTE")
            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen error", e)
                    return@addSnapshotListener
                }

                for (change in querySnapshot!!.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        Log.d(TAG, "InventarioCosechaDiaRezagos: ${change.document.data}")
                    }

                    if (querySnapshot.metadata.isFromCache){
                        Log.d(TAG, "Información obtenida del cache en la vista RezagosCosechaDiaActivity")
                        val inventario = ListaInventario()
                        inventario.cantidad = change.document["Cantidad"].toString()
                        inventario.comun = change.document["Comun"].toString()
                        inventario.trabajador = change.document["Trabajador"].toString()
                        inventario.subido = change.document["Subido"].toString()
                        inventario.invernadero = change.document["Invernadero"].toString()
                        inventario.variedad = change.document["Variedad"].toString()
                        inventario.ncomun = change.document["NComun"].toString()
                        inventario.ntrabajador = change.document["NTrabajador"].toString()
                        inventario.fecha = change.document["Fecha"].toString()
                        inventario.id = change.document.id
                        arrayListDetails.add(inventario)
                        arrayListDetails.distinct()
                    }
                    else{
                        Log.d(TAG, "Información obtenida del servidor en la vista RezagosCosechaDiaActivity")
                        val inventario = ListaInventario()
                        inventario.cantidad = change.document["Cantidad"].toString()
                        inventario.comun = change.document["Comun"].toString()
                        inventario.trabajador = change.document["Trabajador"].toString()
                        inventario.subido = change.document["Subido"].toString()
                        inventario.invernadero = change.document["Invernadero"].toString()
                        inventario.variedad = change.document["Variedad"].toString()
                        inventario.ncomun = change.document["NComun"].toString()
                        inventario.ntrabajador = change.document["NTrabajador"].toString()
                        inventario.fecha = change.document["Fecha"].toString()
                        inventario.id = change.document.id
                        arrayListDetails.add(inventario)
                        arrayListDetails.distinct()
                    }
                }
                setupRecyclerView(arrayListDetails)
            }
    }

    private fun setupRecyclerView(arrayListDetails: ArrayList<ListaInventario>) {
        contador += 1
        if(contador==1){
            arrayListDetails.reverse()
        }
        recyclerView.adapter = RezagosCosechaHoyAdapter(ArrayList(arrayListDetails), this)
    }

    override fun onItemClick(obj: ListaInventario) {
        //Cambiar el estado del objeto de Firestore a CARGADO (o eliminarlo)
        //Subir el objeto a SQL Server
        //Volver a cargar el recyclerView

        if (conectividad.connectedTo(context)){
            cambiarEstadoRezago(obj)
            guardarDatos(obj)
            Toast.makeText(
                applicationContext,
                "Datos guardados correctamente",
                Toast.LENGTH_SHORT
            ).show()
            val intent = (Intent(this, ListaInventarioActivity::class.java)).putExtra("idFinca", intent.getStringExtra("idFinca"))
            startActivity(intent)
        }else{
            Toast.makeText(
                applicationContext,
                "Verifique su conexión con el servidor e intente de nuevo",
                Toast.LENGTH_SHORT
            ).show()

        }
    }

    private fun cambiarEstadoRezago(obj: ListaInventario) {
        firebaseModel.updateDocumentRezagoCosecha(obj)
    }

    private fun guardarDatos(obj: ListaInventario) {
        val thread = Thread {
            try {
                val inventario = Inventario()
                inventario.cantidad = obj.cantidad
                inventario.idComun = obj.comun
                inventario.idInvernadero = obj.invernadero
                inventario.codigoTrabajador = obj.trabajador
                inventario.idVariedad = obj.variedad
                inventario.dateTime = obj.fecha
                request.postRequestEndPoint(inventario, "api/Cosecha")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        thread.start()
    }
}