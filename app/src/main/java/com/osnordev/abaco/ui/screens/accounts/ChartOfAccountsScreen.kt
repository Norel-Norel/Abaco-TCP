package com.osnordev.abaco.ui.screens.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.osnordev.abaco.data.local.ChartOfAccountEntity
import com.osnordev.abaco.domain.model.AccountType

@Composable
fun ChartOfAccountsScreen(
    viewModel: ChartOfAccountsViewModel = hiltViewModel()
) {
    val accounts by viewModel.accounts.collectAsState()
    var query by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val filtered = remember(accounts, query) {
        if (query.isBlank()) accounts
        else accounts.filter {
            it.code.startsWith(query, ignoreCase = true) ||
            it.name.contains(query, ignoreCase = true)
        }
    }

    val main = filtered.filter { it.parentCode == null }
    val subMap = filtered.filter { it.parentCode != null }.groupBy { it.parentCode }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar cuenta")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar cuenta") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                singleLine = true
            )

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay cuentas registradas", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    main.forEach { account ->
                        item {
                            AccountCard(
                                account = account,
                                isSubAccount = false,
                                onDelete = { viewModel.deactivate(account.code) }
                            )
                        }
                        val subs = subMap[account.code] ?: emptyList()
                        items(subs) { sub ->
                            AccountCard(
                                account = sub,
                                isSubAccount = true,
                                onDelete = { viewModel.deactivate(sub.code) }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showDialog) {
        AddAccountDialog(
            onDismiss = { showDialog = false },
            onConfirm = { code, name, type, nature, parentCode ->
                viewModel.addAccount(code, name, type, nature, parentCode)
                showDialog = false
            }
        )
    }
}

// ── Tarjeta de cuenta ─────────────────────────────────────────────────────────

@Composable
private fun AccountCard(
    account: ChartOfAccountEntity,
    isSubAccount: Boolean,
    onDelete: () -> Unit
) {
    val typeColor = when (account.type) {
        AccountType.ASSET     -> MaterialTheme.colorScheme.primary
        AccountType.LIABILITY -> MaterialTheme.colorScheme.error
        AccountType.EQUITY    -> MaterialTheme.colorScheme.tertiary
        AccountType.INCOME    -> MaterialTheme.colorScheme.secondary
        AccountType.EXPENSE   -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (isSubAccount) 24.dp else 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSubAccount)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = account.code,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (!isSubAccount) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
                if (account.parentCode != null) {
                    Text(
                        text = "Subcuenta de ${account.parentCode}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 4.dp)) {
                Text(text = account.type.name, style = MaterialTheme.typography.labelSmall, color = typeColor)
                Text(text = account.nature, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
            }
        }
    }
}

// ── Diálogo agregar cuenta ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (code: String, name: String, type: AccountType, nature: String, parentCode: String?) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var parentCode by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.ASSET) }
    var typeExpanded by remember { mutableStateOf(false) }
    var selectedNature by remember { mutableStateOf("DEBIT") }
    var natureExpanded by remember { mutableStateOf(false) }

    val isValid = code.isNotBlank() && name.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Cuenta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Código *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // Tipo
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        AccountType.entries.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.name) },
                                onClick = { selectedType = t; typeExpanded = false }
                            )
                        }
                    }
                }
                // Naturaleza
                ExposedDropdownMenuBox(
                    expanded = natureExpanded,
                    onExpandedChange = { natureExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedNature,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Naturaleza") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(natureExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = natureExpanded, onDismissRequest = { natureExpanded = false }) {
                        listOf("DEBIT", "CREDIT").forEach { n ->
                            DropdownMenuItem(
                                text = { Text(n) },
                                onClick = { selectedNature = n; natureExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = parentCode,
                    onValueChange = { parentCode = it },
                    label = { Text("Código cuenta padre (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(code, name, selectedType, selectedNature, parentCode.ifBlank { null }) },
                enabled = isValid
            ) { Text("Agregar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
