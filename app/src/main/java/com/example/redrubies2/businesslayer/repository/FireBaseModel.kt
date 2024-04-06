package com.example.redrubies2.businesslayer.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.redrubies2.datalayer.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import java.text.SimpleDateFormat
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

class FireBaseModel (){
    //Firestore
    val db = FirebaseFirestore.getInstance()
    private val TAG = "FireBaseModel"
    //Iniciar el Firebase, construirlo, habilitar la persistencia y definir el tamaño del caché (ilimitado)
    fun setupCacheSize() {
        // [START fs_setup_cache]
        val settings = FirebaseFirestoreSettings.Builder()
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
        // [END fs_setup_cache]
    }

    //Deshabilita la conexion con firebase
    fun disableNetwork(){
        db.disableNetwork().addOnCompleteListener {

        }
    }

    //Deshabilita la conexion con firebase
    fun enableNetwork(){
        db.enableNetwork().addOnCompleteListener {

        }
    }

    fun addDocumentFinca(arrayListDetails: ArrayList<Finca>) {
        // [START add_document]
        for (finca: Finca in arrayListDetails){
            val data = hashMapOf(
                "Nombre" to finca.nombre,
                "Imagen" to finca.imagen,
                "Codigo" to finca.codigo,
                "Estado" to finca.estado,
                "EnProduccion" to finca.enProduccion
            )

            db.collection("Fincas").document(finca.id)
                .set(data).addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot Finca written with ID: ${finca.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
            // [END add_document]
        }

    }

    fun addDocumentComunes(arrayListDetailsComunes: ArrayList<Comun>) {
        for (comun: Comun in arrayListDetailsComunes){
            var estandar = 0.0
            if (comun.nombre.equals("FRESA") || comun.nombre.equals("UCHUVA")){
                estandar = 18.0
            }else if (comun.nombre.equals("MORA")){
                estandar = 3.0

            }else{
                estandar = 3.5
            }
            val data = hashMapOf(
                "Nombre" to comun.nombre,
                "EstandarHora" to estandar
            )

            db.collection("Comunes").document(comun.id)
                .set(data).addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot Comunes written with ID: ${comun.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
            // [END add_document]
        }
    }

    fun addDocumentTrabajadores(arrayListDetailsTrabajadores: ArrayList<Trabajador>) {
        for (trabajador: Trabajador in arrayListDetailsTrabajadores){
            val data = hashMapOf(
                "PrimerNombre" to trabajador.primerNombre,
                "PrimerApellido" to trabajador.primerApellido
            )

            db.collection("Trabajadores").document(trabajador.codigo)
                .set(data).addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot Trabajador written with ID: ${trabajador.codigo}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
            // [END add_document]
        }
    }

    fun addDocumentVariedades(arrayListDetailsVaridades: ArrayList<Variedades>) {
        for (variedades: Variedades in arrayListDetailsVaridades){
            val data = hashMapOf(
                "Nombre" to variedades.nombre,
                "ComunId" to variedades.comunId,
                "Id" to variedades.id
            )

            db.collection("Variedades").document(variedades.id)
                .set(data).addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot Variedad written with ID: ${variedades.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
            // [END add_document]
        }
    }

    fun addDocumentInvernadero(arrayListDetailsInvernadero: ArrayList<Invernadero>) {
        for (invernadero: Invernadero in arrayListDetailsInvernadero){
            val data = hashMapOf(
                "Descripcion" to invernadero.descripcion,
                "FincaId" to invernadero.fincaId,
                "Id" to invernadero.id,
                "CodigoInvernadero" to invernadero.codigo
            )

            db.collection("Invernaderos").document(invernadero.id)
                .set(data).addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot Invernadero written with ID: ${invernadero.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
            // [END add_document]
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addDocumentInventario(arrayListDetailsInventario: ArrayList<ListaInventario>) {

        for (inventario: ListaInventario in arrayListDetailsInventario){
            val dateTime = LocalDateTime.parse(inventario.hora, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
            val date = dateTime.toLocalDate()
            var shour = ""
            if (dateTime.hour < 10) {
                shour = "0${dateTime.hour}"
            } else {
                shour = "${dateTime.hour}"
            }
            var sminutes = ""
            if (dateTime.minute < 10) {
                sminutes = "0${dateTime.minute}"
            } else {
                sminutes = "${dateTime.minute}"
            }
            val data = hashMapOf(
                "Cantidad" to inventario.cantidad,
                "Comun" to inventario.comun,
                "Trabajador" to inventario.ntrabajador,
                "CodigoTrabajador" to inventario.trabajador,
                "Hora" to "${shour}:${sminutes}",
                "Invernadero" to inventario.invernadero,
                "Variedad" to inventario.variedad,
                "Fecha" to date.toString()
            )

            db.collection("InventarioCosechaDia").document(inventario.id)
                .set(data).addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot Inventario written with ID: ${inventario.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
            // [END add_document]
        }
    }

    fun addDocumentInventarioRezagos(inventario: ListaInventario) {
        val data = hashMapOf(
            "Cantidad" to inventario.cantidad,
            "Comun" to inventario.comun,
            "Trabajador" to inventario.trabajador,
            "Invernadero" to inventario.invernadero,
            "Variedad" to inventario.variedad,
            "Subido" to inventario.subido,
            "NComun" to inventario.ncomun,
            "NTrabajador" to inventario.ntrabajador,
            "Fecha" to inventario.fecha
        )

        db.collection("InventarioCosechaDiaRezagos")
            .add(data).addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot Inventario Rezagado written without ID")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
        // [END add_document]
    }

    fun updateDocumentRezagoCosecha(obj: ListaInventario){
        val ref = db.collection("InventarioCosechaDiaRezagos").document(obj.id)

        // Set the "isCapital" field of the city 'DC'
        ref
            .update("Subido", "CARGADO")
            .addOnSuccessListener { Log.d(
                TAG,
                "DocumentSnapshot Rezago Cosecha successfully updated !"
            ) }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
        // [END update_document]
    }

    fun addDocumentDespacho(arrayListDetailsDespacho: ArrayList<Despacho>) {
        for (despacho: Despacho in arrayListDetailsDespacho){
            val data = hashMapOf(
                "Cantidad" to despacho.cantidad,
                "Id" to despacho.idDespacho,
                "Entera" to despacho.sentera,
                "Pedido" to despacho.pedido,
                "Oferta" to despacho.oferta,
                "Empacador" to despacho.empacador,
                "Finca" to despacho.finca,
                "Nombre" to despacho.presentacion,
                "idTrabajador" to despacho.idTrabajador,
                "Fecha" to SimpleDateFormat("yyyy-MM-dd").format(Date())
            )

            db.collection("Despachos").document(despacho.idDespacho)
                .set(data).addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot Despacho written with ID: ${despacho.idDespacho}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
            // [END add_document]
        }
    }

    fun addDocumentClientes(arrayListDetailsClientes: ArrayList<Cliente>) {
        for (cliente: Cliente in arrayListDetailsClientes){
            val data = hashMapOf(
                "Nombre" to cliente.nombre
            )

            db.collection("Clientes").document(cliente.codigo)
                .set(data).addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot Cliente written with ID: ${cliente.codigo}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
            // [END add_document]
        }
    }

    fun addDocumentOfertas(arrayListDetailsOfertas: ArrayList<Oferta>) {
        for (oferta: Oferta in arrayListDetailsOfertas){
            val data = hashMapOf(
                "Descripcion" to oferta.descripcion,
                "Presentaciones" to oferta.presentaciones,
                "IdCliente" to oferta.idCliente
            )

            db.collection("Presentaciones").document(oferta.id.toString())
                .set(data).addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot Presentacion written with ID: ${oferta.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
            // [END add_document]
        }
    }

    fun addDocumentDespachoRezagos(despacho: Despacho) {
        val data = hashMapOf(
            "Cantidad" to despacho.cantidad,
            "Empacador" to despacho.empacador,
            "IdTrabajador" to despacho.idTrabajador,
            "Oferta" to despacho.oferta,
            "Pedido" to despacho.pedido,
            "Finca" to despacho.finca,
            "Entera" to despacho.entera,
            "Nombre" to despacho.presentacion,
            "Subido" to despacho.subido,
            "Fecha" to despacho.fecha
        )

        db.collection("DespachoRezagos")
            .add(data).addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot Despacho Rezagado written without ID")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
        // [END add_document]
    }

    fun updateDocumentRezagoDespacho(obj: Despacho) {
        val ref = db.collection("DespachoRezagos").document(obj.idDespacho)

        // Set the "isCapital" field of the city 'DC'
        ref
            .update("Subido", "CARGADO")
            .addOnSuccessListener { Log.d(
                TAG,
                "DocumentSnapshot Rezago Despacho successfully updated !"
            ) }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
        // [END update_document]
    }

    fun addDocumentEliminarIngresos(despacho: Despacho) {
        despacho.subido = "NO ELIMINADO"
        val data = hashMapOf(
            "Empacador" to despacho.empacador,
            "Entera" to despacho.sentera,
            "Nombre" to despacho.presentacion,
            "Subido" to despacho.subido
        )

        db.collection("EliminarIngresoRezagos").document(despacho.idDespacho)
            .set(data).addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot Eliminar Ingreso Rezagado written without ID")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
        // [END add_document]
    }

    fun addDocumentEliminarInventario(inventario: ListaInventario) {
//        despacho.subido = "NO ELIMINADO"
//        val data = hashMapOf(
//            "Empacador" to despacho.empacador,
//            "Entera" to despacho.sentera,
//            "Nombre" to despacho.presentacion,
//            "Subido" to despacho.subido
//        )
//
//        db.collection("EliminarIngresoRezagos").document(despacho.idDespacho)
//            .set(data).addOnSuccessListener {
//                Log.d(TAG, "DocumentSnapshot Eliminar Ingreso Rezagado written without ID")
//            }
//            .addOnFailureListener { e ->
//                Log.w(TAG, "Error adding document", e)
//            }
        // [END add_document]
    }

    fun deleteDocumentRezagoDespacho(idDespacho: String) {
        val ref = db.collection("EliminarIngresoRezagos").document(idDespacho)

        // Set the "isCapital" field of the city 'DC'
        ref
            .update("Subido", "ELIMINADO")
            .addOnSuccessListener { Log.d(
                TAG,
                "DocumentSnapshot Despacho Eliminado successfully updated !"
            ) }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
        // [END update_document]

        db.collection("Despachos").document(idDespacho)
            .delete()
            .addOnSuccessListener { Log.d(
                TAG,
                "DocumentSnapshot $idDespacho eliminado exitosamente "
            ) }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
        // [END delete_document]
    }

    fun deleteDocumentRezagoInventario(idInventario: String) {
        val ref = db.collection("EliminarInventarioRezagado").document(idInventario)

        // Set the "isCapital" field of the city 'DC'
        ref
            .update("Subido", "ELIMINADO")
            .addOnSuccessListener { Log.d(
                TAG,
                "DocumentSnapshot Inventario Eliminado successfully updated !"
            ) }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
        // [END update_document]

        db.collection("Inventario").document(idInventario)
            .delete()
            .addOnSuccessListener { Log.d(
                TAG,
                "DocumentSnapshot $idInventario eliminado exitosamente "
            ) }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
        // [END delete_document]
    }
}