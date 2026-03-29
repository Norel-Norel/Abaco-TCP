package com.osnordev.abaco.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PaymentQrData(
    val accountNumber: String = "",
    val phone: String = "",
    val holderName: String = ""
)
