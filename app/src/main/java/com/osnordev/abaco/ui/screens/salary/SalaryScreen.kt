package com.osnordev.abaco.ui.screens.salary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.osnordev.abaco.domain.validation.ValidationResult
import java.util.Locale

@Composable
fun SalaryScreen(
    viewModel: SalaryViewModel = hiltViewModel()
) {
    val employeeName by viewModel.employeeName.collectAsState()
    val ci by viewModel.ci.collectAsState()
    val grossInput by viewModel.grossInput.collectAsState()
    val payroll by viewModel.payrollResult.collectAsState()
    val saveStatus by viewModel.saveStatus.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saveStatus) {
        saveStatus?.let {
            when (it) {
                is ValidationResult.Valid -> snackbarHostState.showSnackbar("Asiento contable generado correctamente")
                is ValidationResult.Invalid -> snackbarHostState.showSnackbar("Error: ${it.message}")
            }
            viewModel.clearSaveStatus()
        }
    }

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

        // --- Datos del Empleado ---
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = employeeName,
                    onValueChange = { viewModel.onEmployeeNameChange(it) },
                    label = { Text("Nombre del Empleado") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ci,
                    onValueChange = { viewModel.onCiChange(it) },
                    label = { Text("Carnet de Identidad (CI)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = grossInput,
                    onValueChange = { viewModel.onGrossInputChange(it) },
                    label = { Text("Salario Base (CUP)") },
                    placeholder = { Text("Ej: 3000") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    supportingText = {
                        val base = grossInput.toDoubleOrNull() ?: 0.0
                        if (base > 0) {
                            val devengado = base * 1.0909
                            Text("Devengado (+ 9.09% vacaciones): ${"%.2f".format(devengado)} CUP",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            }
        }

        payroll?.let { p ->
            // --- Resumen Empleado ---
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Resumen Empleado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    SalaryRow(label = "Salario Base", value = grossInput.toDoubleOrNull() ?: 0.0,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    SalaryRow(label = "Provisión Vacaciones (9.09%)", value = p.holidayProvision,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    SalaryRow(label = "Salario Devengado (base + 9.09%)", value = p.grossSalary, isBold = true)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    SalaryRow(label = "Retención SS (5%)", value = p.cssEmployee, isDeduction = true)
                    SalaryRow(label = "Retención IIP (ONAT)", value = p.iipRetained, isDeduction = true)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    SalaryRow(label = "Salario Neto a Cobrar", value = p.netSalary, isBold = true, color = MaterialTheme.colorScheme.primary)
                }
            }

            // --- Costo Empresa ---
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Costo para la Empresa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    SalaryRow(label = "Salario Devengado", value = p.grossSalary)
                    SalaryRow(label = "SS para la utilización de la fuerza de trabajo (12.5%)", value = p.cssEmployer)
                    SalaryRow(label = "Provisión Subsidio (1.5%)", value = p.subsidyProvision)
                    SalaryRow(label = "SS Especial (5%)", value = p.specialSS)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    SalaryRow(label = "Costo Total Empresa", value = p.totalCompanyCost, isBold = true)
                }
            }

            Button(
                onClick = { viewModel.postToJournal() },
                modifier = Modifier.fillMaxWidth(),
                enabled = p.grossSalary > 0 && employeeName.isNotBlank()
            ) {
                Text("Generar Asiento Contable")
            }
        }
        
        SnackbarHost(hostState = snackbarHostState)
    }
}

@Composable
private fun SalaryRow(
    label: String,
    value: Double,
    isDeduction: Boolean = false,
    isBold: Boolean = false,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold) 
                    else MaterialTheme.typography.bodyMedium
        )
        val formattedValue = String.format(Locale.US, "%.2f CUP", value)
        Text(
            text = if (isDeduction) "- $formattedValue" else formattedValue,
            style = if (isBold) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold) 
                    else MaterialTheme.typography.bodyMedium,
            color = if (isDeduction) MaterialTheme.colorScheme.error else color
        )
    }
}
