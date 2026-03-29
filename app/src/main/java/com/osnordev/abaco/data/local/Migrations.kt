package com.osnordev.abaco.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Extend transactions table
        db.execSQL("ALTER TABLE transactions ADD COLUMN currency TEXT NOT NULL DEFAULT 'CUP'")
        db.execSQL("ALTER TABLE transactions ADD COLUMN amountCup REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE transactions ADD COLUMN contactId INTEGER")
        db.execSQL("ALTER TABLE transactions ADD COLUMN receiptImagePath TEXT")
        db.execSQL("ALTER TABLE transactions ADD COLUMN isRecurring INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE transactions ADD COLUMN recurringId INTEGER")
        db.execSQL("ALTER TABLE transactions ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE transactions ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDING'")
        db.execSQL("UPDATE transactions SET amountCup = amount, updatedAt = strftime('%s','now') * 1000")

        // Create journal_entries table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS journal_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                date TEXT NOT NULL,
                description TEXT NOT NULL,
                updatedAt INTEGER NOT NULL DEFAULT 0,
                syncStatus TEXT NOT NULL DEFAULT 'PENDING'
            )
        """.trimIndent())

        // Create journal_lines table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS journal_lines (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                entryId INTEGER NOT NULL,
                accountName TEXT NOT NULL,
                accountType TEXT NOT NULL,
                debit REAL NOT NULL DEFAULT 0.0,
                credit REAL NOT NULL DEFAULT 0.0,
                FOREIGN KEY (entryId) REFERENCES journal_entries(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_journal_lines_entryId ON journal_lines(entryId)")

        // Create payment_dues table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS payment_dues (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                description TEXT NOT NULL,
                amount REAL NOT NULL,
                currency TEXT NOT NULL DEFAULT 'CUP',
                dueDate TEXT NOT NULL,
                isPaid INTEGER NOT NULL DEFAULT 0,
                alarmId1 INTEGER,
                alarmId2 INTEGER,
                updatedAt INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        // Create budgets table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS budgets (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                category TEXT NOT NULL,
                limitAmount REAL NOT NULL,
                month INTEGER NOT NULL,
                year INTEGER NOT NULL
            )
        """.trimIndent())

        // Create contacts table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS contacts (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                phone TEXT NOT NULL,
                type TEXT NOT NULL,
                notes TEXT NOT NULL DEFAULT '',
                updatedAt INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        // Create recurring_templates table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS recurring_templates (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                type TEXT NOT NULL,
                amount REAL NOT NULL,
                currency TEXT NOT NULL,
                category TEXT NOT NULL,
                description TEXT NOT NULL,
                frequency TEXT NOT NULL,
                startDate TEXT NOT NULL,
                nextDate TEXT NOT NULL,
                isActive INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent())
    }
}
