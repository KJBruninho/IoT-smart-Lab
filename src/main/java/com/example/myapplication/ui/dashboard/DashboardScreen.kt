package com.example.myapplication.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
fun DashboardScreen(
    onOpenSensors: () -> Unit,
    onOpenAlertas: () -> Unit,
    onOpenControlo: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "IoT Room",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        DashboardCard(
            title = "Sensores",
            description = "Ver sensores, leituras recentes e estado dos dispositivos.",
            buttonText = "Abrir sensores",
            onClick = onOpenSensors
        )

        Spacer(modifier = Modifier.height(16.dp))

        DashboardCard(
            title = "Alertas",
            description = "Consultar alertas de temperatura, humidade, TDS, pH e outros sensores.",
            buttonText = "Abrir alertas",
            onClick = onOpenAlertas
        )

        Spacer(modifier = Modifier.height(16.dp))

        DashboardCard(
            title = "Controlo",
            description = "Área para calibração, alteração de intervalos e comandos para ESP32.",
            buttonText = "Abrir controlo",
            onClick = onOpenControlo
        )

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Terminar sessão")
        }
    }
}

@Composable
private fun DashboardCard(
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onClick
                ) {
                    Text(buttonText)
                }
            }
        }
    }
}