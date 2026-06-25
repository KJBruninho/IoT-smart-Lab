package com.example.myapplication.ui.sensors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SensorsScreen(
    viewModel: SensorsViewModel,
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
            text = "Sensores",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Leituras reais vindas do backend.",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.carregar() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.loading
        ) {
            Text(if (state.loading) "A carregar..." else "Atualizar leituras")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.error != null) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        state.estado?.let { estado ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Estado do sistema",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text("MQTT: ${if (estado.mqttOnline) "Online" else "Offline"}")
                    Text("Fonte: ${estado.fonte}")
                    Text("Leituras pendentes em cache: ${estado.leiturasPendentesCache}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (estado.leituras.isEmpty()) {
                Text("Ainda não existem leituras recentes.")
            } else {
                estado.leituras.forEach { leitura ->
                    SensorReadingCard(
                        tipo = leitura.tipo,
                        valor = leitura.valor,
                        unidade = leitura.unidade ?: "",
                        dataRegisto = leitura.dataRegisto ?: "Sem data"
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
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

@Composable
private fun SensorReadingCard(
    tipo: String,
    valor: Double,
    unidade: String,
    dataRegisto: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = tipo,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "$valor $unidade",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Última leitura: $dataRegisto",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}