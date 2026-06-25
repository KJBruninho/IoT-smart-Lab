package com.example.myapplication.ui.dashboard

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.SensorDisponivelResponse
import com.example.myapplication.data.repository.ControloRepository
import kotlinx.coroutines.launch

data class ControloUiState(
    val sensores: List<SensorDisponivelResponse> = emptyList(),
    val sensorSelecionadoId: Long? = null,
    val fatorCalibracao: String = "1.0",
    val intervaloSegundos: String = "10",
    val loading: Boolean = false,
    val mensagem: String? = null,
    val erro: String? = null
)

class ControloViewModel(
    private val controloRepository: ControloRepository
) : ViewModel() {

    val uiState = mutableStateOf(ControloUiState())

    fun carregarSensores() {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(
                loading = true,
                erro = null,
                mensagem = null
            )

            val result = controloRepository.sensoresDisponiveis()

            uiState.value = if (result.isSuccess) {
                val sensores = result.getOrNull().orEmpty()

                uiState.value.copy(
                    loading = false,
                    sensores = sensores,
                    sensorSelecionadoId = sensores.firstOrNull()?.id,
                    erro = null
                )
            } else {
                uiState.value.copy(
                    loading = false,
                    erro = result.exceptionOrNull()?.message ?: "Erro ao carregar sensores."
                )
            }
        }
    }

    fun selecionarSensor(sensorId: Long) {
        uiState.value = uiState.value.copy(
            sensorSelecionadoId = sensorId,
            erro = null,
            mensagem = null
        )
    }

    fun onFatorChange(value: String) {
        uiState.value = uiState.value.copy(
            fatorCalibracao = value,
            erro = null,
            mensagem = null
        )
    }

    fun onIntervaloChange(value: String) {
        uiState.value = uiState.value.copy(
            intervaloSegundos = value,
            erro = null,
            mensagem = null
        )
    }

    fun pedirCalibracao() {
        val fator = uiState.value.fatorCalibracao
            .replace(",", ".")
            .toDoubleOrNull()

        if (fator == null) {
            uiState.value = uiState.value.copy(
                erro = "Fator de calibração inválido."
            )
            return
        }

        criarPedido(
            comando = "CALIBRAR",
            valor = fator
        )
    }

    fun pedirAlteracaoIntervalo() {
        val intervalo = uiState.value.intervaloSegundos.toDoubleOrNull()

        if (intervalo == null || intervalo <= 0) {
            uiState.value = uiState.value.copy(
                erro = "Intervalo inválido."
            )
            return
        }

        criarPedido(
            comando = "INTERVALO",
            valor = intervalo
        )
    }

    fun pedirLigarSensor() {
        criarPedido(
            comando = "LIGAR",
            valor = null
        )
    }

    fun pedirDesligarSensor() {
        criarPedido(
            comando = "DESLIGAR",
            valor = null
        )
    }

    private fun criarPedido(
        comando: String,
        valor: Double?
    ) {
        viewModelScope.launch {
            val estadoAtual = uiState.value
            val sensorId = estadoAtual.sensorSelecionadoId

            if (sensorId == null) {
                uiState.value = estadoAtual.copy(
                    erro = "Seleciona um sensor."
                )
                return@launch
            }

            uiState.value = estadoAtual.copy(
                loading = true,
                erro = null,
                mensagem = null
            )

            val result = controloRepository.criarPedido(
                sensorId = sensorId,
                comando = comando,
                valor = valor
            )

            uiState.value = if (result.isSuccess) {
                uiState.value.copy(
                    loading = false,
                    mensagem = result.getOrNull()?.mensagem ?: "Pedido enviado para aprovação.",
                    erro = null
                )
            } else {
                uiState.value.copy(
                    loading = false,
                    erro = result.exceptionOrNull()?.message ?: "Erro ao criar pedido.",
                    mensagem = null
                )
            }
        }
    }
}