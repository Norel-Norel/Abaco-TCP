package com.osnordev.abaco.ui.screens.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.format.DateTimeFormatter

private val DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEntryFormScreen(
    onNavigateBack: () -> Unit,
    viewModel: JournalViewModel = hiltViewModel()
) {
    val state by viewModel.form.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) { viewModel.resetForm(); onNavigateBack() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Asiento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancelar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Cabecera ──────────────────────────────────────────────────
            item {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.date.format(DATE_FMT),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = "COMPROBANTE DIARIO",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("Descripción del asiento") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // ── Encabezado de tabla ───────────────────────────────────────
            item {
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CUENTA", style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold, modifier = Modifier.weight(2.5f))
                    Text("PARCIAL", style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.0f))
                    Text("DEBE", style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.0f))
                    Text("HABER", style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.0f))
                    Spacer(Modifier.width(36.dp))
                }
                HorizontalDivider()
            }

            // ── Líneas ────────────────────────────────────────────────────
            itemsIndexed(state.lines) { index, line ->
                LineRow(
                    index = index,
                    line = line,
                    canRemove = state.lines.size > 2,
                    suggestions = androidx.compose.runtime.produceState(
                        initialValue = emptyList<AccountSuggestion>(),
                        key1 = line.accountQuery
                    ) { value = viewModel.getSuggestions(line.accountQuery) }.value,
                    onAccountQueryChange = { viewModel.onAccountQueryChange(index, it) },
                    onAccountSelected = { viewModel.onAccountSelected(index, it) },
                    onSuggestionsHide = { viewModel.onSuggestionsHide(index) },
                    onPartialChange = { viewModel.onPartialChange(index, it) },
                    onDebitChange = { viewModel.onDebitChange(index, it) },
                    onCreditChange = { viewModel.onCreditChange(index, it) },
                    onRemove = { viewModel.removeLine(index) }
                )
            }

            // ── Agregar línea ─────────────────────────────────────────────
            item {
                TextButton(
                    onClick = viewModel::addLine,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  Agregar línea")
                }
                HorizontalDivider()
            }

            // ── Totales y estado de cuadre ────────────────────────────────
            item {
                TotalsRow(
                    totalDebit = state.totalDebit,
                    totalCredit = state.totalCredit,
                    difference = state.difference,
                    isBalanced = state.isBalanced
                )
            }

            // ── Error de validación ───────────────────────────────────────
            state.validationError?.let { err ->
                item {
                    Text(err, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
            }

            // ── Botones de acción ─────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onNavigateBack, modifier = Modifier.weight(1f)) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = viewModel::save,
                        modifier = Modifier.weight(2f),
                        enabled = state.isBalanced && !state.isSaving
                    ) {
                        Text("Guardar Asiento")
                    }
                }
            }
        }
    }
}

// ── Fila de línea ─────────────────────────────────────────────────────────────

@Composable
private fun LineRow(
    index: Int,
    line: LineFormState,
    canRemove: Boolean,
    suggestions: List<AccountSuggestion>,
    onAccountQueryChange: (String) -> Unit,
    onAccountSelected: (AccountSuggestion) -> Unit,
    onSuggestionsHide: () -> Unit,
    onPartialChange: (String) -> Unit,
    onDebitChange: (String) -> Unit,
    onCreditChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Cuenta con autocompletado
            Box(modifier = Modifier.weight(2.5f)) {
                OutlinedTextField(
                    value = line.accountQuery,
                    onValueChange = onAccountQueryChange,
                    placeholder = { Text("Cód. o nombre", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )
                if (line.showSuggestions && suggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 56.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column {
                            suggestions.take(5).forEach { suggestion ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(suggestion.display, style = MaterialTheme.typography.bodySmall)
                                            Text(suggestion.type.name, style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = { onAccountSelected(suggestion) }
                                )
                            }
                        }
                    }
                }
            }

            // Parcial — campo informativo
            OutlinedTextField(
                value = line.partial,
                onValueChange = onPartialChange,
                placeholder = { Text("0.00", style = MaterialTheme.typography.bodySmall) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1.0f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall
            )

            // Debe — bloqueado si hay Haber
            OutlinedTextField(
                value = line.debit,
                onValueChange = onDebitChange,
                placeholder = { Text("0.00", style = MaterialTheme.typography.bodySmall) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1.0f),
                singleLine = true,
                enabled = line.credit.isBlank(),
                textStyle = MaterialTheme.typography.bodySmall
            )

            // Haber — bloqueado si hay Debe
            OutlinedTextField(
                value = line.credit,
                onValueChange = onCreditChange,
                placeholder = { Text("0.00", style = MaterialTheme.typography.bodySmall) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1.0f),
                singleLine = true,
                enabled = line.debit.isBlank(),
                textStyle = MaterialTheme.typography.bodySmall
            )

            // Eliminar
            IconButton(
                onClick = onRemove,
                enabled = canRemove,
                modifier = Modifier.size(36.dp).align(Alignment.CenterVertically)
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Eliminar línea",
                    tint = if (canRemove) MaterialTheme.colorScheme.error
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        HorizontalDivider()
    }
}

// ── Totales ───────────────────────────────────────────────────────────────────

@Composable
private fun TotalsRow(
    totalDebit: Double,
    totalCredit: Double,
    difference: Double,
    isBalanced: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isBalanced) MaterialTheme.colorScheme.primaryContainer
                             else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("TOTAL DEBE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("${"%.2f".format(totalDebit)} CUP", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("TOTAL HABER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("${"%.2f".format(totalCredit)} CUP", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold)
            }
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isBalanced) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (isBalanced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (isBalanced) "BALANCEADO (DIFERENCIA = 0.00)"
                           else "DIFERENCIA = ${"%.2f".format(Math.abs(difference))}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isBalanced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
