package com.example.myapplication.data.dto

data class SensorDisponivelResponse(
    val id: Long,
    val nome: String,
    val tipo: String,
    val estacao: String?
)

data class PedidoComandoRequest(
    val sensorId: Long,
    val comando: String,
    val valor: Double? = null
)

data class PedidoComandoResponse(
    val id: Long?,
    val estado: String,
    val mensagem: String
)