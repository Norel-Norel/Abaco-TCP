package com.osnordev.abaco.domain.calculator

enum class BudgetStatus { NORMAL, WARNING, EXCEEDED }

data class BudgetCheckResult(
    val category: String,
    val spent: Double,
    val limit: Double,
    val status: BudgetStatus,
    val percentage: Double
)

object BudgetChecker {

    /**
     * Returns the [BudgetStatus] for a category given [spent] and [limit].
     * - EXCEEDED  when spent >= 100% of limit
     * - WARNING   when spent >= 80% of limit
     * - NORMAL    otherwise
     */
    fun check(category: String, spent: Double, limit: Double): BudgetCheckResult {
        val percentage = if (limit > 0) spent / limit else 0.0
        val status = when {
            percentage >= 1.0 -> BudgetStatus.EXCEEDED
            percentage >= 0.8 -> BudgetStatus.WARNING
            else -> BudgetStatus.NORMAL
        }
        return BudgetCheckResult(
            category = category,
            spent = spent,
            limit = limit,
            status = status,
            percentage = percentage
        )
    }
}
