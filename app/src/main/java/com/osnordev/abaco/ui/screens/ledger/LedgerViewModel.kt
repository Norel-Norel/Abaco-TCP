package com.osnordev.abaco.ui.screens.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.osnordev.abaco.data.local.ChartOfAccountDao
import com.osnordev.abaco.domain.client.CurrentClientManager
import com.osnordev.abaco.domain.repository.JournalEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class LedgerMovement(
    val date: String,
    val description: String,
    val debit: Double,
    val credit: Double
)

data class LedgerEntry(
    val accountName: String,
    val movements: List<LedgerMovement>,
    /** Saldo según naturaleza de la cuenta:
     *  - Naturaleza DEBIT  → saldo = totalDebit - totalCredit  (positivo = deudor)
     *  - Naturaleza CREDIT → saldo = totalCredit - totalDebit  (positivo = acreedor)
     */
    val balance: Double,
    /** "Saldo Deudor" o "Saldo Acreedor" */
    val balanceLabel: String
)

private val DATE_FMT = DateTimeFormatter.ofPattern("dd/MM")

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val repository: JournalEntryRepository,
    private val chartOfAccountDao: ChartOfAccountDao,
    private val currentClientManager: CurrentClientManager
) : ViewModel() {

    val ledger: StateFlow<List<LedgerEntry>> =
        currentClientManager.activeClientId
            .flatMapLatest { clientId ->
                val id = clientId ?: 1L
                combine(
                    repository.getAllEntriesByClient(id),
                    chartOfAccountDao.getAllByClient(id)
                ) { entries, accounts ->
                    // Mapa nombre → naturaleza ("DEBIT" o "CREDIT")
                    val natureMap = accounts.associate { it.name to it.nature }

                    val grouped = mutableMapOf<String, MutableList<LedgerMovement>>()
                    entries.sortedBy { it.entry.date }.forEach { entryWithLines ->
                        entryWithLines.lines.forEach { line ->
                            val movements = grouped.getOrPut(line.accountName) { mutableListOf() }
                            movements.add(
                                LedgerMovement(
                                    date = entryWithLines.entry.date.format(DATE_FMT),
                                    description = entryWithLines.entry.description,
                                    debit = line.debit,
                                    credit = line.credit
                                )
                            )
                        }
                    }

                    grouped.map { (accountName, movements) ->
                        val totalDebit  = movements.sumOf { it.debit }
                        val totalCredit = movements.sumOf { it.credit }

                        // Determinar naturaleza: si no está en el catálogo, usar DEBIT por defecto
                        val nature = natureMap[accountName] ?: "DEBIT"
                        val (balance, label) = if (nature == "DEBIT") {
                            (totalDebit - totalCredit) to "Saldo Deudor"
                        } else {
                            (totalCredit - totalDebit) to "Saldo Acreedor"
                        }

                        LedgerEntry(
                            accountName  = accountName,
                            movements    = movements,
                            balance      = balance,
                            balanceLabel = label
                        )
                    }.sortedBy { it.accountName }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
