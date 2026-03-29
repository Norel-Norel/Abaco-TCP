package com.osnordev.abaco.ui.screens.payments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.osnordev.abaco.data.local.PaymentDueEntity
import java.time.LocalDate

@Composable
fun PaymentDueListScreen(
    viewModel: PaymentDueViewModel = hiltViewModel()
) {
    val payments by viewModel.pendingPayments.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Nuevo pago")
            }
        }
    ) { padding ->
        if (payments.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No hay pagos pendientes.", style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(payments) { payment ->
                    PaymentCard(payment = payment, onMarkPaid = { viewModel.markAsPaid(payment) })
                }
            }
        }
    }

    if (showDialog) {
        AddPaymentDialog(
            onDismiss = { showDialog = false },
            onConfirm = { desc, amount, date ->
                viewModel.addPayment(desc, amount, date)
                showDialog = false
            }
        )
    }
}

@Composable
private fun PaymentCard(payment: PaymentDueEntity, onMarkPaid: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(payment.description, style = MaterialTheme.typography.titleSmall)
                Text("%.2f %s".format(payment.amount, payment.currency),
                    style = MaterialTheme.typography.bodyMedium)
                Text("Vence: ${payment.dueDate}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onMarkPaid) {
                Icon(Icons.Filled.CheckCircle, contentDescription = "Marcar como pagado",
                    tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun AddPaymentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, LocalDate) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var dueDateText by remember { mutableStateOf(LocalDate.now().plusDays(7).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo pago pendiente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text("Descripción") }, singleLine = true)
                OutlinedTextField(value = amountText, onValueChange = { amountText = it },
                    label = { Text("Importe (CUP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true)
                OutlinedTextField(value = dueDateText, onValueChange = { dueDateText = it },
                    label = { Text("Fecha de vencimiento (YYYY-MM-DD)") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                val amount = amountText.toDoubleOrNull() ?: return@Button
                val date = runCatching { LocalDate.parse(dueDateText) }.getOrNull() ?: return@Button
                onConfirm(description, amount, date)
            }) { Text("Agregar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
