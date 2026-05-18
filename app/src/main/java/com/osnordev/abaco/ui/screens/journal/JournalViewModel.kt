package com.osnordev.abaco.ui.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osnordev.abaco.data.local.ChartOfAccountDao
import com.osnordev.abaco.data.local.JournalEntryEntity
import com.osnordev.abaco.data.local.JournalEntryWithLines
import com.osnordev.abaco.data.local.JournalLineEntity
import com.osnordev.abaco.domain.client.CurrentClientManager
import com.osnordev.abaco.domain.model.AccountType
import com.osnordev.abaco.domain.usecase.GetJournalEntriesUseCase
import com.osnordev.abaco.domain.usecase.InsertJournalEntryUseCase
import com.osnordev.abaco.domain.validation.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AccountSuggestion(val code: String, val name: String, val type: AccountType) {
    val display: String get() = "$code - $name"
}

data class LineFormState(
    val accountQuery: String = "",
    val accountName: String = "",
    val accountType: AccountType = AccountType.ASSET,
    val partial: String = "",
    val debit: String = "",
    val credit: String = "",
    val showSuggestions: Boolean = false
)

data class JournalFormUiState(
    val description: String = "",
    val date: LocalDate = LocalDate.now(),
    val lines: List<LineFormState> = listOf(LineFormState(), LineFormState()),
    val validationError: String? = null,
    val totalDebit: Double = 0.0,
    val totalCredit: Double = 0.0,
    val difference: Double = 0.0,
    val isBalanced: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val getEntries: GetJournalEntriesUseCase,
    private val insertEntry: InsertJournalEntryUseCase,
    private val chartOfAccountDao: ChartOfAccountDao,
    private val currentClientManager: CurrentClientManager
) : ViewModel() {

    val entries: StateFlow<List<JournalEntryWithLines>> = getEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _form = MutableStateFlow(JournalFormUiState())
    val form: StateFlow<JournalFormUiState> = _form.asStateFlow()

    fun onDescriptionChange(v: String) = _form.update { it.copy(description = v) }
    fun onDateChange(v: LocalDate) = _form.update { it.copy(date = v) }

    /**
     * Busca cuentas en la BD del plan de cuentas, filtradas por cliente activo.
     */
    suspend fun getSuggestions(query: String): List<AccountSuggestion> {
        val clientId = currentClientManager.activeClientId.value
        val results = if (clientId != null) {
            chartOfAccountDao.searchByClient(clientId, query.ifBlank { "" })
        } else {
            chartOfAccountDao.search(query.ifBlank { "" })
        }
        return results.map { acc ->
            AccountSuggestion(code = acc.code, name = acc.name, type = acc.type)
        }
    }

    fun onAccountQueryChange(index: Int, query: String) = updateLine(index) {
        copy(accountQuery = query, showSuggestions = query.isNotBlank())
    }

    fun onPartialChange(index: Int, v: String) = updateLine(index) { copy(partial = v) }

    fun onAccountSelected(index: Int, suggestion: AccountSuggestion) = updateLine(index) {
        copy(
            accountQuery = suggestion.display,
            accountName = suggestion.name,
            accountType = suggestion.type,
            showSuggestions = false
        )
    }

    fun onSuggestionsHide(index: Int) = updateLine(index) { copy(showSuggestions = false) }

    fun onDebitChange(index: Int, v: String) = updateLine(index) {
        copy(debit = v, credit = if (v.isNotBlank()) "" else credit)
    }.also { recalcTotals() }

    fun onCreditChange(index: Int, v: String) = updateLine(index) {
        copy(credit = v, debit = if (v.isNotBlank()) "" else debit)
    }.also { recalcTotals() }

    fun addLine() {
        _form.update { it.copy(lines = it.lines + LineFormState()) }
        recalcTotals()
    }

    fun removeLine(index: Int) {
        if (_form.value.lines.size > 2) {
            _form.update { it.copy(lines = it.lines.toMutableList().also { l -> l.removeAt(index) }) }
            recalcTotals()
        }
    }

    fun save() {
        val state = _form.value
        if (!state.isBalanced) return

        val lines = state.lines.mapNotNull { l ->
            val debit = l.debit.toDoubleOrNull() ?: 0.0
            val credit = l.credit.toDoubleOrNull() ?: 0.0
            val name = l.accountName.ifBlank { l.accountQuery.trim() }
            if (name.isBlank()) null
            else JournalLineEntity(
                entryId = 0,
                accountName = name,
                accountType = l.accountType,
                debit = debit,
                credit = credit
            )
        }
        val entry = JournalEntryEntity(date = state.date, description = state.description.trim())
        _form.update { it.copy(isSaving = true, validationError = null) }
        viewModelScope.launch {
            when (val result = insertEntry(entry, lines)) {
                is ValidationResult.Valid ->
                    _form.update { JournalFormUiState(saved = true) }
                is ValidationResult.Invalid ->
                    _form.update { it.copy(isSaving = false, validationError = result.message, difference = result.difference) }
            }
        }
    }

    fun resetForm() = _form.update { JournalFormUiState() }

    private fun recalcTotals() {
        _form.update { state ->
            val totalDebit = state.lines.sumOf { it.debit.toDoubleOrNull() ?: 0.0 }
            val totalCredit = state.lines.sumOf { it.credit.toDoubleOrNull() ?: 0.0 }
            val diff = totalDebit - totalCredit
            state.copy(
                totalDebit = totalDebit,
                totalCredit = totalCredit,
                difference = diff,
                isBalanced = Math.abs(diff) < 0.01 && totalDebit > 0
            )
        }
    }

    private fun updateLine(index: Int, block: LineFormState.() -> LineFormState): Unit {
        _form.update { state ->
            val updated = state.lines.toMutableList()
            if (index in updated.indices) updated[index] = updated[index].block()
            state.copy(lines = updated)
        }
    }
}
