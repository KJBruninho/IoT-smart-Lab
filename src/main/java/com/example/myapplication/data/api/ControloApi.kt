package com.example.myapplication.data.api

import com.example.myapplication.data.dto.PedidoComandoRequest
import com.example.myapplication.data.dto.PedidoComandoResponse
import com.example.myapplication.data.dto.SensorDisponivelResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ControloApi {

    @GET("api/pedidos-comando/sensores-disponiveis")
    suspend fun sensoresDisponiveis(): List<SensorDisponivelResponse>

    @POST("api/pedidos-comando")
    suspend fun criarPedido(
        @Body request: PedidoComandoRequest
    ): PedidoComandoResponse
}