package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.domain.calculator.TaxCalculator
import com.osnordev.abaco.domain.model.TaxResult
import com.osnordev.abaco.domain.model.TransactionType
import com.osnordev.abaco.domain.repository.TaxConfigRepository
import com.osnordev.abaco.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetTaxResultUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val taxConfigRepository: TaxConfigRepository
) {
    /**
     * Combines transactions for the given period with the current tax config
     * and returns a reactive [TaxResult] stream.
     *
     * Note: IIP is calculated on the annualised net income (monthly × 12)
     * to apply the progressive scale correctly, as per Cuban legislation.
     */
    operator fun invoke(year: Int, month: Int): Flow<TaxResult> =
        combine(
            transactionRepository.getTransactionsByPeriod(year, month),
            taxConfigRepository.getTaxConfig()
        ) { transactions, config ->
            val grossIncome = transactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }

            val totalExpenses = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }

            // Annualise net income for IIP progressive scale
            val monthlyNet = (grossIncome - totalExpenses).coerceAtLeast(0.0)
            val annualNet = monthlyNet * 12

            TaxCalculator.calculateTaxResult(
                grossIncome = grossIncome,
                totalExpenses = totalExpenses,
                cssRate = config.cssRate,
                iipBrackets = config.iipBrackets
            ).copy(netIncome = annualNet)
        }
}
