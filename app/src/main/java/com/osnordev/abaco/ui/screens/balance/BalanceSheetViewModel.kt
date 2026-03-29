package com.osnordev.abaco.ui.screens.balance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osnordev.abaco.domain.calculator.BalanceSheet
import com.osnordev.abaco.domain.calculator.BalanceSheetCalculator
import com.osnordev.abaco.domain.repository.JournalEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class BalanceSheetUiState(
    val balanceSheet: BalanceSheet? = null,
    val cutoffDate: LocalDate = LocalDate.now(),
    val isLoading: Boolean = true
)

@HiltViewModel
class BalanceSheetViewModel @Inject constructor(
    private val journalRepository: JournalEntryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BalanceSheetUiState())
    val uiState: StateFlow<BalanceSheetUiState> = _uiState.asStateFlow()

    init {
        loadBalance()
    }

    fun onCutoffDateChange(date: LocalDate) {
        _uiState.update { it.copy(cutoffDate = date) }
        loadBalance()
    }

    private fun loadBalance() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            journalRepository.getAllEntries().collect { entries ->
                val sheet = BalanceSheetCalculator.calculate(entries, _uiState.value.cutoffDate)
                _uiState.update { it.copy(balanceSheet = sheet, isLoading = false) }
            }
        }
    }
}
