package com.osnordev.abaco.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.osnordev.abaco.domain.model.AppModule
import com.osnordev.abaco.domain.model.TaxBracket

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar("Configuración guardada")
            viewModel.resetSavedFlag()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Configuración",
            style = MaterialTheme.typography.headlineMedium
        )

        // --- Modules section ---
        ModulesSection(
            moduleStates = uiState.moduleStates,
            onToggle = viewModel::onModuleToggle
        )

        // --- Tax config section (only if TAX_SETTINGS module is active) ---
        if (uiState.moduleStates[AppModule.TAX_SETTINGS] == true) {
            TaxConfigSection(
                cssRateInput = uiState.cssRateInput,
                cssRateError = uiState.cssRateError,
                brackets = uiState.taxConfig.iipBrackets,
                bracketsError = uiState.bracketsError,
                bracketRateErrors = uiState.bracketRateErrors,
                onCssRateChange = viewModel::onCssRateInputChange,
                onBracketRateChange = viewModel::onBracketRateChange,
                onBracketToChange = viewModel::onBracketToChange,
                onSave = viewModel::saveTaxConfig
            )
        }

        // --- Currency config section ---
        CurrencyConfigSection(
            mlcInput = uiState.mlcToCupInput,
            usdInput = uiState.usdToCupInput,
            error = uiState.currencyError,
            onMlcChange = viewModel::onMlcRateChange,
            onUsdChange = viewModel::onUsdRateChange,
            onSave = viewModel::saveCurrencyConfig
        )

        SnackbarHost(hostState = snackbarHostState)
    }
}

@Composable
private fun ModulesSection(
    moduleStates: Map<AppModule, Boolean>,
    onToggle: (AppModule, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Módulos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            AppModule.entries.forEachIndexed { index, module ->
                ModuleToggleRow(
                    module = module,
                    enabled = moduleStates[module] ?: true,
                    onToggle = { onToggle(module, it) }
                )
                if (index < AppModule.entries.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun ModuleToggleRow(
    module: AppModule,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = module.label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = enabled, onCheckedChange = onToggle)
    }
}

@Composable
private fun TaxConfigSection(
    cssRateInput: String,
    cssRateError: String?,
    brackets: List<TaxBracket>,
    bracketsError: String?,
    bracketRateErrors: Map<Int, String>,
    onCssRateChange: (String) -> Unit,
    onBracketRateChange: (Int, String) -> Unit,
    onBracketToChange: (Int, String) -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Configuración tributaria",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // CSS Rate
            OutlinedTextField(
                value = cssRateInput,
                onValueChange = onCssRateChange,
                label = { Text("Tasa CSS (%)") },
                isError = cssRateError != null,
                supportingText = cssRateError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider()

            Text(
                text = "Tramos IIP",
                style = MaterialTheme.typography.titleSmall
            )

            brackets.forEachIndexed { index, bracket ->
                BracketRow(
                    index = index,
                    bracket = bracket,
                    isLast = index == brackets.lastIndex,
                    rateError = bracketRateErrors[index],
                    onRateChange = { onBracketRateChange(index, it) },
                    onToChange = { onBracketToChange(index, it) }
                )
            }

            if (bracketsError != null) {
                Text(
                    text = bracketsError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar configuración")
            }
        }
    }
}

@Composable
private fun CurrencyConfigSection(
    mlcInput: String,
    usdInput: String,
    error: String?,
    onMlcChange: (String) -> Unit,
    onUsdChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Tipos de cambio",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
                value = mlcInput,
                onValueChange = onMlcChange,
                label = { Text("MLC → CUP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = usdInput,
                onValueChange = onUsdChange,
                label = { Text("USD → CUP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar tipos de cambio")
            }
        }
    }
}

@Composable
private fun BracketRow(
    index: Int,
    bracket: TaxBracket,
    isLast: Boolean,
    rateError: String?,
    onRateChange: (String) -> Unit,
    onToChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Tramo ${index + 1}: desde ${bracket.from.toLong()} CUP",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = (bracket.rate * 100).toBigDecimal().stripTrailingZeros().toPlainString(),
                onValueChange = onRateChange,
                label = { Text("Tasa (%)") },
                isError = rateError != null,
                supportingText = rateError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            if (!isLast) {
                Spacer(modifier = Modifier.width(4.dp))
                OutlinedTextField(
                    value = bracket.to?.toLong()?.toString() ?: "",
                    onValueChange = onToChange,
                    label = { Text("Hasta (CUP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            } else {
                OutlinedTextField(
                    value = "Sin límite",
                    onValueChange = {},
                    label = { Text("Hasta (CUP)") },
                    enabled = false,
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }
    }
}
