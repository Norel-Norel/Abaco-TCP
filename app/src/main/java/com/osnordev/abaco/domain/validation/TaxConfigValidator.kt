package com.osnordev.abaco.domain.validation

import com.osnordev.abaco.domain.model.TaxBracket

/**
 * Validates a CSS rate value.
 * Per Requisito 6.4: the CSS rate must be in the range [0, 100] (as a percentage).
 *
 * @param ratePercent the rate expressed as a percentage (e.g. 20.0 for 20%)
 * @return an error message, or null if valid
 */
fun validateCssRate(ratePercent: Double?): String? {
    if (ratePercent == null) return "La tasa CSS debe ser un número válido"
    if (ratePercent < 0.0 || ratePercent > 100.0) return "La tasa CSS debe estar entre 0 y 100"
    return null
}

/**
 * Validates a list of IIP tax brackets.
 * Per Requisito 6.2: brackets must be consecutive and rates must be in [0%, 100%].
 *
 * Rules:
 * - At least one bracket required.
 * - All rates must be in [0.0, 1.0] (fractional, not percentage).
 * - Each bracket's `from` must equal the previous bracket's `to` + 1.
 * - Only the last bracket may have a null `to` (no upper limit).
 *
 * @return an error message, or null if valid
 */
fun validateIipBrackets(brackets: List<TaxBracket>): String? {
    if (brackets.isEmpty()) return "Debe haber al menos un tramo IIP"

    for ((index, bracket) in brackets.withIndex()) {
        if (bracket.rate < 0.0 || bracket.rate > 1.0) {
            return "La tasa del tramo ${index + 1} debe estar entre 0% y 100%"
        }
        // Only the last bracket may omit the upper limit
        if (bracket.to == null && index != brackets.lastIndex) {
            return "Solo el último tramo puede no tener límite superior"
        }
    }

    // Check consecutiveness
    for (i in 1 until brackets.size) {
        val prev = brackets[i - 1]
        val curr = brackets[i]
        val expectedFrom = prev.to!! + 1.0
        if (curr.from != expectedFrom) {
            return "Los tramos deben ser consecutivos (tramo ${i + 1} debe comenzar en ${expectedFrom.toLong()})"
        }
    }

    // Last bracket must have no upper limit
    if (brackets.last().to != null) {
        return "El último tramo no debe tener límite superior"
    }

    return null
}
