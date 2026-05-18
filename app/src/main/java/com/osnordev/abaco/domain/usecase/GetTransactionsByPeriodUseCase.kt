package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.domain.client.CurrentClientManager
import com.osnordev.abaco.domain.model.Transaction
import com.osnordev.abaco.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class GetTransactionsByPeriodUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val currentClientManager: CurrentClientManager
) {
    operator fun invoke(year: Int, month: Int): Flow<List<Transaction>> =
        currentClientManager.activeClientId.flatMapLatest { clientId ->
            if (clientId != null) {
                repository.getTransactionsByPeriodAndClient(clientId, year, month)
            } else {
                repository.getTransactionsByPeriod(year, month)
            }
        }
}
