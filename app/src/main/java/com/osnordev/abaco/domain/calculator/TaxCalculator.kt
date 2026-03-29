package com.osnordev.abaco.domain.calculator

import com.osnordev.abaco.domain.model.BracketDetail
import com.osnordev.abaco.domain.model.TaxBracket
import com.osnordev.abaco.domain.model.TaxResult

/**
 * Pure calculation functions for Cuban TCP tax obligations (ONAT).
 * All functions are stateless and side-effect free.
 */
object TaxCalculator {

    /**
     * Calculates the Contribución a la Seguridad Social (CSS).
     *
     * @param grossIncome Total gross income for the period (>= 0)
     * @param cssRate     CSS rate as a decimal in [0.0, 1.0] (default 20% = 0.20)
     * @return CSS amount (always >= 0)
     */
    fun calculateCSS(grossIncome: Double, cssRate: Double): Double {
        require(grossIncome >= 0.0) { "grossIncome must be >= 0" }
        require(cssRate in 0.0..1.0) { "cssRate must be in [0, 1]" }
        return grossIncome * cssRate
    }

    /**
     * Calculates the Impuesto sobre Ingresos Personales (IIP) using a progressive bracket scale.
     *
     * @param netIncome Annual net income (utilidad neta) >= 0
     * @param brackets  Ordered list of tax brackets (ascending by `from`)
     * @return Pair of total IIP amount and per-bracket detail list
     */
    fun calculateIIP(netIncome: Double, brackets: List<TaxBracket>): Pair<Double, List<BracketDetail>> {
        require(netIncome >= 0.0) { "netIncome must be >= 0" }

        if (netIncome == 0.0 || brackets.isEmpty()) {
            return 0.0 to emptyList()
        }

        val details = mutableListOf<BracketDetail>()
        var totalTax = 0.0

        for (bracket in brackets) {
            if (netIncome <= bracket.from) break

            val upperBound = bracket.to ?: Double.MAX_VALUE
            val taxableInBracket = (minOf(netIncome, upperBound) - bracket.from)
                .coerceAtLeast(0.0)

            val taxInBracket = taxableInBracket * bracket.rate
            totalTax += taxInBracket

            details.add(
                BracketDetail(
                    bracket = bracket,
                    taxableAmount = taxableInBracket,
                    taxAmount = taxInBracket
                )
            )

            if (bracket.to == null || netIncome <= upperBound) break
        }

        return totalTax to details
    }

    /**
     * Convenience function that combines CSS and IIP into a full [TaxResult].
     *
     * @param grossIncome   Total gross income for the period
     * @param totalExpenses Total deductible expenses for the period
     * @param cssRate       CSS rate as a decimal in [0.0, 1.0]
     * @param iipBrackets   Ordered list of IIP tax brackets
     */
    fun calculateTaxResult(
        grossIncome: Double,
        totalExpenses: Double,
        cssRate: Double,
        iipBrackets: List<TaxBracket>
    ): TaxResult {
        val netIncome = (grossIncome - totalExpenses).coerceAtLeast(0.0)
        val cssAmount = calculateCSS(grossIncome, cssRate)
        val (iipAmount, bracketDetails) = calculateIIP(netIncome, iipBrackets)

        return TaxResult(
            grossIncome = grossIncome,
            totalExpenses = totalExpenses,
            netIncome = netIncome,
            cssAmount = cssAmount,
            iipAmount = iipAmount,
            iipBracketDetails = bracketDetails
        )
    }
}
