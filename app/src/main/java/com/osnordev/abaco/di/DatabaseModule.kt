package com.osnordev.abaco.di

import android.content.Context
import androidx.room.Room
import com.osnordev.abaco.data.local.AppDatabase
import com.osnordev.abaco.data.local.BudgetDao
import com.osnordev.abaco.data.local.ChartOfAccountDao
import com.osnordev.abaco.data.local.ContactDao
import com.osnordev.abaco.data.local.InventoryDao
import com.osnordev.abaco.data.local.JournalEntryDao
import com.osnordev.abaco.data.local.MIGRATION_1_2
import com.osnordev.abaco.data.local.MIGRATION_2_3
import com.osnordev.abaco.data.local.MIGRATION_3_4
import com.osnordev.abaco.data.local.PaymentDueDao
import com.osnordev.abaco.data.local.RecurringTemplateDao
import com.osnordev.abaco.data.local.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DB_NAME = "abaco_database"
    private val SQLITE_HEADER = "SQLite format 3\u0000".toByteArray(Charsets.UTF_8)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        val passphrase = SQLiteDatabase.getBytes("abaco_default_key".toCharArray())
        val factory = SupportFactory(passphrase)

        val dbFile = context.getDatabasePath(DB_NAME)
        if (dbFile.exists() && isPlainSqlite(dbFile)) {
            dbFile.delete()
            context.getDatabasePath("$DB_NAME-shm").delete()
            context.getDatabasePath("$DB_NAME-wal").delete()
        }

        return Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
            .openHelperFactory(factory)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()
    }

    private fun isPlainSqlite(file: java.io.File): Boolean {
        return try {
            val header = ByteArray(16)
            file.inputStream().use { it.read(header) }
            header.contentEquals(SQLITE_HEADER)
        } catch (_: Exception) {
            false
        }
    }

    @Provides fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()
    @Provides fun provideJournalEntryDao(db: AppDatabase): JournalEntryDao = db.journalEntryDao()
    @Provides fun providePaymentDueDao(db: AppDatabase): PaymentDueDao = db.paymentDueDao()
    @Provides fun provideBudgetDao(db: AppDatabase): BudgetDao = db.budgetDao()
    @Provides fun provideContactDao(db: AppDatabase): ContactDao = db.contactDao()
    @Provides fun provideRecurringTemplateDao(db: AppDatabase): RecurringTemplateDao = db.recurringTemplateDao()
    @Provides fun provideInventoryDao(db: AppDatabase): InventoryDao = db.inventoryDao()
    @Provides fun provideChartOfAccountDao(db: AppDatabase): ChartOfAccountDao = db.chartOfAccountDao()
}
