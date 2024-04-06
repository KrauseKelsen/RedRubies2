package com.example.redrubies2.businesslayer.libraries

import com.example.redrubies2.datalayer.Despacho
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.*
import com.google.gson.Gson
import okhttp3.*
import org.json.JSONObject

class Resquest() {
    val client = OkHttpClient()
    var url: String = "http://192.168.200.107:80/Applications/Apis/RedRubies/"
    var url2: String = "http://192.168.200.110/Services/RedRubiesWS/"
    val JSON: MediaType? = MediaType.parse("application/json; charset=utf-8")
    val mapper = ObjectMapper()

    fun getRequestEndPoint(endPoint: String): Request {
        val request = Request.Builder()
            .url(url + endPoint)
            .build()

        return request
    }

    fun convertToJson(obj: Any): JSONObject {
        val json:String = Gson().toJson(obj)
        return JSONObject(json)
    }

    fun convertToDespacho(json: String): Despacho {
        return mapper.readValue(json)
    }

    fun postRequestEndPoint(obj: Any, route: String): String {
        val jsonObj = convertToJson(obj)
        val body: RequestBody = RequestBody.create(JSON, jsonObj.toString()) // new

        val ip = if (route=="api/Cosecha") url2 else url
        val request: Request = Request.Builder()
            .url("$ip$route")
            .post(body)
            .build()

        val response: Response = client.newCall(request).execute()
        return response.body().toString()
    }

}