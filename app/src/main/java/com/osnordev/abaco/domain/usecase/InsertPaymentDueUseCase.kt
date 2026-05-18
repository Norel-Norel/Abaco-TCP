package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.data.local.PaymentDueEntity
import com.osnordev.abaco.data.repository.PaymentDueRepository
import com.osnordev.abaco.domain.client.CurrentClientManager
import javax.inject.Inject

class InsertPaymentDueUseCase @Inject constructor(
    private val repository: PaymentDueRepository,
    private val currentClientManager: CurrentClientManager
) {
    suspend operator fun invoke(entity: PaymentDueEntity): Long {
        val clientId = currentClientManager.activeClientId.value ?: 1L
        return repository.insert(entity.copy(clientId = clientId))
    }
}
