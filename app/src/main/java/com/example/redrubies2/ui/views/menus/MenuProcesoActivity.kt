package com.example.redrubies2.ui.views.menus

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.redrubies2.R
import com.example.redrubies2.businesslayer.libraries.Conectividad
import com.example.redrubies2.datalayer.CardMenu
import com.example.redrubies2.ui.adapters.CardMenuAdapter
import com.example.redrubies2.ui.views.despacho.ListaDespachoActivity
import com.example.redrubies2.ui.views.despacho.NuevoIngresoActivity
import com.example.redrubies2.ui.views.despacho.RezagosDespachoActivity
import com.example.redrubies2.ui.views.despacho.RezagosEliminarIngresoActivity
import kotlinx.android.synthetic.main.activity_menu_proceso.*

class MenuProcesoActivity : AppCompatActivity(), CardMenuAdapter.onCardMenuClickListener {
    //Verificar conexion a internet
    val conectividad : Conectividad = Conectividad()
    private val TAG = "MenuProcesoActivity"
    var context: Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_proceso)
        setupRecyclerView()
    }

    override fun onBackPressed() {
        // INTENT FOR YOUR HOME ACTIVITY
        Log.d(TAG, "Finca seleccionada en MenuProcesoActivity.onBack(): ${intent.getStringExtra("idFinca")}" )

        val intent = (Intent(this, MenuActivity::class.java)).putExtra("idFinca", intent.getStringExtra("idFinca"))
        startActivity(intent)
    }

    //Cuando se cambia de activity esto pasa por debajo (en resumen)
    override fun onResume() {
        super.onResume()
        offline.isVisible = !conectividad.connectedTo(context)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        val listaCardMenu = listOf(
            CardMenu(
                "https://res.cloudinary.com/plantas-y-flores-ornamentales/image/upload/v1609869012/FT_RedRubbies/FT10_ntelep.png",
                "Crear Ingreso",
                "Crea los ingresos diarios seleccionando cajas completas o incompletas, el empacador, la oferta, la variedad y el cliente respectivo",
                R.drawable.ft10
            ),
            CardMenu(
                "https://res.cloudinary.com/plantas-y-flores-ornamentales/image/upload/v1609869012/FT_RedRubbies/FT9_sswus8.png",
                "Cargar Ingresos Rezagados",
                "Esta opción te permite cargar los ingresos que no se hayan registrado a causa de no tener una conexión estable a la red",
                R.drawable.ft9
            ),
            CardMenu(
                "https://res.cloudinary.com/plantas-y-flores-ornamentales/image/upload/v1609868904/FT_RedRubbies/FT7_ufjfjt.png",
                "Eliminar Ingresos Rezagados",
                "Esta opción te permite eliminar los ingresos que no se hayan borrado a causa de no tener una conexión estable a la red",
                R.drawable.ft7
            ),
            CardMenu(
                "https://res.cloudinary.com/plantas-y-flores-ornamentales/image/upload/v1609869012/FT_RedRubbies/FT8_hieeko.png",
                "Ingresos",
                "Obten una lista actual de los registrados y su información adicional ingresando a esta ventana, selecciona uno en específico para eliminar",
                R.drawable.ft8
            )
        )

        recyclerView.adapter = CardMenuAdapter(this, listaCardMenu, this)

    }

    override fun onItemClick(texto: String) {
        when {
            texto.equals("Crear Ingreso") -> {
                Log.d(TAG, "Finca seleccionada en MenuProcesoActivity.onclick(Crear Ingreso): ${intent.getStringExtra("idFinca")}" )

                val intent = (Intent(this, NuevoIngresoActivity::class.java)).putExtra("idFinca", intent.getStringExtra("idFinca"))
                startActivity(intent)
            }
            texto.equals("Cargar Ingresos Rezagados") -> {
                Log.d(TAG, "Finca seleccionada en MenuProcesoActivity.onclick(Cargar Ingresos Rezagados): ${intent.getStringExtra("idFinca")}" )

                val intent = (Intent(this, RezagosDespachoActivity::class.java)).putExtra("idFinca", intent.getStringExtra("idFinca"))
                startActivity(intent)
            }
            texto.equals("Eliminar Ingresos Rezagados") -> {
                Log.d(TAG, "Finca seleccionada en MenuProcesoActivity.onclick(Eliminar Ingresos Rezagados): ${intent.getStringExtra("idFinca")}" )

                val intent = (Intent(this, RezagosEliminarIngresoActivity::class.java)).putExtra("idFinca", intent.getStringExtra("idFinca"))
                startActivity(intent)
            }
            texto.equals("Ingresos") -> {
                Log.d(TAG, "Finca seleccionada en MenuProcesoActivity.onclick(Ingresos): ${intent.getStringExtra("idFinca")}" )

                val intent = (Intent(this, ListaDespachoActivity::class.java)).putExtra("idFinca", intent.getStringExtra("idFinca"))
                startActivity(intent)
            }
        }

    }
}