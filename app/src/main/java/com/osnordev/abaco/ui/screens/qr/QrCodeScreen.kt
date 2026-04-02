package com.osnordev.abaco.ui.screens.qr

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Screen for generating and sharing a payment QR code.
 * Requirements: 24.1, 24.2, 24.3, 24.4, 24.5
 */
@Composable
fun QrCodeScreen(
    viewModel: QrCodeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("QR de Cobro", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Genera un código QR con tus datos de cobro para que tus clientes puedan pagarte fácilmente.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Form fields (Req 24.1)
        OutlinedTextField(
            value = state.accountNumber,
            onValueChange = viewModel::onAccountNumberChange,
            label = { Text("Número de cuenta bancaria") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = state.accountNumberError != null,
            supportingText = state.accountNumberError?.let { { Text(it) } },
            placeholder = { Text("16 dígitos") }
        )
        OutlinedTextField(
            value = state.phone,
            onValueChange = viewModel::onPhoneChange,
            label = { Text("Número de teléfono") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = state.phoneError != null,
            supportingText = state.phoneError?.let { { Text(it) } },
            placeholder = { Text("+53...") }
        )
        OutlinedTextField(
            value = state.holderName,
            onValueChange = viewModel::onHolderNameChange,
            label = { Text("Nombre del titular") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = viewModel::generateQr,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.accountNumber.isNotBlank() && state.phone.isNotBlank() && state.holderName.isNotBlank()
        ) {
            Icon(Icons.Filled.QrCode, contentDescription = null)
            Text("  Generar QR")
        }

        // QR display (Req 24.3)
        state.qrBitmap?.let { bitmap ->
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Código QR de cobro",
                modifier = Modifier.size(280.dp)
            )
            Text(
                text = state.holderName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Share button (Req 24.4)
            OutlinedButton(
                onClick = {
                    viewModel.shareQr(context)?.let { intent ->
                        context.startActivity(Intent.createChooser(intent, "Compartir QR"))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Share, contentDescription = null)
                Text("  Compartir QR")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
