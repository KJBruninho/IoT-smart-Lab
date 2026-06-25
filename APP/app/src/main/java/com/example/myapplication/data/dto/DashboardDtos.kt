package com.example.myapplication.data.dto

data class DashboardEstadoResponse(
    val mqttOnline: Boolean,
    val fonte: String,
    val leiturasPendentesCache: Long,
    val leituras: List<UltimaLeituraResponse>
)

data class UltimaLeituraResponse(
    val tipo: String,
    val valor: Double,
    val unidade: String?,
    val dataRegisto: String?
)

data class GraficoLeituraResponse(
    val hora: String,
    val valor: Double
)