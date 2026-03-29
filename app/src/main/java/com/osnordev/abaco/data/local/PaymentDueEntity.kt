package com.osnordev.abaco.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.osnordev.abaco.domain.model.Currency
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "payment_dues")
data class PaymentDueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val amount: Double,
    val currency: String = Currency.CUP.name,
    val dueDate: LocalDate,
    val isPaid: Boolean = false,
    val alarmId1: Int? = null,   // 1 día antes
    val alarmId2: Int? = null,   // día del vencimiento
    val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface PaymentDueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PaymentDueEntity): Long

    @Update
    suspend fun update(entity: PaymentDueEntity)

    @Query("SELECT * FROM payment_dues ORDER BY dueDate ASC")
    fun getAll(): Flow<List<PaymentDueEntity>>

    @Query("SELECT * FROM payment_dues WHERE isPaid = 0 ORDER BY dueDate ASC")
    fun getPending(): Flow<List<PaymentDueEntity>>

    @Query("SELECT * FROM payment_dues WHERE id = :id")
    suspend fun getById(id: Long): PaymentDueEntity?

    @Query("DELETE FROM payment_dues WHERE id = :id")
    suspend fun delete(id: Long)
}
