package com.osnordev.abaco.ui.screens.transactions

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osnordev.abaco.data.local.RecurringFrequency
import com.osnordev.abaco.data.local.RecurringTemplateEntity
import com.osnordev.abaco.data.repository.RecurringTemplateRepository
import com.osnordev.abaco.domain.model.CurrencyConfig
import com.osnordev.abaco.domain.model.Transaction
import com.osnordev.abaco.domain.repository.CurrencyRepository
import com.osnordev.abaco.domain.usecase.DeleteTransactionUseCase
import com.osnordev.abaco.domain.usecase.GetTransactionsByPeriodUseCase
import com.osnordev.abaco.domain.usecase.InsertTransactionUseCase
import com.osnordev.abaco.domain.usecase.UpdateTransactionUseCase
import com.osnordev.abaco.widget.AbacoWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class PeriodState(val year: Int, val month: Int)

data class TransactionUiState(
    val transactions: List<Transaction> = emptyList(),
    val period: PeriodState = run {
        val now = LocalDate.now()
        PeriodState(now.year, now.monthValue)
    },
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val getTransactionsByPeriod: GetTransactionsByPeriodUseCase,
    private val insertTransaction: InsertTransactionUseCase,
    private val updateTransaction: UpdateTransactionUseCase,
    private val deleteTransaction: DeleteTransactionUseCase,
    private val recurringRepository: RecurringTemplateRepository,
    private val currencyRepository: CurrencyRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val currencyConfig: StateFlow<CurrencyConfig> = currencyRepository.getCurrencyConfig()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CurrencyConfig()
        )

    private val _period = MutableStateFlow(run {
        val now = LocalDate.now()
        PeriodState(now.year, now.monthValue)
    })

    val transactions: StateFlow<List<Transaction>> = _period
        .flatMapLatest { p -> getTransactionsByPeriod(p.year, p.month) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState

    fun setPeriod(year: Int, month: Int) {
        _period.update { PeriodState(year, month) }
        _uiState.update { it.copy(period = PeriodState(year, month)) }
    }

    fun save(transaction: Transaction) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                if (transaction.id == 0L) {
                    insertTransaction(transaction)
                } else {
                    updateTransaction(transaction)
                }
                _uiState.update { it.copy(isSaving = false) }
                AbacoWidget.requestUpdate(context)
            } catch (e: IllegalArgumentException) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }

    /**
     * Saves a transaction and creates a recurring template for it.
     * Requirements: 20.1, 20.3, 20.4, 20.5
     */
    fun saveWithRecurring(
        transaction: Transaction,
        frequency: RecurringFrequency,
        startDate: LocalDate
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                // Insert the template first to get its id
                val template = RecurringTemplateEntity(
                    type = transaction.type,
                    amount = transaction.amount,
                    currency = transaction.currency.name,
                    category = transaction.category,
                    description = transaction.description,
                    frequency = frequency,
                    startDate = startDate,
                    nextDate = startDate
                )
                val templateId = recurringRepository.insert(template)

                // Insert the transaction linked to the template
                val recurringTransaction = transaction.copy(
                    isRecurring = true,
                    recurringId = templateId,
                    date = startDate,
                    year = startDate.year,
                    month = startDate.monthValue
                )
                insertTransaction(recurringTransaction)
                _uiState.update { it.copy(isSaving = false) }
                AbacoWidget.requestUpdate(context)
            } catch (e: IllegalArgumentException) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }

    /**
     * Cancels all future occurrences of a recurring template (Req 20.4).
     */
    fun cancelRecurring(recurringId: Long) {
        viewModelScope.launch {
            recurringRepository.deactivate(recurringId)
        }
    }

    fun delete(transaction: Transaction) {
        viewModelScope.launch {
            deleteTransaction(transaction)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
