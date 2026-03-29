package com.osnordev.abaco.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osnordev.abaco.domain.model.Transaction
import com.osnordev.abaco.domain.model.TransactionType
import com.osnordev.abaco.domain.usecase.GetTransactionsByPeriodUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

data class CategoryTotal(val category: String, val total: Double)

data class DashboardUiState(
    val year: Int,
    val month: Int,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netIncome: Double = 0.0,
    val incomeByCategory: List<CategoryTotal> = emptyList(),
    val expenseByCategory: List<CategoryTotal> = emptyList(),
    val isEmpty: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getTransactionsByPeriod: GetTransactionsByPeriodUseCase
) : ViewModel() {

    private val _period = MutableStateFlow(run {
        val now = LocalDate.now()
        now.year to now.monthValue
    })

    val uiState: StateFlow<DashboardUiState> = _period
        .flatMapLatest { (year, month) ->
            getTransactionsByPeriod(year, month)
                .combine(MutableStateFlow(year to month)) { transactions, (y, m) ->
                    buildState(y, m, transactions)
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = run {
                val now = LocalDate.now()
                DashboardUiState(year = now.year, month = now.monthValue)
            }
        )

    fun setPeriod(year: Int, month: Int) {
        _period.update { year to month }
    }

    private fun buildState(year: Int, month: Int, transactions: List<Transaction>): DashboardUiState {
        val incomes = transactions.filter { it.type == TransactionType.INCOME }
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }

        val totalIncome = incomes.sumOf { it.amount }
        val totalExpenses = expenses.sumOf { it.amount }

        val incomeByCategory = incomes
            .groupBy { it.category }
            .map { (cat, list) -> CategoryTotal(cat, list.sumOf { it.amount }) }
            .sortedByDescending { it.total }

        val expenseByCategory = expenses
            .groupBy { it.category }
            .map { (cat, list) -> CategoryTotal(cat, list.sumOf { it.amount }) }
            .sortedByDescending { it.total }

        return DashboardUiState(
            year = year,
            month = month,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netIncome = totalIncome - totalExpenses,
            incomeByCategory = incomeByCategory,
            expenseByCategory = expenseByCategory,
            isEmpty = transactions.isEmpty()
        )
    }
}
