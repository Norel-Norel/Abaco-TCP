package com.osnordev.abaco.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TaxConfig(
    val cssRate: Double = 0.20,
    val iipBrackets: List<TaxBracket> = defaultIipBrackets()
)

fun defaultIipBrackets(): List<TaxBracket> = listOf(
    TaxBracket(from = 0.0,      to = 10_000.0,  rate = 0.00),
    TaxBracket(from = 10_001.0, to = 20_000.0,  rate = 0.15),
    TaxBracket(from = 20_001.0, to = 30_000.0,  rate = 0.20),
    TaxBracket(from = 30_001.0, to = 50_000.0,  rate = 0.30),
    TaxBracket(from = 50_001.0, to = null,       rate = 0.50)
)
