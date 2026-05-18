package com.osnordev.abaco.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.osnordev.abaco.domain.model.AccountType
import kotlinx.coroutines.flow.Flow

/**
 * Plan de Cuentas del TCP cubano.
 * Soporta cuentas principales y subcuentas mediante [parentCode].
 */
@Entity(tableName = "chart_of_accounts")
data class ChartOfAccountEntity(
    @PrimaryKey val code: String,
    val name: String,
    val type: AccountType,
    /** DEBIT = naturaleza deudora, CREDIT = naturaleza acreedora */
    val nature: String,
    /** Código de la cuenta padre; null = cuenta principal */
    val parentCode: String? = null,
    val isActive: Boolean = true,
    /** ID del cliente al que pertenece esta cuenta. DEFAULT 1 para datos legacy. */
    @androidx.room.ColumnInfo(defaultValue = "1")
    val clientId: Long = 1L
)

@Dao
interface ChartOfAccountDao {

    @Query("SELECT * FROM chart_of_accounts WHERE isActive = 1 ORDER BY code ASC")
    fun getAll(): Flow<List<ChartOfAccountEntity>>

    /** Filtrado por cliente activo */
    @Query("SELECT * FROM chart_of_accounts WHERE isActive = 1 AND clientId = :clientId ORDER BY code ASC")
    fun getAllByClient(clientId: Long): Flow<List<ChartOfAccountEntity>>

    @Query("""
        SELECT * FROM chart_of_accounts
        WHERE isActive = 1
          AND (code LIKE :query || '%' OR name LIKE '%' || :query || '%')
        ORDER BY code ASC
        LIMIT 20
    """)
    suspend fun search(query: String): List<ChartOfAccountEntity>

    /** Búsqueda filtrada por cliente */
    @Query("""
        SELECT * FROM chart_of_accounts
        WHERE isActive = 1 AND clientId = :clientId
          AND (code LIKE :query || '%' OR name LIKE '%' || :query || '%')
        ORDER BY code ASC
        LIMIT 20
    """)
    suspend fun searchByClient(clientId: Long, query: String): List<ChartOfAccountEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(accounts: List<ChartOfAccountEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(account: ChartOfAccountEntity)

    @Query("UPDATE chart_of_accounts SET isActive = 0 WHERE code = :code")
    suspend fun deactivate(code: String)

    @Query("SELECT COUNT(*) FROM chart_of_accounts")
    suspend fun count(): Int
}
