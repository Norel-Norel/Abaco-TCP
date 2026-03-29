package com.osnordev.abaco.data.repository

import com.osnordev.abaco.data.local.TransactionDao
import com.osnordev.abaco.data.local.TransactionEntity
import com.osnordev.abaco.domain.model.Transaction
import com.osnordev.abaco.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun getTransactionsByPeriod(year: Int, month: Int): Flow<List<Transaction>> =
        dao.getByPeriod(year, month).map { entities -> entities.map { it.toDomain() } }

    override fun getAllTransactions(): Flow<List<Transaction>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun insert(transaction: Transaction): Long =
        dao.insert(TransactionEntity.fromDomain(transaction))

    override suspend fun update(transaction: Transaction) =
        dao.update(TransactionEntity.fromDomain(transaction))

    override suspend fun delete(transaction: Transaction) =
        dao.delete(TransactionEntity.fromDomain(transaction))

    override suspend fun deleteById(id: Long) =
        dao.deleteById(id)
}
