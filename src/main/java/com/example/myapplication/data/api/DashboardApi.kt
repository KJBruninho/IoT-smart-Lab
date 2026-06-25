package com.example.myapplication.data.api

import com.example.myapplication.data.dto.DashboardEstadoResponse
import com.example.myapplication.data.dto.GraficoLeituraResponse
import retrofit2.http.GET

interface DashboardApi {

    @GET("api/dashboard/estado")
    suspend fun estadoDashboard(): DashboardEstadoResponse

    @GET("api/temperatura")
    suspend fun temperatura(): List<GraficoLeituraResponse>

    @GET("api/tds")
    suspend fun tds(): List<GraficoLeituraResponse>
}