package com.example.redrubies2.businesslayer.libraries.PDF

import com.itextpdf.text.*
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.draw.VerticalPositionMark

class Designer (){
    private val mDoc = Document()
    private var file_name = ""

    //Colores de las letras
    private val colorRedRubbies = BaseColor(248, 95, 106) // Rosa
    private val rojo = BaseColor(255, 20, 20) // Alert
    private val verde = BaseColor(0, 200, 81) // Success

    //Varieble para el tipo de letra principal
    private val fontName = BaseFont.createFont("assets/fonts/brandon_medium.otf", "UTF-8", BaseFont.EMBEDDED)

    private val subheadingStyle = Font(fontName, 15.0f, Font.NORMAL, colorRedRubbies) // sub titulos
    private val totalRojo = Font(fontName, 12.0f, Font.NORMAL, rojo) // totales
    private val totalVerde = Font(fontName, 12.0f, Font.NORMAL, verde) // totales
    private val valueStyle = Font(fontName, 10.0f, Font.NORMAL, BaseColor.BLACK) // letra normal

    fun getDocument() : Document {
        return mDoc
    }
    fun getFileName() : String {
        return file_name
    }

    fun setFileName(file:String){
        file_name = file
    }

    //Esta función crea un titulo central (principal o secundario) (Ej. Cosechas del Lunes)
    //Por lo tanto recibe el tamaño por parametro y el contenido por parametro
    fun crearTitulo(size : Float, contenido: String) {
        addNewItem(contenido, Element.ALIGN_CENTER, Font(fontName, size, Font.NORMAL, colorRedRubbies))
    }

    fun crearTitulo(contenido: String, aprueba: Boolean){
        if (aprueba){
            addNewItem(contenido, Element.ALIGN_CENTER, totalVerde)
        }else{
            addNewItem(contenido, Element.ALIGN_CENTER, totalRojo)
        }
    }

    //Esta función crea un encabezado que posee un subtitulo y su contenido (Ej. Variedad: Arandano 1)
    fun crearEncabezado(subTitulo : String, contenido: String) {
        addNewItem(subTitulo, Element.ALIGN_LEFT, subheadingStyle)

        addNewItem( contenido, Element.ALIGN_LEFT, valueStyle)
    }

    @Throws(DocumentException::class)
    private fun addNewItem(text: String, alignCenter: Int, style: Font) {
        val chunk   = Chunk(text, style)
        val p = Paragraph(chunk)
        p.alignment = alignCenter
        mDoc.add(p)
    }

    //Esta función crea grupos de información (para informacion y para totales) Ej.
    //Trabajador: 1sad      Cantidad: 0,34kg
    @Throws(DocumentException::class)
    fun crearGrupo(tituloIzq: String, contenidoIzq: String, tituloDer: String, contenidoDer: String) {
        val chunkTextLeft = Chunk("$tituloIzq $contenidoIzq", valueStyle)
        val chunkTextRight = Chunk("$tituloDer $contenidoDer", valueStyle)
        val p = Paragraph(chunkTextLeft)
        p.add(Chunk(VerticalPositionMark()))
        p.add(chunkTextRight)
        mDoc.add(p)
    }

    @Throws(DocumentException::class)
    fun crearGrupo(tituloIzq: String, contenidoIzq: String, tituloDer: String, contenidoDer: String, fontIzq : Font, fontDer : Font) {
        val chunkTextLeft = Chunk("$tituloIzq $contenidoIzq", fontIzq)
        val chunkTextRight = Chunk("$tituloDer $contenidoDer", fontDer)
        val p = Paragraph(chunkTextLeft)
        p.add(Chunk(VerticalPositionMark()))
        p.add(chunkTextRight)
        mDoc.add(p)
    }

    @Throws(DocumentException::class)
    fun crearGrupo(tituloIzq: String, contenidoDer: String, style: Boolean) {
        val font = if (style){
            totalVerde
        }else{
            totalRojo
        }
        val chunkTextLeft = Chunk(tituloIzq, font)
        val chunkTextRight = Chunk(contenidoDer, font)
        val p = Paragraph(chunkTextLeft)
        p.add(Chunk(VerticalPositionMark()))
        p.add(chunkTextRight)
        mDoc.add(p)
    }

    fun espacio(){
        crearGrupo("", "", "", "")
    }
//
//    private fun addLineSeparator(mDoc: Document) {
//        val lineSeparator = LineSeparator()
//        lineSeparator.lineColor = BaseColor(0,0,0,68)
//        addLineSpace(mDoc)
//        //mDoc.add(lineSeparator)
//    }
//
//    private fun addLineSpace(mDoc: Document) {
//        mDoc.add(Paragraph(""))
//    }

}