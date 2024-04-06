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
import com.example.redrubies2.ui.views.reporte.GenerarReporteProduccionActivity
import com.example.redrubies2.ui.views.reporte.GenerarReporteRendimientoActivity
import kotlinx.android.synthetic.main.activity_menu_reporte.*

class MenuReporteActivity : AppCompatActivity(), CardMenuAdapter.onCardMenuClickListener {
    //Verificar conexion a internet
    val conectividad : Conectividad = Conectividad()
    private val TAG = "MenuReporteActivity"
    var context: Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_proceso)
        setupRecyclerView()
    }

    override fun onBackPressed() {
        // INTENT FOR YOUR HOME ACTIVITY
        Log.d(TAG, "Finca seleccionada en MenuReporteActivity.onBack(): ${intent.getStringExtra("idFinca")}" )

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
                "https://res.cloudinary.com/plantas-y-flores-ornamentales/image/upload/v1610136859/FT_RedRubbies/FT12_qfzulf.png",
                "Reporte de producción",
                "Selecciona la variedad específica y la fecha de cosecha para generar un reporte que te permita gestionar su producción diaria",
                R.drawable.ft12
            ),
            CardMenu(
                "https://res.cloudinary.com/plantas-y-flores-ornamentales/image/upload/v1610136860/FT_RedRubbies/FT11_dsddnl.png",
                "Reporte de rendimiento laboral",
                "Gestiona el rendimiento laboral de tus trabajadores seleccionando su jornada laboral",
                R.drawable.ft11
            )
        )

        recyclerView.adapter = CardMenuAdapter(this, listaCardMenu, this)

    }

    override fun onItemClick(texto: String) {
        when {
            texto.equals("Reporte de producción") -> {
                Log.d(TAG, "Finca seleccionada en MenuReporteActivity.onclick(Reporte de producción): ${intent.getStringExtra("idFinca")}" )

                val intent = (Intent(this, GenerarReporteProduccionActivity::class.java)).putExtra("idFinca", intent.getStringExtra("idFinca"))
                startActivity(intent)
            }
            texto.equals("Reporte de rendimiento laboral") -> {
                Log.d(TAG, "Finca seleccionada en MenuReporteActivity.onclick(Reporte de rendimiento laboral): ${intent.getStringExtra("idFinca")}" )

                val intent = (Intent(this, GenerarReporteRendimientoActivity::class.java)).putExtra("idFinca", intent.getStringExtra("idFinca"))
                startActivity(intent)
            }
        }

    }
}