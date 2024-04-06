package com.example.redrubies2.businesslayer.libraries

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    /**
     * Muestra las cartas de diferentes colores mientras son cargadas
     */
    var vibrantLightColorList = arrayOf(
        ColorDrawable(Color.parseColor("#ffeead")),
        ColorDrawable(Color.parseColor("#93cfb3")),
        ColorDrawable(Color.parseColor("#fd7a7a")),
        ColorDrawable(Color.parseColor("#faca5f")),
        ColorDrawable(Color.parseColor("#1ba798")),
        ColorDrawable(Color.parseColor("#6aa9ae")),
        ColorDrawable(Color.parseColor("#ffbf27")),
        ColorDrawable(Color.parseColor("#d93947"))
    )
    val randomDrawbleColor: ColorDrawable
        get() {
            val idx = Random().nextInt(vibrantLightColorList.size)
            return vibrantLightColorList[idx]
        }

    /**
     * Formatea la fecha para las cartas
     * @param oldstringDate
     * @return
     */
    fun DateFormat(oldstringDate: String?): String? {
        val newDate: String?
        val dateFormat = SimpleDateFormat("E, d MMM yyyy", Locale(country))
        newDate = try {
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(oldstringDate)
            dateFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            oldstringDate
        }
        return newDate
    }

    /**
     * Obtiene el país de la localización
     * @return
     */
    val country: String
        get() {
            val locale = Locale.getDefault()
            val country = locale.country
            return country.toLowerCase()
        }

    /**
     * Esta función permite saber si un String contiene caracteres del otro
     * independientemente de sus mayusculas y minisculas
     * @param src
     * @param what
     * @return
     */
    fun containsIgnoreCase(src: String, what: String): Boolean {
        val length = what.length
        if (length == 0) return true
        val firstLo = Character.toLowerCase(what[0])
        val firstUp = Character.toUpperCase(what[0])
        for (i in src.length - length downTo 0) {
            val ch = src[i]
            if (ch != firstLo && ch != firstUp) continue
            if (src.regionMatches(i, what, 0, length, ignoreCase = true)) return true
        }
        return false
    }
}