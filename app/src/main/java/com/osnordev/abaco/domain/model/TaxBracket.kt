package com.osnordev.abaco.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TaxBracket(
    val from: Double,
    val to: Double? = null, // null = sin límite superior
    val rate: Double
)
