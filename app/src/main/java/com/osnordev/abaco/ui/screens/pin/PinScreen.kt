package com.osnordev.abaco.ui.screens.pin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PinScreen(
    onUnlocked: () -> Unit,
    viewModel: PinViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isUnlocked) {
        onUnlocked()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Ingresa tu PIN", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(24.dp))

        // PIN dots
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(6) { index ->
                Icon(
                    imageVector = if (index < state.pin.length) Icons.Filled.Circle else Icons.Outlined.Circle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Error / lockout message
        when {
            state.isLocked -> Text(
                "Bloqueado. Espera ${state.lockCountdown} s",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            state.errorMessage != null -> Text(
                state.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(24.dp))

        // Numpad
        val digits = listOf("1","2","3","4","5","6","7","8","9","","0","⌫")
        val rows = digits.chunked(3)
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { label ->
                    when (label) {
                        "" -> Spacer(Modifier.size(80.dp))
                        "⌫" -> OutlinedButton(
                            onClick = { viewModel.onDelete() },
                            enabled = !state.isLocked,
                            modifier = Modifier.size(80.dp)
                        ) { Text(label) }
                        else -> FilledTonalButton(
                            onClick = { viewModel.onDigit(label) },
                            enabled = !state.isLocked,
                            modifier = Modifier.size(80.dp)
                        ) { Text(label, style = MaterialTheme.typography.titleLarge) }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(16.dp))

        FilledTonalButton(
            onClick = { viewModel.onSubmit() },
            enabled = !state.isLocked && state.pin.length >= 4
        ) {
            Text("Confirmar")
        }
    }
}
