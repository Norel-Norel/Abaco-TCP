package com.osnordev.abaco.data.repository

import com.osnordev.abaco.data.local.PaymentDueDao
import com.osnordev.abaco.data.local.PaymentDueEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface PaymentDueRepository {
    fun getPending(): Flow<List<PaymentDueEntity>>
    fun getPendingByClient(clientId: Long): Flow<List<PaymentDueEntity>>
    fun getAll(): Flow<List<PaymentDueEntity>>
    suspend fun insert(entity: PaymentDueEntity): Long
    suspend fun markAsPaid(id: Long)
    suspend fun getById(id: Long): PaymentDueEntity?
    suspend fun delete(id: Long)
    suspend fun updateAlarmIds(id: Long, alarmId1: Int?, alarmId2: Int?)
}

class PaymentDueRepositoryImpl @Inject constructor(
    private val dao: PaymentDueDao
) : PaymentDueRepository {

    override fun getPending(): Flow<List<PaymentDueEntity>> = dao.getPending()
    override fun getPendingByClient(clientId: Long): Flow<List<PaymentDueEntity>> = dao.getPendingByClient(clientId)
    override fun getAll(): Flow<List<PaymentDueEntity>> = dao.getAll()
    override suspend fun insert(entity: PaymentDueEntity): Long = dao.insert(entity)
    override suspend fun getById(id: Long): PaymentDueEntity? = dao.getById(id)
    override suspend fun delete(id: Long) = dao.delete(id)

    override suspend fun markAsPaid(id: Long) {
        val entity = dao.getById(id) ?: return
        dao.update(entity.copy(isPaid = true, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun updateAlarmIds(id: Long, alarmId1: Int?, alarmId2: Int?) {
        val entity = dao.getById(id) ?: return
        dao.update(entity.copy(alarmId1 = alarmId1, alarmId2 = alarmId2))
    }
}
