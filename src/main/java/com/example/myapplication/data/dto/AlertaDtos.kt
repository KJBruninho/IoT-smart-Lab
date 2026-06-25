package com.example.myapplication.data.dto

data class AlertaSensorResponse(
    val id: Long,
    val tipoSensor: String,
    val valorLido: Double,
    val valorMin: Double?,
    val valorMax: Double?,
    val titulo: String,
    val mensagem: String?,
    val severidade: String,
    val estado: String,
    val criadoEm: String?,
    val lidoEm: String?,
    val resolvidoEm: String?
)