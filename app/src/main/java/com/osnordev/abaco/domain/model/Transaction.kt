package com.osnordev.abaco.domain.model

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class Transaction(
    val id: Long = 0,
    val type: TransactionType,
    val amount: Double,
    val category: String,
    val description: String = "",
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val year: Int,
    val month: Int,
    val currency: Currency = Currency.CUP,
    val amountCup: Double = amount,
    val contactId: Long? = null,
    val receiptImagePath: String? = null,
    val isRecurring: Boolean = false,
    val recurringId: Long? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
