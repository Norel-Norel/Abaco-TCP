package com.osnordev.abaco.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CurrencyConfig(
    val mlcToCup: Double = 1.0,
    val usdToCup: Double = 1.0
)
