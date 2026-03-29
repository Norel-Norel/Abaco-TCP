package com.osnordev.abaco.ui.screens.reports

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osnordev.abaco.domain.calculator.CashFlowCalculator
import com.osnordev.abaco.domain.calculator.MonthlyFlow
import com.osnordev.abaco.domain.calculator.TaxCalculator
import com.osnordev.abaco.domain.calculator.TaxProjection
import com.osnordev.abaco.domain.calculator.TaxProjectionCalculator
import com.osnordev.abaco.domain.export.CsvExporter
import com.osnordev.abaco.domain.export.XlsxExporter
import com.osnordev.abaco.domain.model.Transaction
import com.osnordev.abaco.domain.model.TransactionType
import com.osnordev.abaco.domain.repository.TaxConfigRepository
import com.osnordev.abaco.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class PeriodComparison(
    val currentIncome: Double,
    val currentExpenses: Double,
    val currentNet: Double,
    val previousIncome: Double,
    val previousExpenses: Double,
    val previousNet: Double,
    val incomeChange: Double,
    val expensesChange: Double,
    val netChange: Double
)

data class ReportsUiState(
    val cashFlow: List<MonthlyFlow> = emptyList(),
    val projection: TaxProjection? = null,
    val comparison: PeriodComparison? = null,
    val selectedYear: Int = LocalDate.now().year,
    val selectedMonth: Int = LocalDate.now().monthValue,
    /** Snapshot of all transactions — used for CSV export */
    val allTransactions: List<Transaction> = emptyList()
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val taxConfigRepository: TaxConfigRepository,
    private val cashFlowCalculator: CashFlowCalculator,
    private val taxProjectionCalculator: TaxProjectionCalculator,
    private val csvExporter: CsvExporter,
    private val xlsxExporter: XlsxExporter
) : ViewModel() {

    private val _period = MutableStateFlow(
        LocalDate.now().let { it.year to it.monthValue }
    )

    val uiState: StateFlow<ReportsUiState> = combine(
        transactionRepository.getAllTransactions(),
        taxConfigRepository.getTaxConfig(),
        _period
    ) { allTransactions, taxConfig, (year, month) ->

        val cashFlow = cashFlowCalculator.calculate(allTransactions, 12)

        val currentTxs = allTransactions.filter { it.year == year && it.month == month }
        val grossIncome = currentTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amountCup }
        val totalExpenses = currentTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCup }
        val currentResult = TaxCalculator.calculateTaxResult(
            grossIncome = grossIncome,
            totalExpenses = totalExpenses,
            cssRate = taxConfig.cssRate,
            iipBrackets = taxConfig.iipBrackets
        )
        val projection = taxProjectionCalculator.project(currentResult, month, taxConfig)

        val prevYm = YearMonth.of(year, month).minusMonths(1)
        val prevTxs = allTransactions.filter { it.year == prevYm.year && it.month == prevYm.monthValue }

        val curIncome = currentTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amountCup }
        val curExpenses = currentTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCup }
        val curNet = curIncome - curExpenses
        val prevIncome = prevTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amountCup }
        val prevExpenses = prevTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCup }
        val prevNet = prevIncome - prevExpenses

        val comparison = PeriodComparison(
            currentIncome = curIncome,
            currentExpenses = curExpenses,
            currentNet = curNet,
            previousIncome = prevIncome,
            previousExpenses = prevExpenses,
            previousNet = prevNet,
            incomeChange = percentChange(prevIncome, curIncome),
            expensesChange = percentChange(prevExpenses, curExpenses),
            netChange = percentChange(prevNet, curNet)
        )

        ReportsUiState(
            cashFlow = cashFlow,
            projection = projection,
            comparison = comparison,
            selectedYear = year,
            selectedMonth = month,
            allTransactions = allTransactions
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReportsUiState()
    )

    fun setPeriod(year: Int, month: Int) {
        _period.update { year to month }
    }

    /**
     * Exports the current period's transactions as CSV and returns a share Intent.
     * Requirements: 14.1
     */
    fun exportCsv(context: Context): Intent {
        val state = uiState.value
        val periodTxs = state.allTransactions.filter {
            it.year == state.selectedYear && it.month == state.selectedMonth
        }
        return csvExporter.export(
            context,
            periodTxs,
            "transacciones_${state.selectedYear}_${state.selectedMonth}.csv"
        )
    }

    /**
     * Exports the current period's transactions as XLSX and returns a share Intent.
     * Requirements: 14.2
     */
    fun exportXlsx(context: Context): Intent {
        val state = uiState.value
        val periodTxs = state.allTransactions.filter {
            it.year == state.selectedYear && it.month == state.selectedMonth
        }
        return xlsxExporter.export(
            context,
            periodTxs,
            "transacciones_${state.selectedYear}_${state.selectedMonth}.xlsx"
        )
    }

    private fun percentChange(previous: Double, current: Double): Double =
        if (previous == 0.0) 0.0 else (current - previous) / previous
}
