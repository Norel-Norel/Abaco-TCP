package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.domain.model.Transaction
import com.osnordev.abaco.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction) =
        repository.delete(transaction)
}
