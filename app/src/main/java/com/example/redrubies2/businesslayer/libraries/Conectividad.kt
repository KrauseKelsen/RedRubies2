package com.example.redrubies2.businesslayer.libraries

import android.content.Context
import android.util.Log
import java.io.IOException
import java.net.*
import java.util.*


class Conectividad (){
    private val TAG = "Conectividad"

    //fun verificarConectividad(context: Context): Boolean = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo?.isConnectedOrConnecting == true

    fun connectedTo(context: Context): Boolean {
        var network = false
        val thread = Thread {
            try {
                network = if (verificarConectividad(context)) {
                    Log.d(TAG, "Conexión segura")
                    true
                } else {
                    Log.d(TAG, "Conexión insegura")
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        thread.start()
        //une hilos, es decir, se espera a que se este hilo se termine para seguir ejecutando otra cosa
        thread.join()
        return network
    }

    fun verificarConectividad(context: Context): Boolean {
        return try {
            val timeoutMs = 500
            val sock = Socket()
            val sockaddr: SocketAddress = InetSocketAddress("192.168.200.107", 80)
            sock.connect(sockaddr, timeoutMs)
            sock.close()
            true
        } catch (e: IOException) {
            false
        }
    }
    // esta validación permite conocer si hay o no conexión a internet (a cualquier red)
    //(context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo?.isConnectedOrConnecting == true

    //se envia "wlan" por parametro si es por wifi y sirve para conocer la red a la que se esta conectado
    //ref: https://es.stackoverflow.com/questions/67404/obtener-direcci%C3%B3n-ip-interna-de-la-conexi%C3%B3n-actual-en-android
    fun getIPAddressIPv4(id: String?): String? {
        try {
            val interfaces: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (intf.name.contains(id!!)) {
                    val addrs: List<InetAddress> = Collections.list(intf.inetAddresses)
                    for (addr in addrs) {
                        if (!addr.isLoopbackAddress) {
                            val sAddr = addr.hostAddress
                            if (addr is Inet4Address) {
                                return sAddr
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    // permite saber si hay conexión a una red en especifico (uno especifica la ip, en este caso verificamos si hay conexión al webservice)
//    fun isOnline(): Boolean {
//        val runtime = Runtime.getRuntime()
//        try {
//            val ipProcess = runtime.exec("/system/bin/ping -c 1 192.168.200.107")
//            val exitValue = ipProcess.waitFor()
//            return exitValue == 0
//        } catch (e: IOException) {
//            e.printStackTrace()
//        } catch (e: InterruptedException) {
//            e.printStackTrace()
//        }
//        return false
//    }

//    fun isOnline(): Boolean {
//        return try {
//            val timeoutMs = 1500
//            val sock = Socket()
//            val sockaddr: SocketAddress = InetSocketAddress("192.168.200.107", 80)
//            sock.connect(sockaddr, timeoutMs)
//            sock.close()
//            true
//        } catch (e: IOException) {
//            false
//        }
//    }
}