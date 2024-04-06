package com.example.redrubies2.ui.views.menus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.redrubies2.R
import com.example.redrubies2.businesslayer.libraries.Conectividad
import com.example.redrubies2.datalayer.CardMenu
import com.example.redrubies2.ui.adapters.CardMenuAdapter
import com.example.redrubies2.ui.views.cosecha.ListaInventarioActivity
import com.example.redrubies2.ui.views.cosecha.NuevaCosechaHoyActivity
import com.example.redrubies2.ui.views.cosecha.RezagosCosechaDiaActivity
import kotlinx.android.synthetic.main.activity_menu_cosecha.*

class MenuCosechaActivity : AppCompatActivity(), CardMenuAdapter.onCardMenuClickListener {
    //Verificar conexion a internet
    val conectividad : Conectividad = Conectividad()
    private val TAG = "MenuCosechaActivity"

    val context : Context = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_cosecha)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        setupRecyclerView()
    }

    override fun onBackPressed() {
        // INTENT FOR YOUR HOME ACTIVITY
        val intent = (Intent(this, MenuActivity::class.java)).putExtra("idFinca", intent.getStringExtra("idFinca"))
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        offline.isVisible = !conectividad.connectedTo(context)
    }

    private fun setupRecyclerView() {
        val listaCardMenu = listOf(
            CardMenu(
                "https://res.cloudinary.com/plantas-y-flores-ornamentales/image/upload/v1609868352/FT_RedRubbies/FT4_gsljqt.png",
                "Crear Cosecha",
                "Registra las cosechas de hoy seleccionando el comun, el trabajador, la variedad, el inventario y la cantidad",
                R.drawable.ft4
            ),
            CardMenu(
                "https://res.cloudinary.com/plantas-y-flores-ornamentales/image/upload/v1609868242/FT_RedRubbies/FT5_taq32w.png",
                "Cargar Cosechas Rezagadas",
                "Esta opción te permite cargar las cosechas que no se hayan registrado a causa de no tener una conexión estable a la red",
                R.drawable.ft5
            ),
            CardMenu(
                "https://res.cloudinary.com/plantas-y-flores-ornamentales/image/upload/v1609868586/FT_RedRubbies/FT6_pcdkas.png",
                "Inventario",
                "Obten una lista actual de las cosechas registradas y su información ingresando a esta ventana",
                R.drawable.ft6
            )
        )

        recyclerView.adapter = CardMenuAdapter(this, listaCardMenu, this)

    }

    override fun onItemClick(texto: String) {
        when {
            texto.equals("Crear Cosecha") -> {
                val intent = (Intent(this, NuevaCosechaHoyActivity::class.java)).putExtra("idFinca", intent.getStringExtra("idFinca"))
                startActivity(intent)
            }
            texto.equals("Cargar Cosechas Rezagadas") -> {
                val intent = (Intent(this, RezagosCosechaDiaActivity::class.java)).putExtra("idFinca", intent.getStringExtra("idFinca"))
                startActivity(intent)
            }
            texto.equals("Inventario") -> {
                val intent = (Intent(this, ListaInventarioActivity::class.java)).putExtra("idFinca", intent.getStringExtra("idFinca"))
                startActivity(intent)
            }
        }

    }
}