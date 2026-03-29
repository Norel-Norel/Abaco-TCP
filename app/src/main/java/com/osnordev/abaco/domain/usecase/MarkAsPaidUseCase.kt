package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.data.repository.PaymentDueRepository
import javax.inject.Inject

class MarkAsPaidUseCase @Inject constructor(
    private val repository: PaymentDueRepository
) {
    suspend operator fun invoke(id: Long) = repository.markAsPaid(id)
}
