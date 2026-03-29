package com.osnordev.abaco.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<JournalLineEntity>)

    @Transaction
    suspend fun insertEntryWithLines(entry: JournalEntryEntity, lines: List<JournalLineEntity>) {
        val id = insertEntry(entry)
        insertLines(lines.map { it.copy(entryId = id) })
    }

    @Transaction
    @Query("SELECT * FROM journal_entries ORDER BY date DESC")
    fun getAllEntriesWithLines(): Flow<List<JournalEntryWithLines>>

    @Transaction
    @Query("""
        SELECT * FROM journal_entries
        WHERE date BETWEEN :from AND :to
        ORDER BY date DESC
    """)
    fun getEntriesByPeriod(from: String, to: String): Flow<List<JournalEntryWithLines>>

    @Transaction
    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getEntryWithLines(id: Long): JournalEntryWithLines?

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteEntry(id: Long)
}
