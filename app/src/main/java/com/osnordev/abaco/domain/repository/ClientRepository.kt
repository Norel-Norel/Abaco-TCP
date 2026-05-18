package com.osnordev.abaco.domain.repository

import com.osnordev.abaco.data.local.ClientEntity
import kotlinx.coroutines.flow.Flow

interface ClientRepository {
    fun getAll(): Flow<List<ClientEntity>>
    suspend fun getById(id: Long): ClientEntity?
    suspend fun count(): Int
    /** @throws NitAlreadyExistsException si el NIT ya está registrado */
    suspend fun insert(entity: ClientEntity): Long
    /** @throws NitAlreadyExistsException si el nuevo NIT ya existe en otro cliente */
    suspend fun update(entity: ClientEntity)
    /** @throws LastClientException si es el único cliente registrado */
    suspend fun delete(id: Long)
}

class NitAlreadyExistsException(nit: String) :
    Exception("El NIT '$nit' ya está registrado en otro cliente")

class LastClientException :
    Exception("Debe existir al menos un cliente. No se puede eliminar el único cliente registrado")
