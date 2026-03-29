package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.data.local.JournalEntryWithLines
import com.osnordev.abaco.domain.repository.JournalEntryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetJournalEntriesUseCase @Inject constructor(
    private val repository: JournalEntryRepository
) {
    operator fun invoke(): Flow<List<JournalEntryWithLines>> = repository.getAllEntries()
}
