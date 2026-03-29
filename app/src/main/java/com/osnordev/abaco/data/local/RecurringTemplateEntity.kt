package com.osnordev.abaco.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.osnordev.abaco.domain.model.TransactionType
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

enum class RecurringFrequency { DAILY, WEEKLY, BIWEEKLY, MONTHLY }

@Entity(tableName = "recurring_templates")
data class RecurringTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val amount: Double,
    val currency: String,
    val category: String,
    val description: String,
    val frequency: RecurringFrequency,
    val startDate: LocalDate,
    val nextDate: LocalDate,
    val isActive: Boolean = true
)

@Dao
interface RecurringTemplateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RecurringTemplateEntity): Long

    @Update
    suspend fun update(entity: RecurringTemplateEntity)

    @Query("SELECT * FROM recurring_templates WHERE isActive = 1 ORDER BY nextDate ASC")
    fun getActive(): Flow<List<RecurringTemplateEntity>>

    @Query("SELECT * FROM recurring_templates WHERE isActive = 1 AND nextDate <= :today")
    suspend fun getDue(today: String): List<RecurringTemplateEntity>

    @Query("SELECT * FROM recurring_templates WHERE id = :id")
    suspend fun getById(id: Long): RecurringTemplateEntity?

    @Query("UPDATE recurring_templates SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)

    @Query("DELETE FROM recurring_templates WHERE id = :id")
    suspend fun delete(id: Long)
}
