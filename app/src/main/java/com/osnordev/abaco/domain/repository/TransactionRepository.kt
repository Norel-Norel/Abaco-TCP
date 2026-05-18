package com.osnordev.abaco.domain.repository

import com.osnordev.abaco.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getTransactionsByPeriod(year: Int, month: Int): Flow<List<Transaction>>
    fun getTransactionsByPeriodAndClient(clientId: Long, year: Int, month: Int): Flow<List<Transaction>>
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getAllTransactionsByClient(clientId: Long): Flow<List<Transaction>>
    suspend fun insert(transaction: Transaction): Long
    suspend fun update(transaction: Transaction)
    suspend fun delete(transaction: Transaction)
    suspend fun deleteById(id: Long)
}
