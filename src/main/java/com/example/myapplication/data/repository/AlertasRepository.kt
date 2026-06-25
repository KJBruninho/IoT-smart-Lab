package com.example.myapplication.data.repository

import com.example.myapplication.data.api.AlertasApi
import com.example.myapplication.data.dto.AlertaSensorResponse
import retrofit2.HttpException
import java.io.IOException

class AlertasRepository(
    private val alertasApi: AlertasApi
) {
    suspend fun listar(): Result<List<AlertaSensorResponse>> {
        return try {
            Result.success(alertasApi.listar())
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            Result.failure(Exception("Erro de ligação: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Erro inesperado: ${e.message}"))
        }
    }

    suspend fun marcarLido(id: Long): Result<Unit> {
        return executarAcao { alertasApi.marcarLido(id) }
    }

    suspend fun resolver(id: Long): Result<Unit> {
        return executarAcao { alertasApi.resolver(id) }
    }

    suspend fun ignorar(id: Long): Result<Unit> {
        return executarAcao { alertasApi.ignorar(id) }
    }

    private suspend fun executarAcao(
        acao: suspend () -> retrofit2.Response<okhttp3.ResponseBody>
    ): Result<Unit> {
        return try {
            val response = acao()

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("HTTP ${response.code()} - ${response.message()}"))
            }
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            Result.failure(Exception("Erro de ligação: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Erro inesperado: ${e.message}"))
        }
    }
}