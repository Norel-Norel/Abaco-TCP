package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.data.local.JournalEntryEntity
import com.osnordev.abaco.data.local.JournalLineEntity
import com.osnordev.abaco.domain.repository.JournalEntryRepository
import com.osnordev.abaco.domain.validation.JournalEntryValidator
import com.osnordev.abaco.domain.validation.ValidationResult
import javax.inject.Inject

class InsertJournalEntryUseCase @Inject constructor(
    private val repository: JournalEntryRepository
) {
    suspend operator fun invoke(
        entry: JournalEntryEntity,
        lines: List<JournalLineEntity>
    ): ValidationResult {
        val result = JournalEntryValidator.validate(lines)
        if (result is ValidationResult.Valid) {
            repository.insertEntry(entry, lines)
        }
        return result
    }
}
