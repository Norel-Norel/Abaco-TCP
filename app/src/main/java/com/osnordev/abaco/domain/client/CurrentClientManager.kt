package com.osnordev.abaco.domain.client

import com.osnordev.abaco.domain.repository.ActiveClientRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton que mantiene en memoria el ID del cliente contable activo.
 *
 * - Se inicializa leyendo el valor persistido en DataStore al arrancar.
 * - Expone [activeClientId] como [StateFlow] para que los ViewModels
 *   reaccionen reactivamente al cambio de cliente sin relanzar corrutinas.
 * - Actúa como caché en memoria: las lecturas son O(1) sin I/O.
 */
@Singleton
class CurrentClientManager @Inject constructor(
    private val activeClientRepository: ActiveClientRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _activeClientId = MutableStateFlow<Long?>(null)

    /** ID del cliente activo. Null si no hay ninguno seleccionado. */
    val activeClientId: StateFlow<Long?> = _activeClientId.asStateFlow()

    init {
        // Carga el valor persistido al iniciar la app
        scope.launch {
            activeClientRepository.getActiveClientId().collect { id ->
                _activeClientId.value = id
            }
        }
    }

    /**
     * Actualiza el cliente activo en memoria y lo persiste en DataStore.
     * Llamar desde [SwitchClientUseCase], no directamente desde la UI.
     */
    internal suspend fun setClient(id: Long) {
        _activeClientId.value = id
        activeClientRepository.setActiveClientId(id)
    }

    /**
     * Limpia el cliente activo en memoria y en DataStore.
     * Se usa cuando el cliente activo es eliminado.
     */
    internal suspend fun clearClient() {
        _activeClientId.value = null
        activeClientRepository.clearActiveClientId()
    }
}
