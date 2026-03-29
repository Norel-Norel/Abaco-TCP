package com.osnordev.abaco.domain.calculator

import com.osnordev.abaco.data.local.JournalEntryWithLines
import com.osnordev.abaco.domain.model.AccountType
import java.time.LocalDate

data class AccountBalance(
    val accountName: String,
    val accountType: AccountType,
    val debitTotal: Double,
    val creditTotal: Double
) {
    val netBalance: Double get() = debitTotal - creditTotal
}

data class BalanceSheet(
    val assets: List<AccountBalance>,
    val liabilities: List<AccountBalance>,
    val equity: List<AccountBalance>,
    val cutoffDate: LocalDate
) {
    val totalAssets: Double get() = assets.sumOf { it.netBalance }
    val totalLiabilities: Double get() = liabilities.sumOf { -it.netBalance }  // credits increase liabilities
    val totalEquity: Double get() = equity.sumOf { -it.netBalance }             // credits increase equity
    val isBalanced: Boolean get() = Math.abs(totalAssets - (totalLiabilities + totalEquity)) < 0.01
}

object BalanceSheetCalculator {

    /**
     * Calculates a [BalanceSheet] from [entries] considering only those with
     * date <= [cutoffDate]. Groups balances by account name and type.
     */
    fun calculate(
        entries: List<JournalEntryWithLines>,
        cutoffDate: LocalDate
    ): BalanceSheet {
        // Filter by cutoff date
        val filtered = entries.filter { !it.entry.date.isAfter(cutoffDate) }

        // Aggregate by (accountName, accountType)
        val balances = mutableMapOf<Pair<String, AccountType>, AccountBalance>()
        for (entry in filtered) {
            for (line in entry.lines) {
                val key = line.accountName to line.accountType
                val existing = balances[key] ?: AccountBalance(
                    accountName = line.accountName,
                    accountType = line.accountType,
                    debitTotal = 0.0,
                    creditTotal = 0.0
                )
                balances[key] = existing.copy(
                    debitTotal = existing.debitTotal + line.debit,
                    creditTotal = existing.creditTotal + line.credit
                )
            }
        }

        val all = balances.values.toList()
        return BalanceSheet(
            assets = all.filter { it.accountType == AccountType.ASSET },
            liabilities = all.filter { it.accountType == AccountType.LIABILITY },
            equity = all.filter { it.accountType == AccountType.EQUITY ||
                                  it.accountType == AccountType.INCOME ||
                                  it.accountType == AccountType.EXPENSE },
            cutoffDate = cutoffDate
        )
    }
}
