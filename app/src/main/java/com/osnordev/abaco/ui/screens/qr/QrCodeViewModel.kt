package com.osnordev.abaco.ui.screens.qr

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osnordev.abaco.domain.model.PaymentQrData
import com.osnordev.abaco.domain.qr.QrCodeGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class QrUiState(
    val accountNumber: String = "",
    val phone: String = "",
    val holderName: String = "",
    val qrBitmap: Bitmap? = null,
    val accountNumberError: String? = null,
    val phoneError: String? = null
)

@HiltViewModel
class QrCodeViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val qrGenerator: QrCodeGenerator
) : ViewModel() {

    private val key = stringPreferencesKey("payment_qr_data")

    private val _uiState = MutableStateFlow(QrUiState())
    val uiState: StateFlow<QrUiState> = _uiState

    init {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            prefs[key]?.let { json ->
                try {
                    val saved = Json.decodeFromString<PaymentQrData>(json)
                    _uiState.update {
                        it.copy(
                            accountNumber = saved.accountNumber,
                            phone = saved.phone,
                            holderName = saved.holderName
                        )
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun onAccountNumberChange(value: String) {
        // Solo dígitos, máximo 16 caracteres
        val filtered = value.filter { it.isDigit() }.take(16)
        _uiState.update { it.copy(accountNumber = filtered, qrBitmap = null, accountNumberError = null) }
    }

    fun onPhoneChange(value: String) {
        // Permite "+53" seguido de dígitos, máximo 12 caracteres (+53 + 8 dígitos)
        val filtered = when {
            value.isEmpty() -> ""
            value == "+" -> "+"
            value.startsWith("+") -> "+" + value.drop(1).filter { it.isDigit() }.take(11)
            else -> value.filter { it.isDigit() }.take(11)
        }
        _uiState.update { it.copy(phone = filtered, qrBitmap = null, phoneError = null) }
    }
    fun onHolderNameChange(value: String) = _uiState.update { it.copy(holderName = value, qrBitmap = null) }

    /**
     * Generates the QR bitmap and persists the data for future sessions.
     * Requirements: 24.2, 24.5
     * Validates: account number must be exactly 16 digits; phone must start with +53
     */
    fun generateQr() {
        val state = _uiState.value
        val account = state.accountNumber.trim()
        val phone = state.phone.trim()

        // Validate account number: exactly 16 digits
        val accountError = when {
            account.length != 16 -> "La cuenta debe tener exactamente 16 dígitos"
            !account.all { it.isDigit() } -> "La cuenta solo puede contener dígitos"
            else -> null
        }
        // Validate phone: must start with +53
        val phoneError = if (!phone.startsWith("+53")) "El teléfono debe empezar por +53" else null

        if (accountError != null || phoneError != null) {
            _uiState.update { it.copy(accountNumberError = accountError, phoneError = phoneError) }
            return
        }

        val data = PaymentQrData(
            accountNumber = state.accountNumber.trim(),
            phone = state.phone.trim(),
            holderName = state.holderName.trim()
        )
        val bitmap = qrGenerator.generate(data)
        _uiState.update { it.copy(qrBitmap = bitmap) }

        viewModelScope.launch {
            dataStore.edit { prefs -> prefs[key] = Json.encodeToString(data) }
        }
    }

    /**
     * Exports the QR bitmap as PNG and returns a share Intent.
     * Requirements: 24.4
     */
    fun shareQr(context: Context): Intent? {
        val bitmap = _uiState.value.qrBitmap ?: return null
        val file = File(context.cacheDir, "qr_cobro.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
