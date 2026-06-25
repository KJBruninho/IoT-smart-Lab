package com.example.myapplication.data.repository

import com.example.myapplication.data.api.ControloApi
import com.example.myapplication.data.dto.PedidoComandoRequest
import com.example.myapplication.data.dto.PedidoComandoResponse
import com.example.myapplication.data.dto.SensorDisponivelResponse
import retrofit2.HttpException
import java.io.IOException

class ControloRepository(
    private val controloApi: ControloApi
) {
    suspend fun sensoresDisponiveis(): Result<List<SensorDisponivelResponse>> {
        return try {
            Result.success(controloApi.sensoresDisponiveis())
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            Result.failure(Exception("Erro de ligação: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Erro inesperado: ${e.message}"))
        }
    }

    suspend fun criarPedido(
        sensorId: Long,
        comando: String,
        valor: Double? = null
    ): Result<PedidoComandoResponse> {
        return try {
            val response = controloApi.criarPedido(
                PedidoComandoRequest(
                    sensorId = sensorId,
                    comando = comando,
                    valor = valor
                )
            )

            Result.success(response)
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            Result.failure(Exception("Erro de ligação: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Erro inesperado: ${e.message}"))
        }
    }
}