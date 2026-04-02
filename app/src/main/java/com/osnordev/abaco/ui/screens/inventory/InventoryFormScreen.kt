package com.osnordev.abaco.ui.screens.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.osnordev.abaco.data.local.InventoryItemEntity
import com.osnordev.abaco.data.local.InventoryMovementType

private val UNIT_OPTIONS = listOf("unidad", "kg", "g", "litro", "ml", "caja", "paquete", "docena")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryFormScreen(
    itemId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load existing item
    var loaded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("unidad") }
    var quantityText by remember { mutableStateOf("0") }
    var minStockText by remember { mutableStateOf("0") }
    var costPriceText by remember { mutableStateOf("0") }
    var salePriceText by remember { mutableStateOf("0") }
    var unitExpanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }

    // Movement dialog state
    var showMovementDialog by remember { mutableStateOf(false) }
    var movementType by remember { mutableStateOf(InventoryMovementType.IN) }
    var movementQtyText by remember { mutableStateOf("") }
    var movementNote by remember { mutableStateOf("") }
    var movementQtyError by remember { mutableStateOf<String?>(null) }
    var movementTypeExpanded by remember { mutableStateOf(false) }

    // Delete confirmation
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(itemId) {
        if (itemId != null && !loaded) {
            val item = viewModel.getItemById(itemId)
            item?.let {
                name = it.name
                description = it.description
                category = it.category
                unit = it.unit
                quantityText = it.quantity.toString()
                minStockText = it.minStock.toString()
                costPriceText = it.costPrice.toString()
                salePriceText = it.salePrice.toString()
            }
            loaded = true
        }
    }

    // Movement dialog
    if (showMovementDialog && itemId != null) {
        AlertDialog(
            onDismissRequest = { showMovementDialog = false },
            title = { Text("Registrar movimiento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Movement type
                    ExposedDropdownMenuBox(
                        expanded = movementTypeExpanded,
                        onExpandedChange = { movementTypeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = movementType.label(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = movementTypeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = movementTypeExpanded,
                            onDismissRequest = { movementTypeExpanded = false }
                        ) {
                            InventoryMovementType.entries.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(t.label()) },
                                    onClick = { movementType = t; movementTypeExpanded = false }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = movementQtyText,
                        onValueChange = { movementQtyText = it; movementQtyError = null },
                        label = { Text(if (movementType == InventoryMovementType.ADJUSTMENT) "Nueva cantidad" else "Cantidad") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = movementQtyError != null,
                        supportingText = movementQtyError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = movementNote,
                        onValueChange = { movementNote = it },
                        label = { Text("Nota (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val qty = movementQtyText.toDoubleOrNull()
                    if (qty == null || qty < 0) {
                        movementQtyError = "Cantidad inválida"
                        return@TextButton
                    }
                    viewModel.registerMovement(itemId, movementType, qty, movementNote.trim())
                    showMovementDialog = false
                    movementQtyText = ""
                    movementNote = ""
                }) { Text("Registrar") }
            },
            dismissButton = {
                TextButton(onClick = { showMovementDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog && itemId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar producto") },
            text = { Text("¿Eliminar \"$name\" del inventario? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteItem(
                            InventoryItemEntity(
                                id = itemId, name = name, description = description,
                                category = category, unit = unit,
                                quantity = quantityText.toDoubleOrNull() ?: 0.0,
                                minStock = minStockText.toDoubleOrNull() ?: 0.0,
                                costPrice = costPriceText.toDoubleOrNull() ?: 0.0,
                                salePrice = salePriceText.toDoubleOrNull() ?: 0.0
                            )
                        )
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (itemId != null) "Editar producto" else "Nuevo producto") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (itemId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = null },
                label = { Text("Nombre del producto *") },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Categoría (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Unit dropdown
            ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = it }) {
                OutlinedTextField(
                    value = unit,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Unidad de medida") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                    UNIT_OPTIONS.forEach { u ->
                        DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; unitExpanded = false })
                    }
                }
            }

            HorizontalDivider()
            Text("Cantidades y precios", style = MaterialTheme.typography.labelLarge)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Cantidad inicial") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = itemId == null  // only editable on new items; use movements for existing
                )
                OutlinedTextField(
                    value = minStockText,
                    onValueChange = { minStockText = it },
                    label = { Text("Stock mínimo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = costPriceText,
                    onValueChange = { costPriceText = it },
                    label = { Text("Precio costo (CUP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = salePriceText,
                    onValueChange = { salePriceText = it },
                    label = { Text("Precio venta (CUP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Register movement button (only for existing items)
            if (itemId != null) {
                OutlinedButton(
                    onClick = { showMovementDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrar entrada / salida / ajuste")
                }
            }

            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = "El nombre es obligatorio"
                        return@Button
                    }
                    viewModel.saveItem(
                        InventoryItemEntity(
                            id = itemId ?: 0L,
                            name = name.trim(),
                            description = description.trim(),
                            category = category.trim(),
                            unit = unit,
                            quantity = quantityText.toDoubleOrNull() ?: 0.0,
                            minStock = minStockText.toDoubleOrNull() ?: 0.0,
                            costPrice = costPriceText.toDoubleOrNull() ?: 0.0,
                            salePrice = salePriceText.toDoubleOrNull() ?: 0.0
                        )
                    )
                    if (uiState.errorMessage == null) onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                Text(if (itemId != null) "Guardar cambios" else "Agregar producto")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun InventoryMovementType.label(): String = when (this) {
    InventoryMovementType.IN         -> "Entrada"
    InventoryMovementType.OUT        -> "Salida"
    InventoryMovementType.ADJUSTMENT -> "Ajuste"
}
