package com.example.myapplication.ui.dashboard

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.AlertaSensorResponse
import com.example.myapplication.data.repository.AlertasRepository
import kotlinx.coroutines.launch

data class AlertasUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val alertas: List<AlertaSensorResponse> = emptyList()
)

class AlertasViewModel(
    private val alertasRepository: AlertasRepository
) : ViewModel() {

    val uiState = mutableStateOf(AlertasUiState())

    fun carregar() {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(
                loading = true,
                error = null
            )

            val result = alertasRepository.listar()

            uiState.value = if (result.isSuccess) {
                AlertasUiState(
                    loading = false,
                    alertas = result.getOrNull().orEmpty()
                )
            } else {
                AlertasUiState(
                    loading = false,
                    error = result.exceptionOrNull()?.message ?: "Erro ao carregar alertas."
                )
            }
        }
    }

    fun marcarLido(id: Long) {
        executarEAtualizar {
            alertasRepository.marcarLido(id)
        }
    }

    fun resolver(id: Long) {
        executarEAtualizar {
            alertasRepository.resolver(id)
        }
    }

    fun ignorar(id: Long) {
        executarEAtualizar {
            alertasRepository.ignorar(id)
        }
    }

    private fun executarEAtualizar(
        acao: suspend () -> Result<Unit>
    ) {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(
                loading = true,
                error = null
            )

            val result = acao()

            if (result.isSuccess) {
                carregar()
            } else {
                uiState.value = uiState.value.copy(
                    loading = false,
                    error = result.exceptionOrNull()?.message ?: "Erro ao atualizar alerta."
                )
            }
        }
    }
}