package com.osnordev.abaco.ui.screens.periods

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun PeriodsScreen(
    viewModel: PeriodsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showCloseConfirm by remember { mutableStateOf(false) }

    // Diálogo de confirmación de cierre
    if (showCloseConfirm) {
        AlertDialog(
            onDismissRequest = { showCloseConfirm = false },
            title = { Text("Cerrar Período") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "¿Confirmas el cierre de ${
                            Month.of(state.currentMonth)
                                .getDisplayName(TextStyle.FULL, Locale("es"))
                                .replaceFirstChar { it.uppercase() }
                        } ${state.currentYear}?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    state.summary?.let { s ->
                        Spacer(Modifier.height(4.dp))
                        Text("Resumen del período:", style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold)
                        SummaryRow("Ingresos", s.totalIncome)
                        SummaryRow("Gastos", s.totalExpenses)
                        SummaryRow("Resultado neto", s.totalIncome - s.totalExpenses)
                        Text("Asientos contables: ${s.journalEntryCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Una vez cerrado, no se podrán agregar asientos en este período.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.closePeriod()
                    showCloseConfirm = false
                }) { Text("Cerrar Período", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showCloseConfirm = false }) { Text("Cancelar") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Período Activo",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Período actual
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${
                                Month.of(state.currentMonth)
                                    .getDisplayName(TextStyle.FULL, Locale("es"))
                                    .replaceFirstChar { it.uppercase() }
                            } ${state.currentYear}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (state.isOpen) "ABIERTO" else "CERRADO",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (state.isOpen) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error
                        )
                    }
                    Icon(
                        imageVector = if (state.isOpen) Icons.Filled.LockOpen else Icons.Filled.Lock,
                        contentDescription = null,
                        tint = if (state.isOpen) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.error
                    )
                }

                // Resumen del período activo
                state.summary?.let { s ->
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        SummaryChip("Ingresos", s.totalIncome, MaterialTheme.colorScheme.primary)
                        SummaryChip("Gastos", s.totalExpenses, MaterialTheme.colorScheme.error)
                        SummaryChip(
                            "Neto",
                            s.totalIncome - s.totalExpenses,
                            if (s.totalIncome >= s.totalExpenses) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )
                    }
                    Text(
                        text = "${s.journalEntryCount} asiento(s) contable(s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        if (state.isOpen) {
            OutlinedButton(
                onClick = { showCloseConfirm = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Lock, contentDescription = null)
                Text("  Cerrar Período")
            }
        } else {
            OutlinedButton(
                onClick = { viewModel.openNextPeriod() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.LockOpen, contentDescription = null)
                Text("  Abrir Siguiente Período")
            }
        }

        if (state.closedPeriods.isNotEmpty()) {
            Text(
                "Períodos Cerrados",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            state.closedPeriods.sortedDescending().forEach { period ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(period, style = MaterialTheme.typography.bodyMedium)
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SummaryChip(label: String, value: Double, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "${"%.0f".format(value)} CUP",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: Double) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(
            "${"%.2f".format(value)} CUP",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}
