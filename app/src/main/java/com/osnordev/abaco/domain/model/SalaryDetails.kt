package com.osnordev.abaco.domain.model

import kotlinx.serialization.Serializable

/**
 * Representa el desglose detallado de un salario.
 */
@Serializable
data class SalaryDetails(
    val grossSalary: Double,
    val socialSecurity: Double, // CSS
    val incomeTax: Double,      // IIP / IRPF
    val otherDeductions: Double = 0.0,
    val netSalary: Double
)
