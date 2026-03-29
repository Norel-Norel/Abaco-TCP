package com.osnordev.abaco.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osnordev.abaco.domain.model.Transaction
import com.osnordev.abaco.domain.repository.TransactionRepository
import com.osnordev.abaco.domain.usecase.FilterCriteria
import com.osnordev.abaco.domain.usecase.TransactionFilterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

data class SearchUiState(
    val results: List<Transaction> = emptyList(),
    val query: String = "",
    val selectedCategory: String? = null,
    val dateFrom: LocalDate? = null,
    val dateTo: LocalDate? = null,
    val amountMin: String = "",
    val amountMax: String = "",
    val showFilters: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    repository: TransactionRepository,
    private val filterUseCase: TransactionFilterUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _dateFrom = MutableStateFlow<LocalDate?>(null)
    private val _dateTo = MutableStateFlow<LocalDate?>(null)
    private val _amountMin = MutableStateFlow("")
    private val _amountMax = MutableStateFlow("")
    private val _showFilters = MutableStateFlow(false)

    private val allTransactions = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState = combine(
        allTransactions,
        _query,
        _selectedCategory,
        _dateFrom,
        _dateTo,
        _amountMin,
        _amountMax,
        _showFilters
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val transactions = args[0] as List<Transaction>
        val query = args[1] as String
        val category = args[2] as String?
        val dateFrom = args[3] as LocalDate?
        val dateTo = args[4] as LocalDate?
        val amountMin = args[5] as String
        val amountMax = args[6] as String
        val showFilters = args[7] as Boolean

        val criteria = FilterCriteria(
            query = query.takeIf { it.isNotBlank() },
            category = category,
            dateFrom = dateFrom,
            dateTo = dateTo,
            amountMin = amountMin.toDoubleOrNull(),
            amountMax = amountMax.toDoubleOrNull()
        )

        SearchUiState(
            results = filterUseCase.filter(transactions, criteria),
            query = query,
            selectedCategory = category,
            dateFrom = dateFrom,
            dateTo = dateTo,
            amountMin = amountMin,
            amountMax = amountMax,
            showFilters = showFilters
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SearchUiState()
    )

    fun onQueryChange(value: String) { _query.value = value }
    fun onCategorySelected(category: String?) { _selectedCategory.value = category }
    fun onDateFromSelected(date: LocalDate?) { _dateFrom.value = date }
    fun onDateToSelected(date: LocalDate?) { _dateTo.value = date }
    fun onAmountMinChange(value: String) { _amountMin.value = value }
    fun onAmountMaxChange(value: String) { _amountMax.value = value }
    fun toggleFilters() { _showFilters.value = !_showFilters.value }

    fun clearFilters() {
        _query.value = ""
        _selectedCategory.value = null
        _dateFrom.value = null
        _dateTo.value = null
        _amountMin.value = ""
        _amountMax.value = ""
    }
}
