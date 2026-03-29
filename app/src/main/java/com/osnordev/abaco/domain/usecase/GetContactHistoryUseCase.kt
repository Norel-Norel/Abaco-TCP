package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.data.repository.ContactRepository
import com.osnordev.abaco.domain.model.Transaction
import javax.inject.Inject

class GetContactHistoryUseCase @Inject constructor(
    private val repository: ContactRepository
) {
    suspend operator fun invoke(contactId: Long): List<Transaction> =
        repository.getTransactionHistory(contactId)
}
