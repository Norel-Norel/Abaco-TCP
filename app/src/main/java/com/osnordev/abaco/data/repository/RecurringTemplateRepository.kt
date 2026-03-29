package com.osnordev.abaco.data.repository

import com.osnordev.abaco.data.local.RecurringTemplateDao
import com.osnordev.abaco.data.local.RecurringTemplateEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringTemplateRepository @Inject constructor(
    private val dao: RecurringTemplateDao
) {
    fun getActive(): Flow<List<RecurringTemplateEntity>> = dao.getActive()
    suspend fun insert(entity: RecurringTemplateEntity): Long = dao.insert(entity)
    suspend fun update(entity: RecurringTemplateEntity) = dao.update(entity)
    suspend fun deactivate(id: Long) = dao.deactivate(id)
    suspend fun delete(id: Long) = dao.delete(id)
}
