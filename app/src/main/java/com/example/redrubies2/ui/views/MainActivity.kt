package com.example.redrubies2.ui.views

import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.redrubies2.R
import com.example.redrubies2.businesslayer.libraries.Conectividad
import com.example.redrubies2.businesslayer.repository.FireBaseModel
import com.example.redrubies2.businesslayer.libraries.Resquest
import com.example.redrubies2.datalayer.Comun
import com.example.redrubies2.datalayer.Invernadero
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_nueva_cosecha_hoy.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URL

class MainActivity : AppCompatActivity() {
    val conectividad: Conectividad = Conectividad()
    private val TAG = "EliminarIngresoActivity"
    val context = this
    val request = Resquest()
    val firebaseModel: FireBaseModel = FireBaseModel()
    var arrayListDetailsComunes: ArrayList<Comun> = ArrayList();
    var arrayListDetailsInvernaderos: ArrayList<Invernadero> = ArrayList();
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initFirebase()
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        //MyAsyncTask(intent).execute()
    }

    fun initFirebase() {
        firebaseModel.setupCacheSize()
        firebaseModel.enableNetwork()
    }

    private fun cargarComunes() {
        request.client.newCall(request.getRequestEndPoint("Comun")).enqueue(object : Callback {
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
                    firebaseModel.addDocumentComunes(arrayListDetailsComunes)
                }
            }
        })

        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

    inner class MyAsyncTask(intent: Intent) : AsyncTask<URL, Int, String>() {

        private var result: String = "";

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: URL?): String? {
            var bandera = false
            //Por debajo, mientras carga 100 valores, para el contador del progress circule
            for (i in 0..100) {
                //Mientras carga el for, en la interfaz gráfica pasa lo siguiente
                runOnUiThread {
                    // Primero se pone el valor del porcentaje por el que va la carga a la vez que carga por debajo el circulo
                    porcentaje.text = "${i}%"
                    //Se le anuncia al usuario que esta veriifcando la conexión
                    if (i < 10)
                        carga.text = "Verificando conexión..."
                    // Si se llega al 5% entonces se verifica la conexión a internet
                    if (conectividad.connectedTo(context)) {
                        bandera = true
                        //Con la conexión establecida luego podremos cargar la información
                    } else {
                        bandera = false
                        carga.text = "Conexión inestable..."
                        //Con la conexión inestable podremos romper el proceso
                    }
                }

                //Se verifica si la conexión es estable y abre un hilo por debajo para cargar los datos
                try {
                    runOnUiThread {
                        if (bandera) {
                            if (i < 15) {
                                carga.text = "Conexión establecida..."
                            } else if (i == 15) {
                                carga.text = "Descargando invernaderos..."
                                cargarInvernaderos()
                            } else if (i == 20) {
                                carga.text = "Descargando trabajadores..."
                            } else if (i == 30) {
                                carga.text = "Descargando varieadaes..."
                            } else if (i == 40) {
                                carga.text = "Descargando invernaderos..."
                            } else if (i == 50) {
                                carga.text = "Descargando cosechas..."
                            } else if (i == 60) {
                                carga.text = "Descargando ingresos..."
                            } else if (i == 70) {
                                carga.text = "Descargando rezagos..."
                            } else if (i == 80) {
                                carga.text = "Descargando comunes..."
                                cargarComunes()
                            }
                        }
                    }

                    Thread.sleep(50)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                publishProgress(i)

                //Se verifica si la conexión es inestable para romper el proceso y mostrar el boton
                if (carga.text.toString().equals("Conexión inestable...")) {
                    runOnUiThread {
                        val intent = Intent(this@MainActivity, HomeActivity::class.java)
                        startActivity(intent)
                    }
                    return ""
                }
            }
            return "Finalizando carga"
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            val k = values[0]?.toFloat()
            progress_circular.apply {
                // or with animation
                if (k != null) {
                    setProgressWithAnimation(k, 50)
                    // Set Progress Max
                    progressMax = 100f

                    // Set ProgressBar Color
                    progressBarColor = Color.WHITE
                    // or with gradient
                    progressBarColorStart = Color.parseColor("#ffbcc1")
                    progressBarColorEnd = Color.parseColor("#F85F6A")
                    progressBarColorDirection = CircularProgressBar.GradientDirection.TOP_TO_BOTTOM
                } // =1s


            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            Toast.makeText(context, result, Toast.LENGTH_LONG)
        }
    }

    private fun cargarInvernaderos() {
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

                        firebaseModel.addDocumentInvernadero(arrayListDetailsInvernaderos)

                    }
                }
            })
    }


}