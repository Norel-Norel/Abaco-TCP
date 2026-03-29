package com.osnordev.abaco.domain.calculator

import com.osnordev.abaco.domain.model.Currency
import com.osnordev.abaco.domain.model.CurrencyConfig

object CurrencyConverter {

    /**
     * Converts [amount] in [currency] to CUP using the rates in [config].
     * CUP → CUP is a no-op (rate = 1.0).
     */
    fun toCup(amount: Double, currency: Currency, config: CurrencyConfig): Double =
        amount * rateFor(currency, config)

    /**
     * Returns the CUP exchange rate for [currency].
     */
    fun rateFor(currency: Currency, config: CurrencyConfig): Double = when (currency) {
        Currency.CUP -> 1.0
        Currency.MLC -> config.mlcToCup
        Currency.USD -> config.usdToCup
    }

    /**
     * Converts a list of (amount, currency) pairs to a total in CUP.
     */
    fun totalInCup(
        items: List<Pair<Double, Currency>>,
        config: CurrencyConfig
    ): Double = items.sumOf { (amount, currency) -> toCup(amount, currency, config) }
}
