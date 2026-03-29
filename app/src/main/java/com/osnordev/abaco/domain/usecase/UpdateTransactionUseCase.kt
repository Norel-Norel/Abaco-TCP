package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.domain.model.Transaction
import com.osnordev.abaco.domain.repository.TransactionRepository
import javax.inject.Inject

class UpdateTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val validate: ValidateTransactionUseCase
) {
    /**
     * Validates and updates an existing transaction.
     * @throws IllegalArgumentException if the transaction is invalid
     */
    suspend operator fun invoke(transaction: Transaction) {
        validate(transaction)
        repository.update(transaction)
    }
}
