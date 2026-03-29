package com.osnordev.abaco.ui.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osnordev.abaco.data.local.JournalEntryEntity
import com.osnordev.abaco.data.local.JournalEntryWithLines
import com.osnordev.abaco.data.local.JournalLineEntity
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

data class LineFormState(
    val accountName: String = "",
    val accountType: AccountType = AccountType.ASSET,
    val debit: String = "",
    val credit: String = ""
)

data class JournalFormUiState(
    val description: String = "",
    val date: LocalDate = LocalDate.now(),
    val lines: List<LineFormState> = listOf(LineFormState(), LineFormState()),
    val validationError: String? = null,
    val difference: Double = 0.0,
    val isSaving: Boolean = false,
    val saved: Boolean = false
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val getEntries: GetJournalEntriesUseCase,
    private val insertEntry: InsertJournalEntryUseCase
) : ViewModel() {

    val entries: StateFlow<List<JournalEntryWithLines>> = getEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _form = MutableStateFlow(JournalFormUiState())
    val form: StateFlow<JournalFormUiState> = _form.asStateFlow()

    fun onDescriptionChange(v: String) = _form.update { it.copy(description = v) }

    fun onLineAccountNameChange(index: Int, v: String) = updateLine(index) { copy(accountName = v) }
    fun onLineAccountTypeChange(index: Int, v: AccountType) = updateLine(index) { copy(accountType = v) }
    fun onLineDebitChange(index: Int, v: String) = updateLine(index) { copy(debit = v) }
    fun onLineCreditChange(index: Int, v: String) = updateLine(index) { copy(credit = v) }

    fun addLine() = _form.update { it.copy(lines = it.lines + LineFormState()) }

    fun removeLine(index: Int) {
        if (_form.value.lines.size > 2) {
            _form.update { it.copy(lines = it.lines.toMutableList().also { l -> l.removeAt(index) }) }
        }
    }

    fun save() {
        val state = _form.value
        val lines = state.lines.mapNotNull { l ->
            val debit = l.debit.toDoubleOrNull() ?: 0.0
            val credit = l.credit.toDoubleOrNull() ?: 0.0
            if (l.accountName.isBlank()) null
            else JournalLineEntity(
                entryId = 0,
                accountName = l.accountName.trim(),
                accountType = l.accountType,
                debit = debit,
                credit = credit
            )
        }
        val entry = JournalEntryEntity(
            date = state.date,
            description = state.description.trim()
        )
        _form.update { it.copy(isSaving = true, validationError = null) }
        viewModelScope.launch {
            val result = insertEntry(entry, lines)
            when (result) {
                is ValidationResult.Valid ->
                    _form.update { JournalFormUiState(saved = true) }
                is ValidationResult.Invalid ->
                    _form.update { it.copy(isSaving = false, validationError = result.message, difference = result.difference) }
            }
        }
    }

    fun resetForm() = _form.update { JournalFormUiState() }

    private fun updateLine(index: Int, block: LineFormState.() -> LineFormState) {
        _form.update { state ->
            val updated = state.lines.toMutableList()
            updated[index] = updated[index].block()
            state.copy(lines = updated)
        }
    }
}
