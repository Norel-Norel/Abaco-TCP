package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.data.local.PaymentDueEntity
import com.osnordev.abaco.data.repository.PaymentDueRepository
import javax.inject.Inject

class InsertPaymentDueUseCase @Inject constructor(
    private val repository: PaymentDueRepository
) {
    suspend operator fun invoke(entity: PaymentDueEntity): Long = repository.insert(entity)
}
