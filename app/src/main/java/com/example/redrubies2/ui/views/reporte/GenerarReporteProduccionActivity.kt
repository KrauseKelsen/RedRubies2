package com.example.redrubies2.ui.views.reporte

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.redrubies2.R
import com.example.redrubies2.businesslayer.libraries.Conectividad
import com.example.redrubies2.businesslayer.repository.FireBaseModel
import com.example.redrubies2.businesslayer.libraries.Resquest
import com.example.redrubies2.datalayer.Comun
import com.example.redrubies2.datalayer.Invernadero
import com.example.redrubies2.datalayer.Variedades
import com.example.redrubies2.ui.views.menus.MenuReporteActivity
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.MetadataChanges
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_generar_reporte_produccion.*
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.*
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.O)
class GenerarReporteProduccionActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    val conectividad: Conectividad = Conectividad()
    private val TAG = "GenerarReporteProduccionActivity"
    val context: Context = this
    val firebaseModel: FireBaseModel = FireBaseModel()
    private val request: Resquest = Resquest()
    var arrayListDetailsComunes: ArrayList<Comun> = ArrayList();
    var arrayListDetailsVariedades: ArrayList<Variedades> = ArrayList();
    var arrayListDetailsInvernaderos: ArrayList<Invernadero> = ArrayList();
    var diasSeleccionados: ArrayList<String> = ArrayList();
    var checkBoxs: ArrayList<String> = ArrayList();

    var sidComun = ""
    var nComun = ""
    var sidInvernadero = ""
    var nInvernadero = ""
    var sidVariedad = ""
    var nVariedad = ""

    private lateinit var str:String
    val myCalendar: Calendar = Calendar.getInstance()

    override fun onBackPressed() {
        Log.d(
            TAG,
            "Finca seleccionada en GenerarReporteProduccionActivity.onBack(): ${
                intent.getStringExtra("idFinca")
            }"
        )
        // INTENT FOR YOUR HOME ACTIVITY
        val intent = (Intent(this, MenuReporteActivity::class.java)).putExtra(
            "idFinca",
            intent.getStringExtra("idFinca")
        )
        startActivity(intent)
    }

    fun initFirebase() {
        firebaseModel.setupCacheSize()
        firebaseModel.enableNetwork()
    }

    //Cuando se cambia de activity esto pasa por debajo (en resumen)
    override fun onResume() {
        super.onResume()
        offline.isVisible = !conectividad.connectedTo(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generar_reporte_produccion)
        AndroidThreeTen.init(application);
        str = LocalDate.now(ZoneId.of("America/Tortola")).toString()
        val dateTimeActual = LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
        val fecha = dateTimeActual.format(formatter)
        validarCheckBoxs(dateTimeActual)
        editTextFecha.text = fecha
        initFirebase()
        initSpinnerComunFromFirestore()
        initSpinnerInvernaderoFromFirestore()

        onClickComun()
        onClickVariedad()
        onClickInvernadero()
        onClickFecha()
        onClickButton()
    }

    private fun onClickButton() {
        generarReporteRendimiento.setOnClickListener {
            if(LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd")).dayOfWeek.ordinal == 6){
                Toast.makeText(
                    applicationContext,
                    "La fecha seleccionada no debe ser domingo",
                    Toast.LENGTH_SHORT
                ).show()
            }else{
                validaCampos()
            }
        }
    }

    private fun validaCampos() {
        try {
            val cantidad = editTextEstandar.text.toString().toDouble()
            if (cantidad <= 0 || cantidad > 9999) {
                Toast.makeText(
                    applicationContext,
                    "La cantidad en kilos debe ser mayor a 0 y menor a 9999",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                if (editTextFecha.text.toString() == "" || sidComun == "" || sidVariedad == "" || sidInvernadero == "" || editTextEstandar.text.toString() == "") {
                    Toast.makeText(
                        applicationContext,
                        "Uno o mas campos están vacíos",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    generarReporte()
                }
            }
        } catch (nfe: NumberFormatException) {
            Toast.makeText(
                applicationContext,
                "Debe ingresar un valor válido",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun onClickFecha() {
        editTextFecha.setOnClickListener {
            val dialog = DatePickerDialog(
                this, R.style.DatePickerDialogTheme, this, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            )

            dialog.show()
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        var smonth = ""
        if ((month + 1) < 10) {
            smonth = "0${month + 1}"
        } else {
            smonth = (month + 1).toString()
        }
        var sday = ""
        if ((dayOfMonth) < 10) {
            sday = "0${dayOfMonth}"
        } else {
            sday = dayOfMonth.toString()
        }

        str = "$year-$smonth-${sday}"
        val dateTimeActual = LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
        val fecha = dateTimeActual.format(formatter)
        validarCheckBoxs(dateTimeActual)
        editTextFecha.setText(fecha)
    }

    private fun validarCheckBoxs(dateTimeActual: LocalDate) {
        val dia = dateTimeActual.dayOfWeek.ordinal
        lunes.isChecked = false
        lunes.isClickable = true
        martes.isChecked = false
        martes.isClickable = true
        miercoles.isChecked = false
        miercoles.isClickable = true
        jueves.isChecked = false
        jueves.isClickable = true
        viernes.isChecked = false
        viernes.isClickable = true
        sabado.isChecked = false
        sabado.isClickable = true
        if(dia == 0) {
            lunes.isChecked = true
            lunes.isClickable = false
        }else if(dia == 1){
            martes.isChecked = true
            martes.isClickable = false
        }else if(dia == 2){
            miercoles.isChecked = true
            miercoles.isClickable = false
        }else if(dia == 3){
            jueves.isChecked = true
            jueves.isClickable = false
        }else if(dia == 4){
            viernes.isChecked = true
            viernes.isClickable = false
        }else if(dia == 5){
            sabado.isChecked = true
            sabado.isClickable = false
        }
    }

    private fun generarReporte() {
        //Este codigo permite cambiar a la vista de menu
        val diasDeCosecha = calcularDiasDeCosecha().toTypedArray()
        Log.d(TAG, "Finca seleccionada: ${intent.getStringExtra("idFinca")}")
        val intent2 = (Intent(this, ReporteProduccionActivity::class.java))
        intent2.putExtra("idFinca", intent.getStringExtra("idFinca"))
        intent2.putExtra("comun", nComun)
        intent2.putExtra("variedad", sidVariedad)
        intent2.putExtra("invernadero", sidInvernadero)
        intent2.putExtra("ninvernadero", nInvernadero)
        intent2.putExtra("nvariedad", nVariedad)
        intent2.putExtra("fecha", str)
        intent2.putExtra("diasDeCosecha", diasDeCosecha)
        intent2.putExtra("estimacion", editTextEstandar.text.toString())
        startActivity(intent2)
    }

    private fun calcularDiasDeCosecha(): ArrayList<String> {
        checkBoxs = ArrayList()
        diasSeleccionados = ArrayList()
        //empezar a crear un array para contar los checks seleccionados para luego filtrar las cosechas de solo esos días marcados
        if(lunes.isChecked)
            checkBoxs.add("0")
        if (martes.isChecked)
            checkBoxs.add("1")
        if (miercoles.isChecked)
            checkBoxs.add("2")
        if (jueves.isChecked)
            checkBoxs.add("3")
        if (viernes.isChecked)
            checkBoxs.add("4")
        if (sabado.isChecked)
            checkBoxs.add("5")

        val diaSemana = LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd")).dayOfWeek.ordinal // SI ES JUEVES DEVUELVE 3 (Monday = 0, Sunday = 6)
        for (check in checkBoxs){
            if (check.toInt() < diaSemana){
                diasSeleccionados.add(LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd")).minusDays(diaSemana.toLong()-check.toLong()).toString())
            }else if (check.toInt() == diaSemana){
                diasSeleccionados.add(LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd")).toString())
            }else if (check.toInt() > diaSemana){
                diasSeleccionados.add(LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd")).plusDays(check.toLong()-diaSemana.toLong()).toString())
            }
        }
        return diasSeleccionados
    }

    private fun initSpinnerComunFromFirestore() {
        firebaseModel.db.collection("Comunes")
            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen error", e)
                    return@addSnapshotListener
                }

                for (change in querySnapshot!!.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        Log.d(TAG, "Comun: ${change.document.data}")
                    }

                    if (querySnapshot.metadata.isFromCache) {
                        Log.d(
                            TAG,
                            "Información obtenida del cache en la vista NuevaCosechaHoyActivity"
                        )
                        val comun = Comun()
                        comun.nombre = change.document["Nombre"].toString()
                        comun.id = change.document.id
                        arrayListDetailsComunes.add(comun)
                        arrayListDetailsComunes.distinct()
                    } else {
                        Log.d(
                            TAG,
                            "Información obtenida del servidor en la vista NuevaCosechaHoyActivity"
                        )
                        val comun = Comun()
                        comun.nombre = change.document["Nombre"].toString()
                        comun.id = change.document.id
                        arrayListDetailsComunes.add(comun)
                        arrayListDetailsComunes.distinct()
                    }
                }
                spinnerComun.adapter = ArrayAdapter(
                    context, android.R.layout.simple_spinner_item, generarValoresStrings(
                        arrayListDetailsComunes,
                        "Comun"
                    )
                )
            }
    }

    private fun onClickComun() {

        spinnerComun.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (arrayListDetailsComunes.size != 0) {
                    sidComun = arrayListDetailsComunes[position].id
                    nComun = arrayListDetailsComunes[position].nombre

                    initSpinnerVariedadesFromFirestore(true)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}

        }


    }

    private fun initSpinnerVariedadesFromFirestore(bandera: Boolean) {
        arrayListDetailsVariedades.clear()
        if (bandera)
            firebaseModel.db.collection("Variedades").whereEqualTo("ComunId", sidComun)
                .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen error", e)
                        return@addSnapshotListener
                    }
                    for (change in querySnapshot!!.documentChanges) {
                        if (change.type == DocumentChange.Type.ADDED) {
                            Log.d(TAG, "Variedadd: ${change.document.data}")
                        }

                        if (querySnapshot.metadata.isFromCache) {
                            Log.d(
                                TAG,
                                "Información obtenida del cache en la vista NuevaCosechaHoyActivity"
                            )
                            val variedades = Variedades()
                            variedades.nombre = change.document["Nombre"].toString()
                            variedades.id = change.document.id
                            variedades.comunId = change.document["ComunId"].toString()
                            arrayListDetailsVariedades.add(variedades)
                            arrayListDetailsVariedades.distinct()
                        } else {
                            Log.d(
                                TAG,
                                "Información obtenida del servidor en la vista NuevaCosechaHoyActivity"
                            )
                            val variedades = Variedades()
                            variedades.nombre = change.document["Nombre"].toString()
                            variedades.id = change.document.id
                            variedades.comunId = change.document["ComunId"].toString()
                            arrayListDetailsVariedades.add(variedades)
                            arrayListDetailsVariedades.distinct()
                        }
                    }
                    if (bandera) {
                        //Se añade esta validacion porque cuando se clickea el spinner vacio, la activity crashea
                        if (arrayListDetailsVariedades.size == 0) {
                            spinnerVariedad.adapter = ArrayAdapter(
                                context,
                                android.R.layout.simple_spinner_item,
                                generarValoresStrings(
                                    arrayListDetailsVariedades,
                                    "Variedad"
                                )
                            )
                            spinnerVariedad.isEnabled = false
                            sidVariedad = ""
                        } else {
                            spinnerVariedad.adapter = ArrayAdapter(
                                context,
                                android.R.layout.simple_spinner_item,
                                generarValoresStrings(
                                    arrayListDetailsVariedades,
                                    "Variedad"
                                )
                            )
                            spinnerVariedad.isEnabled = true
                        }
                    }
                }
    }

    private fun onClickVariedad() {
        spinnerVariedad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sidVariedad = arrayListDetailsVariedades[position].id
                nVariedad = arrayListDetailsVariedades[position].nombre

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
    }

    private fun initSpinnerInvernaderoFromFirestore() {
        firebaseModel.db.collection("Invernaderos")
            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen error", e)
                    return@addSnapshotListener
                }

                for (change in querySnapshot!!.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        Log.d(TAG, "Invernadero: ${change.document.data}")
                    }

                    if (querySnapshot.metadata.isFromCache) {
                        Log.d(
                            TAG,
                            "Información obtenida del cache en la vista NuevaCosechaHoyActivity"
                        )
                        val invernadero = Invernadero()
                        invernadero.descripcion = change.document["Descripcion"].toString()
                        invernadero.fincaId = change.document["FincaId"].toString()
                        invernadero.codigo = change.document["CodigoInvernadero"].toString()
                        invernadero.id = change.document.id
                        arrayListDetailsInvernaderos.add(invernadero)
                        arrayListDetailsInvernaderos.distinct()
                    } else {
                        val invernadero = Invernadero()
                        invernadero.descripcion = change.document["Descripcion"].toString()
                        invernadero.fincaId = change.document["FincaId"].toString()
                        invernadero.codigo = change.document["CodigoInvernadero"].toString()
                        invernadero.id = change.document.id
                        arrayListDetailsInvernaderos.add(invernadero)
                        arrayListDetailsInvernaderos.distinct()
                    }
                }
                spinnerInvernadero.adapter = ArrayAdapter(
                    context, android.R.layout.simple_spinner_item, generarValoresStrings(
                        arrayListDetailsInvernaderos,
                        "Invernadero"
                    )
                )
            }
    }

    private fun onClickInvernadero() {
        spinnerInvernadero.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sidInvernadero = arrayListDetailsInvernaderos[position].codigo
                nInvernadero = arrayListDetailsInvernaderos[position].descripcion
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
    }

    fun generarValoresStrings(array: ArrayList<*>, objeto: String): ArrayList<String> {
        val valores: ArrayList<String> = ArrayList(array.size)

        for (obj in array) {
            when (objeto) {
                "Comun" -> valores.add((obj as Comun).nombre)
                "Invernadero" -> valores.add((obj as Invernadero).descripcion)
                "Variedad" -> valores.add((obj as Variedades).nombre)
            }
        }
        return valores
    }
}