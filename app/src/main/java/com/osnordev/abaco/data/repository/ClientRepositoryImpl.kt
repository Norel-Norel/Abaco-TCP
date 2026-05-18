package com.osnordev.abaco.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.osnordev.abaco.data.local.ClientDao
import com.osnordev.abaco.data.local.ClientEntity
import com.osnordev.abaco.domain.repository.ClientRepository
import com.osnordev.abaco.domain.repository.LastClientException
import com.osnordev.abaco.domain.repository.NitAlreadyExistsException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ClientRepositoryImpl @Inject constructor(
    private val dao: ClientDao
) : ClientRepository {

    override fun getAll(): Flow<List<ClientEntity>> = dao.getAll()

    override suspend fun getById(id: Long): ClientEntity? = dao.getById(id)

    override suspend fun count(): Int = dao.count()

    override suspend fun insert(entity: ClientEntity): Long {
        return try {
            dao.insert(entity)
        } catch (e: SQLiteConstraintException) {
            throw NitAlreadyExistsException(entity.nit)
        }
    }

    override suspend fun update(entity: ClientEntity) {
        if (dao.nitExists(entity.nit, excludeId = entity.id)) {
            throw NitAlreadyExistsException(entity.nit)
        }
        dao.update(entity)
    }

    override suspend fun delete(id: Long) {
        if (dao.count() <= 1) throw LastClientException()
        dao.delete(id)
    }
}
