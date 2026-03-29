package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.data.local.PaymentDueEntity
import com.osnordev.abaco.data.repository.PaymentDueRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPendingPaymentsUseCase @Inject constructor(
    private val repository: PaymentDueRepository
) {
    operator fun invoke(): Flow<List<PaymentDueEntity>> = repository.getPending()
}
