package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.data.local.ContactEntity
import com.osnordev.abaco.data.repository.ContactRepository
import com.osnordev.abaco.domain.client.CurrentClientManager
import javax.inject.Inject

class InsertContactUseCase @Inject constructor(
    private val repository: ContactRepository,
    private val currentClientManager: CurrentClientManager
) {
    suspend operator fun invoke(entity: ContactEntity): Long {
        val clientId = currentClientManager.activeClientId.value ?: 1L
        return repository.insert(entity.copy(clientId = clientId))
    }
}
