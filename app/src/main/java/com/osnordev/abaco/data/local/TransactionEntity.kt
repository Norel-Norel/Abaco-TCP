package com.osnordev.abaco.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.osnordev.abaco.domain.model.Currency
import com.osnordev.abaco.domain.model.SyncStatus
import com.osnordev.abaco.domain.model.Transaction
import com.osnordev.abaco.domain.model.TransactionType
import java.time.LocalDate

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val amount: Double,
    val category: String,
    val description: String,
    val date: LocalDate,
    val year: Int,
    val month: Int,
    // v2 fields
    val currency: String = Currency.CUP.name,
    val amountCup: Double = amount,
    val contactId: Long? = null,
    val receiptImagePath: String? = null,
    val isRecurring: Boolean = false,
    val recurringId: Long? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = SyncStatus.PENDING.name
) {
    fun toDomain(): Transaction = Transaction(
        id = id,
        type = type,
        amount = amount,
        category = category,
        description = description,
        date = date,
        year = year,
        month = month,
        currency = Currency.valueOf(currency),
        amountCup = amountCup,
        contactId = contactId,
        receiptImagePath = receiptImagePath,
        isRecurring = isRecurring,
        recurringId = recurringId,
        updatedAt = updatedAt,
        syncStatus = SyncStatus.valueOf(syncStatus)
    )

    companion object {
        fun fromDomain(t: Transaction): TransactionEntity = TransactionEntity(
            id = t.id,
            type = t.type,
            amount = t.amount,
            category = t.category,
            description = t.description,
            date = t.date,
            year = t.year,
            month = t.month,
            currency = t.currency.name,
            amountCup = t.amountCup,
            contactId = t.contactId,
            receiptImagePath = t.receiptImagePath,
            isRecurring = t.isRecurring,
            recurringId = t.recurringId,
            updatedAt = t.updatedAt,
            syncStatus = t.syncStatus.name
        )
    }
}
