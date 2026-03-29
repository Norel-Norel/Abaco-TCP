package com.osnordev.abaco.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        TransactionEntity::class,
        JournalEntryEntity::class,
        JournalLineEntity::class,
        PaymentDueEntity::class,
        BudgetEntity::class,
        ContactEntity::class,
        RecurringTemplateEntity::class,
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun paymentDueDao(): PaymentDueDao
    abstract fun budgetDao(): BudgetDao
    abstract fun contactDao(): ContactDao
    abstract fun recurringTemplateDao(): RecurringTemplateDao
}
