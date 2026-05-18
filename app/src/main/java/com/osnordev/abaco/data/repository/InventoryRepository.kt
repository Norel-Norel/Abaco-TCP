package com.osnordev.abaco.data.repository

import com.osnordev.abaco.data.local.InventoryDao
import com.osnordev.abaco.data.local.InventoryItemEntity
import com.osnordev.abaco.data.local.InventoryMovementEntity
import com.osnordev.abaco.data.local.InventoryMovementType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepository @Inject constructor(
    private val dao: InventoryDao
) {
    fun getAllItems(): Flow<List<InventoryItemEntity>> = dao.getAllItems()

    fun getAllItemsByClient(clientId: Long): Flow<List<InventoryItemEntity>> =
        dao.getAllItemsByClient(clientId)

    fun getLowStockItems(): Flow<List<InventoryItemEntity>> = dao.getLowStockItems()

    fun getLowStockItemsByClient(clientId: Long): Flow<List<InventoryItemEntity>> =
        dao.getLowStockItemsByClient(clientId)

    fun searchItems(query: String): Flow<List<InventoryItemEntity>> = dao.searchItems(query)

    fun searchItemsByClient(clientId: Long, query: String): Flow<List<InventoryItemEntity>> =
        dao.searchItemsByClient(clientId, query)

    fun getMovementsForItem(itemId: Long): Flow<List<InventoryMovementEntity>> =
        dao.getMovementsForItem(itemId)

    suspend fun getItemById(id: Long): InventoryItemEntity? = dao.getItemById(id)

    suspend fun saveItem(item: InventoryItemEntity): Long {
        return if (item.id == 0L) dao.insertItem(item)
        else { dao.updateItem(item); item.id }
    }

    suspend fun deleteItem(item: InventoryItemEntity) = dao.deleteItem(item)

    /**
     * Registers a movement and updates the item's quantity accordingly.
     */
    suspend fun registerMovement(movement: InventoryMovementEntity) {
        val item = dao.getItemById(movement.itemId) ?: return
        val newQty = when (movement.type) {
            InventoryMovementType.IN         -> item.quantity + movement.quantity
            InventoryMovementType.OUT        -> item.quantity - movement.quantity
            InventoryMovementType.ADJUSTMENT -> movement.quantity
        }
        dao.updateItem(item.copy(quantity = newQty, updatedAt = System.currentTimeMillis()))
        dao.insertMovement(movement)
    }
}
