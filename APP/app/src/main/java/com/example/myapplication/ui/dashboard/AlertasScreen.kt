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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.dto.AlertaSensorResponse

@Composable
fun AlertasScreen(
    viewModel: AlertasViewModel,
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
            text = "Alertas",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Alertas reais do backend.",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.carregar() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.loading
        ) {
            Text(if (state.loading) "A carregar..." else "Atualizar alertas")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.error != null) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (state.alertas.isEmpty() && !state.loading) {
            Text("Não existem alertas para mostrar.")
        } else {
            state.alertas.forEach { alerta ->
                AlertaCard(
                    alerta = alerta,
                    onMarcarLido = { viewModel.marcarLido(alerta.id) },
                    onResolver = { viewModel.resolver(alerta.id) },
                    onIgnorar = { viewModel.ignorar(alerta.id) }
                )

                Spacer(modifier = Modifier.height(12.dp))
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
private fun AlertaCard(
    alerta: AlertaSensorResponse,
    onMarcarLido: () -> Unit,
    onResolver: () -> Unit,
    onIgnorar: () -> Unit
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
                text = alerta.titulo,
                style = MaterialTheme.typography.titleLarge
            )

            Text("Sensor: ${alerta.tipoSensor}")
            Text("Valor lido: ${alerta.valorLido}")
            Text("Severidade: ${alerta.severidade}")
            Text("Estado: ${alerta.estado}")
            Text("Criado em: ${alerta.criadoEm ?: "Sem data"}")

            if (!alerta.mensagem.isNullOrBlank()) {
                Text("Mensagem: ${alerta.mensagem}")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onMarcarLido,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Lido")
                }

                OutlinedButton(
                    onClick = onResolver,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Resolver")
                }
            }

            OutlinedButton(
                onClick = onIgnorar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ignorar")
            }
        }
    }
}