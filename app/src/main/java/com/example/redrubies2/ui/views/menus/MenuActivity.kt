package com.example.redrubies2.ui.views.menus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.redrubies2.R
import com.example.redrubies2.businesslayer.libraries.Conectividad
import com.example.redrubies2.datalayer.CardMenu
import com.example.redrubies2.ui.adapters.CardMenuAdapter
import com.example.redrubies2.ui.views.MainActivity
import kotlinx.android.synthetic.main.activity_menu.*

class MenuActivity : AppCompatActivity(), CardMenuAdapter.onCardMenuClickListener {
    //Verificar conexion a internet
    val conectividad : Conectividad = Conectividad()
    private val TAG = "MenuActivity"

    val context : Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        offline.isVisible = !conectividad.connectedTo(context)
    }

    private fun setupRecyclerView() {
        val listaCardMenu = listOf(
            CardMenu(
                "https://res.cloudinary.com/plantas-y-flores-ornamentales/image/upload/v1609867998/FT_RedRubbies/FT1_zs2gvp.png",
                "Cosecha",
                "Registra tus cosechas diarias y carga los rezagos que no se hayan guardado al no tener una conexión estable a la red",
                R.drawable.ft1
            ),
//            CardMenu(
//                "https://res.cloudinary.com/plantas-y-flores-ornamentales/image/upload/v1609867876/FT_RedRubbies/FT2_kzzxjf.png",
//                "Proceso",
//                "Registra los ingresos diarios y filtralos apartir de los empacadores asignados, eliminalos y manipula la información sin conexión a la red desde los rezagos",
//                R.drawable.ft2
//
//            ),
            CardMenu(
                "https://res.cloudinary.com/plantas-y-flores-ornamentales/image/upload/v1609867767/FT_RedRubbies/FT3_cfbfqo.png",
                "Reporte",
                "Genera reportes de producción y rendimiento laboral las veces que desees en tiempo real",
                R.drawable.ft3

            )
        )
        recyclerView.adapter = CardMenuAdapter(this, listaCardMenu, this)
    }

    override fun onBackPressed() {
        // INTENT FOR YOUR HOME ACTIVITY
        Log.d(TAG, "Finca seleccionada: ${intent.getStringExtra("idFinca")}" )

        val intent = (Intent(this, MainActivity::class.java)).putExtra("idFinca", intent.getStringExtra("idFinca"))
        startActivity(intent)
    }

    override fun onItemClick(texto: String) {
        when {
            texto.equals("Cosecha") -> {
                Log.d(TAG, "Finca seleccionada en MenuActivity.onclick(Cosecha): ${intent.getStringExtra("idFinca")}" )

                val intent = (Intent(this, MenuCosechaActivity::class.java)).putExtra("idFinca", intent.getStringExtra("idFinca"))
                startActivity(intent)
            }
            texto.equals("Proceso") -> {
                Log.d(TAG, "Finca seleccionada en MenuActivity.onclick(Proceso): ${intent.getStringExtra("idFinca")}" )

                val intent = (Intent(this, MenuProcesoActivity::class.java)).putExtra("idFinca", intent.getStringExtra("idFinca"))
                startActivity(intent)
            }
            texto.equals("Reporte") -> {
                Log.d(TAG, "Finca seleccionada en MenuActivity.onclick(Reporte): ${intent.getStringExtra("idFinca")}" )

                val intent = (Intent(this, MenuReporteActivity::class.java)).putExtra("idFinca", intent.getStringExtra("idFinca"))
                startActivity(intent)
            }
        }

    }
}