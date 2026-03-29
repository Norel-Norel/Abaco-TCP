package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.domain.model.Transaction
import com.osnordev.abaco.domain.repository.TransactionRepository
import javax.inject.Inject

class InsertTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val validate: ValidateTransactionUseCase
) {
    /**
     * Validates and inserts a transaction.
     * @return the new row id on success
     * @throws IllegalArgumentException if the transaction is invalid
     */
    suspend operator fun invoke(transaction: Transaction): Long {
        validate(transaction)
        return repository.insert(transaction)
    }
}
