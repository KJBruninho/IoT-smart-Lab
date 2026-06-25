package com.example.myapplication.ui.sensors

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.DashboardEstadoResponse
import com.example.myapplication.data.repository.DashboardRepository
import kotlinx.coroutines.launch

data class SensorsUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val estado: DashboardEstadoResponse? = null
)

class SensorsViewModel(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    val uiState = mutableStateOf(SensorsUiState())

    fun carregar() {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(
                loading = true,
                error = null
            )

            val result = dashboardRepository.estadoDashboard()

            uiState.value = if (result.isSuccess) {
                SensorsUiState(
                    loading = false,
                    error = null,
                    estado = result.getOrNull()
                )
            } else {
                SensorsUiState(
                    loading = false,
                    error = result.exceptionOrNull()?.message ?: "Erro ao carregar sensores.",
                    estado = null
                )
            }
        }
    }
}