package com.osnordev.abaco.data.repository

import com.osnordev.abaco.data.local.TransactionDao
import com.osnordev.abaco.data.local.TransactionEntity
import com.osnordev.abaco.domain.client.CurrentClientManager
import com.osnordev.abaco.domain.model.Transaction
import com.osnordev.abaco.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao,
    private val currentClientManager: CurrentClientManager
) : TransactionRepository {

    override fun getTransactionsByPeriod(year: Int, month: Int): Flow<List<Transaction>> =
        dao.getByPeriod(year, month).map { it.map { e -> e.toDomain() } }

    override fun getTransactionsByPeriodAndClient(clientId: Long, year: Int, month: Int): Flow<List<Transaction>> =
        dao.getByPeriodAndClient(clientId, year, month).map { it.map { e -> e.toDomain() } }

    override fun getAllTransactions(): Flow<List<Transaction>> =
        dao.getAll().map { it.map { e -> e.toDomain() } }

    override fun getAllTransactionsByClient(clientId: Long): Flow<List<Transaction>> =
        dao.getAllByClient(clientId).map { it.map { e -> e.toDomain() } }

    override suspend fun insert(transaction: Transaction): Long {
        val clientId = currentClientManager.activeClientId.value ?: 1L
        return dao.insert(TransactionEntity.fromDomain(transaction).copy(clientId = clientId))
    }

    override suspend fun update(transaction: Transaction) {
        // Preserve existing clientId on update — read it from DB first
        val existing = dao.getById(transaction.id)
        val clientId = existing?.clientId ?: currentClientManager.activeClientId.value ?: 1L
        // Reset syncStatus to PENDING so the change gets pushed to Supabase
        dao.update(
            TransactionEntity.fromDomain(transaction).copy(
                clientId = clientId,
                syncStatus = com.osnordev.abaco.domain.model.SyncStatus.PENDING.name
            )
        )
    }

    override suspend fun delete(transaction: Transaction) =
        dao.delete(TransactionEntity.fromDomain(transaction))

    override suspend fun deleteById(id: Long) =
        dao.deleteById(id)
}
