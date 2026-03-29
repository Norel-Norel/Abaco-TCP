package com.osnordev.abaco.domain.calculator

import com.osnordev.abaco.domain.model.TaxConfig
import com.osnordev.abaco.domain.model.TaxResult
import javax.inject.Inject
import javax.inject.Singleton

data class TaxProjection(
    /** Annualized gross income based on monthly average */
    val annualizedGrossIncome: Double,
    /** Annualized net income based on monthly average */
    val annualizedNetIncome: Double,
    /** Estimated annual CSS */
    val estimatedCss: Double,
    /** Estimated annual IIP */
    val estimatedIip: Double,
    /** Total estimated annual tax burden */
    val totalEstimatedTax: Double = estimatedCss + estimatedIip,
    /** Number of months used to compute the average */
    val basedOnMonths: Int
)

/**
 * Projects annual tax obligations from a partial-year [TaxResult].
 * Requirements: 14.4
 */
@Singleton
class TaxProjectionCalculator @Inject constructor() {

    /**
     * Extrapolates [currentPeriodResult] (which covers [elapsedMonths] months)
     * to a full 12-month year and recalculates CSS + IIP.
     *
     * @param currentPeriodResult  Accumulated tax result for the elapsed months
     * @param elapsedMonths        How many months the result covers (1–12)
     * @param config               Tax configuration to use for projection
     */
    fun project(
        currentPeriodResult: TaxResult,
        elapsedMonths: Int,
        config: TaxConfig
    ): TaxProjection {
        require(elapsedMonths in 1..12) { "elapsedMonths must be between 1 and 12" }

        val factor = 12.0 / elapsedMonths
        val annualGross = currentPeriodResult.grossIncome * factor
        val annualExpenses = currentPeriodResult.totalExpenses * factor
        val annualNet = (annualGross - annualExpenses).coerceAtLeast(0.0)

        val estimatedCss = TaxCalculator.calculateCSS(annualGross, config.cssRate)
        val (estimatedIip, _) = TaxCalculator.calculateIIP(annualNet, config.iipBrackets)

        return TaxProjection(
            annualizedGrossIncome = annualGross,
            annualizedNetIncome = annualNet,
            estimatedCss = estimatedCss,
            estimatedIip = estimatedIip,
            basedOnMonths = elapsedMonths
        )
    }
}
