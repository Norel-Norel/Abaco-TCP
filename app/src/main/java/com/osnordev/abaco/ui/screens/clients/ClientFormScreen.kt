package com.osnordev.abaco.ui.screens.clients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientFormScreen(
    clientId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: ClientViewModel = hiltViewModel()
) {
    val clients by viewModel.clients.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val existing = remember(clientId, clients) {
        clientId?.let { id -> clients.find { it.id == id } }
    }

    var nombreNegocio by rememberSaveable(existing) { mutableStateOf(existing?.nombreNegocio ?: "") }
    var nit by rememberSaveable(existing) { mutableStateOf(existing?.nit ?: "") }
    var direccion by rememberSaveable(existing) { mutableStateOf(existing?.direccion ?: "") }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            viewModel.clearSaved()
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (clientId == null) "Nuevo Cliente" else "Editar Cliente") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Nombre del negocio
            val nombreError = uiState.nombreNegocioError
            OutlinedTextField(
                value = nombreNegocio,
                onValueChange = {
                    nombreNegocio = it
                    viewModel.clearFieldErrors()
                },
                label = { Text("Nombre del Negocio *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nombreError != null,
                supportingText = {
                    if (nombreError != null)
                        Text(nombreError, color = MaterialTheme.colorScheme.error)
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            // NIT — solo dígitos
            val nitError = uiState.nitError
            OutlinedTextField(
                value = nit,
                onValueChange = { input ->
                    nit = input.filter { it.isDigit() }
                    viewModel.clearFieldErrors()
                },
                label = { Text("NIT *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nitError != null,
                supportingText = {
                    if (nitError != null)
                        Text(nitError, color = MaterialTheme.colorScheme.error)
                    else
                        Text("Solo dígitos numéricos (mín. 8)")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Dirección (opcional)
            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("Opcional") }
            )

            Text(
                text = "* Campos obligatorios",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = {
                    if (clientId == null) {
                        viewModel.createClient(nombreNegocio, nit, direccion)
                    } else {
                        viewModel.updateClient(clientId, nombreNegocio, nit, direccion)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                Text(if (clientId == null) "Crear Cliente" else "Guardar Cambios")
            }
        }
    }
}
