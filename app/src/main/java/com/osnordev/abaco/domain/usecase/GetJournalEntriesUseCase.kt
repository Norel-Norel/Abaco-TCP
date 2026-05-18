package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.data.local.JournalEntryWithLines
import com.osnordev.abaco.domain.client.CurrentClientManager
import com.osnordev.abaco.domain.repository.JournalEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class GetJournalEntriesUseCase @Inject constructor(
    private val repository: JournalEntryRepository,
    private val currentClientManager: CurrentClientManager
) {
    /**
     * Retorna los asientos del cliente activo.
     * Si no hay cliente activo (null), retorna todos (compatibilidad legacy).
     */
    operator fun invoke(): Flow<List<JournalEntryWithLines>> =
        currentClientManager.activeClientId.flatMapLatest { clientId ->
            if (clientId != null) {
                repository.getAllEntriesByClient(clientId)
            } else {
                repository.getAllEntries()
            }
        }
}
