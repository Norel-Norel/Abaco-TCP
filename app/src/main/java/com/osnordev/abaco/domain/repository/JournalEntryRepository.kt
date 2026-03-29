package com.osnordev.abaco.domain.repository

import com.osnordev.abaco.data.local.JournalEntryEntity
import com.osnordev.abaco.data.local.JournalEntryWithLines
import com.osnordev.abaco.data.local.JournalLineEntity
import kotlinx.coroutines.flow.Flow

interface JournalEntryRepository {
    fun getAllEntries(): Flow<List<JournalEntryWithLines>>
    suspend fun insertEntry(entry: JournalEntryEntity, lines: List<JournalLineEntity>)
    suspend fun deleteEntry(id: Long)
}
