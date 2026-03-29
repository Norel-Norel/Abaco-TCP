package com.osnordev.abaco.domain.calculator

import com.osnordev.abaco.domain.model.Transaction
import com.osnordev.abaco.domain.model.TransactionType
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

data class MonthlyFlow(
    val yearMonth: YearMonth,
    val income: Double,
    val expenses: Double,
    val net: Double = income - expenses
)

/**
 * Calculates monthly cash flow from a flat list of transactions.
 * Requirements: 14.3
 */
@Singleton
class CashFlowCalculator @Inject constructor() {

    /**
     * Groups [transactions] by year-month and returns the last [months] periods
     * sorted chronologically (oldest first).
     */
    fun calculate(transactions: List<Transaction>, months: Int): List<MonthlyFlow> {
        val grouped = transactions.groupBy { YearMonth.of(it.year, it.month) }
        return grouped.entries
            .sortedByDescending { it.key }
            .take(months)
            .sortedBy { it.key }
            .map { (ym, txs) ->
                val income = txs.filter { it.type == TransactionType.INCOME }.sumOf { it.amountCup }
                val expenses = txs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCup }
                MonthlyFlow(yearMonth = ym, income = income, expenses = expenses)
            }
    }
}
