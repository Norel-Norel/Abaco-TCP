package com.osnordev.abaco.data.repository

import com.osnordev.abaco.data.local.TransactionDao
import com.osnordev.abaco.data.local.TransactionEntity
import com.osnordev.abaco.domain.model.SyncStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote DTO that mirrors the `transactions` table in Supabase.
 * Column names use snake_case to match the PostgreSQL schema.
 */
@Serializable
data class RemoteTransaction(
    val id: Long = 0,
    val type: String,
    val amount: Double,
    val category: String,
    val description: String = "",
    val date: String,
    val year: Int,
    val month: Int,
    val currency: String = "CUP",
    @SerialName("amount_cup") val amountCup: Double = amount,
    @SerialName("contact_id") val contactId: Long? = null,
    @SerialName("receipt_image_path") val receiptImagePath: String? = null,
    @SerialName("is_recurring") val isRecurring: Boolean = false,
    @SerialName("recurring_id") val recurringId: Long? = null,
    @SerialName("updated_at") val updatedAt: Long,
    @SerialName("sync_status") val syncStatus: String = "SYNCED"
)

/**
 * Handles bidirectional sync between Room and Supabase.
 * Conflict resolution: "last write wins" based on [updatedAt].
 * Requirements: 12.1, 12.4
 */
@Singleton
class SyncRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val supabase: SupabaseClient
) {

    /**
     * Pushes all locally PENDING transactions to Supabase.
     * On success, marks them as SYNCED locally.
     * Requirements: 12.1, 12.2
     */
    suspend fun pushPending() {
        val allLocal = transactionDao.getAllSync()
        val pendingTxs = allLocal.filter { it.syncStatus == SyncStatus.PENDING.name }

        if (pendingTxs.isEmpty()) return

        val remoteList = pendingTxs.map { it.toRemote() }

        supabase.from("transactions").upsert(remoteList) {
            onConflict = "id"
        }

        // Mark as synced
        pendingTxs.forEach { tx ->
            transactionDao.update(tx.copy(syncStatus = SyncStatus.SYNCED.name))
        }
    }

    /**
     * Pulls remote transactions and merges using last-write-wins on [updatedAt].
     * Requirements: 12.1, 12.4
     */
    suspend fun pullRemote() {
        val remoteList = supabase.from("transactions")
            .select()
            .decodeList<RemoteTransaction>()

        remoteList.forEach { remote ->
            val local = transactionDao.getById(remote.id)
            if (local == null || remote.updatedAt > local.updatedAt) {
                // Remote is newer — upsert locally
                transactionDao.insert(remote.toEntity())
            }
            // If local is newer, it will be pushed on next pushPending()
        }
    }

    private fun TransactionEntity.toRemote() = RemoteTransaction(
        id = id,
        type = type.name,
        amount = amount,
        category = category,
        description = description,
        date = date.toString(),
        year = year,
        month = month,
        currency = currency,
        amountCup = amountCup,
        contactId = contactId,
        receiptImagePath = receiptImagePath,
        isRecurring = isRecurring,
        recurringId = recurringId,
        updatedAt = updatedAt,
        syncStatus = "SYNCED"
    )

    private fun RemoteTransaction.toEntity(): TransactionEntity {
        val txType = com.osnordev.abaco.domain.model.TransactionType.valueOf(type)
        return TransactionEntity(
            id = id,
            type = txType,
            amount = amount,
            category = category,
            description = description,
            date = java.time.LocalDate.parse(date),
            year = year,
            month = month,
            currency = currency,
            amountCup = amountCup,
            contactId = contactId,
            receiptImagePath = receiptImagePath,
            isRecurring = isRecurring,
            recurringId = recurringId,
            updatedAt = updatedAt,
            syncStatus = SyncStatus.SYNCED.name
        )
    }
}
