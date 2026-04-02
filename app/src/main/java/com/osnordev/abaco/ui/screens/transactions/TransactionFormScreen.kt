package com.osnordev.abaco.ui.screens.transactions

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.osnordev.abaco.data.local.RecurringFrequency
import com.osnordev.abaco.domain.model.Currency
import com.osnordev.abaco.domain.model.ExpenseCategory
import com.osnordev.abaco.domain.model.IncomeCategory
import com.osnordev.abaco.domain.model.Transaction
import com.osnordev.abaco.domain.model.TransactionType
import com.osnordev.abaco.domain.receipt.ReceiptImageManager
import com.osnordev.abaco.ui.screens.contacts.ContactViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    transactionId: Long? = null,
    onNavigateBack: () -> Unit = {},
    viewModel: TransactionViewModel = hiltViewModel(),
    contactViewModel: ContactViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val receiptImageManager = remember { ReceiptImageManager(context) }
    val uiState by viewModel.uiState.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val contacts by contactViewModel.contacts.collectAsState()
    val currencyConfig by viewModel.currencyConfig.collectAsState()

    val existing = remember(transactionId, transactions) {
        transactionId?.let { id -> transactions.find { it.id == id } }
    }

    var type by remember(existing) { mutableStateOf(existing?.type ?: TransactionType.INCOME) }
    var amountText by remember(existing) { mutableStateOf(existing?.amount?.toString() ?: "") }
    var category by remember(existing) { mutableStateOf(existing?.category ?: IncomeCategory.SALES.label) }
    var description by remember(existing) { mutableStateOf(existing?.description ?: "") }
    var currency by remember(existing) { mutableStateOf(existing?.currency ?: Currency.CUP) }
    // Exchange rate override — allows user to enter a custom rate for MLC/USD
    var exchangeRateText by remember(currency, currencyConfig) {
        mutableStateOf(
            when (currency) {
                Currency.MLC -> currencyConfig.mlcToCup.toString()
                Currency.USD -> currencyConfig.usdToCup.toString()
                Currency.CUP -> "1.0"
            }
        )
    }
    // Update rate text when currency changes
    LaunchedEffect(currency) {
        exchangeRateText = when (currency) {
            Currency.MLC -> currencyConfig.mlcToCup.toString()
            Currency.USD -> currencyConfig.usdToCup.toString()
            Currency.CUP -> "1.0"
        }
    }
    var selectedContactId by remember(existing) { mutableStateOf(existing?.contactId) }
    var contactExpanded by remember { mutableStateOf(false) }
    var receiptImagePath by remember(existing) { mutableStateOf(existing?.receiptImagePath) }
    var tempCameraFile by remember { mutableStateOf(receiptImageManager.createTempImageFile()) }

    // Recurring state
    var isRecurring by remember(existing) { mutableStateOf(existing?.isRecurring ?: false) }
    var recurringFrequency by remember { mutableStateOf(RecurringFrequency.MONTHLY) }
    var recurringStartDate by remember { mutableStateOf(LocalDate.now()) }
    var frequencyExpanded by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }

    // Edit/delete scope dialog for existing recurring transactions (Req 20.3, 20.4)
    var showRecurringScopeDialog by remember { mutableStateOf(false) }
    var pendingDeleteRecurring by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val path = receiptImageManager.saveFromUri(it, existing?.id ?: 0L)
            if (path != null) receiptImagePath = path
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val saved = receiptImageManager.saveFromUri(
                FileProvider.getUriForFile(context, "${context.packageName}.provider", tempCameraFile),
                existing?.id ?: 0L
            )
            if (saved != null) receiptImagePath = saved
            tempCameraFile = receiptImageManager.createTempImageFile()
        }
    }

    var amountError by remember { mutableStateOf<String?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(type) {
        if (existing == null) {
            category = if (type == TransactionType.INCOME) IncomeCategory.SALES.label
                       else ExpenseCategory.RAW_MATERIALS.label
        }
    }

    val categoryOptions = if (type == TransactionType.INCOME)
        IncomeCategory.entries.map { it.label }
    else
        ExpenseCategory.entries.map { it.label }

    // Date picker state for recurring start date
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = recurringStartDate
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        recurringStartDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showStartDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Dialog: edit/delete scope for recurring transactions (Req 20.3, 20.4)
    if (showRecurringScopeDialog) {
        AlertDialog(
            onDismissRequest = { showRecurringScopeDialog = false },
            title = { Text(if (pendingDeleteRecurring) "Eliminar recurrente" else "Editar recurrente") },
            text = {
                Text(
                    if (pendingDeleteRecurring)
                        "¿Eliminar solo esta ocurrencia o cancelar todas las futuras?"
                    else
                        "¿Aplicar cambios solo a esta ocurrencia o a todas las futuras?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRecurringScopeDialog = false
                    if (pendingDeleteRecurring) {
                        existing?.let { viewModel.delete(it) }
                        // Cancel all future occurrences
                        existing?.recurringId?.let { viewModel.cancelRecurring(it) }
                    } else {
                        // Update all future: deactivate old template and save new one
                        existing?.recurringId?.let { viewModel.cancelRecurring(it) }
                        submitForm(
                            existing, type, amountText, category, description, currency,
                            exchangeRateText, selectedContactId, receiptImagePath, isRecurring,
                            recurringFrequency, recurringStartDate, viewModel
                        ) { amountError = it }
                    }
                    onNavigateBack()
                }) { Text(if (pendingDeleteRecurring) "Cancelar todas" else "Todas las futuras") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRecurringScopeDialog = false
                    if (pendingDeleteRecurring) {
                        existing?.let { viewModel.delete(it) }
                    } else {
                        submitForm(
                            existing, type, amountText, category, description, currency,
                            exchangeRateText, selectedContactId, receiptImagePath, false,
                            recurringFrequency, recurringStartDate, viewModel
                        ) { amountError = it }
                    }
                    onNavigateBack()
                }) { Text("Solo esta") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (transactionId != null) "Editar transacción" else "Nueva transacción") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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

            // Type selector
            Text("Tipo", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = type == TransactionType.INCOME,
                    onClick = { type = TransactionType.INCOME },
                    label = { Text("Ingreso") }
                )
                FilterChip(
                    selected = type == TransactionType.EXPENSE,
                    onClick = { type = TransactionType.EXPENSE },
                    label = { Text("Gasto") }
                )
            }

            // Amount
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it; amountError = null },
                label = { Text("Importe") },
                isError = amountError != null,
                supportingText = amountError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = { Text(currency.name, style = MaterialTheme.typography.labelMedium) }
            )

            // Currency selector
            Text("Moneda", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Currency.entries.forEach { c ->
                    FilterChip(
                        selected = currency == c,
                        onClick = { currency = c },
                        label = { Text(c.name) }
                    )
                }
            }

            // Exchange rate field — only shown for MLC and USD
            if (currency != Currency.CUP) {
                val rateLabel = if (currency == Currency.MLC) "Tasa MLC → CUP" else "Tasa USD → CUP"
                OutlinedTextField(
                    value = exchangeRateText,
                    onValueChange = { exchangeRateText = it },
                    label = { Text(rateLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        val rate = exchangeRateText.toDoubleOrNull() ?: 0.0
                        val cup = amount * rate
                        Text("≈ ${"%.2f".format(cup)} CUP",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                )
            }

            // Category dropdown
            ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    categoryOptions.forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = { category = option; categoryExpanded = false })
                    }
                }
            }

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // Contact selector (optional)
            if (contacts.isNotEmpty()) {
                val selectedContact = contacts.find { it.id == selectedContactId }
                ExposedDropdownMenuBox(expanded = contactExpanded, onExpandedChange = { contactExpanded = it }) {
                    OutlinedTextField(
                        value = selectedContact?.name ?: "Sin contacto",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Contacto (opcional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = contactExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = contactExpanded, onDismissRequest = { contactExpanded = false }) {
                        DropdownMenuItem(text = { Text("Sin contacto") }, onClick = { selectedContactId = null; contactExpanded = false })
                        contacts.forEach { contact ->
                            DropdownMenuItem(
                                text = { Text("${contact.name} (${contact.type.name})") },
                                onClick = { selectedContactId = contact.id; contactExpanded = false }
                            )
                        }
                    }
                }
            }

            // Receipt image section
            Text("Comprobante", style = MaterialTheme.typography.labelLarge)
            if (receiptImagePath != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AsyncImage(
                        model = receiptImagePath,
                        contentDescription = "Comprobante",
                        modifier = Modifier.size(80.dp).clickable { },
                        contentScale = ContentScale.Crop
                    )
                    IconButton(onClick = {
                        receiptImagePath?.let { receiptImageManager.delete(it) }
                        receiptImagePath = null
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar comprobante",
                            tint = MaterialTheme.colorScheme.error)
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", tempCameraFile)
                        cameraLauncher.launch(uri)
                    }) {
                        Icon(Icons.Filled.AttachFile, contentDescription = null)
                        Text("  Cámara")
                    }
                    OutlinedButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(Icons.Filled.AttachFile, contentDescription = null)
                        Text("  Galería")
                    }
                }
            }

            HorizontalDivider()

            // ── Recurring section (Req 20.1, 20.3, 20.4, 20.5) ──────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Repeat, contentDescription = null,
                        tint = if (isRecurring) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Transacción recurrente", style = MaterialTheme.typography.bodyMedium)
                }
                Switch(
                    checked = isRecurring,
                    onCheckedChange = { isRecurring = it },
                    // Disable toggle when editing an existing recurring transaction
                    enabled = existing?.isRecurring != true
                )
            }

            if (isRecurring) {
                // Frequency dropdown
                ExposedDropdownMenuBox(expanded = frequencyExpanded, onExpandedChange = { frequencyExpanded = it }) {
                    OutlinedTextField(
                        value = recurringFrequency.label(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frecuencia") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = frequencyExpanded, onDismissRequest = { frequencyExpanded = false }) {
                        RecurringFrequency.entries.forEach { freq ->
                            DropdownMenuItem(
                                text = { Text(freq.label()) },
                                onClick = { recurringFrequency = freq; frequencyExpanded = false }
                            )
                        }
                    }
                }

                // Start date picker
                OutlinedTextField(
                    value = recurringStartDate.format(dateFormatter),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha de inicio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showStartDatePicker = true },
                    enabled = false
                )
                TextButton(onClick = { showStartDatePicker = true }) {
                    Text("Cambiar fecha de inicio")
                }
            }
            // ─────────────────────────────────────────────────────────────────────

            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0.0) {
                        amountError = "El importe debe ser mayor que cero"
                        return@Button
                    }
                    // If editing an existing recurring transaction, show scope dialog (Req 20.3)
                    if (existing?.isRecurring == true) {
                        pendingDeleteRecurring = false
                        showRecurringScopeDialog = true
                        return@Button
                    }
                    submitForm(
                        existing, type, amountText, category, description, currency,
                        exchangeRateText, selectedContactId, receiptImagePath, isRecurring,
                        recurringFrequency, recurringStartDate, viewModel
                    ) { amountError = it }
                    if (uiState.errorMessage == null) onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                Text(if (transactionId != null) "Guardar cambios" else "Agregar transacción")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun RecurringFrequency.label(): String = when (this) {
    RecurringFrequency.DAILY     -> "Diaria"
    RecurringFrequency.WEEKLY    -> "Semanal"
    RecurringFrequency.BIWEEKLY  -> "Quincenal"
    RecurringFrequency.MONTHLY   -> "Mensual"
}

private fun submitForm(
    existing: com.osnordev.abaco.domain.model.Transaction?,
    type: TransactionType,
    amountText: String,
    category: String,
    description: String,
    currency: Currency,
    exchangeRateText: String,
    selectedContactId: Long?,
    receiptImagePath: String?,
    isRecurring: Boolean,
    recurringFrequency: RecurringFrequency,
    recurringStartDate: LocalDate,
    viewModel: TransactionViewModel,
    onAmountError: (String) -> Unit
) {
    val amount = amountText.toDoubleOrNull()
    if (amount == null || amount <= 0.0) {
        onAmountError("El importe debe ser mayor que cero")
        return
    }
    // Calculate amountCup using the exchange rate
    val rate = exchangeRateText.toDoubleOrNull()?.takeIf { it > 0.0 } ?: 1.0
    val amountCup = amount * rate

    val today = LocalDate.now()
    val transaction = Transaction(
        id = existing?.id ?: 0L,
        type = type,
        amount = amount,
        category = category,
        description = description.trim(),
        date = existing?.date ?: today,
        year = existing?.year ?: today.year,
        month = existing?.month ?: today.monthValue,
        currency = currency,
        amountCup = amountCup,
        contactId = selectedContactId,
        receiptImagePath = receiptImagePath
    )
    if (isRecurring && existing == null) {
        viewModel.saveWithRecurring(transaction, recurringFrequency, recurringStartDate)
    } else {
        viewModel.save(transaction)
    }
}
