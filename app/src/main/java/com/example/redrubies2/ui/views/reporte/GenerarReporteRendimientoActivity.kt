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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.redrubies2.R
import com.example.redrubies2.businesslayer.libraries.Conectividad
import com.example.redrubies2.businesslayer.repository.FireBaseModel
import com.example.redrubies2.businesslayer.libraries.Resquest
import com.example.redrubies2.datalayer.Jornada
import com.example.redrubies2.datalayer.Trabajador
import com.example.redrubies2.ui.views.menus.MenuReporteActivity
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.MetadataChanges
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_generar_reporte_produccion.*
import kotlinx.android.synthetic.main.activity_generar_reporte_rendimiento.*
import kotlinx.android.synthetic.main.activity_generar_reporte_rendimiento.editTextFecha
import kotlinx.android.synthetic.main.activity_generar_reporte_rendimiento.generarReporteRendimiento
import kotlinx.android.synthetic.main.activity_generar_reporte_rendimiento.jueves
import kotlinx.android.synthetic.main.activity_generar_reporte_rendimiento.lunes
import kotlinx.android.synthetic.main.activity_generar_reporte_rendimiento.martes
import kotlinx.android.synthetic.main.activity_generar_reporte_rendimiento.miercoles
import kotlinx.android.synthetic.main.activity_generar_reporte_rendimiento.offline
import kotlinx.android.synthetic.main.activity_generar_reporte_rendimiento.sabado
import kotlinx.android.synthetic.main.activity_generar_reporte_rendimiento.viernes
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class GenerarReporteRendimientoActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    val conectividad : Conectividad = Conectividad()
    private val TAG = "GenerarReporteRendimientoActivity"

    val context : Context = this

    val firebaseModel : FireBaseModel = FireBaseModel()
    val myCalendar: Calendar = Calendar.getInstance()
    private val request: Resquest = Resquest()
    var arrayListDetailsTrabajadores: ArrayList<Trabajador> = ArrayList();
    var nTrabajador: String = ""
    var sidTrabajador: String = ""
    var diasSeleccionados: ArrayList<String> = ArrayList();
    var checkBoxs: ArrayList<String> = ArrayList();
    private lateinit var str: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generar_reporte_rendimiento)
        AndroidThreeTen.init(application);
        str = LocalDate.now(ZoneId.of("America/Tortola")).toString()
        val dateTimeActual = LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
        val fecha = dateTimeActual.format(formatter)
        validarCheckBoxs(dateTimeActual)
        editTextFecha.setText(fecha)
        initFirebase()

        initSpinnerTrabajadorFromFirestore()

        onClickTrabajador()
        onClickFecha()
        onClickGenerarReporte()
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onClickGenerarReporte() {
        generarReporteRendimiento.setOnClickListener {
            if(sidTrabajador == "" ){
                Log.d(TAG,"Uno o más campos están vacíos, verifique los campos")
            }else{
                generarReporte()
            }
        }
    }

    private fun generarReporte() {
        val diasDeCosecha = calcularDiasDeCosecha().toTypedArray()
        Log.d(TAG, "Finca seleccionada: ${intent.getStringExtra("idFinca")}")
        val intent2 = (Intent(this, ReporteRendimientoActivity::class.java))
        intent2.putExtra("idFinca", intent.getStringExtra("idFinca"))
        intent2.putExtra("idTrabajador", sidTrabajador)
        intent2.putExtra("nTrabajador", nTrabajador)
        intent2.putExtra("Fecha", str )
        intent2.putExtra("diasDeCosecha", diasDeCosecha)
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


    private fun initSpinnerTrabajadorFromFirestore() {
        firebaseModel.db.collection("Trabajadores").orderBy("PrimerNombre")
            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen error", e)
                    return@addSnapshotListener
                }

                for (change in querySnapshot!!.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        Log.d(TAG, "Trabajador: ${change.document.data}")
                    }

                    if (querySnapshot.metadata.isFromCache) {
                        Log.d(
                            TAG,
                            "Información obtenida del cache en la vista GenerarReporteRendimientoActivity"
                        )
                        val trabajador = Trabajador()
                        trabajador.primerNombre = change.document["PrimerNombre"].toString()
                        trabajador.primerApellido = change.document["PrimerApellido"].toString()
                        trabajador.codigo = change.document.id
                        arrayListDetailsTrabajadores.add(trabajador)
                        arrayListDetailsTrabajadores.distinct()
                    } else {
                        Log.d(
                            TAG,
                            "Información obtenida del servidor en la vista GenerarReporteRendimientoActivity"
                        )
                        val trabajador = Trabajador()
                        trabajador.primerNombre = change.document["PrimerNombre"].toString()
                        trabajador.primerApellido = change.document["PrimerApellido"].toString()
                        trabajador.codigo = change.document.id
                        arrayListDetailsTrabajadores.add(trabajador)
                        arrayListDetailsTrabajadores.distinct()
                    }
                }
                spinnerTrabajador.adapter = ArrayAdapter(
                    context, android.R.layout.simple_spinner_item, generarValoresStrings(
                        arrayListDetailsTrabajadores,
                        "Trabajador"
                    )
                )
            }
    }

    private fun onClickTrabajador() {
        spinnerTrabajador.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sidTrabajador = arrayListDetailsTrabajadores[position].codigo
                nTrabajador = "${arrayListDetailsTrabajadores[position].primerNombre} ${arrayListDetailsTrabajadores[position].primerApellido}"

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    fun generarValoresStrings(array: ArrayList<*>, objeto: String): ArrayList<String> {
        val valores: ArrayList<String> = ArrayList(array.size)

        for (obj in array) {
            when (objeto) {
                "Jornada" -> valores.add((obj as Jornada).descripcion)
                "Trabajador" -> valores.add("${(obj as Trabajador).primerNombre} ${obj.primerApellido}")
            }
        }
        return valores
    }

    override fun onBackPressed() {
        // INTENT FOR YOUR HOME ACTIVITY
        Log.d(
            TAG, "Finca seleccionada en GenerarReporteRendimientoActivity.onBack(): ${
                intent.getStringExtra(
                    "idFinca"
                )
            }"
        )

        val intent = (Intent(this, MenuReporteActivity::class.java)).putExtra(
            "idFinca", intent.getStringExtra(
                "idFinca"
            )
        )
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


    fun validarFecha(year: Int, month: Int, dayOfMonth: Int, separador: String) : String {
        var smonth1 = ""
        if((month+1) < 10){
            smonth1 = "0${month+1}"
        }else{
            smonth1 = (month+1).toString()
        }
        var sday1 = ""
        if((dayOfMonth) < 10){
            sday1 = "0${dayOfMonth}"
        }else{
            sday1 = dayOfMonth.toString()
        }

        if(separador == "-"){
            return "$year-${smonth1}-${sday1}"
        }
        return "$year/${smonth1}/${sday1}"
    }
}