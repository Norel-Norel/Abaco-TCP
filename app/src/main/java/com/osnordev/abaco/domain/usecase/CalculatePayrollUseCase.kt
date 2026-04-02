package com.osnordev.abaco.domain.usecase

import com.osnordev.abaco.domain.model.PayrollCalculation
import com.osnordev.abaco.domain.model.TaxConfig
import javax.inject.Inject

/**
 * Calcula la nómina completa bajo normas cubanas.
 *
 * Flujo correcto:
 *   1. grossSalary = salarioBase + (salarioBase × 9.09%)
 *      → El 9.09% de vacaciones se SUMA al salario base para obtener el salario devengado real.
 *      → Ejemplo: base 3 000 → grossSalary = 3 000 + 272.70 = 3 272.70
 *   2. Sobre grossSalary se calculan todos los aportes y retenciones.
 */
class CalculatePayrollUseCase @Inject constructor() {

    /**
     * @param employeeName  Nombre del trabajador
     * @param ci            Carnet de Identidad
     * @param baseSalary    Salario base (lo que el usuario introduce)
     * @param taxConfig     Configuración de tasas
     */
    operator fun invoke(
        employeeName: String,
        ci: String,
        baseSalary: Double,
        taxConfig: TaxConfig
    ): PayrollCalculation {

        // ── 1. Salario devengado real = base + provisión de vacaciones (9.09%) ──
        val holidayProvision = baseSalary * taxConfig.holidayRate   // 9.09%
        val grossSalary = baseSalary + holidayProvision             // 3 272.70 para base 3 000

        // ── 2. Lado del empleado (retenciones sobre grossSalary) ─────────────
        val cssEmployee = grossSalary * taxConfig.cssEmployeeRate   // 5%

        // Base imponible para IIP = grossSalary - SS empleado
        val taxableIncome = grossSalary - cssEmployee

        // IIP (ONAT) — escala progresiva
        var iipRetained = 0.0
        taxConfig.iipBrackets.forEach { bracket ->
            if (taxableIncome > bracket.from) {
                val upperLimit = bracket.to ?: Double.MAX_VALUE
                val taxableInBracket = minOf(taxableIncome, upperLimit) - bracket.from
                if (taxableInBracket > 0) iipRetained += taxableInBracket * bracket.rate
            }
        }

        val netSalary = grossSalary - cssEmployee - iipRetained

        // ── 3. Lado de la empresa (aportes patronales sobre grossSalary) ─────
        val cssEmployer      = grossSalary * taxConfig.cssEmployerRate  // 12.5%
        val subsidyProvision = grossSalary * taxConfig.subsidyRate      // 1.5%
        val specialSS        = grossSalary * taxConfig.specialSSRate    // 5%

        // Costo total = grossSalary + todos los aportes patronales
        val totalCompanyCost = grossSalary + cssEmployer + holidayProvision + subsidyProvision + specialSS

        return PayrollCalculation(
            employeeName     = employeeName,
            ci               = ci,
            grossSalary      = grossSalary,
            cssEmployee      = cssEmployee,
            iipRetained      = iipRetained,
            netSalary        = netSalary,
            cssEmployer      = cssEmployer,
            holidayProvision = holidayProvision,
            subsidyProvision = subsidyProvision,
            specialSS        = specialSS,
            totalCompanyCost = totalCompanyCost
        )
    }
}
