package com.osnordev.abaco.domain.model

data class BracketDetail(
    val bracket: TaxBracket,
    val taxableAmount: Double,
    val taxAmount: Double
)

data class TaxResult(
    val grossIncome: Double,
    val totalExpenses: Double,
    val netIncome: Double,
    val cssAmount: Double,
    val iipAmount: Double,
    val iipBracketDetails: List<BracketDetail>
)
