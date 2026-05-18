package com.osnordev.abaco.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Persiste y expone el ID del cliente contable activo.
 * El ID se guarda en DataStore para sobrevivir reinicios de la app.
 */
interface ActiveClientRepository {
    /** Flow reactivo del clientId activo. Emite null si no hay cliente seleccionado. */
    fun getActiveClientId(): Flow<Long?>

    /** Persiste el clientId activo en DataStore. */
    suspend fun setActiveClientId(id: Long)

    /** Borra el clientId activo (ej. tras eliminar el cliente seleccionado). */
    suspend fun clearActiveClientId()
}
