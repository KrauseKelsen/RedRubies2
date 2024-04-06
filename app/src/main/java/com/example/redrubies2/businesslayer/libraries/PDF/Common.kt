package com.example.redrubies2.businesslayer.libraries.PDF

import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import java.io.File

object Common {
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun getAppPath(context: Context): String{
        var dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString())
            dir.mkdir()
        return dir.path+File.separator
    }
}