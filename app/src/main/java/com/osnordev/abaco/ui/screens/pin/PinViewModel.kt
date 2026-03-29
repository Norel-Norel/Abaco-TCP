package com.osnordev.abaco.ui.screens.pin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osnordev.abaco.data.repository.PinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PinUiState(
    val pin: String = "",
    val isLocked: Boolean = false,
    val lockCountdown: Int = 0,   // seconds remaining
    val failedAttempts: Int = 0,
    val isUnlocked: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PinViewModel @Inject constructor(
    private val pinRepository: PinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinUiState())
    val uiState: StateFlow<PinUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    fun onDigit(digit: String) {
        if (_uiState.value.isLocked) return
        val current = _uiState.value.pin
        if (current.length < 6) {
            _uiState.update { it.copy(pin = current + digit, errorMessage = null) }
        }
    }

    fun onDelete() {
        val current = _uiState.value.pin
        if (current.isNotEmpty()) {
            _uiState.update { it.copy(pin = current.dropLast(1), errorMessage = null) }
        }
    }

    fun onSubmit() {
        val pin = _uiState.value.pin
        if (pin.length < 4) {
            _uiState.update { it.copy(errorMessage = "El PIN debe tener al menos 4 dígitos") }
            return
        }
        if (pinRepository.verifyPin(pin)) {
            _uiState.update { it.copy(isUnlocked = true, failedAttempts = 0, pin = "") }
        } else {
            val attempts = _uiState.value.failedAttempts + 1
            if (attempts >= 5) {
                startLockout()
            } else {
                _uiState.update {
                    it.copy(
                        pin = "",
                        failedAttempts = attempts,
                        errorMessage = "PIN incorrecto. Intentos restantes: ${5 - attempts}"
                    )
                }
            }
        }
    }

    fun setPin(pin: String) {
        pinRepository.setPin(pin)
    }

    fun clearPin() {
        pinRepository.clearPin()
    }

    private fun startLockout() {
        _uiState.update { it.copy(isLocked = true, lockCountdown = 30, pin = "", errorMessage = null) }
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (remaining in 29 downTo 0) {
                delay(1_000)
                _uiState.update { it.copy(lockCountdown = remaining) }
            }
            _uiState.update { it.copy(isLocked = false, failedAttempts = 0, lockCountdown = 0) }
        }
    }
}
