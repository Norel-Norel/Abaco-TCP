package com.osnordev.abaco.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

enum class InventoryMovementType { IN, OUT, ADJUSTMENT }

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val category: String = "",
    val unit: String = "unidad",          // unidad, kg, litro, etc.
    val quantity: Double = 0.0,
    val minStock: Double = 0.0,           // alerta de stock mínimo
    val costPrice: Double = 0.0,          // precio de costo en CUP
    val salePrice: Double = 0.0,          // precio de venta en CUP
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "inventory_movements")
data class InventoryMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long,
    val type: InventoryMovementType,
    val quantity: Double,
    val note: String = "",
    val date: java.time.LocalDate = java.time.LocalDate.now(),
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface InventoryDao {

    // Items
    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAllItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getItemById(id: Long): InventoryItemEntity?

    @Query("SELECT * FROM inventory_items WHERE quantity <= minStock AND minStock > 0 ORDER BY name ASC")
    fun getLowStockItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchItems(query: String): Flow<List<InventoryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItemEntity): Long

    @Update
    suspend fun updateItem(item: InventoryItemEntity)

    @Delete
    suspend fun deleteItem(item: InventoryItemEntity)

    // Movements
    @Query("SELECT * FROM inventory_movements WHERE itemId = :itemId ORDER BY date DESC, createdAt DESC")
    fun getMovementsForItem(itemId: Long): Flow<List<InventoryMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: InventoryMovementEntity): Long
}
