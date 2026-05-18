package com.osnordev.abaco.ui.screens.periods

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osnordev.abaco.domain.client.CurrentClientManager
import com.osnordev.abaco.domain.repository.JournalEntryRepository
import com.osnordev.abaco.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

data class PeriodSummary(
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val journalEntryCount: Int = 0
)

data class PeriodsUiState(
    val currentYear: Int = LocalDate.now().year,
    val currentMonth: Int = LocalDate.now().monthValue,
    val isOpen: Boolean = true,
    val closedPeriods: List<String> = emptyList(),
    val summary: PeriodSummary? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class PeriodsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val currentClientManager: CurrentClientManager,
    private val journalRepository: JournalEntryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PeriodsUiState())
    val uiState: StateFlow<PeriodsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            currentClientManager.activeClientId.collect { clientId ->
                loadState(clientId ?: 1L)
            }
        }
    }

    private suspend fun loadState(clientId: Long) {
        val prefs = dataStore.data.first()
        val year  = prefs[yearKey(clientId)]   ?: LocalDate.now().year
        val month = prefs[monthKey(clientId)]  ?: LocalDate.now().monthValue
        val open  = prefs[openKey(clientId)]   ?: true
        val closed = prefs[closedKey(clientId)]?.toList()?.sorted() ?: emptyList()
        _uiState.value = PeriodsUiState(year, month, open, closed)
        loadSummary(clientId, year, month)
    }

    private fun loadSummary(clientId: Long, year: Int, month: Int) {
        viewModelScope.launch {
            val ym = YearMonth.of(year, month)
            val from = ym.atDay(1).toString()
            val to   = ym.atEndOfMonth().toString()

            val entries = journalRepository.getAllEntriesByClient(clientId).first()
            val periodEntries = entries.filter {
                it.entry.date.toString() in from..to
            }

            val transactions = transactionRepository.getTransactionsByPeriodAndClient(clientId, year, month).first()
            val income   = transactions.filter { it.type.name == "INCOME" }.sumOf { it.amount }
            val expenses = transactions.filter { it.type.name == "EXPENSE" }.sumOf { it.amount }

            _uiState.value = _uiState.value.copy(
                summary = PeriodSummary(
                    totalIncome = income,
                    totalExpenses = expenses,
                    journalEntryCount = periodEntries.size
                )
            )
        }
    }

    fun closePeriod() {
        viewModelScope.launch {
            val clientId = currentClientManager.activeClientId.value ?: 1L
            val state = _uiState.value
            val label = periodLabel(state.currentYear, state.currentMonth)
            val newClosed = (state.closedPeriods + label).toSet()
            dataStore.edit { prefs ->
                prefs[openKey(clientId)]   = false
                prefs[closedKey(clientId)] = newClosed
            }
            _uiState.value = state.copy(isOpen = false, closedPeriods = newClosed.toList().sorted())
        }
    }

    fun openNextPeriod() {
        viewModelScope.launch {
            val clientId = currentClientManager.activeClientId.value ?: 1L
            val state = _uiState.value
            val next = LocalDate.of(state.currentYear, state.currentMonth, 1).plusMonths(1)
            dataStore.edit { prefs ->
                prefs[yearKey(clientId)]  = next.year
                prefs[monthKey(clientId)] = next.monthValue
                prefs[openKey(clientId)]  = true
            }
            _uiState.value = state.copy(
                currentYear  = next.year,
                currentMonth = next.monthValue,
                isOpen       = true
            )
            loadSummary(clientId, next.year, next.monthValue)
        }
    }

    /**
     * Verifica si un período específico está cerrado para el cliente activo.
     * Usado por InsertJournalEntryUseCase para bloquear asientos en períodos cerrados.
     */
    suspend fun isPeriodClosed(year: Int, month: Int): Boolean {
        val clientId = currentClientManager.activeClientId.value ?: 1L
        val prefs = dataStore.data.first()
        val closed = prefs[closedKey(clientId)] ?: emptySet()
        return periodLabel(year, month) in closed
    }

    // ── Claves por cliente ────────────────────────────────────────────────────
    private fun yearKey(clientId: Long)   = intPreferencesKey("period_year_$clientId")
    private fun monthKey(clientId: Long)  = intPreferencesKey("period_month_$clientId")
    private fun openKey(clientId: Long)   = booleanPreferencesKey("period_open_$clientId")
    private fun closedKey(clientId: Long) = stringSetPreferencesKey("closed_periods_$clientId")

    private fun periodLabel(year: Int, month: Int) =
        "${Month.of(month).getDisplayName(TextStyle.SHORT, Locale("es")).replaceFirstChar { it.uppercase() }} $year"
}
