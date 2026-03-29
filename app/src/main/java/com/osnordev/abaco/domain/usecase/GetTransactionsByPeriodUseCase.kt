package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.domain.model.Transaction
import com.osnordev.abaco.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsByPeriodUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(year: Int, month: Int): Flow<List<Transaction>> =
        repository.getTransactionsByPeriod(year, month)
}
