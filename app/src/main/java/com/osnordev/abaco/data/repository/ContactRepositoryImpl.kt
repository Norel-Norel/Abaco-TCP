package com.osnordev.abaco.data.repository

import com.osnordev.abaco.data.local.ContactDao
import com.osnordev.abaco.data.local.ContactEntity
import com.osnordev.abaco.domain.model.Transaction
import com.osnordev.abaco.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

interface ContactRepository {
    fun getAll(): Flow<List<ContactEntity>>
    fun getAllByClient(clientId: Long): Flow<List<ContactEntity>>
    fun search(query: String): Flow<List<ContactEntity>>
    fun searchByClient(clientId: Long, query: String): Flow<List<ContactEntity>>
    suspend fun getById(id: Long): ContactEntity?
    suspend fun insert(entity: ContactEntity): Long
    suspend fun update(entity: ContactEntity)
    suspend fun delete(id: Long)
    suspend fun getTransactionHistory(contactId: Long): List<Transaction>
}

class ContactRepositoryImpl @Inject constructor(
    private val dao: ContactDao,
    private val transactionRepository: TransactionRepository
) : ContactRepository {
    override fun getAll(): Flow<List<ContactEntity>> = dao.getAll()
    override fun getAllByClient(clientId: Long): Flow<List<ContactEntity>> = dao.getAllByClient(clientId)
    override fun search(query: String): Flow<List<ContactEntity>> = dao.search(query)
    override fun searchByClient(clientId: Long, query: String): Flow<List<ContactEntity>> = dao.searchByClient(clientId, query)
    override suspend fun getById(id: Long): ContactEntity? = dao.getById(id)
    override suspend fun insert(entity: ContactEntity): Long = dao.insert(entity)
    override suspend fun update(entity: ContactEntity) = dao.update(entity)
    override suspend fun delete(id: Long) = dao.delete(id)

    override suspend fun getTransactionHistory(contactId: Long): List<Transaction> =
        transactionRepository.getAllTransactions().first()
            .filter { it.contactId == contactId }
}
