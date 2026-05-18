package com.osnordev.abaco.ui.screens.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osnordev.abaco.data.local.BudgetEntity
import com.osnordev.abaco.data.repository.BudgetRepository
import com.osnordev.abaco.domain.calculator.BudgetCheckResult
import com.osnordev.abaco.domain.calculator.BudgetChecker
import com.osnordev.abaco.domain.client.CurrentClientManager
import com.osnordev.abaco.domain.model.TransactionType
import com.osnordev.abaco.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class BudgetUiState(
    val results: List<BudgetCheckResult> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val month: Int = LocalDate.now().monthValue,
    val year: Int = LocalDate.now().year
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val currentClientManager: CurrentClientManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadBudgets()
    }

    private fun loadBudgets() {
        val state = _uiState.value
        val clientId = currentClientManager.activeClientId.value ?: 1L
        viewModelScope.launch {
            combine(
                budgetRepository.getByPeriodAndClient(clientId, state.month, state.year),
                transactionRepository.getTransactionsByPeriodAndClient(clientId, state.year, state.month)
            ) { budgets, transactions ->
                val spentByCategory = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.category }
                    .mapValues { (_, txs) -> txs.sumOf { it.amountCup } }

                val results = budgets.map { budget ->
                    val spent = spentByCategory[budget.category] ?: 0.0
                    BudgetChecker.check(budget.category, spent, budget.limitAmount)
                }
                budgets to results
            }.collect { (budgets, results) ->
                _uiState.update { it.copy(budgets = budgets, results = results) }
            }
        }
    }

    fun saveBudget(category: String, limitAmount: Double) {
        viewModelScope.launch {
            val state = _uiState.value
            val clientId = currentClientManager.activeClientId.value ?: 1L
            val existing = budgetRepository.getByCategoryAndClient(clientId, category, state.month, state.year)
            val entity = existing?.copy(limitAmount = limitAmount)
                ?: BudgetEntity(
                    category = category,
                    limitAmount = limitAmount,
                    month = state.month,
                    year = state.year,
                    clientId = clientId
                )
            budgetRepository.save(entity)
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch { budgetRepository.delete(id) }
    }
}
