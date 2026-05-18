package com.osnordev.abaco.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Entidad raíz del modelo multi-tenant.
 * Cada cliente representa un TCP cubano gestionado por el contador.
 */
@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombreNegocio: String,
    /** NIT / RUT del TCP — único en la BD */
    val nit: String,
    val direccion: String = "",
    /** URI de la imagen de logo (nullable) */
    val logoUri: String? = null
)

@Dao
interface ClientDao {

    @Query("SELECT * FROM clients ORDER BY nombreNegocio ASC")
    fun getAll(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id")
    suspend fun getById(id: Long): ClientEntity?

    @Query("SELECT COUNT(*) FROM clients")
    suspend fun count(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM clients WHERE nit = :nit AND id != :excludeId)")
    suspend fun nitExists(nit: String, excludeId: Long = 0): Boolean

    /** @throws android.database.sqlite.SQLiteConstraintException si el NIT ya existe */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ClientEntity): Long

    @Update
    suspend fun update(entity: ClientEntity)

    @Query("DELETE FROM clients WHERE id = :id")
    suspend fun delete(id: Long)
}
