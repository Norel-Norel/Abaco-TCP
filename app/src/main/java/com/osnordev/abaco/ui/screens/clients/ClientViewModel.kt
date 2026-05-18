package com.osnordev.abaco.ui.screens.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osnordev.abaco.data.local.ClientEntity
import com.osnordev.abaco.domain.client.CurrentClientManager
import com.osnordev.abaco.domain.repository.ClientRepository
import com.osnordev.abaco.domain.repository.LastClientException
import com.osnordev.abaco.domain.repository.NitAlreadyExistsException
import com.osnordev.abaco.domain.usecase.InitClientChartOfAccountsUseCase
import com.osnordev.abaco.domain.usecase.SwitchClientUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClientUiState(
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    // Errores por campo
    val nombreNegocioError: String? = null,
    val nitError: String? = null
)

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val switchClientUseCase: SwitchClientUseCase,
    private val currentClientManager: CurrentClientManager,
    private val initChartOfAccounts: InitClientChartOfAccountsUseCase
) : ViewModel() {

    val clients: StateFlow<List<ClientEntity>> = clientRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeClientId: StateFlow<Long?> = currentClientManager.activeClientId

    private val _uiState = MutableStateFlow(ClientUiState())
    val uiState: StateFlow<ClientUiState> = _uiState.asStateFlow()

    fun selectClient(id: Long) {
        viewModelScope.launch { switchClientUseCase(id) }
    }

    fun createClient(nombreNegocio: String, nit: String, direccion: String) {
        if (!validate(nombreNegocio, nit)) return
        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                val newId = clientRepository.insert(
                    ClientEntity(nombreNegocio = nombreNegocio.trim(), nit = nit.trim(), direccion = direccion.trim())
                )
                initChartOfAccounts(newId)
                _uiState.update { it.copy(isSaving = false, saved = true) }
            } catch (e: NitAlreadyExistsException) {
                _uiState.update { it.copy(isSaving = false, nitError = e.message) }
            }
        }
    }

    fun updateClient(id: Long, nombreNegocio: String, nit: String, direccion: String) {
        if (!validate(nombreNegocio, nit)) return
        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                clientRepository.update(
                    ClientEntity(id = id, nombreNegocio = nombreNegocio.trim(), nit = nit.trim(), direccion = direccion.trim())
                )
                _uiState.update { it.copy(isSaving = false, saved = true) }
            } catch (e: NitAlreadyExistsException) {
                _uiState.update { it.copy(isSaving = false, nitError = e.message) }
            }
        }
    }

    fun deleteClient(id: Long) {
        viewModelScope.launch {
            try {
                clientRepository.delete(id)
                if (currentClientManager.activeClientId.value == id) {
                    switchClientUseCase(null)
                }
            } catch (e: LastClientException) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    /** Valida los campos y actualiza los errores por campo. Devuelve true si todo es válido. */
    private fun validate(nombreNegocio: String, nit: String): Boolean {
        val nombreError = when {
            nombreNegocio.isBlank() -> "El nombre del negocio es obligatorio"
            nombreNegocio.trim().length < 2 -> "Debe tener al menos 2 caracteres"
            else -> null
        }
        val nitError = when {
            nit.isBlank() -> "El NIT es obligatorio"
            !nit.all { it.isDigit() } -> "El NIT solo puede contener dígitos"
            nit.length < 8 -> "El NIT debe tener al menos 8 dígitos"
            else -> null
        }
        _uiState.update { it.copy(nombreNegocioError = nombreError, nitError = nitError) }
        return nombreError == null && nitError == null
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearSaved() = _uiState.update { it.copy(saved = false) }
    fun clearFieldErrors() = _uiState.update { it.copy(nombreNegocioError = null, nitError = null) }
}
