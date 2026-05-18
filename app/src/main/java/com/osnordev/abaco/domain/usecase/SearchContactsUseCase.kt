package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.data.local.ContactEntity
import com.osnordev.abaco.data.repository.ContactRepository
import com.osnordev.abaco.domain.client.CurrentClientManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class SearchContactsUseCase @Inject constructor(
    private val repository: ContactRepository,
    private val currentClientManager: CurrentClientManager
) {
    operator fun invoke(query: String): Flow<List<ContactEntity>> =
        currentClientManager.activeClientId.flatMapLatest { clientId ->
            when {
                clientId != null && query.isBlank() -> repository.getAllByClient(clientId)
                clientId != null -> repository.searchByClient(clientId, query)
                query.isBlank() -> repository.getAll()
                else -> repository.search(query)
            }
        }
}
