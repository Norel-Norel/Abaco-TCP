package com.osnordev.abaco.domain.model

import java.math.BigDecimal

/**
 * Representa el resultado completo del cálculo de nómina bajo normas cubanas.
 */
data class PayrollCalculation(
    val employeeName: String,
    val ci: String,
    val grossSalary: Double,
    
    // --- Lado del Empleado (Deducciones) ---
    val cssEmployee: Double,    // 5%
    val iipRetained: Double,    // Escala ONAT
    val netSalary: Double,      // Lo que recibe el trabajador
    
    // --- Lado de la Empresa (Aportes Patronales) ---
    val cssEmployer: Double,    // 12.5%
    val holidayProvision: Double, // 9.09%
    val subsidyProvision: Double, // 1.5%
    val specialSS: Double,       // 5% (Riesgos)
    
    val totalCompanyCost: Double // Costo total para el empleador
)
