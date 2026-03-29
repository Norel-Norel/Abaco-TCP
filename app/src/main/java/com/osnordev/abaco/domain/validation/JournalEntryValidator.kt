package com.osnordev.abaco.domain.validation

import com.osnordev.abaco.data.local.JournalLineEntity

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val message: String, val difference: Double = 0.0) : ValidationResult()
}

object JournalEntryValidator {

    private const val EPSILON = 0.001

    /**
     * Validates that [lines] form a balanced double-entry journal entry:
     * - At least one debit line
     * - At least one credit line
     * - sum(debits) == sum(credits) within [EPSILON]
     */
    fun validate(lines: List<JournalLineEntity>): ValidationResult {
        if (lines.none { it.debit > 0 }) {
            return ValidationResult.Invalid("Se requiere al menos una línea de débito")
        }
        if (lines.none { it.credit > 0 }) {
            return ValidationResult.Invalid("Se requiere al menos una línea de crédito")
        }
        val totalDebit = lines.sumOf { it.debit }
        val totalCredit = lines.sumOf { it.credit }
        val diff = totalDebit - totalCredit
        return if (Math.abs(diff) <= EPSILON) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(
                message = "El asiento no cuadra: diferencia de %.2f".format(diff),
                difference = diff
            )
        }
    }
}
