package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.domain.model.Transaction
import javax.inject.Inject

class ValidateTransactionUseCase @Inject constructor() {
    /**
     * Validates a transaction before persistence.
     * @throws IllegalArgumentException if amount is <= 0
     */
    operator fun invoke(transaction: Transaction) {
        require(transaction.amount > 0.0) {
            "El importe debe ser mayor que cero"
        }
    }
}
