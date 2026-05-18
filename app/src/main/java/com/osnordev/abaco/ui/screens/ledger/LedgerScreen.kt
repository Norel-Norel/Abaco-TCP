package com.osnordev.abaco.ui.screens.ledger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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

@Composable
fun LedgerScreen(
    viewModel: LedgerViewModel = hiltViewModel()
) {
    val ledger by viewModel.ledger.collectAsState()
    var query by remember { mutableStateOf("") }

    val filtered = remember(ledger, query) {
        if (query.isBlank()) ledger
        else ledger.filter { it.accountName.contains(query, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Filtrar por cuenta") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            singleLine = true
        )

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (ledger.isEmpty()) "No hay asientos registrados"
                    else "Sin resultados para \"$query\"",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered) { entry ->
                    LedgerAccountCard(entry)
                }
            }
        }
    }
}

@Composable
private fun LedgerAccountCard(entry: LedgerEntry) {
    val totalDebit = entry.movements.sumOf { it.debit }
    val totalCredit = entry.movements.sumOf { it.credit }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // Cabecera de cuenta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.accountName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${entry.movements.size} mov.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            // Encabezado columnas
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Fecha", style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                Text("Descripción", style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                Text("Debe", style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f))
                Text("Haber", style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f))
            }

            HorizontalDivider()

            // Movimientos
            entry.movements.forEach { mov ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(mov.date, style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1.2f))
                    Text(mov.description, style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(2f), maxLines = 1)
                    Text(
                        if (mov.debit > 0) "${"%.2f".format(mov.debit)}" else "—",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (mov.debit > 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        if (mov.credit > 0) "${"%.2f".format(mov.credit)}" else "—",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (mov.credit > 0) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider()

            // Totales y saldo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TOTALES", style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(3.2f))
                Text("${"%.2f".format(totalDebit)}",
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                Text("${"%.2f".format(totalCredit)}",
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "${entry.balanceLabel}: ${"%.2f".format(entry.balance)} CUP",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (entry.balance >= 0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
