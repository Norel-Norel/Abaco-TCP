package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.domain.client.CurrentClientManager
import javax.inject.Inject

/**
 * Cambia el cliente contable activo.
 *
 * Responsabilidades:
 * 1. Actualiza [CurrentClientManager] en memoria (StateFlow).
 * 2. Persiste el nuevo clientId en DataStore.
 * 3. Limpia cualquier caché temporal de UI (extensible).
 *
 * Los ViewModels que observan [CurrentClientManager.activeClientId]
 * reaccionarán automáticamente al cambio sin necesidad de acción adicional.
 *
 * Uso:
 * ```kotlin
 * viewModelScope.launch { switchClientUseCase(clientId) }
 * ```
 */
class SwitchClientUseCase @Inject constructor(
    private val currentClientManager: CurrentClientManager
) {
    /**
     * @param clientId ID del cliente al que se desea cambiar.
     *                 Pasar null para limpiar el cliente activo.
     */
    suspend operator fun invoke(clientId: Long?) {
        if (clientId != null) {
            currentClientManager.setClient(clientId)
        } else {
            currentClientManager.clearClient()
        }
    }
}
