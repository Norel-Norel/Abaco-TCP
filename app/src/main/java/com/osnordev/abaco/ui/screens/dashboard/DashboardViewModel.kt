package com.osnordev.abaco.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osnordev.abaco.data.local.JournalEntryWithLines
import com.osnordev.abaco.domain.calculator.BalanceSheet
import com.osnordev.abaco.domain.calculator.BalanceSheetCalculator
import com.osnordev.abaco.domain.model.Transaction
import com.osnordev.abaco.domain.model.TransactionType
import com.osnordev.abaco.domain.repository.JournalEntryRepository
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

data class RecentEntry(
    val id: Long,
    val date: LocalDate,
    val description: String,
    val totalAmount: Double,
    val isBalanced: Boolean
)

data class DashboardUiState(
    val year: Int,
    val month: Int,
    // Transacciones
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netIncome: Double = 0.0,
    val incomeByCategory: List<CategoryTotal> = emptyList(),
    val expenseByCategory: List<CategoryTotal> = emptyList(),
    val isEmpty: Boolean = true,
    // Balance contable
    val balanceSheet: BalanceSheet? = null,
    val prevBalanceSheet: BalanceSheet? = null,
    // Últimos asientos
    val recentEntries: List<RecentEntry> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getTransactionsByPeriod: GetTransactionsByPeriodUseCase,
    private val journalRepository: JournalEntryRepository
) : ViewModel() {

    private val _period = MutableStateFlow(run {
        val now = LocalDate.now()
        now.year to now.monthValue
    })

    val uiState: StateFlow<DashboardUiState> = _period
        .flatMapLatest { (year, month) ->
            combine(
                getTransactionsByPeriod(year, month),
                journalRepository.getAllEntries()
            ) { transactions, allEntries ->
                buildState(year, month, transactions, allEntries)
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

    private fun buildState(
        year: Int,
        month: Int,
        transactions: List<Transaction>,
        allEntries: List<JournalEntryWithLines>
    ): DashboardUiState {
        val incomes = transactions.filter { it.type == TransactionType.INCOME }
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        val totalIncome = incomes.sumOf { it.amount }
        val totalExpenses = expenses.sumOf { it.amount }

        val incomeByCategory = incomes.groupBy { it.category }
            .map { (cat, list) -> CategoryTotal(cat, list.sumOf { it.amount }) }
            .sortedByDescending { it.total }
        val expenseByCategory = expenses.groupBy { it.category }
            .map { (cat, list) -> CategoryTotal(cat, list.sumOf { it.amount }) }
            .sortedByDescending { it.total }

        // Balance al final del período actual
        val cutoff = LocalDate.of(year, month, java.time.YearMonth.of(year, month).lengthOfMonth())
        val balanceSheet = BalanceSheetCalculator.calculate(allEntries, cutoff)

        // Balance del mes anterior para calcular variación
        val prevCutoff = cutoff.minusMonths(1)
            .withDayOfMonth(java.time.YearMonth.of(cutoff.minusMonths(1).year, cutoff.minusMonths(1).month).lengthOfMonth())
        val prevBalanceSheet = BalanceSheetCalculator.calculate(allEntries, prevCutoff)

        // Últimos 5 asientos con estado de cuadre
        val recentEntries = allEntries
            .sortedByDescending { it.entry.date }
            .take(5)
            .map { entry ->
                val totalDebit = entry.lines.sumOf { it.debit }
                val totalCredit = entry.lines.sumOf { it.credit }
                val balanced = Math.abs(totalDebit - totalCredit) < 0.01
                RecentEntry(
                    id = entry.entry.id,
                    date = entry.entry.date,
                    description = entry.entry.description,
                    totalAmount = totalDebit,
                    isBalanced = balanced
                )
            }

        return DashboardUiState(
            year = year,
            month = month,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netIncome = totalIncome - totalExpenses,
            incomeByCategory = incomeByCategory,
            expenseByCategory = expenseByCategory,
            isEmpty = transactions.isEmpty() && allEntries.isEmpty(),
            balanceSheet = balanceSheet,
            prevBalanceSheet = prevBalanceSheet,
            recentEntries = recentEntries
        )
    }
}
