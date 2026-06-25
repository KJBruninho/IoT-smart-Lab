package com.example.myapplication.data.repository

import com.example.myapplication.data.api.DashboardApi
import com.example.myapplication.data.dto.DashboardEstadoResponse
import retrofit2.HttpException
import java.io.IOException

class DashboardRepository(
    private val dashboardApi: DashboardApi
) {
    suspend fun estadoDashboard(): Result<DashboardEstadoResponse> {
        return try {
            Result.success(dashboardApi.estadoDashboard())
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP ${e.code()} - ${e.message()}"))
        } catch (e: IOException) {
            Result.failure(Exception("Erro de ligação: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Erro inesperado: ${e.message}"))
        }
    }
}