package com.osnordev.abaco.data.repository

import com.osnordev.abaco.data.local.JournalEntryDao
import com.osnordev.abaco.data.local.JournalEntryEntity
import com.osnordev.abaco.data.local.JournalEntryWithLines
import com.osnordev.abaco.data.local.JournalLineEntity
import com.osnordev.abaco.domain.repository.JournalEntryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class JournalEntryRepositoryImpl @Inject constructor(
    private val dao: JournalEntryDao
) : JournalEntryRepository {

    override fun getAllEntries(): Flow<List<JournalEntryWithLines>> =
        dao.getAllEntriesWithLines()

    override suspend fun insertEntry(entry: JournalEntryEntity, lines: List<JournalLineEntity>) =
        dao.insertEntryWithLines(entry, lines)

    override suspend fun deleteEntry(id: Long) = dao.deleteEntry(id)
}
