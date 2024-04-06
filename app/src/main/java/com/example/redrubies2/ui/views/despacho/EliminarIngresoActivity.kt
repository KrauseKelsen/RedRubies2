package com.example.redrubies2.ui.views.despacho

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.redrubies2.R
import com.example.redrubies2.businesslayer.libraries.Conectividad
import com.example.redrubies2.businesslayer.repository.FireBaseModel
import com.example.redrubies2.businesslayer.libraries.Resquest
import com.example.redrubies2.datalayer.Despacho
import com.example.redrubies2.datalayer.Excepciones
import kotlinx.android.synthetic.main.activity_eliminar_ingreso.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class EliminarIngresoActivity : AppCompatActivity() {
    val conectividad : Conectividad = Conectividad()
    private val TAG = "EliminarIngresoActivity"
    val context = this
    val firebaseModel : FireBaseModel = FireBaseModel()

    private val request: Resquest = Resquest()
    var despacho: Despacho = Despacho()
    var excepciones: Excepciones = Excepciones()

    override fun onBackPressed() {
        // INTENT FOR YOUR HOME ACTIVITY
        Log.d(TAG, "Finca seleccionada en EliminarIngresoActivity.onBack(): ${intent.getStringExtra("idFinca")}" )

        val intent = (Intent(this, ListaDespachoActivity::class.java)).putExtra("idFinca", intent.getStringExtra("idFinca"))
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eliminar_ingreso)
        initFirebase()
        despacho = intent.getStringExtra("despacho")?.let { request.convertToDespacho(it) }!!
        loadDespachoId()
        eliminar.setOnClickListener {
            if (conectividad.connectedTo(context)){
                eliminarDespacho()
                firebaseModel.deleteDocumentRezagoDespacho(despacho.idDespacho)
                Toast.makeText(
                    applicationContext,
                    "Eliminado exitosamente",
                    Toast.LENGTH_LONG
                ).show()
                // Se pasa el id del trabajador para autollenar el select de ListaDespacho, pero no se implementó
                val intent1 = (Intent(this, ListaDespachoActivity::class.java)).putExtra("idTrabajador", despacho.idTrabajador)
                intent1.putExtra("idFinca", intent.getStringExtra("idFinca"))

                startActivity(intent1)
            }else{
                firebaseModel.addDocumentEliminarIngresos(despacho)
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
    }

    private fun eliminarDespacho() {
        val thread = Thread {
            try {
                request.client.newCall(request.getRequestEndPoint("Despacho?id=${despacho.idDespacho}")).enqueue(object : Callback {
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

    private fun loadDespachoId() {
        empacador.text = despacho.empacador
        oferta.text = despacho.oferta
        presentacion.text = despacho.presentacion
        cantidad.text = "${despacho.cantidad} kg"
        finca.text = despacho.finca
        caja.text = despacho.sentera
    }
}