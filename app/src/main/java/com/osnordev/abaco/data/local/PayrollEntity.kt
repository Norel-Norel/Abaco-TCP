package com.osnordev.abaco.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "payroll_records")
data class PayrollRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeName: String,
    val ci: String,
    val baseSalary: Double,
    val grossSalary: Double,
    val cssEmployee: Double,
    val iipRetained: Double,
    val netSalary: Double,
    val cssEmployer: Double,
    val holidayProvision: Double,
    val subsidyProvision: Double,
    val specialSS: Double,
    val totalCompanyCost: Double,
    val period: String,
    val createdAt: Long = System.currentTimeMillis(),
    @androidx.room.ColumnInfo(defaultValue = "1")
    val clientId: Long = 1L
)

@Dao
interface PayrollDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: PayrollRecordEntity): Long

    @Query("SELECT * FROM payroll_records ORDER BY createdAt DESC")
    fun getAll(): Flow<List<PayrollRecordEntity>>

    @Query("SELECT * FROM payroll_records WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getAllByClient(clientId: Long): Flow<List<PayrollRecordEntity>>

    @Query("SELECT * FROM payroll_records WHERE period = :period ORDER BY createdAt DESC")
    fun getByPeriod(period: String): Flow<List<PayrollRecordEntity>>

    @Query("SELECT * FROM payroll_records WHERE clientId = :clientId AND period = :period ORDER BY createdAt DESC")
    fun getByPeriodAndClient(clientId: Long, period: String): Flow<List<PayrollRecordEntity>>

    @Query("DELETE FROM payroll_records WHERE id = :id")
    suspend fun delete(id: Long)
}
