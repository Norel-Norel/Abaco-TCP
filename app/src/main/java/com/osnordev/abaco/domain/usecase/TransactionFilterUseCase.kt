package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.domain.model.Transaction
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Criteria for filtering transactions. Null fields are ignored (no filter applied).
 */
data class FilterCriteria(
    val query: String? = null,           // matches description or category (case-insensitive)
    val category: String? = null,        // exact category match
    val dateFrom: LocalDate? = null,     // inclusive lower bound
    val dateTo: LocalDate? = null,       // inclusive upper bound
    val amountMin: Double? = null,       // inclusive minimum (uses amountCup)
    val amountMax: Double? = null        // inclusive maximum (uses amountCup)
) {
    val isEmpty: Boolean
        get() = query.isNullOrBlank() &&
                category == null &&
                dateFrom == null &&
                dateTo == null &&
                amountMin == null &&
                amountMax == null
}

@Singleton
class TransactionFilterUseCase @Inject constructor() {

    /**
     * Filters [transactions] applying all non-null criteria with AND logic.
     * Requirements: 19.1, 19.2, 19.3, 19.4, 19.5
     */
    fun filter(transactions: List<Transaction>, criteria: FilterCriteria): List<Transaction> {
        if (criteria.isEmpty) return transactions

        return transactions.filter { tx ->
            matchesQuery(tx, criteria.query) &&
            matchesCategory(tx, criteria.category) &&
            matchesDateRange(tx, criteria.dateFrom, criteria.dateTo) &&
            matchesAmountRange(tx, criteria.amountMin, criteria.amountMax)
        }
    }

    private fun matchesQuery(tx: Transaction, query: String?): Boolean {
        if (query.isNullOrBlank()) return true
        val q = query.trim().lowercase()
        return tx.description.lowercase().contains(q) ||
               tx.category.lowercase().contains(q)
    }

    private fun matchesCategory(tx: Transaction, category: String?): Boolean {
        if (category == null) return true
        return tx.category == category
    }

    private fun matchesDateRange(tx: Transaction, from: LocalDate?, to: LocalDate?): Boolean {
        if (from != null && tx.date < from) return false
        if (to != null && tx.date > to) return false
        return true
    }

    private fun matchesAmountRange(tx: Transaction, min: Double?, max: Double?): Boolean {
        if (min != null && tx.amountCup < min) return false
        if (max != null && tx.amountCup > max) return false
        return true
    }
}
