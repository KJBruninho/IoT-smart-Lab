package com.example.myapplication.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ControloScreen(
    viewModel: ControloViewModel,
    onBack: () -> Unit
) {
    val state = viewModel.uiState.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Controlo",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Enviar comandos para sensores via backend.",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Sensor",
                    style = MaterialTheme.typography.titleLarge
                )

                val dropdownAberto = remember { mutableStateOf(false) }

                val sensorSelecionado = state.sensores.firstOrNull {
                    it.id == state.sensorSelecionadoId
                }

                Text(
                    text = "Sensor",
                    style = MaterialTheme.typography.titleLarge
                )

                if (state.sensores.isEmpty()) {
                    Text("Não existem sensores disponíveis para o teu grupo.")
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                dropdownAberto.value = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = sensorSelecionado?.let {
                                    "${it.nome} - ${it.tipo}"
                                } ?: "Selecionar sensor"
                            )
                        }

                        DropdownMenu(
                            expanded = dropdownAberto.value,
                            onDismissRequest = {
                                dropdownAberto.value = false
                            }
                        ) {
                            state.sensores.forEach { sensor ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("${sensor.nome} - ${sensor.tipo}")

                                            if (!sensor.estacao.isNullOrBlank()) {
                                                Text(
                                                    text = sensor.estacao,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.selecionarSensor(sensor.id)
                                        dropdownAberto.value = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = viewModel::pedirLigarSensor,
                        enabled = !state.loading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ligar")
                    }

                    OutlinedButton(
                        onClick = viewModel::pedirDesligarSensor,
                        enabled = !state.loading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Desligar")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Calibração",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = state.fatorCalibracao,
                    onValueChange = viewModel::onFatorChange,
                    label = { Text("Fator de calibração") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Button(
                    onClick = viewModel::pedirCalibracao,
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.loading) "A enviar..." else "Enviar calibração")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Intervalo de leitura",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = state.intervaloSegundos,
                    onValueChange = viewModel::onIntervaloChange,
                    label = { Text("Intervalo em segundos") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Button(
                    onClick = viewModel::pedirAlteracaoIntervalo,
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.loading) "A enviar..." else "Pedir alteração de intervalo")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.erro != null) {
            Text(
                text = state.erro,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (state.mensagem != null) {
            Text(
                text = state.mensagem,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Voltar")
        }
    }
}