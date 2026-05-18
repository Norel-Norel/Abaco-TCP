package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.data.local.JournalEntryEntity
import com.osnordev.abaco.data.local.JournalLineEntity
import com.osnordev.abaco.domain.client.CurrentClientManager
import com.osnordev.abaco.domain.repository.JournalEntryRepository
import com.osnordev.abaco.domain.repository.PeriodRepository
import com.osnordev.abaco.domain.validation.JournalEntryValidator
import com.osnordev.abaco.domain.validation.ValidationResult
import javax.inject.Inject

class InsertJournalEntryUseCase @Inject constructor(
    private val repository: JournalEntryRepository,
    private val currentClientManager: CurrentClientManager,
    private val periodRepository: PeriodRepository
) {
    suspend operator fun invoke(
        entry: JournalEntryEntity,
        lines: List<JournalLineEntity>
    ): ValidationResult {
        // Verificar que el período no esté cerrado
        val entryYear  = entry.date.year
        val entryMonth = entry.date.monthValue
        if (periodRepository.isPeriodClosed(entryYear, entryMonth)) {
            return ValidationResult.Invalid(
                "El período ${entry.date.month.name.lowercase().replaceFirstChar { it.uppercase() }} $entryYear está cerrado. No se pueden agregar asientos."
            )
        }

        val result = JournalEntryValidator.validate(lines)
        if (result is ValidationResult.Valid) {
            val clientId = currentClientManager.activeClientId.value ?: 1L
            repository.insertEntry(entry.copy(clientId = clientId), lines)
        }
        return result
    }
}
