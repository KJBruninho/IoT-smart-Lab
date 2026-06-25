package com.example.myapplication.data.api

import com.example.myapplication.data.dto.AlertaSensorResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AlertasApi {

    @GET("api/alertas")
    suspend fun listar(): List<AlertaSensorResponse>

    @POST("api/alertas/{id}/marcar-lido")
    suspend fun marcarLido(
        @Path("id") id: Long
    ): Response<ResponseBody>

    @POST("api/alertas/{id}/resolver")
    suspend fun resolver(
        @Path("id") id: Long
    ): Response<ResponseBody>

    @POST("api/alertas/{id}/ignorar")
    suspend fun ignorar(
        @Path("id") id: Long
    ): Response<ResponseBody>
}