package com.osnordev.abaco.data.repository

import com.osnordev.abaco.data.local.BudgetDao
import com.osnordev.abaco.data.local.BudgetEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface BudgetRepository {
    fun getByPeriod(month: Int, year: Int): Flow<List<BudgetEntity>>
    suspend fun getByCategory(category: String, month: Int, year: Int): BudgetEntity?
    suspend fun save(entity: BudgetEntity)
    suspend fun delete(id: Long)
}

class BudgetRepositoryImpl @Inject constructor(
    private val dao: BudgetDao
) : BudgetRepository {
    override fun getByPeriod(month: Int, year: Int): Flow<List<BudgetEntity>> =
        dao.getByPeriod(month, year)

    override suspend fun getByCategory(category: String, month: Int, year: Int): BudgetEntity? =
        dao.getByCategory(category, month, year)

    override suspend fun save(entity: BudgetEntity) {
        if (entity.id == 0L) dao.insert(entity) else dao.update(entity)
    }

    override suspend fun delete(id: Long) = dao.delete(id)
}
