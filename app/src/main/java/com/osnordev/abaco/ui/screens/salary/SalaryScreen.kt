package com.osnordev.abaco.ui.screens.salary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.osnordev.abaco.data.local.PayrollRecordEntity
import com.osnordev.abaco.domain.validation.ValidationResult
import java.util.Locale

@Composable
fun SalaryScreen(viewModel: SalaryViewModel = hiltViewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val saveStatus by viewModel.saveStatus.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(saveStatus) {
        saveStatus?.let {
            when (it) {
                is ValidationResult.Valid -> {
                    snackbarHostState.showSnackbar("Asiento contable generado correctamente")
                    selectedTab = 1 // ir al historial tras guardar
                }
                is ValidationResult.Invalid -> snackbarHostState.showSnackbar("Error: ${it.message}")
            }
            viewModel.clearSaveStatus()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                text = { Text("Calcular") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                text = { Text("Historial") })
        }

        when (selectedTab) {
            0 -> CalculateTab(viewModel)
            1 -> HistoryTab(viewModel)
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}

// ── Tab Calcular ──────────────────────────────────────────────────────────────

@Composable
private fun CalculateTab(viewModel: SalaryViewModel) {
    val employeeName by viewModel.employeeName.collectAsState()
    val ci by viewModel.ci.collectAsState()
    val grossInput by viewModel.grossInput.collectAsState()
    val payroll by viewModel.payrollResult.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Cálculo de Nómina (Norma Cubana)",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = employeeName,
                    onValueChange = viewModel::onEmployeeNameChange,
                    label = { Text("Nombre del Empleado") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ci,
                    onValueChange = { input -> viewModel.onCiChange(input.filter { it.isDigit() }) },
                    label = { Text("Carnet de Identidad (CI)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = grossInput,
                    onValueChange = viewModel::onGrossInputChange,
                    label = { Text("Salario Base (CUP)") },
                    placeholder = { Text("Ej: 3000") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    supportingText = {
                        val base = grossInput.toDoubleOrNull() ?: 0.0
                        if (base > 0) {
                            val devengado = base * 1.0909
                            Text(
                                "Devengado (+ 9.09% vacaciones): ${"%.2f".format(devengado)} CUP",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        }

        payroll?.let { p ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Resumen Empleado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    SalaryRow("Salario Base", grossInput.toDoubleOrNull() ?: 0.0,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    SalaryRow("Provisión Vacaciones (9.09%)", p.holidayProvision,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    SalaryRow("Salario Devengado", p.grossSalary, isBold = true)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    SalaryRow("Retención SS (5%)", p.cssEmployee, isDeduction = true)
                    SalaryRow("Retención IIP (ONAT)", p.iipRetained, isDeduction = true)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    SalaryRow("Salario Neto a Cobrar", p.netSalary, isBold = true,
                        color = MaterialTheme.colorScheme.primary)
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Costo para la Empresa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    SalaryRow("Salario Devengado", p.grossSalary)
                    SalaryRow("SS para la utilización de la fuerza de trabajo (12.5%)", p.cssEmployer)
                    SalaryRow("Provisión Subsidio (1.5%)", p.subsidyProvision)
                    SalaryRow("SS Especial (5%)", p.specialSS)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    SalaryRow("Costo Total Empresa", p.totalCompanyCost, isBold = true)
                }
            }

            Button(
                onClick = viewModel::postToJournal,
                modifier = Modifier.fillMaxWidth(),
                enabled = p.grossSalary > 0 && employeeName.isNotBlank()
            ) {
                Text("Generar Asiento Contable")
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ── Tab Historial ─────────────────────────────────────────────────────────────

@Composable
private fun HistoryTab(viewModel: SalaryViewModel) {
    val history by viewModel.payrollHistory.collectAsState()

    if (history.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Sin registros de nómina", style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    // Agrupar por período
    val grouped = history.groupBy { it.period }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        grouped.forEach { (period, records) ->
            item {
                Text(
                    text = period,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            items(records, key = { it.id }) { record ->
                PayrollAccordionCard(
                    record = record,
                    onDelete = { viewModel.deleteRecord(record.id) }
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ── Tarjeta acordeón ──────────────────────────────────────────────────────────

@Composable
private fun PayrollAccordionCard(
    record: PayrollRecordEntity,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // ── Cabecera (siempre visible) ────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(record.employeeName, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                    Text("CI: ${record.ci}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 8.dp)) {
                    Text(
                        text = "${"%.2f".format(record.grossSalary)} CUP",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Neto: ${"%.2f".format(record.netSalary)} CUP",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            // ── Detalle expandible ────────────────────────────────────────
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    HorizontalDivider()
                    Spacer(Modifier.height(4.dp))

                    Text("Detalle del Empleado", style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold)
                    SalaryRow("Salario Base", record.baseSalary,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    SalaryRow("Provisión Vacaciones (9.09%)", record.holidayProvision,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    SalaryRow("Salario Devengado", record.grossSalary, isBold = true)
                    SalaryRow("Retención SS (5%)", record.cssEmployee, isDeduction = true)
                    SalaryRow("Retención IIP (ONAT)", record.iipRetained, isDeduction = true)
                    SalaryRow("Salario Neto", record.netSalary, isBold = true,
                        color = MaterialTheme.colorScheme.primary)

                    Spacer(Modifier.height(8.dp))
                    Text("Costo Empresa", style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold)
                    SalaryRow("SS utilización fuerza trabajo (12.5%)", record.cssEmployer)
                    SalaryRow("Provisión Subsidio (1.5%)", record.subsidyProvision)
                    SalaryRow("SS Especial (5%)", record.specialSS)
                    SalaryRow("Costo Total Empresa", record.totalCompanyCost, isBold = true)

                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

// ── Fila de valor ─────────────────────────────────────────────────────────────

@Composable
private fun SalaryRow(
    label: String,
    value: Double,
    isDeduction: Boolean = false,
    isBold: Boolean = false,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    else MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        val formatted = String.format(Locale.US, "%.2f CUP", value)
        Text(
            text = if (isDeduction) "- $formatted" else formatted,
            style = if (isBold) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    else MaterialTheme.typography.bodyMedium,
            color = if (isDeduction) MaterialTheme.colorScheme.error else color
        )
    }
}
